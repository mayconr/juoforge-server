# Transportes de rede

`NetworkServer` define o ciclo de vida (`start()` e `close()`). O shard escolhe
o transporte em `NetworkBootstrap.build(NetworkSettings)`. `NetworkSettings`
é separado de gameplay e arquivos de conteúdo.

| Modo | Implementação | Comunicação |
| --- | --- | --- |
| `standalone` | `ServerStartup` | Escuta TCP diretamente no host/porta configurados. |
| `gateway` | `GatewayNetworkServer` | Conecta ao túnel e registra o shard. Sem listener TCP de jogo. |

Ambos usam `UOChannelInitializer`, `NettySessionManager` e os mesmos handlers e
regras. O adaptador gateway cria um par de `LocalChannel` em memória por sessão,
conectando o fluxo de bytes ao pipeline UO. Não há conexão TCP local intermediária.
Os tipos do gateway ficam em `infrastructure.gateway`, sem entrar no domínio
ou nas interfaces de sessão de jogo.

## Configuração

O servidor é uma biblioteca. O ponto de entrada do shard carrega as propriedades
e inicia a rede explicitamente:

```java
NetworkSettings network = NetworkSettings.from(System.getProperties());
NetworkServer server = new NetworkBootstrap(runtime).build(network);
Runtime.getRuntime().addShutdownHook(new Thread(server::close));
server.start().toCompletableFuture().join();
```

Standalone:

```properties
network.mode=standalone
network.host=0.0.0.0
network.port=2593
```

Gateway:

```properties
network.mode=gateway
network.host=127.0.0.1
network.port=2594
network.gateway.shard=default
```

Forneça também `network.gateway.token`, igual ao segredo configurado no gateway.
O shard pode preencher essa propriedade a partir de
`System.getenv("GATEWAY_SHARD_TOKEN")` antes de chamar `NetworkSettings.from`.
O token é obrigatório nesse modo e omitido de `NetworkSettings.toString()`.
Também é possível carregar um arquivo com `Properties.load`; a biblioteca não
carrega arquivos nem variáveis implicitamente.

`network.host`/`network.port` indicam bind em standalone e destino do túnel em
gateway. Os padrões são `0.0.0.0:2593` e `127.0.0.1:2594`. No gateway,
`routes.2593: default` direciona a porta pública 2593 ao shard `default`.
O gateway consultado encaminha bytes e não implementa o login UO; as condições
de login e suporte a versões do servidor continuam aplicáveis.

O modo é obrigatório: `network.mode` ausente ou vazio resulta em erro.
O bootstrap não oferece `build()` sem argumentos. Migre o uso legado
`new NetworkBootstrap(runtime).build().bindAsync(port)` para o modo servidor
explícito:

```java
NetworkServer server = new NetworkBootstrap(runtime)
        .build(NetworkSettings.standalone("0.0.0.0", port));
server.start();
```

## Ciclo de vida e protocolo

`start()` é não bloqueante e idempotente. Sua conclusão indica bind concluído em
standalone e recebimento de `REGISTER_ACK` em gateway. Enquanto o gateway estiver
indisponível, o cliente tenta novamente com backoff de 250 ms até 30 s. A perda
do túnel encerra suas sessões; a reconexão registra o shard novamente, sem
restaurar sessões antigas. `close()` é idempotente e inicia o encerramento
assíncrono dos canais e event loops próprios.

O adaptador segue o protocolo v1 de `juoforge-gateway`:
`protocol/TunnelFrameDecoder`, `TunnelFrameEncoder`, `TunnelPipeline` e
`client/GatewayTunnelClient`, `TunnelSessionManager`. O frame contém tamanho de
4 bytes, versão/tipo de 1 byte cada, ID de sessão de 8 bytes e payload;
inteiros são big endian. O registro contém `shard + NUL + token` em UTF-8.
Há heartbeat, timeout de registro/inatividade e limites por sessão. Respostas
grandes são divididas em frames. Os limites seguem `TunnelSettings.defaults()`:
frames de 64 KiB, buffer de 256 KiB, 4096 sessões, heartbeat de 10 s e
inatividade de 35 s.

O transporte foi adaptado para Netty 4.1/Java 21 dentro da infraestrutura do
servidor. Não há dependência Maven do aplicativo gateway (Java 25/Spring Boot 4).
Mudanças futuras do protocolo precisam ser compatibilizadas neste adaptador.

## Compatibilidade ClassicUO

Comprovado em `docs/references/ClassicUO/src/ClassicUO.Client/`:

- `Game/Scenes/LoginScene.cs`, `HandleRelayServerPacket`: após o relay, habilita
  compressão e envia o seed de 4 bytes antes do segundo login.
- `Network/OutgoingPackets.cs`, `Send_SecondLogin`: envia `0x91`, seed e os
  campos de 30 bytes para usuário e senha.
- `Network/PacketsTable.cs`, tabela e construtor: distingue comprimentos fixos
  e variáveis e ajusta alguns conforme a versão, incluindo drop de item.

O túnel preserva o fluxo de bytes e a compressão Huffman existente. O decoder
compartilhado acumula seed e pacotes incompletos antes de interpretá-los;
limites de frames do túnel não são limites de pacotes UO. A tabela segue os
formatos já implementados pelo servidor, incluindo drop de 15 bytes; não amplia
suporte a clientes antigos. O protocolo não transmite o IP original do jogador:
a sessão gateway recebe um endereço local lógico.

`GatewayNetworkTest` usa um peer TCP com implementação independente do frame para
verificar registro, sessões intercaladas, fragmentação, respostas comprimidas,
fechamento isolado e reconexão, sem iniciar o aplicativo Spring do gateway.
