# Mobile properties

Use `world.mobile()` to read and write a mobile's named properties:

```java
world.mobile().setProperty(player, "craft.level", 10, false);
int level = world.mobile().getProperty(player, "craft.level", 0, false);

world.mobile().setProperty(player, "craft.active", true, true);
boolean active = world.mobile().getProperty(player, "craft.active", false, true);
```

`runtimeOnly = false` selects persistent properties; `true` selects runtime-only
properties. The module chooses the attribute map internally.

Persistent properties use the mobile's existing persistent attribute map and
participate in its normal persistence lifecycle. Runtime properties use its
runtime map and are not retained when a mobile is reloaded. These operations do
not save to the database immediately or send client updates.

Getters without a default return `Object`, or `null` for a missing key. Getters
with a default return the stored value or the supplied default, without storing
the default. The caller must use a type compatible with the stored value.
Setting `null` removes a property, following `AttributeMap.set` semantics.
Mobile and key must be non-null.

Each read or write runs under `synchronized (mobile)`, so callers of this API
share a lock per mobile. Multiple calls are separate operations; mutable values
are returned as stored, rather than copied. Direct attribute-map access does not
automatically acquire this monitor.

For atomic read-modify-write operations, use `computeProperty(mobile, key, update, runtimeOnly)`. The callback receives the key and previous value (null
when absent), runs under the mobile monitor, and returns the replacement value.
Returning null removes the property. The callback must not modify the same
attribute map recursively. A thrown exception leaves the previous mapping intact.

Reference: POL's `pol-core/pol/module/uomod.cpp`,
`UOExecutorModule::mf_GetObjProperty` and `mf_SetObjProperty`, exposes named object
properties. POL returns an error for a missing property and packs values for
storage. Juoforge keeps its own attribute-map representation, missing-value
semantics, and synchronization conventions.
