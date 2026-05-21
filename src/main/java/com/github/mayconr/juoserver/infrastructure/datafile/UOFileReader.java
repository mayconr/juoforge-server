package com.github.mayconr.juoserver.infrastructure.datafile;

import com.github.mayconr.juoforge.reader.skill.Skill;
import com.github.mayconr.juoforge.reader.view.LandTile;
import com.github.mayconr.juoforge.reader.view.StaticTile;
import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.UOMobile;

import java.util.List;
import java.util.Optional;

public interface UOFileReader {

    LandTile getLandTile(Location location);

    LandTile getLandTile(int x, int y);

    List<StaticTile> getStatics(Location location);

    List<StaticTile> getStatics(int x, int y);

    boolean hasBlockingCollision(UOMobile mobile, int x, int y, int z);

    boolean hasBlockingCollision(UOMobile mobile, Location location);

    Optional<Skill> getSkill(int skillId);
}
