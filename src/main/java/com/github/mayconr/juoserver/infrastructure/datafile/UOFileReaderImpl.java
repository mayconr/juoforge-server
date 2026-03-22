package com.github.mayconr.juoserver.infrastructure.datafile;

import com.github.mayconr.juoserver.game.GamePlaySettings;
import com.github.mayconr.juoserver.game.model.LandTile;
import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.Static;
import eu.janinko.andaria.ultimasdk.files.Map;
import eu.janinko.andaria.ultimasdk.files.Statics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class UOFileReaderImpl implements UOFileReader{

    private final GamePlaySettings settings;
    private Map map;
    private Statics statics;

    public void loadFiles() {
        log.info("Loading files...");
        try {
            final var mapPath = Paths.get(settings.files().dataFileRoot() + File.separator + "map0.mul");
            this.map = Map.open(mapPath);
            log.info("Map loaded successfully!");

            Path staidx0 = Paths.get(settings.files().dataFileRoot() + File.separator + "staidx0.mul");
            Path statics0 = Paths.get(settings.files().dataFileRoot() + File.separator + "statics0.mul");
            this.statics = Statics.open(staidx0, statics0);
            log.info("Statics loaded successfully!");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public LandTile getLandTile(Location location) {
        return getLandTile(location.getX(), location.getY());
    }

    public LandTile getLandTile(int x, int y) {
        try {
            final var tile = map.getTile(x, y);
            return new LandTile(tile.getId(), tile.getX(), tile.getY(), tile.getAlt());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public List<Static> getStatics(Location location) {
        return getStatics(location.getX(), location.getY());
    }

    public List<Static> getStatics(int x, int y) {
        try {
            return statics.getStatics(x, y)
                    .stream()
                    .map(s->new Static(s.getId(), s.getX(), s.getY(), s.getZ(), s.getColor(), s.getXBlock(), s.getYBlock()))
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
