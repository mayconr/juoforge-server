# Preparing Maven Central artifacts

The POM declares the repository URL, developer, SCM and the existing Mozilla
Public License 2.0. The `release` profile attaches sources and Javadoc JARs during
`package` and signs the main JAR, attached JARs and POM during `verify`.

Generate the release artifacts without signing:

```sh
mvn -Prelease package
```

Generate and sign the release artifacts:

```sh
mvn -Prelease verify
```

Signing requires GnuPG on PATH and an imported private signing key. Use gpg-agent
or the `MAVEN_GPG_PASSPHRASE` environment variable for the passphrase. Do not put
private keys or passphrases in the POM or version control. When multiple keys are
available, select the intended key with `-Dgpg.keyname=KEY_FINGERPRINT`.
The corresponding public key must be distributed as required by Central.

This prepares artifacts locally; it does not upload them. Central Portal
namespace verification, the publishing plugin and its credentials are separate
steps. The current Maven coordinates remain unchanged. Before publishing under
a new `io.github.mayconr` namespace, migrate the project and dependency coordinates
and make the filereader dependency available to consumers.

References:

- [Central requirements](https://central.sonatype.org/publish/requirements/)
- [Maven GPG plugin](https://maven.apache.org/plugins/maven-gpg-plugin/)
