# Mensagens do shard

Registre os bundles durante a configuração do shard:

```java
@Override
public void configure(WorldCfg cfg) {
    cfg.addMessageBundle("legacy.messages");
    cfg.addMessageBundle("legacy.mining");
    // Demais configurações do shard...
}
```

Os nomes são relativos ao classpath, sem extensão. Por exemplo:

```text
src/main/resources/legacy/messages.properties
src/main/resources/legacy/messages_pt_BR.properties
src/main/resources/legacy/mining.properties
src/main/resources/legacy/mining_pt_BR.properties
```

Os arquivos usam UTF-8. Exemplo de tradução:

```properties
mining.prospect.found=Você encontrou sinais de minério perto de {location}.
```

Envie a mensagem pela API habitual:

```java
messages.send(player, MessageContent.localized(
        "mining.prospect.found", Map.of("location", "uma montanha")));
```

Para cada idioma, os bundles são consultados na ordem de registro; o bundle
`messages` da biblioteca é acrescentado ao final automaticamente. Assim, o shard
pode adicionar chaves e substituir mensagens da biblioteca.

A busca percorre o idioma solicitado (por exemplo, `pt_BR`, depois `pt`) em todos
os bundles antes de consultar o fallback `en_US`/`en`. Por último, consulta os
arquivos base, sem sufixo. Uma mensagem base do shard não esconde uma tradução
da biblioteca para o idioma solicitado. O idioma padrão da JVM não interfere.
Uma chave ausente em todos os bundles retorna `!chave!`.

Os recursos e suas ausências são armazenados em cache por localizador, compartilhado
pelas sessões. Alterações nos arquivos exigem reiniciar o servidor.
