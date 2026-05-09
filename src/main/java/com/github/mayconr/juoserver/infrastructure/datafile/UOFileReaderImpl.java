package com.github.mayconr.juoserver.infrastructure.datafile;

import com.github.mayconr.juoforge.reader.skill.Skill;
import com.github.mayconr.juoforge.reader.tiledata.TileFlag;
import com.github.mayconr.juoforge.reader.view.GameDataProvider;
import com.github.mayconr.juoforge.reader.view.GameDataProviderFactory;
import com.github.mayconr.juoforge.reader.view.LandTile;
import com.github.mayconr.juoforge.reader.view.StaticTile;
import com.github.mayconr.juoserver.game.GamePlaySettings;
import com.github.mayconr.juoserver.game.model.Location;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public class UOFileReaderImpl implements UOFileReader{

    private final GamePlaySettings settings;
    private GameDataProvider gameDataProvider;

    public void loadFiles() {
        log.info("Loading files...");
        gameDataProvider = GameDataProviderFactory.create(Path.of(settings.files().dataFileRoot()), true);
        log.info("Loading files done!");
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
    public boolean hasBlockingStatics(int x, int y, int z) {
        boolean impassable = false;
        for (StaticTile statics : gameDataProvider.staticsAt(x, y)) {
            if (statics.flags().contains(TileFlag.IMPASSABLE)) {
                impassable = true;
                break;
            }
        }
        return impassable;
    }

    @Override
    public Optional<Skill> getSkill(int skillId) {
        return gameDataProvider.skillBy(skillId);
    }
}
