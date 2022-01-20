package atomFinal;

import battlecode.common.*;
import java.util.*;

public class Builder {
    static MapLocation currentLoc;
    static int randomMoves = 0;
    static boolean healing = false;

    static void runBuilder(RobotController rc) throws GameActionException {
        currentLoc = rc.getLocation();
        Team opponent = rc.getTeam().opponent();

        //UnitCounter.addBuilders(rc);
        checkPossibleMetalLocationsExist(rc);
        checkNeedsHealing(rc);

        RobotInfo[] nearbyRobots = rc.senseNearbyRobots();
        int nearbyMinerCount = 0;

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
        //not in range of base, move closer
        //else, try to build in minimal rubble
        if(distanceToClosest >= RobotType.ARCHON.visionRadiusSquared){
            Pathfinding.greedyPathfinding(rc, closestBase);
        } else{
            rc.wander();
            
            ArrayList<Direction> dirs = new ArrayList<Direction>();
            dirs.add(Direction.NORTH);
            dirs.add(Direction.NORTHEAST);
            dirs.add(Direction.EAST);
            dirs.add(Direction.SOUTHEAST);
            dirs.add(Direction.SOUTH);
            dirs.add(Direction.SOUTHWEST);
            dirs.add(Direction.WEST);
            dirs.add(Direction.NORTHWEST);

            Direction leastRubble = leastRubble(rc, dirs);

            RobotTyple robotToBuild = null;
            if(UnitCounter.getWatchtower() > UnitCounter.getLaboratory()){
                robotToBuild = LABORATORY;
            } else {    
                robotToBuild = WATCHTOWER;
            }

            if(rc.canBuildRobot(robotToBuild, leastRubble)){
                rc.buildRobot(robotToBuild, leastRubble);
            }
        }


    }

    static void checkNeedsHealing(RobotController rc) throws GameActionException {
        if (rc.getHealth() < 10 || healing) {
            healing = true;

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

            if (closestBase != null
                    && closestBase.distanceSquaredTo(rc.getLocation()) > RobotType.ARCHON.actionRadiusSquared - 4) {
                Direction dir = rc.getLocation().directionTo(closestBase);
                dir = Pathfinding.greedyPathfinding(rc, dir);
                if (rc.canMove(dir)) {
                    rc.move(dir);
                }
            } else if (Data.spawnBaseLocation.distanceSquaredTo(rc.getLocation()) > RobotType.ARCHON.actionRadiusSquared
                    - 4) {
                Direction dir = rc.getLocation().directionTo(Data.spawnBaseLocation);
                dir = Pathfinding.greedyPathfinding(rc, dir);
                if (rc.canMove(dir)) {
                    rc.move(dir);
                }
            }
        }
        if (healing && rc.getHealth() >= 35) {
            healing = false;
        }
    }

    static void init(RobotController rc) throws GameActionException {

    }
}
