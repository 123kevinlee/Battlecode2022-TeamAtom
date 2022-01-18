package atomV4d5random;

import battlecode.common.*;
import java.util.*;

public class Pathfinding {
    public static Direction basicBug(RobotController rc, Direction dir) throws GameActionException {
        if (dir == null) {
            return Direction.CENTER;
        } else if (rc.canMove(dir)) {
            return dir;
        } else {
            Direction attemptDir = null;
            for (int i = 1; i < 8; i++) {
                switch (i) {
                    case 1:
                        attemptDir = dir.rotateRight();
                        break;
                    case 2:
                        attemptDir = dir.rotateRight().rotateRight();
                        break;
                    case 3:
                        attemptDir = dir.rotateRight().rotateRight().rotateRight();
                        break;
                    case 4:
                        attemptDir = dir.rotateLeft();
                        break;
                    case 5:
                        attemptDir = dir.rotateLeft().rotateLeft();
                        break;
                    case 6:
                        attemptDir = dir.rotateLeft().rotateLeft().rotateLeft();
                        break;
                    case 7:
                        attemptDir = dir.opposite();
                        break;
                    default:
                        break;
                }
                if (rc.canMove(attemptDir)) {
                    return attemptDir;
                }
            }
            return Direction.CENTER;
        }
    }

    public static Direction basicBug(RobotController rc, MapLocation target) throws GameActionException {
        Direction dir = rc.getLocation().directionTo(target);
        return basicBug(rc, dir);
    }

    public static MapLocation leastRubble(RobotController rc, ArrayList<MapLocation> options)
            throws GameActionException {
        int minRubble = Integer.MAX_VALUE;
        MapLocation out = null;

        for (MapLocation ml : options) {
            if ((rc.senseRubble(ml) < minRubble)) {
                minRubble = rc.senseRubble(ml);
                out = ml;
            }
        }
        return out;
    }

    public static Direction greedyPathfinding(RobotController rc, Direction dir) throws GameActionException {
        if (dir == null) {
            return Direction.CENTER;
        } else {
            Direction leftDir = dir.rotateLeft();
            Direction rightDir = dir.rotateRight();
            //Direction leftlDir = dir.rotateLeft().rotateLeft();
            //Direction rightrDir = dir.rotateRight().rotateRight();
            MapLocation frontLoc = rc.getLocation().add(dir);
            MapLocation leftLocation = rc.getLocation().add(leftDir);
            MapLocation rightLocation = rc.getLocation().add(rightDir);
            //MapLocation leftlLocation = rc.getLocation().add(leftlDir);
            //MapLocation rightrLocation = rc.getLocation().add(rightrDir);

            ArrayList<MapLocation> options = new ArrayList<MapLocation>();
            if (rc.canSenseLocation(frontLoc) && !rc.canSenseRobotAtLocation(frontLoc)) {
                options.add(frontLoc);
            }
            if (rc.canSenseLocation(leftLocation) && !rc.canSenseRobotAtLocation(leftLocation)) {
                options.add(leftLocation);
            }
            if (rc.canSenseLocation(rightLocation) && !rc.canSenseRobotAtLocation(rightLocation)) {
                options.add(rightLocation);
            }
            /*
            if (rc.canSenseLocation(leftlLocation)) {
                options.add(leftlLocation);
            }
            if (rc.canSenseLocation(rightrLocation)) {
                options.add(rightrLocation);
            }*/

            MapLocation best = leastRubble(rc, options);

            if (best != null && rc.senseRubble(best) == 99) {
                MapLocation loc = rc.getLocation();
                if (rc.canMove(dir.rotateLeft().rotateLeft()) && rc.canMove(dir.rotateRight().rotateRight())
                        && rc.canSenseLocation(loc.add(dir.rotateLeft().rotateLeft()))
                        && rc.canSenseLocation(loc.add(dir.rotateRight().rotateRight()))
                        && rc.senseRubble(loc.add(dir.rotateLeft().rotateLeft())) < 99
                        && rc.senseRubble(loc.add(dir.rotateRight().rotateRight())) < 99) {
                    int rubbleLeft = rc.senseRubble(loc.add(dir.rotateLeft().rotateLeft()));
                    int rubbleRight = rc.senseRubble(loc.add(dir.rotateRight().rotateRight()));
                    if (rubbleLeft <= rubbleRight) {
                        return dir.rotateLeft().rotateLeft();
                    } else {
                        return dir.rotateRight().rotateRight();
                    }
                } else if (rc.canMove(dir.rotateLeft().rotateLeft())
                        && rc.canSenseLocation(loc.add(dir.rotateLeft().rotateLeft()))
                        && rc.senseRubble(loc.add(dir.rotateLeft().rotateLeft())) < 99) {
                    return dir.rotateLeft().rotateLeft();
                } else if (rc.canMove(dir.rotateRight().rotateRight())
                        && rc.canSenseLocation(loc.add(dir.rotateRight().rotateRight()))
                        && rc.senseRubble(loc.add(dir.rotateRight().rotateRight())) < 99) {
                    return dir.rotateRight().rotateRight();
                } else if (rc.canMove(dir.opposite().rotateLeft()) && rc.canMove(dir.opposite().rotateRight())
                        && rc.canSenseLocation(loc.add(dir.opposite().rotateLeft()))
                        && rc.canSenseLocation(loc.add(dir.opposite().rotateRight()))
                        && rc.senseRubble(loc.add(dir.opposite().rotateLeft())) < 99
                        && rc.senseRubble(loc.add(dir.opposite().rotateRight())) < 99) {
                    int rubbleLeft = rc.senseRubble(loc.add(dir.opposite().rotateLeft()));
                    int rubbleRight = rc.senseRubble(loc.add(dir.opposite().rotateRight()));
                    if (rubbleLeft <= rubbleRight) {
                        return dir.opposite().rotateLeft();
                    } else {
                        return dir.opposite().rotateRight();
                    }
                } else if (rc.canMove(dir.opposite().rotateLeft())
                        && rc.canSenseLocation(loc.add(dir.opposite().rotateLeft()))
                        && rc.senseRubble(loc.add(dir.opposite().rotateLeft())) < 99) {
                    return dir.opposite().rotateLeft();
                } else if (rc.canMove(dir.opposite().rotateRight())
                        && rc.canSenseLocation(loc.add(dir.opposite().rotateRight()))
                        && rc.senseRubble(loc.add(dir.opposite().rotateRight())) < 99) {
                    return dir.opposite().rotateRight();
                } else if (rc.canMove(dir.opposite())
                        && rc.canSenseLocation(loc.add(dir.opposite()))
                        && rc.senseRubble(loc.add(dir.opposite())) < 99) {
                    return dir.opposite();
                }
            }
            if (best != null) {
                Direction bestDir = rc.getLocation().directionTo(best);
                if (rc.canMove(bestDir)) {
                    return bestDir;
                }
            } else {
                if (rc.canMove(dir)) {
                    return dir;
                }
            }
        }
        return Direction.CENTER;
    }

    public static Direction greedyPathfinding(RobotController rc, MapLocation target) throws GameActionException {
        Direction dir = rc.getLocation().directionTo(target);
        return greedyPathfinding(rc, dir);
    }

    public static Direction randomDir(RobotController rc) throws GameActionException {
        int random = Data.rng.nextInt(8);
        Direction dir = Data.directions[random];
        return dir;
    }

    public static Direction wander(RobotController rc) throws GameActionException {
        MapLocation current = rc.getLocation();
        int height = rc.getMapHeight();
        int width = rc.getMapWidth();
        //away from archons and away from walls
        //hit wall, turn 135 (right 3 times)

        MapLocation base = Data.spawnBaseLocation;
        Direction attemptDir = current.directionTo(base).opposite();

        /*MapLocation center = new MapLocation(width / 2, height / 2);
        if (Data.spawnBaseLocation.distanceSquaredTo(center) < Math.min(height / 4, width / 4)) {
            Direction awayCenter = Data.spawnBaseLocation.directionTo(center).opposite();
            if (attemptDir == awayCenter || attemptDir == awayCenter.rotateLeft()
                    || attemptDir == awayCenter.rotateRight()) {
                attemptDir = attemptDir.opposite();
            }
        }*/

        int distThreshold = Integer.MAX_VALUE;
        if (attemptDir == Direction.NORTH || attemptDir == Direction.SOUTH) {
            distThreshold = rc.getMapHeight() * 3 / 5;
        } else if (attemptDir == Direction.WEST || attemptDir == Direction.EAST) {
            distThreshold = rc.getMapWidth() * 3 / 5;
        } else {
            distThreshold = (int) (Math
                    .sqrt(Math.pow(rc.getMapHeight(), 2) + Math.pow(rc.getMapWidth(), 2) * 3 / 5));
        }

        if (Math.sqrt(rc.getLocation().distanceSquaredTo(Data.spawnBaseLocation)) > distThreshold) {
            rc.setIndicatorString("wanderfree");
            return greedyPathfinding(rc, randomDir(rc));
        }

        int rand = (int) (Math.random() * 3);
        if (rand == 1) {
            attemptDir = attemptDir.rotateLeft();
        } else if (rand == 2) {
            attemptDir = attemptDir.rotateRight();
        }

        if (current.x == 0) {
            if (current.y > base.y)
                attemptDir = attemptDir.rotateRight().rotateRight().rotateRight();
            else
                attemptDir = attemptDir.rotateLeft().rotateLeft().rotateLeft();
        } else if (current.x == width - 1) {
            if (current.y < base.y)
                attemptDir = attemptDir.rotateRight().rotateRight().rotateRight();
            else
                attemptDir = attemptDir.rotateLeft().rotateLeft().rotateLeft();
        } else if (current.y == 0) {
            if (current.x < base.x)
                attemptDir = attemptDir.rotateRight().rotateRight().rotateRight();
            else
                attemptDir = attemptDir.rotateLeft().rotateLeft().rotateLeft();
        } else if (current.x == height - 1) {
            if (current.x > base.x)
                attemptDir = attemptDir.rotateRight().rotateRight().rotateRight();
            else
                attemptDir = attemptDir.rotateLeft().rotateLeft().rotateLeft();
        }
        return greedyPathfinding(rc, attemptDir);
    }

    static int extended = 0;
    static Direction extendedDirection = Direction.CENTER;
    static int extendedRand = 0;
    static Direction extendedRandDirection = Direction.CENTER;
    static int extendedWall = 0;
    static Direction extendedWallDirection = Direction.CENTER;

    public static Direction wander2(RobotController rc) throws GameActionException {
        MapLocation loc = rc.getLocation();
        int height = rc.getMapHeight();
        int width = rc.getMapWidth();

        MapLocation base = Data.spawnBaseLocation;
        MapLocation center = new MapLocation(rc.getMapWidth() / 2, rc.getMapHeight() / 2);
        //Direction attemptDir = loc.directionTo(base).opposite();
        Direction attemptDir = base.directionTo(center);

        int distThreshold = Integer.MAX_VALUE;
        if (attemptDir == Direction.NORTH || attemptDir == Direction.SOUTH) {
            distThreshold = rc.getMapHeight() * 3 / 5;
        } else if (attemptDir == Direction.WEST || attemptDir == Direction.EAST) {
            distThreshold = rc.getMapWidth() * 3 / 5;
        } else {
            distThreshold = (int) (Math
                    .sqrt(Math.pow(rc.getMapHeight(), 2) + Math.pow(rc.getMapWidth(), 2) * 3 / 5));
        }

        int rand = (int) (Math.random() * 2);
        if (loc.x == 0) {
            if (rand == 0) {
                extendedWallDirection = Direction.NORTHEAST;
            } else {
                extendedWallDirection = Direction.SOUTHEAST;
            }
            extendedWall = 5;
        } else if (loc.x == width - 1) {

            if (rand == 0) {
                extendedWallDirection = Direction.NORTHWEST;
            } else {
                extendedWallDirection = Direction.SOUTHWEST;
            }
            extendedWall = 5;
        } else if (loc.y == 0) {
            if (rand == 0) {
                extendedWallDirection = Direction.NORTHEAST;
            } else {
                extendedWallDirection = Direction.NORTHWEST;
            }
            extendedWall = 5;
        } else if (loc.x == height - 1) {
            if (rand == 0) {
                extendedWallDirection = Direction.SOUTHEAST;
            } else {
                extendedWallDirection = Direction.SOUTHWEST;
            }
            extendedWall = 5;
        }

        if (extendedWall > 0) {
            attemptDir = greedyPathfinding(rc, extendedWallDirection);
            if (rc.canMove(attemptDir)) {
                extendedWall--;
            }
            return attemptDir;
        }

        if (Math.sqrt(rc.getLocation().distanceSquaredTo(Data.spawnBaseLocation)) >= distThreshold) {
            if (extendedRand == 0) {
                attemptDir = randomDir(rc);
                extendedRandDirection = attemptDir;
                extendedRand = 5;
                return greedyPathfinding(rc, attemptDir);
            } else {
                attemptDir = greedyPathfinding(rc, extendedRandDirection);
                if (rc.canMove(attemptDir)) {
                    extendedRand--;
                }
                return attemptDir;
            }
        }

        attemptDir = base.directionTo(center);
        if (extended == 0) {
            rand = Data.rng.nextInt(3);

            if (rand == 1) {
                attemptDir = attemptDir.rotateLeft();
            } else if (rand == 2) {
                attemptDir = attemptDir.rotateRight();
            }
            extended = 2;
            extendedDirection = attemptDir;
            return greedyPathfinding(rc, extendedDirection);
        } else {
            attemptDir = greedyPathfinding(rc, extendedDirection);
            if (rc.canMove(attemptDir)) {
                extended--;
            }
            return greedyPathfinding(rc, extendedDirection);
        }

        //return greedyPathfinding(rc, attemptDir);
    }

    //static boolean hitCenter = false;
    static Direction current = null;

    public static Direction explore(RobotController rc) throws GameActionException {
        int width = rc.getMapWidth();
        int height = rc.getMapHeight();
        MapLocation center = new MapLocation(width / 2, height / 2);
        MapLocation loc = rc.getLocation();
        //int centerThreshold = (int) Math.pow(Math.min(width, height) / 5, 2);

        Direction toCenter = rc.getLocation().directionTo(center);
        if (current == null) {
            int rand = Data.rng.nextInt(6);
            if (rand == 0) {
                current = toCenter;
            } else if (rand == 1) {
                current = toCenter.rotateLeft();
            } else if (rand == 2) {
                current = toCenter.rotateRight();
            } else if (rand == 3) {
                current = toCenter.rotateRight().rotateRight();
            } else if (rand == 4) {
                current = toCenter.rotateLeft().rotateLeft();
            } else if (rand == 5) {
                current = toCenter.opposite();
            }
        } else if (loc.x == 0) {
            int rand = (int) (Math.random() * 2);
            if (rand == 0) {
                current = Direction.NORTHEAST;
            } else {
                current = Direction.SOUTHEAST;
            }
        } else if (loc.x == width - 1) {
            int rand = (int) (Math.random() * 2);
            if (rand == 0) {
                current = Direction.NORTHWEST;
            } else {
                current = Direction.SOUTHWEST;
            }
        } else if (loc.y == 0) {
            int rand = (int) (Math.random() * 2);
            if (rand == 0) {
                current = Direction.NORTHEAST;
            } else {
                current = Direction.NORTHWEST;
            }
        } else if (loc.x == height - 1) {
            int rand = (int) (Math.random() * 2);
            if (rand == 0) {
                current = Direction.SOUTHEAST;
            } else {
                current = Direction.SOUTHWEST;
            }
        }
        return greedyPathfinding(rc, current);
    }

    public static MapLocation randomLocation = null;

    public static Direction randomMiners(RobotController rc) throws GameActionException {
        MapLocation loc = rc.getLocation();
        int height = rc.getMapHeight();
        int width = rc.getMapWidth();

        //int r = (int) (Math.random() * (upper - lower)) + lower; upper bound is exclusive

        if (randomLocation == null || loc.distanceSquaredTo(randomLocation) <= 20) {
            int lower = 0;
            int upper = width;
            int x = Data.rng.nextInt(upper - lower) + lower;
            upper = height;
            int y = Data.rng.nextInt(upper - lower) + lower;
            randomLocation = new MapLocation(x, y);
        }

        //System.out.println(randomLocation.toString());
        return greedyPathfinding(rc, loc.directionTo(randomLocation));
    }

    public static void setNewExploreLocation(RobotController rc) throws GameActionException {
        int height = rc.getMapHeight();
        int width = rc.getMapWidth();
        int lower = 0;
        int upper = width;
        int x = Data.rng.nextInt(upper - lower) + lower;
        upper = height;
        int y = Data.rng.nextInt(upper - lower) + lower;
        randomLocation = new MapLocation(x, y);
    }

    public static Direction escapeEnemies(RobotController rc) throws GameActionException {
        RobotInfo[] enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent()); //gets all within vision; 100 bytecode
        MapLocation destination = rc.getLocation();
        for (RobotInfo r : enemies) {
            if (r.getType() == RobotType.SOLDIER || r.getType() == RobotType.SAGE) {
                Direction awayFromEnemy = rc.getLocation().directionTo(r.getLocation()).opposite();
                destination.add(awayFromEnemy);
            }
        }

        if (rc.getLocation().directionTo(destination) == Direction.CENTER) {
            MapLocation closestAllyArchon = getClosestAllyArchon(rc);
            destination = closestAllyArchon;
        }
        return greedyPathfinding(rc, destination);
    }

    static MapLocation getClosestAllyArchon(RobotController rc) throws GameActionException {
        int[] allyArchons = Communication.getArchonLocations(rc);
        MapLocation closestBase = null;
        int distanceToClosest = Integer.MAX_VALUE;

        for (int i = 0; i < allyArchons.length; i++) {
            if (allyArchons[i] != 0 && Communication.convertIntToMapLocation(allyArchons[i])
                    .distanceSquaredTo(rc.getLocation()) < distanceToClosest) {
                closestBase = Communication.convertIntToMapLocation(allyArchons[i]);
                distanceToClosest = Communication.convertIntToMapLocation(allyArchons[i])
                        .distanceSquaredTo(rc.getLocation());
            }
        }

        return closestBase;
    }
}
