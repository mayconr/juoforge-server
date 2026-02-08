package com.github.mayconr.shard.storage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.infrastructure.storage.DataNotFoundException;
import com.github.mayconr.juoserver.infrastructure.storage.ItemStorage;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
public class PsqlItemStorage extends AbstractStorage implements ItemStorage {


    private static final String SELECT_CONTAINER_ITEMS = """
            SELECT * FROM v_item_full WHERE parent_item_id = ?
            """;

    private static final String SELECT_ITEM_BY_SERIAL = """
            SELECT * FROM v_item_full
            WHERE serial_id = ?
            LIMIT 1;
            """;

    private static final String SELECT_ITEM_BY_NAME = """
            SELECT * FROM v_item_full
            WHERE name = ?
            LIMIT 1;
            """;

    private static final String SELECT_GROUND_ITEMS = """
            SELECT * FROM v_item_full
            WHERE owner_mobile_id IS NULL
            AND parent_item_id IS NULL;
            """;

    private static final String SELECT_EQUIPPED_ITEMS = """
            SELECT * FROM v_item_full
            WHERE owner_mobile_id = ?;
            """;

    private final Executor executor;
    private final ObjectMapper objectMapper;
    private final InsertItemFull insertItemFull;
    private final SaveItemStates saveItemStates;
    private final GetSerial getSerial;
    private final SaveItems saveItems;

    public PsqlItemStorage(DataSource dataSource, Executor executor, ObjectMapper objectMapper) {
        super(dataSource);
        this.executor = executor;
        this.objectMapper = objectMapper;
        this.insertItemFull = new InsertItemFull(dataSource, executor, objectMapper);
        this.saveItemStates = new SaveItemStates(dataSource, executor, objectMapper);
        this.getSerial = new GetSerial(dataSource, executor);
        this.saveItems = new SaveItems(dataSource, executor, objectMapper);
    }

    @Override
    public CompletableFuture<Integer> getNextItemSerial() {
        return getSerial.getNextSerial("ITEM");
    }

    @Override
    public CompletableFuture<Void> setNextItemSerial(int serial) {
        return null;
    }

    @Override
    public CompletableFuture<List<UOItem>> loadEquippedItems(UOMobile mobile) {
        return CompletableFuture.supplyAsync(
                () -> findMany(SELECT_EQUIPPED_ITEMS, ps -> ps.setObject(1, mobile.getId()), this::mapItem), executor);
    }

    @Override
    public CompletableFuture<List<UOItem>> loadGroundItems() {
        return CompletableFuture.supplyAsync(()-> findMany(SELECT_GROUND_ITEMS, ps -> {}, this::mapItem), executor);
    }

    @Override
    public CompletableFuture<List<UOItem>> loadContainerItems(Container container) {
        return CompletableFuture.supplyAsync(()-> findMany(SELECT_CONTAINER_ITEMS, ps -> ps.setObject(1, container.getId()), this::mapItem), executor)
                .whenComplete(((uoItems, throwable) -> {
            if (throwable != null) {
                log.error("Unable to load container [{}] items", container.getId(), throwable);
            } else {
                log.info("Container [{}] items loaded!", container.getId());
            }
        }));
    }

    @Override
    public CompletableFuture<UOItem> findItemBySerialId(int serialId) {
        return CompletableFuture.supplyAsync(()-> findOne(SELECT_ITEM_BY_SERIAL, ps -> ps.setInt(1, serialId), this::mapItem)
                .orElseThrow(DataNotFoundException::new), executor);
    }

    @Override
    public CompletableFuture<UOItem> findItemByName(String name) {
        return CompletableFuture.supplyAsync(()->findOne(SELECT_ITEM_BY_NAME, ps -> ps.setString(1, name), this::mapItem)
                .orElseThrow(DataNotFoundException::new), executor);
    }

    private UOItem mapItem(ResultSet rs) throws SQLException, JsonProcessingException {
        UUID id = (UUID) rs.getObject("item_id");
        int serialId = rs.getInt("serial_id");
        int modelId = rs.getInt("model_id");
        int hue = rs.getInt("hue");
        int amount = rs.getInt("amount");
        Layer layer = Layer.fromCode(rs.getInt("layer"));
        String name = rs.getString("name");
        String displayName = rs.getString("display_name");
        List<ItemFlag> flags = objectMapper.readValue(rs.getString("flags"), new TypeReference<List<ItemFlag>>() {});

        int x = rs.getObject("x") != null ? rs.getInt("x") : 0;
        int y = rs.getObject("y") != null ? rs.getInt("y") : 0;
        int z = rs.getObject("z") != null ? rs.getInt("z") : 0;

        boolean movable = true;
        boolean hidden = false;
        Direction direction = Direction.SOUTH;

        Map<String, Object> attr;
        try {
            attr = objectMapper.readValue(rs.getString("attr"), new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            attr = Collections.emptyMap();
        }

        Container container = null;
        var parentItemId = rs.getObject("parent_item_id", UUID.class);
        if (parentItemId != null) {
            container = null;
        }

        final var item = new UOItem(id, serialId, modelId, x, y, z, name, displayName, attr, layer, amount, hue, movable, hidden,
                direction, container, flags, "horse");

        if (item.hasFlag(ItemFlag.CONTAINER)) {
            return new UOContainer(item, (int) item.getAttrMap().getOrDefault("gumpId", 0));
        }
        return item;
    }

    @Override
    public CompletableFuture<UOItem> saveItemFull(UOItem item) {
        return insertItemFull.saveItemFull(item);
    }

    @Override
    public CompletableFuture<Collection<UOItem>> saveItems(int serial, Collection<UOItem> items, Collection<UOItem> dirties) {
        return saveItems.save(serial, items, dirties);
    }

    @Override
    public CompletableFuture<Collection<UOItem>> saveStates(Collection<UOItem> items) {
        return saveItemStates.saveStates(items);
    }
}
