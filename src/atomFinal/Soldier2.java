package atomFinal;

import battlecode.common.*;
import java.util.*;

public class Soldier2 {
    static boolean healing = false;
    static int escapeCounter = 0;

    static void runSoldier(RobotController rc) throws GameActionException {
        Team opponent = rc.getTeam().opponent();
        MapLocation current = rc.getLocation();

        UnitCounter.addSoldier(rc);

        int closestEnemyArcon = getClosestEnemyArcon(rc);
        MapLocation closestEnemyArconLocation = null;
        if (closestEnemyArcon != 0) {
            closestEnemyArconLocation = Communication.convertIntToMapLocation(closestEnemyArcon);
            if (rc.canSenseLocation(closestEnemyArconLocation)) {
                if (rc.senseRobotAtLocation(closestEnemyArconLocation) == null) {
                    Communication.removeEnemyArconLocation(closestEnemyArcon, rc);
                }
            }
        }

        RobotInfo[] nearbyRobots = rc.senseNearbyRobots();
        RobotInfo target = null;
        int targetHealth = Integer.MAX_VALUE;
        int targetValue = Integer.MAX_VALUE; //sage = 1, soldier = 2, builder = 3, archon = 4, miner = 5
        int enemyAttackersCount = 0;
        int allyAttackersCount = 1;
        boolean nearAllyArchon = false;

        checkNeedsHealing(rc);

        for (int i = 0; i < nearbyRobots.length; i++) {
            RobotInfo robot = nearbyRobots[i];
            if (robot.getTeam() == opponent) {
                if (robot.getType() == RobotType.ARCHON) {
                    Communication.addEnemyArconLocation(Communication.convertMapLocationToInt(robot.getLocation()), rc);
                } else if ((robot.getType() == RobotType.SOLDIER || robot.getType() == RobotType.SAGE)) {
                    enemyAttackersCount++;
                }
                int enemyValue = Data.determineEnemyValue(robot);
                if (enemyValue < targetValue) {
                    target = robot;
                    targetHealth = robot.getHealth();
                    targetValue = enemyValue;
                } else if (enemyValue == targetValue && robot.health < targetHealth) {
                    target = robot;
                    targetHealth = robot.getHealth();
                    targetValue = enemyValue;
                }
            } else {
                if (robot.getType() == RobotType.SOLDIER || robot.getType() == RobotType.SAGE) {
                    allyAttackersCount++;
                } else if (robot.getType() == RobotType.ARCHON) {
                    nearAllyArchon = true;
                }
            }
        }

        /*int singleOutI = getClosestEnemy(rc);
        if (singleOutI != 0) {
            MapLocation singleOut = Communication.convertIntToMapLocation(singleOutI);
            if (rc.canSenseLocation(singleOut)) {
                target = rc.senseRobotAtLocation(singleOut);
            }
        }*/

        if (target != null) {
            if (rc.getLocation().distanceSquaredTo(target.getLocation()) > RobotType.SOLDIER.actionRadiusSquared) {
                if (allyAttackersCount + 2 < enemyAttackersCount && !nearAllyArchon) {
                    RobotInfo[] allys = rc.senseNearbyRobots(-1, rc.getTeam());
                    if (allyAttackersCount != 0) {
                        RobotInfo nearestAlly = getClosestAlly(rc, allys);
                        if (nearestAlly != null) {
                            Direction escapeDir = Pathfinding.greedyPathfinding(rc, nearestAlly.getLocation());
                            if (rc.canMove(escapeDir)) {
                                rc.move(escapeDir);
                            }
                        }
                    } else {
                        Direction escapeDir = Pathfinding.escapeEnemies(rc);
                        if (rc.canMove(escapeDir)) {
                            rc.move(escapeDir);
                        }
                    }
                } else {
                    MapLocation toAttack = target.getLocation();
                    MapLocation[] surroundings = rc.getAllLocationsWithinRadiusSquared(toAttack,
                            RobotType.SOLDIER.visionRadiusSquared);
                    MapLocation leastRubbleLocation = null;
                    int rubbleAtleastRubbleLocation = Integer.MAX_VALUE;
                    int leastRubbleDistance = Integer.MAX_VALUE;

                    for (int i = 0; i < surroundings.length; i++) {
                        if (rc.canSenseLocation(surroundings[i]) && !rc.canSenseRobotAtLocation(surroundings[i])
                                && surroundings[i].distanceSquaredTo(target.getLocation()) >= 9
                                && surroundings[i]
                                        .distanceSquaredTo(
                                                target.getLocation()) <= RobotType.SOLDIER.actionRadiusSquared) {
                            if (rc.senseRubble(surroundings[i]) < rubbleAtleastRubbleLocation) {
                                leastRubbleLocation = surroundings[i];
                                rubbleAtleastRubbleLocation = rc.senseRubble(surroundings[i]);
                                leastRubbleDistance = current.distanceSquaredTo(surroundings[i]);
                            } else if (rc.senseRubble(surroundings[i]) == rubbleAtleastRubbleLocation
                                    && current.distanceSquaredTo(surroundings[i]) < leastRubbleDistance) {
                                leastRubbleLocation = surroundings[i];
                                rubbleAtleastRubbleLocation = rc.senseRubble(surroundings[i]);
                                leastRubbleDistance = current.distanceSquaredTo(surroundings[i]);
                            }
                        }
                    }

                    if (leastRubbleLocation != null && rc.canSenseLocation(target.getLocation())) {
                        if (rubbleAtleastRubbleLocation <= rc.senseRubble(target.getLocation())) {
                            Direction to = Pathfinding.greedyPathfinding(rc, leastRubbleLocation);
                            if (rc.canMove(to)) {
                                rc.move(to);
                            }
                        } else {
                            RobotInfo[] allys = rc.senseNearbyRobots(-1, rc.getTeam());
                            if (allyAttackersCount != 0) {
                                RobotInfo nearestAlly = getClosestAlly(rc, allys);
                                if (nearestAlly != null) {
                                    Direction escapeDir = Pathfinding.greedyPathfinding(rc, nearestAlly.getLocation());
                                    if (rc.canMove(escapeDir)) {
                                        rc.move(escapeDir);
                                    }
                                }
                            } else {
                                Direction escapeDir = Pathfinding.escapeEnemies(rc);
                                if (rc.canMove(escapeDir)) {
                                    rc.move(escapeDir);
                                }
                            }
                        }
                    }
                }
                if (rc.canAttack(target.getLocation())) {
                    rc.attack(target.getLocation());
                }
            } else {
                if (target.getType() == RobotType.SOLDIER || target.getType() == RobotType.SAGE) {
                    Communication.addEnemyLocation(rc, Communication.convertMapLocationToInt(target.getLocation()));
                }
                if (rc.canAttack(target.getLocation())) {
                    rc.attack(target.getLocation());
                }
                if (target.getType() == RobotType.ARCHON) {
                    swarmArcon(rc, target.getLocation());
                }
                if ((allyAttackersCount + 2 < enemyAttackersCount && !nearAllyArchon)
                        || rc.senseRubble(current) > rc.senseRubble(target.getLocation())) {
                    RobotInfo[] allys = rc.senseNearbyRobots(-1, rc.getTeam());
                    if (allyAttackersCount != 0) {
                        RobotInfo nearestAlly = getClosestAlly(rc, allys);
                        if (nearestAlly != null) {
                            Direction escapeDir = Pathfinding.greedyPathfinding(rc, nearestAlly.getLocation());
                            if (rc.canMove(escapeDir)) {
                                rc.move(escapeDir);
                            }
                        }
                    } else {
                        Direction escapeDir = Pathfinding.escapeEnemies(rc);
                        if (rc.canMove(escapeDir)) {
                            rc.move(escapeDir);
                        }
                    }
                } else if ((target.getType() == RobotType.SOLDIER || target.getType() == RobotType.SAGE)) {
                    Direction away = rc.getLocation().directionTo(target.getLocation()).opposite();
                    away = Pathfinding.greedyPathfinding(rc, away);
                    if (rc.senseRubble(rc.getLocation().add(away)) <= rc.senseRubble(rc.getLocation())) {
                        if (rc.canMove(away)) {
                            rc.move(away);
                        }
                    }
                }
            }
        } else {
            int closestEnemy = getClosestEnemy(rc);
            rc.setIndicatorString(closestEnemy + " ");
            int[] distressSignals = Communication.checkDistressSignal(rc);
            MapLocation distressLocation = null;
            int leastDistressDistance = Integer.MAX_VALUE;
            for (int i = 0; i < distressSignals.length; i++) {
                if (distressSignals[i] != 0) {
                    MapLocation loc = Communication.convertIntToMapLocation(distressSignals[i]);
                    if (rc.getLocation().distanceSquaredTo(loc) < leastDistressDistance) {
                        distressLocation = loc;
                        leastDistressDistance = rc.getLocation().distanceSquaredTo(loc);
                    }
                }
            }
            if (distressLocation != null) {
                Direction dir = Pathfinding.greedyPathfinding(rc, distressLocation);
                if (rc.canMove(dir)) {
                    rc.move(dir);
                    rc.setIndicatorString("MOVINGTODISTRESS:" + dir);
                }
            } else if (closestEnemy != 0) {
                MapLocation closestEnemyLocation = Communication.convertIntToMapLocation(closestEnemy);
                Direction dir = Pathfinding.greedyPathfinding(rc, closestEnemyLocation);
                if (rc.canMove(dir)) {
                    rc.move(dir);
                    rc.setIndicatorString(closestEnemy + "MOVINGTOENEMY");
                }
            } 
            else {
                MapLocation farthestMinerFromBase = null;
                int minerDistanceFromBase = 0;
                RobotInfo[] allys = rc.senseNearbyRobots(-1, rc.getTeam());
                for (int i = 0; i < allys.length; i++) {
                    RobotInfo ally = allys[i];
                    if (ally.getType() == RobotType.MINER) {
                        if (ally.getLocation()
                                .distanceSquaredTo(Data.spawnBaseLocation) > minerDistanceFromBase) {
                            farthestMinerFromBase = ally.getLocation();
                            minerDistanceFromBase = ally.getLocation()
                                    .distanceSquaredTo(Data.spawnBaseLocation);
                        }

                    }
                }
                if (farthestMinerFromBase != null) {
                    Direction minerAwayFromBase = Data.spawnBaseLocation.directionTo(farthestMinerFromBase);
                    MapLocation inFrontOfMiner = farthestMinerFromBase.add(minerAwayFromBase).add(minerAwayFromBase)
                            .add(minerAwayFromBase);
                    Direction dir = Pathfinding.greedyPathfinding(rc,
                            rc.getLocation().directionTo(inFrontOfMiner));
                    if (rc.canMove(dir)) {
                        rc.move(dir);
                        rc.setIndicatorString("MOVINGTOMINER:" + dir);
                        //rc.setIndicatorString("MOVINGRAND");
                    }
                } else if (closestEnemyArcon != 0) {
                    Direction dir = Pathfinding.greedyPathfinding(rc, closestEnemyArconLocation);
                    if (rc.canMove(dir)) {
                        rc.move(dir);
                        rc.setIndicatorString("MOVINGTOENEMYARCHON:" + dir);
                    }
                } else {
                    Direction dir = Pathfinding.wander(rc);
                    if (rc.canMove(dir)) {
                        rc.move(dir);
                        rc.setIndicatorString("MOVINGRAND:" + dir);
                    }
                }
            }
        }
    }

    static int getClosestEnemyArcon(RobotController rc) throws GameActionException {
        int[] enemyArconLocations = Communication.getEnemyArconLocations(rc);
        int closestArconLocation = 0;
        int distanceSquaredToClosest = Integer.MAX_VALUE;
        for (int i = 0; i < enemyArconLocations.length; i++) {
            if (enemyArconLocations[i] != 0) {
                MapLocation location = Communication.convertIntToMapLocation(enemyArconLocations[i]);
                if (rc.getLocation().distanceSquaredTo(location) < distanceSquaredToClosest) {
                    closestArconLocation = enemyArconLocations[i];
                    distanceSquaredToClosest = rc.getLocation().distanceSquaredTo(location);
                }
            }
        }
        return closestArconLocation;
    }

    static int getClosestEnemy(RobotController rc) throws GameActionException {
        int[] enemyLocations = Communication.getEnemyLocations(rc);
        int closestEnemyLocation = 0;
        int distanceSquaredToClosest = Integer.MAX_VALUE;
        for (int i = 0; i < enemyLocations.length; i++) {
            if (enemyLocations[i] != 0) {
                MapLocation location = Communication.convertIntToMapLocation(enemyLocations[i]);
                if (rc.getLocation().distanceSquaredTo(location) < distanceSquaredToClosest) {
                    closestEnemyLocation = enemyLocations[i];
                    distanceSquaredToClosest = rc.getLocation().distanceSquaredTo(location);
                }
            }
        }
        return closestEnemyLocation;
    }

    static RobotInfo getClosestAlly(RobotController rc, RobotInfo[] allies) throws GameActionException {
        RobotInfo closestAlly = null;
        int distanceSquaredToClosest = Integer.MAX_VALUE;
        for (int i = 0; i < allies.length; i++) {
            RobotInfo ally = allies[i];
            if (ally.getType() == RobotType.SOLDIER
                    && rc.getLocation().distanceSquaredTo(ally.getLocation()) < distanceSquaredToClosest) {
                closestAlly = ally;
                distanceSquaredToClosest = rc.getLocation().distanceSquaredTo(ally.getLocation());
            }

        }
        return closestAlly;
    }

    static void swarmArcon(RobotController rc, MapLocation location) throws GameActionException {
        if (!(rc.getLocation().distanceSquaredTo(location) <= 2)) {
            Direction dir = Pathfinding.greedyPathfinding(rc, location);
            if (rc.canMove(dir)) {
                rc.move(dir);
            }
        }
    }

    static void checkArconExist(RobotController rc) throws GameActionException {
        int closestEnemyArcon = getClosestEnemyArcon(rc);
        MapLocation closestEnemyArconLocation = null;
        if (closestEnemyArcon != 0) {
            closestEnemyArconLocation = Communication.convertIntToMapLocation(closestEnemyArcon);
            if (rc.canSenseLocation(closestEnemyArconLocation)) {
                if (rc.senseRobotAtLocation(closestEnemyArconLocation) == null) {
                    Communication.removeEnemyArconLocation(closestEnemyArcon, rc);
                }
            }
        }
    }

    static void checkNeedsHealing(RobotController rc) throws GameActionException {
        if (rc.getHealth() < 20 || healing) {
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
                healing = true;
                Direction dir = rc.getLocation().directionTo(closestBase);
                if (rc.canMove(dir)) {
                    rc.move(dir);
                }
            } else if (closestBase == null) {
                healing = false;
            }
        }
        if (healing && rc.getHealth() >= 45) {
            healing = false;
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