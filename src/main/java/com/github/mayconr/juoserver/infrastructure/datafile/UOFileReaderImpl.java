package com.github.mayconr.juoserver.infrastructure.datafile;

import com.github.mayconr.juoforge.reader.art.ArtMetadata;
import com.github.mayconr.juoforge.reader.skill.Skill;
import com.github.mayconr.juoforge.reader.tiledata.ItemTileData;
import com.github.mayconr.juoforge.reader.tiledata.TileFlag;
import com.github.mayconr.juoforge.reader.view.GameDataProvider;
import com.github.mayconr.juoforge.reader.view.GameDataProviderFactory;
import com.github.mayconr.juoforge.reader.view.LandTile;
import com.github.mayconr.juoforge.reader.view.StaticTile;
import com.github.mayconr.juoserver.game.GamePlaySettings;
import com.github.mayconr.juoserver.game.model.Direction;
import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.UOMobile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public class UOFileReaderImpl implements UOFileReader{

    private final GamePlaySettings settings;
    private GameDataProvider gameDataProvider;
    private MovementCollisionService collisionService;
    private WalkSurfaceResolver walkSurfaceResolver;

    public void loadFiles() {
        log.info("Loading UO files...");
        gameDataProvider = GameDataProviderFactory.create(Path.of(settings.files().dataFileRoot()), true);
        collisionService = new MovementCollisionService(gameDataProvider);
        walkSurfaceResolver = new WalkSurfaceResolver(gameDataProvider);
        log.info("Loading UO files done!");
    }

    @Override
    public LandTile getLandTile(Location location) {
        return gameDataProvider.tileAt(location.getX(), location.getY());
    }

    @Override
    public LandTile getLandTile(int x, int y) {
        return gameDataProvider.tileAt(x, y);
    }

    @Override
    public List<StaticTile> getStatics(Location location) {
        return gameDataProvider.staticsAt(location.getX(), location.getY());
    }

    @Override
    public List<StaticTile> getStatics(int x, int y) {
        return gameDataProvider.staticsAt(x, y);
    }

    @Override
    public boolean hasBlockingCollision(UOMobile mobile, Location location) {
        return collisionService.hasBlockingCollision(mobile, location.getX(), location.getY(), location.getZ());
    }

    @Override
    public boolean hasBlockingCollision(UOMobile mobile, int x, int y, int z) {
        return collisionService.hasBlockingCollision(mobile, x, y, z);
    }

    @Override
    public OptionalInt resolveWalkZ(Location from, Direction direction) {
        return walkSurfaceResolver.resolveWalkZ(from, direction);
    }

    @Override
    public Optional<Skill> getSkill(int skillId) {
        return gameDataProvider.skillBy(skillId);
    }

    @Override
    public Optional<ItemTileInfo> getItemTileInfo(int modelId) {
        return gameDataProvider.itemTileData(modelId).map(data -> toItemTileInfo(modelId, data));
    }

    private ItemTileInfo toItemTileInfo(int modelId, ItemTileData data) {
        var art = gameDataProvider.itemArt(modelId);
        return new ItemTileInfo(
                modelId,
                data.name(),
                data.isStackable(),
                flagNames(data.flags()),
                data.weight(),
                data.layer(),
                data.count(),
                data.animId(),
                data.hue(),
                data.lightIndex(),
                art.map(ArtMetadata::width).orElse(0),
                art.map(ArtMetadata::height).orElse(0));
    }

    private static Set<String> flagNames(Set<TileFlag> flags) {
        return flags.stream().map(TileFlag::name).collect(Collectors.toUnmodifiableSet());
    }
}
