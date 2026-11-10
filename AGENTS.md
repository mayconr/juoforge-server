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

## Documentation language

- Write and maintain this project's documentation in English, including `AGENTS.md`,
  `README.md`, and Markdown files under `docs/`.
- Preserve external reference projects under `docs/references/` unless explicitly
  instructed to modify them.
