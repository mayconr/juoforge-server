# Skill action scheduling

`SkillActionOptions` contains `skillId`, optional `swingCount`,
`onSwing`, and `onComplete`. Each call to `world.skill().startSkillAction` schedules one
action. An explicit `swingCount` overrides the player's persistent `skill.swings`
property for that action. When omitted or null, the count comes from that property (`WorldSkill.SWINGS_PROPERTY`), read through `WorldMobile` with
`runtimeOnly=false`. Missing or invalid values default to 1. Positive whole-number
values up to `Integer.MAX_VALUE` are accepted, including persisted integer and
long representations. Runtime-only properties do not affect this preference.

```java
world.skill().setSwingCount(player, 3);
```

`WorldSkill.setSwingCount` executes `SetSkillSwingCountFlowDefinition`, which
clamps the count to at least 1 and writes the persistent preference through
`WorldMobile.setProperty` with synchronization inside the mobile module. It
affects future actions only.

The override does not update the stored preference. Zero or negative overrides
are clamped to 1. The count is captured when scheduling; later preference changes affect only new
actions. Each swing receives its one-based execution count. Completion runs once
after the final swing and receives the total executed count.

The existing `game.skill.flow.start` flow owns scheduling:

1. `ValidateSkillActionStep` checks the player, required callbacks,
   skill ID, and skill existence in `UOFileReader`.
2. `ResolveSkillSwingsStep` uses `swingCount` when supplied, otherwise reads the
   persistent preference, then normalizes the result to at least 1.
3. `CaptureSkillActionOriginStep` stores a copy of the player position when the
   action is requested.
4. `ScheduleSkillActionStep` creates `SkillAction` with explicit constructor
   arguments (player, captured origin, swing count, delay, `onSwing` callback, completion
   callback), reserves it through the skill
   module, and completes the result. If an action is already reserved, it sends
   `skill.action.already_active`, completes with false, and stops normally.

`SkillModuleImpl.reserveSkillAction` only performs the atomic per-player
reservation. Scheduling rules and busy-player notification live in the flow.
The module delegates each game-loop update to `UpdateSkillActionsFlowDefinition`.
`ScheduleSkillActionStep` fixes the interval at one second, with a TODO to make
it configurable. The first swing runs on the next world update, before waiting; the interval applies only between swings. At most one swing per action runs
per update, even when an update covers several delay intervals.

The reservation is removed before completion, allowing the completion callback
to schedule another action. Actions created during an update are processed on a
later update. Callback exceptions release the reservation and are logged; a
failed swing does not invoke completion.

Reference check: POL's `pol-core/pol/useskill.cpp`, `StartSkillScript`, applies an
attribute-specific skill cooldown when starting a script. That is not the same
contract as juoforge's action delay. Persistent swing preferences and repetition
semantics follow this project's requirements; the flow follows juoforge's existing
conventions.

## Updating pending actions

`SkillModuleImpl.update(delta)` executes `UpdateSkillActionsContext` through the
core flow registry. The module only owns reservation storage and provides
snapshot and conditional-release operations.

1. `ValidateSkillActionsUpdateStep` rejects negative or nonfinite intervals.
2. `AdvanceSkillActionsStep` snapshots reservations, advances simulation time,
   and collects actions whose delay has elapsed.
3. `CancelMovedSkillActionsStep` cancels actions whose player left the captured
   origin tile (X or Y differs). It checks every reservation on each update, not
   only ready ones, so leaving and returning between swings still cancels. It
   releases the reservation, removes it from the ready actions, skips the
   remaining swings and invokes completion once with the swings already executed.
   Completion failures are logged.
4. `ExecuteSkillActionsStep` invokes one swing for each ready action and retains
   its reservation while more swings remain. After the last swing it releases the
   reservation before completion. Individual failures are logged and release the
   affected reservation without interrupting other actions.

Invalid intervals fail with `INVALID_SKILL_ACTION_DELTA`, propagated by the module
as `ModuleOperationException`. Validation runs before any action is advanced.
Release checks action identity, so a failing completion callback cannot remove a
replacement action it scheduled. New actions wait until the next world update.
