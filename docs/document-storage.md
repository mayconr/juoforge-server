# Module document persistence

`DocumentStorage` is the generic contract for JSON documents. The shard configures
the adapter through `WorldCfg.documentStorage(...)`. `RealmStorage.initialize()`
loads all documents before exposing the cache to modules.
`cacheDocument(name, document)` updates the cache and `getDocument(name)` retrieves
a document, returning `Optional<JsonNode>`. Both operations use copies to prevent
external changes to the cache and do not access the database.

- Names must contain 1 to 100 characters and must not be blank.
- `saveAll` atomically upserts the batch. Omitted names remain in the database.
- `loadAll()` loads all documents, indexed by `name`.
- A module updates the cache when its state changes. During `saveWorld()`, the realm
  captures a snapshot and calls `DocumentStorage.saveAll(snapshot)`. The
  `saveWorld()` future completes only after persistence and propagates failures.
  Cache changes made during a write are saved on the next save.
- The domain decides how to serialize its state; the adapter does not know about
  veins or other module-specific types.

In `legacy-shard`, `PsqlDocumentStorage` runs operations on the
`databaseExecutor` and writes to `realm_document(name VARCHAR(100) PRIMARY KEY,
data JSONB NOT NULL)`. PostgreSQL's native
[`jsonb` type](https://www.postgresql.org/docs/current/datatype-json.html)
was chosen because it validates JSON and supports queries and indexes on its contents.

Mining uses the name `mining.veins`, with a list of objects containing `veinId`
and `consumed`. `DefaultMiningModule` loads the document before exposing its
flows and updates the cache after each successful consumption. `VeinsOperation`
keeps only in-memory state. Player discoveries remain in the character's
persistent attributes.

The `realm_document` table is defined in `legacy-shard/sql/schema_creation.sql`,
with `name VARCHAR(100) PRIMARY KEY` and `data JSONB NOT NULL`.
