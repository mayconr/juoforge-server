package com.github.mayconr.juoserver.game.model;

import java.util.ArrayList;
import java.util.List;

public class GameMath {

    public static List<Location> line(Location start, Location end){

        List<Location> points = new ArrayList<>();

        int x1 = start.getX();
        int y1 = start.getY();
        int x2 = end.getX();
        int y2 = end.getY();

        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);

        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;

        int err = dx - dy;

        while(true){

            points.add(new PointInTheWorld(x1,y1, 0));

            if(x1 == x2 && y1 == y2){
                break;
            }

            int e2 = err * 2;

            if(e2 > -dy){
                err -= dy;
                x1 += sx;
            }

            if(e2 < dx){
                err += dx;
                y1 += sy;
            }
        }

        return points;
    }

    public static boolean isInRange(Location location1, Location location2, int radius) {
        int dx = Math.abs(location1.getX() - location2.getX());
        int dy = Math.abs(location1.getY() - location2.getY());

        if (Math.max(dx, dy) > radius) {
            return false;
        }

        int dz = Math.abs(location1.getZ() - location2.getZ());
        return dz <= 8;
    }
}
