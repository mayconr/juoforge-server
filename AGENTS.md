# Consulting client and server implementations

Example client and server projects are located in `docs/references/`.

Use this code exclusively to consult and verify behaviors, packet formats,
rules, and conditions relevant to the task. Do not use it as inspiration for
processes, flows, steps, abstractions, or architecture in juoforge. Juoforge's
organization and processes must follow its own conventions and task requirements.

## Client and server references

When working on features that depend on client behavior or have equivalents in
the example servers, consult the relevant code before completing the analysis
or implementing changes.

This includes, when applicable:

- Reading, interpreting, and processing packets;
- Death, ghosts, and corpses;
- Containers, inventory, and items;
- Tooltips and clilocs;
- Animations, equipment, and movement;
- Gameplay rules, combat, and skills;
- Persistence.

## Using the references

1. Locate the relevant handlers, methods, and functions in the example projects.
2. Verify relevant behaviors, rules, conditions, and effects, including packet
   formats, field conditions, and differences between client versions when applicable.
3. Compare these behaviors with the juoforge implementation.
4. Cite the files, methods, and functions consulted when explaining conclusions.
5. Distinguish behavior proven by code from hypotheses and decisions specific
   to each example project.
6. Do not modify example project code unless explicitly requested.

Consider both client and server references when the task involves interaction
between them. Juoforge's implementation must follow its own architectural and
language conventions. Consulting these implementations does not authorize
copying or deriving processes or flows from them for juoforge.

## Client data and MUL files

Juoforge implements game mechanics only. It must not contain client data or
knowledge about the client's MUL files.

- Do not hardcode values that describe how the client renders or stores content,
  such as gump IDs and their areas, art or gump dimensions, hues, animation frame
  data, or tables copied from client resources.
- Do not add code that reads, interprets, or documents MUL files, their formats,
  offsets, or contents.
- Keep only the network protocol (packet formats and field semantics) and the
  mechanics built on it. When the client decides how something is displayed,
  such as the final position of an item inside a container gump, send a valid
  value and let the client handle the presentation.
- References under `docs/references/` may be consulted to verify how the client
  behaves, but their data must not be copied into juoforge.

## Java imports

- Always add an explicit `import` for each class used and use its simple name in code.
- Do not use fully qualified class names in code or wildcard (`*`) imports.
- This rule does not apply to `import static`.

## Formatting and method responsibilities

- Always deliver generated or modified code correctly indented and formatted,
  following project conventions, including multiline arguments and lambda bodies.
- Prefer short methods with few lines and one clear responsibility. Separate
  distinct responsibilities into methods named after their purpose.
- When a method or lambda grows, extract operations with their own responsibility,
  preserving readability and avoiding artificial fragmentation solely to reduce line counts.

## WorldStorage responsibilities

`WorldStorage` is the single entry point for loading and saving the world. It keeps the
lifecycle (`initialize`, `saveWorld`) and exposes each responsibility through a view:
`accounts()`, `serials()`, `mobiles()`, `items()` and `properties()`.

- New code must use the views, such as `storage.mobiles().get(serial)`. Do not call the
  deprecated flat methods, such as `storage.getMobile(serial)`.
- Whenever you modify code that calls a deprecated `WorldStorage` method, migrate those
  calls in the touched code to the corresponding view method, including its tests.
- `WorldStorageViews` currently delegates to the flat methods. When a responsibility no
  longer has callers of its flat methods, move the implementation into its view, remove
  the deprecated methods and update this section.
- Add new responsibilities as new views instead of new flat methods on `WorldStorage`.
- Keep `infrastructure.storage` organized by responsibility. The root package holds only the
  world storage itself (`WorldStorage`, `CachedWorldStorage`, `WorldStorageViews`, `WorldPersistence`,
  `InitialData`). Each responsibility has its own subpackage with its view, database
  adapter contract, cache, index and mappers: `account`, `serial`, `mobile`, `item` and
  `property`. Place new storage classes in the subpackage of their responsibility.
- `PropertyStore` and `WorldPersistence` are public only because they cross subpackages;
  they are internal to the world storage and must not be used by modules or shards.

## Item properties

- All changes to persistent or runtime properties of an existing item must go through
  the item module's property API, exposed by `ItemModule` / `WorldItem`.
- Use `setProperty` to set, replace or remove a property (`null` removes it), and
  `computeProperty` for atomic read-modify-write operations. Use `getProperty` to
  coordinate reads with these writes.
- These operations synchronize on the item. Do not mutate
  `item.persistentAttributes()` or `item.runtimeAttributes()` directly from flows,
  services, handlers or shards, including decay helpers and lazy property initialization.
- Keep direct property-map mutations inside the item module's property implementation.
  Constructing or hydrating initial item data belongs to the creation and storage layers.

## Item creation

`WorldItem.createItem` is deprecated. Item creation must go through `WorldItem.tryCreateItem`,
which returns an `ItemCreationResult`: `Created` with the item, or `Rejected` with the
`ItemCreationFailure` game rule that prevented it and its player-facing `displayMessage()`.
Failures that are not game-rule rejections are still raised as `ModuleOperationException`.

- New code must call `tryCreateItem` and handle `Rejected` explicitly, for example by
  stopping the flow with the rejection's display message. Do not call `createItem`.
- Whenever you modify code that calls `createItem`, migrate those calls in the touched code
  to `tryCreateItem`, including its tests. Use `orThrow()` only where a rejection is
  a programming error, such as corpses, mounts or starter items.
- Add new game-rule rejections to `ItemCreationFailure`, and make the step that detects them
  return the enum's `code()` and `displayMessage()`.
- When `createItem` no longer has callers, remove it from `WorldItem` and `ItemModuleImpl`
  and update this section.

## Documentation language

- Write and maintain this project's documentation in English, including `AGENTS.md`,
  `README.md`, and Markdown files under `docs/`.
- Preserve external reference projects under `docs/references/` unless explicitly
  instructed to modify them.
