# Referência: ClassicUO

O código-fonte do cliente ClassicUO está em `docs/references/ClassicUO/`.

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