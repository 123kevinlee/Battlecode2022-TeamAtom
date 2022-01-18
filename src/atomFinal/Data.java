package atomV4d5random;

import battlecode.common.*;
import java.util.*;

public class Data {
    static final Direction[] directions = {
            Direction.NORTH,
            Direction.WEST,
            Direction.EAST,
            Direction.SOUTH,
            Direction.SOUTHEAST,
            Direction.SOUTHWEST,
            Direction.NORTHWEST,
            Direction.NORTHEAST,
    };

    static int randCounter = 0;
    static Direction randDirection = Direction.CENTER;
    static MapLocation spawnBaseLocation = null;

    static int determineEnemyValue(RobotInfo robot) {
        switch (robot.getType()) {
            case SAGE:
                return 1;
            case SOLDIER:
                return 2;
            case WATCHTOWER:
                return 3;
            case BUILDER:
                return 4;
            case ARCHON:
                return 5;
            case MINER:
                return 6;
            case LABORATORY:
                return 7;
        }
        return 0;
    }

    static Random rng = new Random();
}
