package atomArchonMovement;

import battlecode.common.*;
import javafx.scene.shape.Arc;

import java.util.*;

public class Archon {
    static int startSpawn = 0; //counter for initial spawn order

    static ArrayList<RobotType> spawnOrder = new ArrayList<RobotType>();
    static int spawnOrderCounter = 0;
    static ArrayList<Direction> spawnDirections = new ArrayList<Direction>();
    static int ogArchonNumber = 0;
    static boolean isMTS = false;

    static boolean enemyArchonNear = false;
    static boolean enemyNear = false;

    static boolean isMoving = false;
    static boolean transformBack = false;
    static MapLocation locationToMove = null;

    //static boolean seenEnemy = false;

    static void runArchon(RobotController rc) throws GameActionException {
        UnitCounter.reset(rc);

        if (rc.getRoundNum() % 3 == 0) {
            Communication.clearEnemyLocations(rc);
        }

        /*if (!seenEnemy) {
            int[] enemyLocations = Communication.getEnemyLocations(rc);
            for (int i = 0; i < enemyLocations.length; i++) {
                if (enemyLocations[i] != 0) {
                    seenEnemy = true;
                }
            }
        }*/

        enemyArchonNear = false;
        enemyNear = false;
        checkEnemyNear(rc);

        if (Communication.getArchonIds(rc)[Communication.getArchonSpawnIndex(rc)] == rc.getID()
                || rc.getArchonCount() != ogArchonNumber) {
            if (!enemyArchonNear) {
                if (startSpawn < 3) {
                    gameStartSequence(rc);
                } else if (startSpawn >= 3 && startSpawn < 6) {
                    soldierStartSequence(rc);
                } else {
                    moveArchonTowardsEnemy(rc);
                    if (isMTS) {
                        normalSpawnSequence(rc);
                    } else {
                        newSpawnLogic(rc);
                    }
                }
                if (rc.isActionReady()) {
                    heal(rc);
                }
            }
        }
        heal(rc);
    }

    public static void moveArchonTowardsEnemy(RobotController rc) throws GameActionException {
        String out = "";
        if (transformBack) {
            if (rc.canTransform()) {
                rc.transform();
                transformBack = false;
                isMoving = false;
                Communication.signalMovingArchonEnd(rc);
            }
        } else if (isMoving && rc.isMovementReady()) {
            int[] enemyLocations = Communication.getEnemyLocations(rc);
            MapLocation closestEnemyLocation = null;
            int distanceToClosestEnemy = Integer.MAX_VALUE;
            for (int i = 0; i < enemyLocations.length; i++) {
                out += enemyLocations[i] + ";";

                if (enemyLocations[i] != 0 && rc.getLocation().distanceSquaredTo(
                        Communication.convertIntToMapLocation(enemyLocations[i])) < distanceToClosestEnemy) {
                    closestEnemyLocation = Communication.convertIntToMapLocation(enemyLocations[i]);
                    distanceToClosestEnemy = rc.getLocation().distanceSquaredTo(
                            Communication.convertIntToMapLocation(enemyLocations[i]));
                }
            }
            RobotInfo[] allys = rc.senseNearbyRobots(-1, rc.getTeam());
            boolean nearAllyArchon = false;
            for (int i = 0; i < allys.length; i++) {
                if (allys[i].getType() == RobotType.ARCHON) {
                    nearAllyArchon = true;
                }
            }

            RobotInfo[] seenEnemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());

            if (nearAllyArchon || seenEnemies.length > 0 || (closestEnemyLocation != null
                    && distanceToClosestEnemy <= RobotType.ARCHON.visionRadiusSquared * 2)) {
                MapLocation current = rc.getLocation();

                MapLocation[] surroundings = new MapLocation[] { current, current.add(Direction.NORTH),
                        current.add(Direction.WEST), current.add(Direction.EAST), current.add(Direction.SOUTH),
                        current.add(Direction.NORTHEAST), current.add(Direction.NORTHWEST),
                        current.add(Direction.SOUTHEAST), current.add(Direction.SOUTHWEST) };

                MapLocation leastRubble = null;
                int leastRubbleAmnt = Integer.MAX_VALUE;

                for (int i = 0; i < surroundings.length; i++) {
                    MapLocation site = surroundings[i];
                    if (rc.canSenseLocation(site) && !rc.canSenseRobotAtLocation(site)
                            && rc.senseRubble(site) < leastRubbleAmnt) {
                        leastRubble = site;
                        leastRubbleAmnt = rc.senseRubble(site);
                    }
                }

                if (leastRubble != null) {
                    Direction dir = rc.getLocation().directionTo(leastRubble);
                    if (rc.canMove(Pathfinding.greedyPathfinding(rc, dir))) {
                        rc.move(Pathfinding.greedyPathfinding(rc, dir));
                        transformBack = true;
                        isMoving = false;
                    } else {
                        transformBack = true;
                        isMoving = false;
                    }
                } else {
                    transformBack = true;
                    isMoving = false;
                }
            } else {
                MapLocation[] surroundings = rc.getAllLocationsWithinRadiusSquared(locationToMove,
                        RobotType.ARCHON.actionRadiusSquared);

                MapLocation leastRubble = null;
                int leastRubbleAmnt = Integer.MAX_VALUE;

                for (int i = 0; i < surroundings.length; i++) {
                    MapLocation site = surroundings[i];
                    if (rc.canSenseLocation(site) && !rc.canSenseRobotAtLocation(site)
                            && rc.senseRubble(site) < leastRubbleAmnt) {
                        leastRubble = site;
                        leastRubbleAmnt = rc.senseRubble(site);
                    }
                }
                Direction dir = rc.getLocation().directionTo(locationToMove);

                if (leastRubble != null) {
                    dir = rc.getLocation().directionTo(leastRubble);
                    if (rc.canMove(Pathfinding.greedyPathfinding(rc, dir))) {
                        rc.move(Pathfinding.greedyPathfinding(rc, dir));
                        if (rc.getLocation().distanceSquaredTo(leastRubble) == 0) {
                            transformBack = true;
                            isMoving = false;
                        }
                    }
                } else if (rc.canMove(Pathfinding.greedyPathfinding(rc, dir))) {
                    rc.move(Pathfinding.greedyPathfinding(rc, dir));
                }
            }

        } else if (!Communication.anArchonIsMoving(rc) && rc.isTransformReady()) {
            int[] enemyLocations = Communication.getEnemyLocations(rc);
            MapLocation closestEnemyLocation = null;
            int distanceToClosestEnemy = Integer.MAX_VALUE;
            for (int i = 0; i < enemyLocations.length; i++) {
                out += enemyLocations[i] + ";";

                if (enemyLocations[i] != 0 && rc.getLocation().distanceSquaredTo(
                        Communication.convertIntToMapLocation(enemyLocations[i])) < distanceToClosestEnemy) {
                    closestEnemyLocation = Communication.convertIntToMapLocation(enemyLocations[i]);
                    distanceToClosestEnemy = rc.getLocation().distanceSquaredTo(
                            Communication.convertIntToMapLocation(enemyLocations[i]));
                }
            }

            if (closestEnemyLocation != null && distanceToClosestEnemy > RobotType.ARCHON.visionRadiusSquared * 4) {
                //System.out.print("SAFE");
                MapLocation current = rc.getLocation();
                int newX = (current.x + closestEnemyLocation.x) / 2;
                int newY = (current.y + closestEnemyLocation.y) / 2;
                locationToMove = new MapLocation(newX, newY);
                isMoving = true;
                rc.transform();
                Communication.signalMovingArchon(rc);
            }
        }
        //System.out.println(out);
    }

    public static void gameStartSequence(RobotController rc) throws GameActionException {
        if (!rc.isActionReady() && rc.getTeamLeadAmount(rc.getTeam()) >= RobotType.MINER.buildCostLead) {
            Communication.increaseArchonSpawnIndex(rc);
        } else {
            Direction dir = openSpawnLocation(rc, RobotType.MINER);
            if (rc.canBuildRobot(RobotType.MINER, dir)) {
                rc.buildRobot(RobotType.MINER, dir);
                Communication.increaseArchonSpawnIndex(rc);
                startSpawn++;
            }
        }
    }

    public static void soldierStartSequence(RobotController rc) throws GameActionException {
        if (!rc.isActionReady() && rc.getTeamLeadAmount(rc.getTeam()) >= RobotType.SOLDIER.buildCostLead) {
            Communication.increaseArchonSpawnIndex(rc);
        } else {
            Direction dir = openSpawnLocation(rc, RobotType.SOLDIER);
            if (rc.canBuildRobot(RobotType.SOLDIER, dir)) {
                rc.buildRobot(RobotType.SOLDIER, dir);
                Communication.increaseArchonSpawnIndex(rc);
                startSpawn++;
            }
        }
    }

    public static void normalSpawnSequence(RobotController rc) throws GameActionException {
        int leadAmnt = rc.getTeamLeadAmount(rc.getTeam());
        int lastLeadAmnt = Communication.getLastLeadAmnt(rc);
        int income = leadAmnt - lastLeadAmnt;
        Communication.setLastLeadAmnt(rc, leadAmnt);

        RobotType spawn = spawnOrder.get(spawnOrderCounter);
        Direction spawnDir = openSpawnLocation(rc, spawn);
        if (!rc.isActionReady() && rc.getTeamLeadAmount(rc.getTeam()) >= spawn.buildCostLead) {
            Communication.increaseArchonSpawnIndex(rc);
        } else {
            switch (spawn) {
                case SOLDIER:
                    if (rc.getTeamGoldAmount(rc.getTeam()) >= RobotType.SAGE.buildCostGold) {
                        if (rc.canBuildRobot(RobotType.SAGE, spawnDir)) {
                            rc.buildRobot(RobotType.SAGE, spawnDir);
                            Communication.increaseArchonSpawnIndex(rc);
                            increaseSpawnOrderCounter();
                        }
                    } else if (rc.canBuildRobot(RobotType.SOLDIER, spawnDir)) {
                        rc.buildRobot(RobotType.SOLDIER, spawnDir);
                        Communication.increaseArchonSpawnIndex(rc);
                        increaseSpawnOrderCounter();
                    }
                    break;
                case MINER:
                    if (rc.canBuildRobot(RobotType.MINER, spawnDir)) {
                        rc.buildRobot(RobotType.MINER, spawnDir);
                        Communication.increaseArchonSpawnIndex(rc);
                        increaseSpawnOrderCounter();
                    }
                    break;
            }
        }
    }

    //returns open spawn direction
    public static Direction openSpawnLocation(RobotController rc, RobotType type) throws GameActionException {
        int rand = (int) (Math.random() * 3);
        if (rc.canBuildRobot(type, spawnDirections.get(rand))) {
            return spawnDirections.get(rand);
        } else {
            for (Direction dir : spawnDirections) {
                if (rc.canBuildRobot(type, dir)) {
                    return dir;
                }
            }
        }
        return Direction.CENTER;
    }

    public static void newSpawnLogic(RobotController rc) throws GameActionException {
        int leadAmnt = rc.getTeamLeadAmount(rc.getTeam());
        int lastLeadAmnt = Communication.getLastLeadAmnt(rc);
        int income = leadAmnt - lastLeadAmnt;
        Communication.setLastLeadAmnt(rc, leadAmnt);

        int[] metalLocation = Communication.getMetalLocations(rc);
        int locations = 0;
        for (int i = 0; i < metalLocation.length; i++) {
            if (metalLocation[i] != 0) {
                locations++;
            }
        }

        RobotType spawn = spawnOrder.get(spawnOrderCounter);
        Direction spawnDir = openSpawnLocation(rc, spawn);
        switch (spawn) {
            case SOLDIER:
                if (rc.getTeamGoldAmount(rc.getTeam()) >= RobotType.SAGE.buildCostGold) {
                    if (!rc.isActionReady() && rc.getTeamGoldAmount(rc.getTeam()) >= RobotType.SAGE.buildCostGold) {
                        Communication.increaseArchonSpawnIndex(rc);
                    } else if (rc.canBuildRobot(RobotType.SAGE, spawnDir)) {
                        rc.buildRobot(RobotType.SAGE, spawnDir);
                        Communication.increaseArchonSpawnIndex(rc);
                        increaseSpawnOrderCounter();
                    }
                } else {
                    if (!rc.isActionReady() && rc.getTeamLeadAmount(rc.getTeam()) >= RobotType.SOLDIER.buildCostLead) {
                        Communication.increaseArchonSpawnIndex(rc);
                    } else if (rc.canBuildRobot(RobotType.SOLDIER, spawnDir)) {
                        rc.buildRobot(RobotType.SOLDIER, spawnDir);
                        Communication.increaseArchonSpawnIndex(rc);
                        increaseSpawnOrderCounter();
                    }
                }
                break;
            case MINER:
                int rand = (int) (Math.random() * 4);
                if (rand != 0 && locations < 3 && locations != 6 && UnitCounter.getMiners(rc) >= 9) {
                    if (!rc.isActionReady() && rc.getTeamLeadAmount(rc.getTeam()) >= RobotType.SOLDIER.buildCostLead) {
                        Communication.increaseArchonSpawnIndex(rc);
                    } else if (rc.canBuildRobot(RobotType.SOLDIER, spawnDir)) {
                        rc.buildRobot(RobotType.SOLDIER, spawnDir);
                        Communication.increaseArchonSpawnIndex(rc);
                        increaseSpawnOrderCounter();
                    }
                } else {
                    if (!rc.isActionReady() && rc.getTeamLeadAmount(rc.getTeam()) >= RobotType.MINER.buildCostLead) {
                        Communication.increaseArchonSpawnIndex(rc);
                    } else if (rc.canBuildRobot(RobotType.MINER, spawnDir)) {
                        rc.buildRobot(RobotType.MINER, spawnDir);
                        Communication.increaseArchonSpawnIndex(rc);
                        increaseSpawnOrderCounter();
                    }
                }
                break;
        }
    }

    public static void increaseSpawnOrderCounter() {
        if (spawnOrderCounter >= spawnOrder.size() - 1) {
            spawnOrderCounter = 0;
        } else {
            spawnOrderCounter++;
        }
    }

    public static void checkEnemyNear(RobotController rc) throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        for (int i = 0; i < robots.length; i++) {
            RobotInfo robot = robots[i];
            if (robot.getType() == RobotType.ARCHON || robot.getType() == RobotType.SOLDIER
                    || robot.getType() == RobotType.SAGE) {
                enemyNear = true;
                Communication.sendDistressSignal(rc, Communication.convertMapLocationToInt(rc.getLocation()));
                if (robot.getType() == RobotType.ARCHON) {
                    enemyArchonNear = true;
                    if (!rc.isActionReady() && rc.getTeamLeadAmount(rc.getTeam()) >= RobotType.SOLDIER.buildCostLead) {
                        Communication.increaseArchonSpawnIndex(rc);
                    } else {
                        Direction spawnDirection = openSpawnLocation(rc, RobotType.SOLDIER);
                        if (rc.canBuildRobot(RobotType.SOLDIER, spawnDirection)) {
                            rc.buildRobot(RobotType.SOLDIER, spawnDirection);
                            Communication.increaseArchonSpawnIndex(rc);
                        }
                    }
                }
            }
        }
        if (!enemyNear) {
            Communication.endDistressSignal(rc, Communication.convertMapLocationToInt(rc.getLocation()));
        }
    }

    public static void heal(RobotController rc) throws GameActionException {
        RobotInfo[] allys = rc.senseNearbyRobots(rc.getLocation(), rc.getType().actionCooldown, rc.getTeam());
        if (enemyNear) {
            RobotInfo ally = null;
            int lowestValue = Integer.MAX_VALUE;
            int lowestHealth = Integer.MAX_VALUE;
            for (int i = 0; i < allys.length; i++) {
                if (allys[i].getHealth() < allys[i].getType().health) {
                    int allyValue = Data.determineEnemyValue(allys[i]);
                    if (allyValue < lowestValue) {
                        ally = allys[i];
                        lowestValue = allyValue;
                        lowestHealth = allys[i].getHealth();
                    } else if (allyValue == lowestValue && allys[i].getHealth() < lowestHealth) {
                        ally = allys[i];
                        lowestValue = allyValue;
                        lowestHealth = allys[i].getHealth();
                    }
                }
            }
            if (ally != null && rc.canRepair(ally.getLocation())) {
                rc.repair(ally.getLocation());
            }
        } else {
            RobotInfo ally = null;
            int lowestValue = Integer.MAX_VALUE;
            int highestHealth = Integer.MAX_VALUE;
            for (int i = 0; i < allys.length; i++) {
                if (allys[i].getHealth() < allys[i].getType().health) {
                    int allyValue = Data.determineEnemyValue(allys[i]);
                    if (allyValue < lowestValue) {
                        ally = allys[i];
                        lowestValue = allyValue;
                        highestHealth = allys[i].getHealth();
                    } else if (allyValue == lowestValue && allys[i].getHealth() > highestHealth) {
                        ally = allys[i];
                        lowestValue = allyValue;
                        highestHealth = allys[i].getHealth();
                    }
                }
            }
            if (ally != null && rc.canRepair(ally.getLocation())) {
                rc.repair(ally.getLocation());
            }
        }
    }

    public static void init(RobotController rc) throws GameActionException {
        Communication.addArchonId(rc, rc.getID());
        Communication.addArchonLocation(rc, Communication.convertMapLocationToInt(rc.getLocation()));
        ogArchonNumber = rc.getArchonCount();

        spawnOrder.add(RobotType.SOLDIER);
        spawnOrder.add(RobotType.SOLDIER);
        spawnOrder.add(RobotType.MINER);

        MapLocation center = new MapLocation(rc.getMapWidth() / 2, rc.getMapHeight() / 2);
        Direction dirToCenter = rc.getLocation().directionTo(center);
        spawnDirections.add(dirToCenter);
        spawnDirections.add(dirToCenter.rotateLeft());
        spawnDirections.add(dirToCenter.rotateRight());
        spawnDirections.add(dirToCenter.rotateLeft().rotateLeft());
        spawnDirections.add(dirToCenter.rotateRight().rotateRight());
        spawnDirections.add(dirToCenter.rotateLeft().rotateLeft().rotateLeft());
        spawnDirections.add(dirToCenter.rotateRight().rotateRight().rotateRight());
        spawnDirections.add(dirToCenter.opposite());

        /*RobotInfo[] robots = rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam().opponent());
        for (int i = 0; i < robots.length; i++) {
            RobotInfo robot = robots[i];
            if (robot.getType() == RobotType.ARCHON) {
                Communication.addEnemyArconLocation(Communication.convertMapLocationToInt(robot.getLocation()), rc);
            }
        }*/

        int amountOfLeadAround = 0;
        MapLocation[] surroundings = rc.getAllLocationsWithinRadiusSquared(rc.getLocation(),
                rc.getType().visionRadiusSquared);
        for (int i = 0; i < surroundings.length; i++) {
            if (rc.canSenseLocation(surroundings[i]) && rc.senseRubble(surroundings[i]) > 0) {
                amountOfLeadAround++;
            }
        }
        if (amountOfLeadAround >= 40) {
            isMTS = true;
        }
        Data.rng = new Random(rc.getID());
    }
}
