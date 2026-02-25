package com.github.mayconr.juoserver.infrastructure.storage;

import com.github.mayconr.juoserver.game.model.AccountMobile;
import com.github.mayconr.juoserver.game.model.UOAccount;
import com.github.mayconr.juoserver.game.model.UOMobile;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface MobileStorage {

    CompletableFuture<Integer> findNextMobileSerial();

    CompletableFuture<Void> setNextMobileSerial(int serial);

    CompletableFuture<List<AccountMobile>> findPlayersByAccount(UOAccount uoAccount);

    CompletableFuture<UOMobile> findMobileById(UUID id);

    CompletableFuture<UOMobile> findMobileBySerialId(int serialId);

    CompletableFuture<Boolean> mobileExists(String name);

    CompletableFuture<List<UOMobile>> findAllNpcs();

    /**
     * Persists the full state of a {@link UOMobile}, including its core attributes and
     * related item associations.
     * <p>
     * This method is intended to store all persistent data of a player entity
     * (player, NPC, or creature), such as stats, skills, position, status flags,
     * and other domain-specific attributes defined by the shard implementation.
     * </p>
     *
     * <p>
     * The {@code mobileSerialId} and {@code itemSerialId} parameters do <strong>not</strong>
     * represent the current serial identifiers of the player or its items.
     * Instead, they define the <em>next available serial values</em> to be used by
     * the persistence layer when generating serials for the player and its related
     * items during the save operation.
     * </p>
     *
     * <p>
     * This allows the persistence mechanism to safely assign unique serials while
     * maintaining consistency with the Ultima Online serial allocation rules.
     * </p>
     *
     * <p>
     * The operation is asynchronous and returns a {@link CompletableFuture} that
     * completes when the persistence process finishes.
     * </p>
     *
     * @param mobileSerialId the next available serial value to be used for player
     *                       entities during persistence; this value does not
     *                       represent the current player serial
     * @param itemSerialId   the next available serial value to be used for item
     *                       entities during persistence; this value does not
     *                       represent an existing item serial
     * @param mobile         the {@link UOMobile} instance containing the complete
     *                       state of the player to be persisted
     *
     * @return a {@link CompletableFuture} that completes with the persisted
     *         {@link UOMobile} instance, or completes exceptionally if the
     *         persistence operation fails
     */
    CompletableFuture<UOMobile> saveMobileFull(int mobileSerialId, int itemSerialId, UOMobile mobile);

    CompletableFuture<Collection<UOMobile>> saveMobiles(int serial, Collection<UOMobile> mobiles, Collection<UOMobile> dirties);

    CompletableFuture<Collection<UOMobile>> saveRuntime(Collection<UOMobile> mobiles);

    CompletableFuture<Collection<UOMobile>> saveVitals(Collection<UOMobile> mobiles);

    CompletableFuture<Collection<UOMobile>> saveAttributes(Collection<UOMobile> mobiles);

    CompletableFuture<Collection<UOMobile>> saveSkills(Collection<UOMobile> mobiles);

}
