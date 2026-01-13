package com.github.mayconr.juoserver.game.shard.storage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import javax.sql.DataSource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mayconr.juoserver.game.core.model.*;
import com.github.mayconr.juoserver.game.storage.item.ItemStorage;

public class PsqlItemStorage extends AbstractStorage implements ItemStorage {
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
                () -> {
                    final var sql = """
                        SELECT * FROM v_item_full
                        WHERE owner_mobile_id = ?
                        and equipped = true;
                    """;
                    return findMany(sql, ps -> ps.setObject(1, mobile.getId()), this::mapItem);
                },
                executor);
    }

    @Override
    public CompletableFuture<Optional<UOItem>> findItemBySerialId(int serialId) {
        return CompletableFuture.supplyAsync(()->{
            final var sql = """
                SELECT * FROM v_item_full
                WHERE serial_id = ?
                LIMIT 1;
                """;
            return findOne(sql, ps -> ps.setInt(1, serialId), this::mapItem);
        });
    }

    private UOItem mapItem(ResultSet rs) throws SQLException {
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

        final var item = new UOItem(
                serialId, modelId, x, y, z, name, type, layer, amount, hue, movable, hidden,
                direction, container, "");

        return switch (type) {
            case CONTAINER -> new UOContainer(item, 60);
            default -> item;
        };
    }
}
