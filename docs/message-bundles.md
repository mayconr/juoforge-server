# Shard messages

Register bundles during shard configuration:

```java
@Override
public void configure(WorldCfg cfg) {
    cfg.addMessageBundle("legacy.messages");
    cfg.addMessageBundle("legacy.mining");
    // Other shard settings...
}
```

Names are relative to the classpath, without an extension. For example:

```text
src/main/resources/legacy/messages.properties
src/main/resources/legacy/messages_pt_BR.properties
src/main/resources/legacy/mining.properties
src/main/resources/legacy/mining_pt_BR.properties
```

Files use UTF-8. Example Brazilian Portuguese translation:

```properties
mining.prospect.found=Você encontrou sinais de minério perto de {location}.
```

Send the message through the usual API:

```java
messages.send(player, MessageContent.localized(
        "mining.prospect.found", Map.of("location", "a mountain")));
```

For each language, bundles are searched in registration order; the library's
`messages` bundle is automatically appended at the end. The shard can therefore
add keys and override library messages.

Lookup searches the requested language (for example, `pt_BR`, then `pt`) across
all bundles before checking the `en_US`/`en` fallback. Finally, it checks base
files without a suffix. A shard's base message does not hide a library
translation for the requested language. The JVM's default locale has no effect.
A key missing from all bundles returns `!key!`.

Resources and missing resources are cached per localizer, shared by sessions.
File changes require a server restart.
