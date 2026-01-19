package com.github.mayconr.juoserver.shard.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.infrastructure.storage.item.ItemStorage;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
public class PsqlItemStorage extends AbstractStorage implements ItemStorage {

    private static final String SAVE_ITEM_FULL = """
            WITH inserted_item AS (
                    INSERT INTO items (
                        id,
                        name,
                        model_id,
                        hue,
                        layer,
                        unit_weight,
                        amount,
                        properties
                    ) VALUES (
                        ?,        -- id (UUID)
                        ?,        -- name
                        ?,        -- model_id
                        ?,        -- hue
                        ?,        -- layer
                        ?,        -- unit_weight
                        ?,        -- amount
                        ?::jsonb  -- properties
                    )
                    RETURNING id, serial_id
                )
                INSERT INTO item_state (
                    item_id,
                    owner_mobile_id,
                    parent_item_id,
                    x,
                    y,
                    z,
                    map,
                    equipped
                )
                SELECT
                    id,
                    ?, -- owner_mobile_id
                    ?, -- parent_item_id
                    ?, -- x
                    ?, -- y
                    ?, -- z
                    ?, -- map
                    ?  -- equipped
                FROM inserted_item
                RETURNING
                    (SELECT serial_id FROM inserted_item) AS serial_id;
            """;
    private static final String UPDATE_STATES = """
            UPDATE item_state
            SET
                owner_mobile_id = ?,   -- UUID ou null
                parent_item_id  = ?,   -- UUID ou null
            
                x   = ?,               -- int ou null
                y   = ?,               -- int ou null
                z   = ?,               -- int ou null
                map = ?,               -- smallint ou null
            
                equipped   = ?,        -- boolean
                updated_at = NOW()
            WHERE item_id = ?;
            """;
    private static final String SELECT_CONTAINER_ITEMS = """
            SELECT * FROM v_item_full WHERE parent_item_id = ?
            """;

    private static final String SELECT_ITEM_BY_SERIAL = """
            SELECT * FROM v_item_full
            WHERE serial_id = ?
            LIMIT 1;
            """;

    private static final String SELECT_GROUND_ITEMS = """
            SELECT * FROM v_item_full
            WHERE equipped = false
            AND owner_mobile_id IS NULL
            AND parent_item_id IS NULL;
            """;

    private static final String SELECT_EQUIPPED_ITEMS = """
            SELECT * FROM v_item_full
            WHERE owner_mobile_id = ?;
            """;

    private final Executor executor;
    private final ObjectMapper objectMapper;

    public PsqlItemStorage(DataSource dataSource, Executor executor, ObjectMapper objectMapper) {
        super(dataSource);
        this.executor = executor;
        this.objectMapper = objectMapper;
    }

    @Override
    public CompletableFuture<List<UOItem>> loadEquippedItems(UOMobile mobile) {
        return CompletableFuture.supplyAsync(
                () -> findMany(SELECT_EQUIPPED_ITEMS, ps -> ps.setObject(1, mobile.getId()), this::mapItem), executor);
    }

    @Override
    public CompletableFuture<List<UOItem>> loadGroundItems() {
        return CompletableFuture.supplyAsync(()-> findMany(SELECT_GROUND_ITEMS, ps -> {}, this::mapItem), executor)
                .whenComplete(((uoItems, throwable) -> {
            if (throwable != null) {
                log.error("Unable to load ground items", throwable);
            } else {
                log.info("Ground items loaded!");
            }
        }));
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
    public CompletableFuture<Optional<UOItem>> findItemBySerialId(int serialId) {
        return CompletableFuture.supplyAsync(()-> findOne(SELECT_ITEM_BY_SERIAL, ps -> ps.setInt(1, serialId), this::mapItem));
    }

    private UOItem mapItem(ResultSet rs) throws SQLException {
        UUID id = (UUID) rs.getObject("item_id");
        int serialId = rs.getInt("serial_id");
        int modelId = rs.getInt("model_id");
        int hue = rs.getInt("hue");
        int amount = rs.getInt("amount");
        Layer layer = Layer.fromCode(rs.getInt("layer"));
        String name = rs.getString("name");

        int x = rs.getObject("x") != null ? rs.getInt("x") : 0;
        int y = rs.getObject("y") != null ? rs.getInt("y") : 0;
        int z = rs.getObject("z") != null ? rs.getInt("z") : 0;

        boolean movable = true;
        boolean hidden = false;
        Direction direction = Direction.SOUTH;

        Container container = null;
        var parentItemId = rs.getObject("parent_item_id", UUID.class);
        if (parentItemId != null) {
            container = null; // classe Container que referencia outro item
        }

        var type = ItemType.OTHER;
        try {
            var props = rs.getString("properties");
            if (props != null && !props.isBlank()) {
                final var node = objectMapper.readTree(props);
                final var typeNode = node.get("type");
                if (typeNode != null && !typeNode.isNull()) {
                    type = ItemType.valueOf(node.get("type").asText());
                }
            }
        } catch (Exception ignored) {
        }

        final var item = new UOItem(id, serialId, modelId, x, y, z, name, type, layer, amount, hue, movable, hidden,
                direction, container, "horse");

        return switch (type) {
            case CONTAINER -> new UOContainer(item, 60);
            default -> item;
        };
    }

    @Override
    public CompletableFuture<UOItem> saveItemFull(UOItem item) {
        return CompletableFuture.supplyAsync(()->{
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(SAVE_ITEM_FULL)) {
                int i = 1;

                ps.setObject(i++, UUID.randomUUID());
                ps.setString(i++, item.getName());
                ps.setInt(i++, item.getModelId());
                ps.setInt(i++, item.getHue());
                ps.setShort(i++, (short)item.getLayer().getCode()); // nullable
                ps.setInt(i++, 0); // weight
                ps.setInt(i++, item.getAmount());
                ps.setString(i++, "{}");

                // item_state
                ps.setObject(i++, Optional.ofNullable(item.getOwner()).map(UOMobile::getId).orElse(null)); // mobile owner id. When equipped
                ps.setObject(i++, Optional.ofNullable(item.getContainer()).map(UOItem.class::cast).map(UOItem::getId).orElse(null)); // container parent id. When in container
                ps.setInt(i++, item.getX());
                ps.setInt(i++, item.getY());
                ps.setInt(i++, item.getZ());
                ps.setShort(i++, (short) 0);
                ps.setBoolean(i, item.getOwner() != null);

                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    item.setSerialId(rs.getInt("serial_id"));
                }
                return item;
            } catch (SQLException e) {
                throw new RuntimeException("Unable to save item", e);
            }
        });
    }

    @Override
    public CompletableFuture<Collection<UOItem>> saveStates(Collection<UOItem> items) {
        return CompletableFuture.supplyAsync(()->{
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(UPDATE_STATES)) {
                for (UOItem item : items) {
                    ps.setObject(1, Optional.ofNullable(item.getOwner()).map(UOMobile::getId).orElse(null));
                    ps.setObject(2, Optional.ofNullable(item.getContainer()).map(UOItem.class::cast).map(UOItem::getId).orElse(null));

                    ps.setInt(3, item.getX());              // Integer | null
                    ps.setInt(4, item.getY());
                    ps.setInt(5, item.getZ());
                    ps.setShort(6, (short)0);

                    ps.setBoolean(7, item.getOwner() != null);

                    ps.setObject(8, item.getId());
                    ps.addBatch();
                }
                ps.executeBatch();
                return items;
            } catch (SQLException e) {
                log.error("Unable to save items", e);
                throw new RuntimeException("Failed to batch save mobile vitals", e);
            }
        }, executor);
    }
}
