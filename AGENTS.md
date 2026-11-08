# Consulta de implementações de cliente e servidor

- O ClassicUO é uma implementação de cliente, disponível em `docs/references/ClassicUO/`.
- O POLServer é uma implementação de servidor, disponível em `docs/references/polserver/`.

Use esses códigos exclusivamente para consultar e verificar comportamentos,
formatos de pacotes, regras e condições relevantes à tarefa. Não os use como
inspiração para gerar processos, fluxos, etapas, abstrações ou arquitetura no
juoforge. A organização e os processos do juoforge devem seguir seus próprios
padrões e os requisitos da tarefa.

## Referência de cliente: ClassicUO

O código-fonte do cliente ClassicUO está em `docs/references/ClassicUO/`.

Os logs do cliente ClassicUO podem ser acessados em
`C:\Users\mgregorio\UO\classicuo\ClassicUO\Logs`.

Ao trabalhar em funcionalidades que dependam do comportamento do
cliente, consulte o código relevante do ClassicUO antes de concluir
a análise ou implementar alterações.

Isso inclui, quando aplicável:

- Leitura e interpretação de pacotes;
- Morte, fantasmas e cadáveres;
- Containers e inventário;
- Tooltips e clilocs;
- Animações, equipamentos e movimentação.

## Como usar a referência

1. Localize no ClassicUO os handlers e métodos relevantes à tarefa.
2. Verifique o formato dos pacotes, as condições dos campos e as
   diferenças entre versões do cliente.
3. Compare esse comportamento com a implementação do juoforge.
4. Cite os arquivos e métodos relevantes ao explicar as conclusões.
5. Diferencie comportamento comprovado no código de hipóteses.
6. Não modifique o código do ClassicUO, salvo solicitação explícita.

A implementação do juoforge deve seguir seus próprios padrões de
arquitetura e linguagem. O ClassicUO é uma referência para
compatibilidade do cliente.

## Referência de servidor: POLServer

O código-fonte do servidor POLServer está em `docs/references/polserver/`.

Ao analisar ou implementar comportamentos de servidor que tenham equivalente
no POLServer, consulte o código relevante como referência. Isso inclui, quando
aplicável, regras de gameplay, combate, skills, itens, containers, movimentação,
persistência e processamento de pacotes.

1. Localize as funções e os fluxos relevantes no POLServer.
2. Compare as regras, condições e efeitos com a implementação do juoforge.
3. Cite os arquivos e as funções consultados ao explicar as conclusões.
4. Diferencie comportamento comprovado no código de hipóteses e decisões
   específicas do POLServer.
5. Não modifique o código do POLServer, salvo solicitação explícita.

O POLServer é uma referência de implementação de servidor; o ClassicUO continua
sendo a referência de comportamento do cliente. Considere ambos quando a tarefa
envolver a interação entre cliente e servidor. A implementação do juoforge deve
seguir seus próprios padrões de arquitetura e linguagem. Consultar essas
implementações não autoriza reproduzir nem derivar delas processos ou fluxos
para o juoforge.

## Imports Java

- Sempre adicione um `import` explícito para cada classe utilizada e use seu nome simples no código.
- Não utilize nomes de classes totalmente qualificados no corpo do código nem imports com wildcard (`*`).
- Esta regra não se aplica a `import static`.
