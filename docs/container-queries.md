# Container item queries

`world.container().getItemsInContainer(serial, predicate, searchNestedContainers)`
executes `GetContainerItemsFlowDefinition` through the core flow registry.
The two-argument overload uses `searchNestedContainers = false`.

The implementation lives in `game.container.flow.query`:

1. `ResolveQueryContainerStep` validates the serial and predicate and resolves
   the loaded root container.
2. `CollectContainerItemsStep` collects loaded direct children and, when requested,
   nested contents. A visited-serial set prevents cycles and duplicate results.
3. `FilterContainerItemsStep` applies the predicate and completes the context
   with a new result list.

The predicate filters results without preventing traversal of nested containers.
The root container is excluded. Queries do not load persistence, consume items,
or publish inventory updates. An empty container returns an empty list.

Missing containers or referenced items fail the flow. The public operation keeps
its `IllegalArgumentException` contract, including the flow's reason and cause.
No partial result is returned on failure.

Reference behavior was checked in POL's `pol-core/pol/containr.cpp`,
`UContainer::find_sumof_objtype_noninuse`, for nested quantity lookup. POL-specific
locked/in-use exclusions are not introduced by this query. Flow organization
follows juoforge's existing `container.flow.add` pattern.
