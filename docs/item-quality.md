# Item quality

Quality lies in [-1, 1]. A material reflects its quality through the factor
`1 + quality`: zero leaves the material unchanged, -1 produces zero properties and
1 doubles them. Template materials reflect quality zero.

An `ItemRequest` carries two values:

- `withQuality(float)` declares the quality the resolved material already reflects.
  It is stored on the item as is and never changes the material. Omitted quality
  defaults to zero. The value must be finite and between -1 and 1, inclusive.
- `adjustQuality(float)` raises (positive) or lowers (negative) that quality.
  Successive calls accumulate. The adjustment must be finite and between -2 and 2;
  zero changes nothing.

The item creation flow resolves the template and any requested material override,
then `ApplyItemQualityStep` computes the adjusted quality as
`clamp(quality + adjustment, -1, 1)`, stores it, and rescales density, hardness,
toughness and magic affinity by `(1 + adjusted) / (1 + quality)`. The material
therefore changes only by the difference between both qualities, and successive
adjustments never compound: a material always equals its quality-zero properties
times `1 + quality`, apart from rounding. Integer properties are rounded to the
nearest integer, with halves rounded up. An integer overflow fails creation rather
than wrapping. A material at quality -1 has no recoverable proportions and stays
unchanged. An item without material keeps its material absent.
`ItemQualityAdjustment` holds these rules.

```java
// Mined ore: the template material reflects quality zero, so adjust from there.
var mined = ItemRequest.byName("iron_ore").adjustQuality(0.25f);

// Smelted ingot: transfer the ore item's material with its quality, then adjust it.
var ingot = ItemRequest.byName("iron_ingot")
        .withMaterial(ore.getMaterial())
        .withQuality(ore.getQuality())
        .adjustQuality(-0.05f);
var result = items.tryCreateItem(ingot, target);
```

Transferring a material together with its quality, without adjustment, keeps the
material unchanged. `ItemRequest.derivedFrom(source, name)` copies hue, material
and quality this way.

Items persist both quality and the resulting material properties. Loading an item
does not reapply quality. Changing the stored quality on an existing item does not
recalculate its material. When stacks merge, quality and effective material
properties are averaged using the original stack amounts; quality is not reapplied.

The shard's main schema stores quality as `REAL NOT NULL DEFAULT 0`, constrained
to [-1, 1]. The MyBatis item mapper includes it in reads, inserts and updates.
Recreate the database using the updated main schema; no migration is provided.

## Calculating quality from a mobile

`WorldItem.calculateQuality(UOMobile mobile, double modifier)` rolls a quality
without creating or changing an item. The modifier is a generic input, independent
of skills. Callers decide where its value comes from.

- Modifier is in [minModifier, maxModifier], [0, 100] by default. The minimum
  returns -1 (minimum quality, no chance to improve); the maximum returns +1
  (maximum quality).
- The mobile's luck is in [minLuck, maxLuck], [-100, 100] by default. Zero luck
  has no effect; positive luck favors higher quality and negative luck favors
  lower quality.
- Out-of-range modifier and luck, including infinite modifiers, are clamped to
  the nearest bound. Only a NaN modifier throws `IllegalArgumentException`.
  Modifier endpoints override luck and do not consume a random sample.

The ranges come from `GamePlaySettings.items().quality()` (`ItemQuality`). The
modifier range must be finite with `minModifier < maxModifier`, and the luck
range must contain zero:

```java
var rules = new GamePlaySettings()
        .withItems(new GamePlaySettings.Items(new GamePlaySettings.ItemQuality(0, 120, -100, 200)));
```

For intermediate modifiers, the calculation is:

```text
p = (modifier - minModifier) / (maxModifier - minModifier)
l = luck / maxLuck when luck > 0, luck / -minLuck when luck < 0, 0 otherwise
m = p + l * p * (1 - p)
u = uniform random sample in [0, 1)
quality = 2 * u^((1 - m) / m) - 1
```

The mean quality is `2 * m - 1`. Higher luck or modifier shifts the distribution
toward higher quality while keeping the result in [-1, 1]. Modifier 50 with zero
luck gives a uniform distribution, with mean zero. At modifier 50, luck -100
gives mean -0.5 and luck +100 gives mean +0.5. These are means, not guaranteed
results; intermediate inputs can produce negative or positive quality.

```java
float adjustment = items.calculateQuality(mobile, modifier);
var request = ItemRequest.byName("iron_ore")
        .adjustQuality(adjustment);
var result = items.tryCreateItem(request, target);
```

`ItemRequest.derivedFrom(source, name)` copies the source hue, material and quality
into a request for the named template. The hue is copied as is, so a source without
color (hue zero) produces an item without color instead of inheriting the template
hue. A source without material inherits the template material. Because the material
travels with its quality, the derived item keeps the source material until an
adjustment is applied.

The caller handles the creation result as usual. The calculation does not inspect
skills or change luck, and creation does not automatically roll quality. The
creation step applies the adjustment once, and persistence stores the resulting
properties and quality.

Reference comparison: ModernUO's `LootPack.GetLuckChance` and `CheckLuck`
(`docs/references/modernuo/Projects/UOContent/Misc/LootPack.cs`) use luck for loot
probability, with a different range and formula. The signed range, modifier,
quality distribution and endpoint guarantees above are juoforge-specific rules.
