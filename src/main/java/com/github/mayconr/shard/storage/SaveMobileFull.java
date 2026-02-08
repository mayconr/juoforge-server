package com.github.mayconr.shard.storage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mayconr.juoserver.game.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@RequiredArgsConstructor
public class SaveMobileFull {

    private static final String UPDATE_SERIAL = """
            UPDATE serial_counters
            SET
                next_serial = ?,
                updated_at  = NOW()
            WHERE entity_type = ?;
            """;

    private static final String INSERT_MOBILE = """
            INSERT INTO mobiles (
                id,
                serial_id,
                account_id,
                name,
                display_name,
                model_id,
                hue,
                race,
                gender,
                notoriety,
                status,
                mount_item_name
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
            """;

    private static final String INSERT_MOBILE_ATTRIBUTES = """
            INSERT INTO mobile_attributes (
                mobile_id,
                strength,
                dexterity,
                intelligence,
                stat_cap,
                followers,
                max_followers
            )
            VALUES (?, ?, ?, ?, ?, ?, ?);
            """;

    private static final String INSERT_MOBILE_VITALS = """
            INSERT INTO mobile_vitals (
                  mobile_id,
                  max_hitpoints,
                  max_stamina,
                  max_mana
              )
              VALUES (?, ?, ?, ?);
            """;

    private static final String INSERT_MOBILE_RUNTIME = """
            INSERT INTO mobile_runtime (
                 mobile_id,
                 x,
                 y,
                 z,
                 direction,
                 running,
                 hitpoints,
                 stamina,
                 mana
             )
             VALUES (
                 ?, ?, ?, ?, ?, ?, ?, ?, ?
            );
            """;

    private static final String INSERT_ITEM = """
            INSERT INTO items (
                id,
                serial_id,
                name,
                display_name,
                model_id,
                hue,
                layer,
                unit_weight,
                amount,
                flags
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb);
            """;

    private static final String INSERT_ITEM_STATE = """
            INSERT INTO item_state (
                item_id,
                owner_mobile_id,
                parent_item_id,
                x,
                y,
                z,
                attr
            )
            VALUES (?, ?, ?, ?, ?, ?, ?::jsonb);
            """;

    private static final String INSERT_SKILL = """
            INSERT INTO mobile_skills (
                id,
                mobile_id,
                skill_id,
                skill_base,
                skill_cap,
                skill_lock
            ) VALUES (
                ?,
                ?,
                ?,
                ?,
                ?,
                ?
            )
            """;

    private final DataSource dataSource;
    private final Executor executor;
    private final ObjectMapper objectMapper;

    public CompletableFuture<UOMobile> saveMobileFull(int mobileSerialId, int itemSerialId, UOMobile mobile) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = dataSource.getConnection()) {
                try {
                    log.info("Inserting player [{}-{}]", mobile.getSerialId(), mobile.getName());
                    conn.setAutoCommit(false);

                    updateCounter(conn, mobileSerialId, "MOBILE");
                    updateCounter(conn, itemSerialId, "ITEM");
                    insertMobile(conn, mobile);
                    insertAttributes(conn, mobile);
                    insertVitals(conn, mobile);
                    insertSkills(conn, mobile);
                    insertRuntime(conn, mobile);
                    insertEquippedItems(conn, mobile);

                    conn.commit();
                    log.info("Mobile [{}-{}] saved!", mobile.getSerialId(), mobile.getName());
                    return mobile;
                } catch (Exception e) {
                    conn.rollback();
                    throw new RuntimeException("Failed to save player", e);
                } finally {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException e) {
                throw new RuntimeException("Unable to execute operation", e);
            }
        }, executor);
    }

    private void updateCounter(Connection conn, int serialId, String entity) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(UPDATE_SERIAL)) {
            ps.setInt(1, serialId);
            ps.setString(2, entity);
            ps.executeUpdate();
        }
    }

    private void insertMobile(Connection conn, UOMobile mobile) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(INSERT_MOBILE)) {
            ps.setObject(1, mobile.getId());
            ps.setInt(2, mobile.getSerialId());
            ps.setObject(3, mobile instanceof UOPlayer p ? p.getAccountId() : null);
            ps.setString(4, mobile.getName());
            ps.setString(5, mobile.getDisplayName());
            ps.setInt(6, mobile.getModelId());
            ps.setInt(7, mobile.getHue());
            ps.setInt(8, mobile.getRace().getCode());
            ps.setInt(9, mobile.getGender().getCode());
            ps.setInt(10, mobile.getNotoriety().getCode());
            ps.setInt(11, mobile.getStatus().getCode());
            if (mobile instanceof UONpc npc) {
                ps.setString(12, npc.getMountItemName());
            } else {
                ps.setNull(12, Types.VARCHAR);
            }
            ps.executeUpdate();
        }
    }

    private void insertAttributes(Connection conn, UOMobile mobile) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(INSERT_MOBILE_ATTRIBUTES)) {
            ps.setObject(1, mobile.getId());
            ps.setInt(2, mobile.getStrength());
            ps.setInt(3, mobile.getDexterity());
            ps.setInt(4, mobile.getIntelligence());
            ps.setInt(5, mobile.getStatCap());
            ps.setInt(6, mobile.getFollowers());
            ps.setInt(7, mobile.getMaxFollowers());
            ps.executeUpdate();
        }
    }

    private void insertVitals(Connection conn, UOMobile mobile) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(INSERT_MOBILE_VITALS)) {
            ps.setObject(1, mobile.getId());
            ps.setInt(2, mobile.getMaxHitpoints());
            ps.setInt(3, mobile.getMaxStamina());
            ps.setInt(4, mobile.getMaxMana());
            ps.executeUpdate();
        }
    }

    private void insertRuntime(Connection conn, UOMobile mobile) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(INSERT_MOBILE_RUNTIME)) {
            ps.setObject(1, mobile.getId());
            ps.setInt(2, mobile.getX());
            ps.setInt(3, mobile.getY());
            ps.setInt(4, mobile.getZ());
            ps.setInt(5, mobile.getDirection().getCode());
            ps.setBoolean(6, mobile.isRunning());
            ps.setInt(7, mobile.getHitpoints());
            ps.setInt(8, mobile.getStamina());
            ps.setInt(9, mobile.getMana());
            ps.executeUpdate();
        }
    }

    private void insertSkills(Connection conn,  UOMobile mobile) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(INSERT_SKILL)) {
            for (SkillValue value : mobile.getSkills().skills()) {
                ps.setObject(1, UUID.randomUUID());
                ps.setObject(2, mobile.getId());
                ps.setInt(3, value.getSkillId());
                ps.setDouble(4, value.getBase());
                ps.setDouble(5, value.getCap());
                ps.setInt(6, value.getLock().getCode());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void insertEquippedItems(Connection conn, UOMobile mobile) throws SQLException, JsonProcessingException {
        try (
                PreparedStatement psItem  = conn.prepareStatement(INSERT_ITEM);
                PreparedStatement psState = conn.prepareStatement(INSERT_ITEM_STATE)
        ) {
            for (UOItem item : mobile.getEquippedItems().values()) {
                insertItem(psItem, item);
                insertItemState(psState, item);
            }
        }
    }

    private void insertItem(PreparedStatement ps, UOItem item) throws SQLException, JsonProcessingException {
        ps.setObject(1, item.getId());
        ps.setInt(2, item.getSerialId());
        ps.setString(3, item.getName());
        ps.setString(4, item.getDisplayName());
        ps.setInt(5, item.getModelId());
        ps.setInt(6, item.getHue());
        if (item.getLayer() != null) {
            ps.setShort(7, (short) item.getLayer().getCode());
        } else {
            ps.setNull(7, Types.SMALLINT);
        }

        ps.setInt(8, 0);
        ps.setInt(9, item.getAmount());
        ps.setObject(10, objectMapper.writeValueAsString(item.getFlags()));
        ps.executeUpdate();
    }

    private void insertItemState(PreparedStatement ps, UOItem item) throws SQLException {
        ps.setObject(1, item.getId());
        ps.setObject(2, item.getOwner() != null ? item.getOwner().getId() : null);
        ps.setObject(3, item.getContainer() != null ? item.getContainer().getId() : null);
        ps.setInt(4, item.getX());
        ps.setInt(5, item.getY());
        ps.setInt(6, item.getZ());
        try {
            ps.setString(7, objectMapper.writeValueAsString(item.getAttrMap()));
        } catch (JsonProcessingException e) {
            log.error("Unable to save attr for item [{}]", item.getSerialId(), e);
            ps.setString(7, "{}");
        }
        ps.executeUpdate();
    }
}
