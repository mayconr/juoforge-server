# Ground item decay

Movable items directly on the ground decay after 120 seconds by default. The
item module scans a snapshot of the global ground index every 60 seconds and
invokes the decay flow for its candidates. Both durations are real seconds of
world update time and can be configured:

```java
var settings = new GamePlaySettings();
settings = settings.withItems(new GamePlaySettings.Items(
        settings.items().quality(),
        new GamePlaySettings.ItemDecay(120, 60)
));
```

The first argument is the decay duration; the second is the scan interval. Both
must be finite and positive. Removal happens on the first scan at or after the
deadline. Candidates are revalidated before removal. Equipped, contained,
orphaned, immovable, removed, and currently held items are not deleted by decay.
Visibility and item type do not grant an exemption: movable corpses follow the
same rule.

## Drop date and restarts

The persistent item property `item.droppedAt` stores a numeric timestamp in
milliseconds. It uses the persistent active-world clock `item.decay.clock`,
which advances through `ItemModule.update(delta)`. It is independent of the
accelerated game calendar. The clock resumes its saved value after restart, so
server downtime does not consume the remaining decay period.

Ground creation and successful ground drops start a new period. Adding to a
ground stack updates the destination stack's date to the last successful
interaction. Rejected drops restored to their original ground position keep
their original date. If restoration must place an item at the player's feet
because its origin disappeared, that placement starts a new period.

An existing ground item without the property receives a full period when first
considered for decay. The item property and active-world clock are persisted by
the normal world save; recovery resumes from the last completed save.

## Containers

When a movable ground container expires, the flow loads its direct contents,
including contents not yet opened or cached, and places them at the container's
world position. Each child receives a fresh drop date. A child container keeps
its own contents and is subsequently subject to the same rule.

Children spilled by the scan are considered in a later scan. A container with
a held direct child waits rather than removing that child's origin. If content
loading fails, the container remains available for a later retry.

Deletion uses the existing item deletion flow and `ItemDeleted` event. Released
children use `GroundedItemCreated` for world visibility updates. Discovery of
decay candidates does not depend on either event.

## Reference behavior

ModernUO's `Item.Decays`, `Item.CanDecay`, and `Item.ScheduledDecayTime` in
[`Item.cs`](references/modernuo/Projects/Server/Items/Item.cs) were consulted for
eligibility and deadline semantics. Its additional visibility, spawner and
region rules are specific to that project. Juoforge uses the eligibility and
container spilling rules described above and its own module update and flow
conventions.

ClassicUO's `DeleteObject` handler in
[`PacketHandlers.cs`](references/classicuo/src/ClassicUO.Client/Network/PacketHandlers.cs)
handles the existing object-removal packet; decay requires no new client packet.
