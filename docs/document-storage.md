# Persistência de documentos dos módulos

`DocumentStorage` é o contrato genérico para documentos JSON. O shard configura
o adaptador com `WorldCfg.documentStorage(...)`. O `RealmStorage.initialize()`
carrega todos os documentos antes de disponibilizar o cache aos módulos.
`cacheDocument(name, document)` atualiza o cache e `getDocument(name)` consulta
um documento, retornando `Optional<JsonNode>`. Ambas as operações usam cópias
para impedir alterações externas no cache e não acessam o banco.

- Os nomes devem conter de 1 a 100 caracteres e não podem ser vazios.
- `saveAll` faz upsert atômico do lote. Nomes omitidos permanecem no banco.
- `loadAll()` carrega todos os documentos, indexados por `name`.
- O módulo atualiza o cache quando seu estado muda. No `saveWorld()`, o realm
  captura um snapshot e chama `DocumentStorage.saveAll(snapshot)`. O future de
  `saveWorld()` só termina depois da persistência e propaga eventuais falhas.
  Alterações no cache durante uma escrita ficam para o próximo save.
- O domínio decide como serializar seu estado; o adaptador não conhece veins
  nem outros tipos específicos de módulos.

No `legacy-shard`, `PsqlDocumentStorage` executa as operações no
`databaseExecutor` e grava em `realm_document(name VARCHAR(100) PRIMARY KEY,
data JSONB NOT NULL)`. Foi escolhido o tipo nativo
[`jsonb` do PostgreSQL](https://www.postgresql.org/docs/current/datatype-json.html),
que valida o JSON e permite consultas e índices sobre seu conteúdo.

O mining usa o nome `mining.veins`, com uma lista de objetos contendo `veinId`
e `consumed`. `DefaultMiningModule` carrega o documento antes de disponibilizar
os flows e atualiza o cache a cada consumo bem-sucedido. `VeinsOperation` mantém
somente o estado em memória. As descobertas dos jogadores continuam nos
atributos persistentes do personagem.

A tabela `realm_document` está definida em `legacy-shard/sql/schema_creation.sql`,
com `name VARCHAR(100) PRIMARY KEY` e `data JSONB NOT NULL`.
