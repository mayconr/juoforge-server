package com.github.mayconr.juoserver.game.reader;

import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.LandTile;
import com.github.mayconr.juoserver.game.model.Static;
import eu.janinko.andaria.ultimasdk.files.Map;
import eu.janinko.andaria.ultimasdk.files.Statics;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
public class UOFileReader {

    private Map map;
    private Statics statics;

    public void loadFiles() {
        log.info("Loading files...");
        try {
            final var mapPath = Paths.get("C:\\Program Files (x86)\\Electronic Arts\\Ultima Online Classic\\map0.mul");
            this.map = Map.open(mapPath);
            log.info("Map loaded successfully!");

            Path staidx0 = Paths.get("C:\\Program Files (x86)\\Electronic Arts\\Ultima Online Classic\\staidx0.mul");
            Path statics0 = Paths.get("C:\\Program Files (x86)\\Electronic Arts\\Ultima Online Classic\\statics0.mul");
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
