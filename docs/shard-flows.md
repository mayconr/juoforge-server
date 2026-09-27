# Shard flows

Register a flow factory in the shard's `WorldCfg` configuration:

```java
cfg.addFlow(HealContext.class,
        runtime -> HealFlowDefinition.build(runtime.world()));

cfg.addSpellTrigger(runtime -> new HealSpellTrigger(runtime.flows()));
```

`HealContext` and `HealFlowDefinition` in this example are shard-defined types.
The context must extend `AbstractContext` (typically `AbstractSyncFlowContext`),
and the factory must return a `Flow<HealContext>` built with `FlowFactory`.
Triggers can receive the public `infrastructure.flow.FlowExecutor` interface and
call `flows.execute(new HealContext(...))` from `execute`.

Bootstrap initializes the core modules and flows, then constructs the runtime.
It invokes shard flow factories once, in registration order, before trigger and
event listener factories. World updates start after all registrations complete.
Factories should construct flows only, not initiate gameplay or execute other flows.

`runtime.flows()` executes both core and shard flows from the same registry,
using the context's exact class. Existing flow tracing, hooks, and `StepResult`
handling apply. Duplicate context classes are rejected, including attempts to
replace core flows. Null context types, factories, and factory results are rejected.

This registration API does not change scheduling: execution uses the caller's
thread, and the current flow implementation waits for asynchronous step results
with `join()`. Do not use a pending target response or a long delay as a step
future on the game loop. Nonblocking suspension and resumption require separate
execution support.
