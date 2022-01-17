package atomV4d5random;

import battlecode.common.*;
import java.util.*;

public class Miner {
    static MapLocation currentLoc;
    static int randomMoves = 0;
    static boolean healing = false;
    static boolean shouldEcoterroism = false;

    static void runMiner(RobotController rc) throws GameActionException {
        currentLoc = rc.getLocation();
        Team opponent = rc.getTeam().opponent();

        shouldEcoterroism = false;
        UnitCounter.addMiner(rc);
        checkPossibleMetalLocationsExist(rc);
        //checkNeedsHealing(rc);

        RobotInfo[] nearbyRobots = rc.senseNearbyRobots();
        int nearbyMinerCount = 0;

        if (nearbyRobots.length > 0) {
            for (int i = 0; i < nearbyRobots.length; i++) {
                RobotInfo robot = nearbyRobots[i];
                if (robot.getTeam() == rc.getTeam() && robot.getType() == RobotType.MINER) {
                    nearbyMinerCount++;
                } else if (robot.getTeam() == opponent && (robot.getType() == RobotType.SOLDIER
                        || robot.getType() == RobotType.SAGE)) {

                    Communication.addEnemyLocation(rc, Communication.convertMapLocationToInt(robot.getLocation()));

                    /*MapLocation closestSoldier = null;
                    int distanceToSoldier = Integer.MAX_VALUE;
                    RobotInfo[] alliesInVisionRange = rc.senseNearbyRobots(-1, rc.getTeam());
                    for (int j = 0; j < alliesInVisionRange.length; j++) {
                        RobotInfo ally = alliesInVisionRange[i];
                        if (ally.getType() == RobotType.SOLDIER
                                && rc.getLocation().distanceSquaredTo(ally.getLocation()) < distanceToSoldier) {
                            closestSoldier = ally.getLocation();
                            distanceToSoldier = rc.getLocation().distanceSquaredTo(ally.getLocation());
                        }
                    }
                    
                    if (closestSoldier != null) {
                        Direction toSoldier = Pathfinding.greedyPathfinding(rc,
                                rc.getLocation().directionTo(closestSoldier));
                        if (rc.canMove(toSoldier)) {
                            rc.move(toSoldier);
                        }
                    }*/

                    //Direction dir = rc.getLocation().directionTo(robot.getLocation()).opposite();
                    //dir = Pathfinding.greedyPathfinding(rc, dir);
                    Direction dir = Pathfinding.escapeEnemies(rc);
                    if (rc.canMove(dir)) {
                        rc.move(dir);
                    }
                    Pathfinding.setNewExploreLocation(rc);
                } else if (robot.getTeam() == opponent) {
                    Communication.addEnemyLocation(rc, Communication.convertMapLocationToInt(robot.getLocation()));
                    if (robot.getType() == RobotType.MINER && currentLoc.distanceSquaredTo(robot.getLocation()) <= 4) {
                        shouldEcoterroism = true;
                    }
                }
            }
        }

        //tries to stop miners from flocking
        /*
        int mapArea = rc.getMapWidth() * rc.getMapHeight();
        
        if (nearbyMinerCount > 2 && UnitCounter.getMiners(rc) < mapArea / 8) {
            Direction dir = null;
            dir = Pathfinding.wander(rc);
            if (rc.canMove(dir)) {
                rc.move(dir);
                rc.setIndicatorString("wanderFROMFLOCK");
            }
        }*/

        action(rc);
    }

    static void action(RobotController rc) throws GameActionException {
        checkShouldEcoterroism(rc);

        ArrayList<MetalLocation> metalLocations = senseNearbyMetals(rc);
        MetalLocation target = null;
        int distanceToTarget = Integer.MAX_VALUE;

        for (MetalLocation loc : metalLocations) {
            int distanceToLoc = currentLoc.distanceSquaredTo(loc.location);
            if (distanceToLoc < distanceToTarget) {
                target = loc;
                distanceToTarget = distanceToLoc;
            }
        }

        if (target != null) {
            MapLocation targetLoc = target.location;

            MapLocation[] surroundings = new MapLocation[] { targetLoc, targetLoc.add(Direction.NORTH),
                    targetLoc.add(Direction.WEST), targetLoc.add(Direction.EAST), targetLoc.add(Direction.SOUTH),
                    targetLoc.add(Direction.NORTHEAST), targetLoc.add(Direction.NORTHWEST),
                    targetLoc.add(Direction.SOUTHEAST), targetLoc.add(Direction.SOUTHWEST) };

            MapLocation leastRubbleLocation = null;
            int rubbleAtleastRubbleLocation = Integer.MAX_VALUE;

            for (int i = 0; i < surroundings.length; i++) {
                int distanceFromBase = surroundings[i].distanceSquaredTo(Data.spawnBaseLocation);
                if (distanceFromBase > 2 && rc.canSenseLocation(surroundings[i])
                        && !rc.canSenseRobotAtLocation(surroundings[i])
                        && rc.senseRubble(surroundings[i]) < rubbleAtleastRubbleLocation) {
                    leastRubbleLocation = surroundings[i];
                    rubbleAtleastRubbleLocation = rc.senseRubble(surroundings[i]);
                }
            }

            if (leastRubbleLocation != null) {
                Direction moveToOptimalLocation = Pathfinding.greedyPathfinding(rc, leastRubbleLocation);
                if (rc.canMove(moveToOptimalLocation)) {
                    rc.move(moveToOptimalLocation);
                }
            }
            //if target is within mining distance
            if (distanceToTarget <= 2) {
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        MapLocation mineLocation = new MapLocation(target.location.x + dx, target.location.y + dy);
                        if (shouldEcoterroism) {
                            if (rc.canSenseLocation(mineLocation)) {
                                if (rc.senseLead(mineLocation) > 0) {
                                    while (rc.canMineLead(mineLocation)) {
                                        rc.mineLead(mineLocation);
                                    }
                                } else if (rc.senseGold(mineLocation) > 0) {
                                    while (rc.canMineGold(mineLocation)) {
                                        rc.mineGold(mineLocation);
                                    }
                                }
                            }
                        } else {
                            if (rc.canSenseLocation(mineLocation)) {
                                if (rc.senseLead(mineLocation) > 2) {
                                    while (rc.canMineLead(mineLocation) && rc.senseLead(mineLocation) > 2) {
                                        rc.mineLead(mineLocation);
                                    }
                                } else if (rc.senseGold(mineLocation) > 0) {
                                    while (rc.canMineGold(mineLocation)) {
                                        rc.mineGold(mineLocation);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Direction dir = Pathfinding.randomMiners(rc);
            if (rc.canMove(dir)) {
                rc.move(dir);
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

    static void checkShouldEcoterroism(RobotController rc) throws GameActionException {
        int[] allyArchons = Communication.getArchonLocations(rc);
        MapLocation closestAllyBase = null;
        int distanceToClosestAllyBase = Integer.MAX_VALUE;

        for (int i = 0; i < allyArchons.length; i++) {
            if (allyArchons[i] != 0 && Communication.convertIntToMapLocation(allyArchons[i])
                    .distanceSquaredTo(rc.getLocation()) < distanceToClosestAllyBase) {
                closestAllyBase = Communication.convertIntToMapLocation(allyArchons[i]);
                distanceToClosestAllyBase = Communication.convertIntToMapLocation(allyArchons[i])
                        .distanceSquaredTo(rc.getLocation());
            }
        }

        int[] enemyArchons = Communication.getEnemyArconLocations(rc);
        MapLocation closestEnemyBase = null;
        int distanceToClosestEnemyBase = Integer.MAX_VALUE;
        for (int i = 0; i < enemyArchons.length; i++) {
            if (enemyArchons[i] != 0 && Communication.convertIntToMapLocation(enemyArchons[i])
                    .distanceSquaredTo(rc.getLocation()) < distanceToClosestEnemyBase) {
                closestEnemyBase = Communication.convertIntToMapLocation(enemyArchons[i]);
                distanceToClosestEnemyBase = Communication.convertIntToMapLocation(enemyArchons[i])
                        .distanceSquaredTo(rc.getLocation());
            }
        }

        if (closestAllyBase != null && closestEnemyBase != null
                && distanceToClosestEnemyBase <= distanceToClosestAllyBase - 25) {
            shouldEcoterroism = true;
        }
    }

    static ArrayList<MetalLocation> senseNearbyMetals(RobotController rc) throws GameActionException {
        int vision = rc.getType().visionRadiusSquared;
        ArrayList<MetalLocation> metalLocations = new ArrayList<MetalLocation>();
        MapLocation[] locations = rc.senseNearbyLocationsWithLead(vision, 3);
        for (int i = 0; i < locations.length; i++) {
            MapLocation senseLoc = locations[i];
            int amnt = rc.senseLead(senseLoc);
            metalLocations.add(new MetalLocation("LEAD", amnt, senseLoc));
        }
        locations = rc.senseNearbyLocationsWithGold(vision);
        for (int i = 0; i < locations.length; i++) {
            MapLocation senseLoc = locations[i];
            int amnt = rc.senseLead(senseLoc);
            metalLocations.add(new MetalLocation("GOLD", amnt, senseLoc));
        }
        return metalLocations;
    }

    static MetalLocation findNearestMetalLocation(ArrayList<MetalLocation> metalLocations, RobotController rc)
            throws GameActionException {
        MetalLocation target = null;
        int distanceToTarget = Integer.MAX_VALUE;
        boolean foundGold = false;

        for (MetalLocation loc : metalLocations) {
            String type = loc.type;
            int distanceToLoc = currentLoc.distanceSquaredTo(loc.location);

            if (type.equals("GOLD")) {
                if (!foundGold) {
                    target = loc;
                    distanceToTarget = distanceToLoc;
                    foundGold = true;
                } else {
                    if (distanceToLoc < distanceToTarget) {
                        target = loc;
                        distanceToTarget = distanceToLoc;
                    }
                }
            }
            if (distanceToLoc < distanceToTarget && !foundGold) {
                target = loc;
                distanceToTarget = distanceToLoc;
            }
        }

        return target;
    }

    static MetalLocation findFarthestMetalLocationFromBase(ArrayList<MetalLocation> metalLocations, RobotController rc)
            throws GameActionException {
        MetalLocation target = null;
        int distanceToTargetFromBase = 0;
        boolean foundGold = false;

        for (MetalLocation loc : metalLocations) {
            String type = loc.type;
            int distanceToLocFromBase = Data.spawnBaseLocation.distanceSquaredTo(loc.location);

            if (type.equals("GOLD")) {
                if (!foundGold) {
                    target = loc;
                    distanceToTargetFromBase = distanceToLocFromBase;
                    foundGold = true;
                } else {
                    if (distanceToLocFromBase > distanceToTargetFromBase) {
                        target = loc;
                        distanceToTargetFromBase = distanceToLocFromBase;
                    }
                }
            }
            if (distanceToLocFromBase > distanceToTargetFromBase && !foundGold) {
                target = loc;
                distanceToTargetFromBase = distanceToLocFromBase;
                if (target.amount > 10) {
                    Communication.addMetalLocation(rc, Communication.convertMapLocationToInt(target.location));
                }
            }
        }

        return target;
    }

    static int getClosestPossibleMetalLocation(RobotController rc) throws GameActionException {
        int[] metalLocations = Communication.getMetalLocations(rc);
        int closestMetalLocation = 0;
        int distanceSquaredToClosest = Integer.MAX_VALUE;

        for (int i = 0; i < metalLocations.length; i++) {
            if (!(metalLocations[i] == 0)) {
                MapLocation location = Communication.convertIntToMapLocation(metalLocations[i]);
                if (rc.getLocation().distanceSquaredTo(location) < distanceSquaredToClosest) {
                    closestMetalLocation = metalLocations[i];
                    distanceSquaredToClosest = rc.getLocation().distanceSquaredTo(location);
                }
            }
        }
        return closestMetalLocation;
    }

    static void checkPossibleMetalLocationsExist(RobotController rc) throws GameActionException {
        int closestPossibleMetal = getClosestPossibleMetalLocation(rc);
        MapLocation closestPossibleMetalLocation = null;
        if (closestPossibleMetal != 0) {
            closestPossibleMetalLocation = Communication.convertIntToMapLocation(closestPossibleMetal);
            if (rc.canSenseLocation(closestPossibleMetalLocation)) {
                if (rc.senseLead(closestPossibleMetalLocation) < 10) {
                    Communication.removeMetalLocation(closestPossibleMetal, rc);
                }
            }
        }
    }

    static void init(RobotController rc) throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots(3, rc.getTeam());
        for (int i = 0; i < robots.length; i++) {
            RobotInfo robot = robots[i];
            if (robot.getType() == RobotType.ARCHON) {
                Data.spawnBaseLocation = robot.getLocation();
            }
        }
        Data.rng = new Random(rc.getID());
    }
}
