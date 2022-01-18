package atom;

import battlecode.common.*;

import java.time.Duration;
import java.util.*;

public class Soldier {
    static boolean healing = false;

    static void runSoldier(RobotController rc) throws GameActionException {
        int actionRadius = rc.getType().actionRadiusSquared;
        Team opponent = rc.getTeam().opponent();

        RobotInfo[] enemiesInActionRadius = rc.senseNearbyRobots(actionRadius, opponent);
        RobotInfo target = null;
        int targetHealth = Integer.MAX_VALUE;
        int targetValue = Integer.MAX_VALUE; //sage = 1, soldier = 2, builder = 3, archon = 4, miner = 5

        UnitCounter.addSoldier(rc);
        //checkArconExist(rc);
        checkNeedsHealing(rc);

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

        // enemies within action radius
        if (enemiesInActionRadius.length > 0) {
            for (int i = 0; i < enemiesInActionRadius.length; i++) {
                RobotInfo enemy = enemiesInActionRadius[i];
                if (enemy.getType() == RobotType.ARCHON) {
                    Communication.addEnemyArconLocation(Communication.convertMapLocationToInt(enemy.getLocation()), rc);
                    swarmArcon(rc, enemy.getLocation());
                }

                int enemyValue = Data.determineEnemyValue(enemiesInActionRadius[i]);
                if (enemyValue < targetValue) {
                    target = enemiesInActionRadius[i];
                    targetHealth = enemiesInActionRadius[i].health;
                    targetValue = enemyValue;
                } else if (enemyValue == targetValue && enemiesInActionRadius[i].health < targetHealth) {
                    target = enemiesInActionRadius[i];
                    targetHealth = enemiesInActionRadius[i].health;
                    targetValue = enemyValue;
                }
            }

            MapLocation toAttack = target.getLocation();

            if (target.getType() == RobotType.SOLDIER || target.getType() == RobotType.SAGE) {
                Communication.addEnemyLocation(rc, Communication.convertMapLocationToInt(toAttack));
            }

            if (rc.canAttack(toAttack)) {
                rc.attack(toAttack);
            }

            if ((target.getType() == RobotType.SOLDIER || target.getType() == RobotType.SAGE)
                    && rc.getLocation().distanceSquaredTo(toAttack) <= actionRadius) {
                Direction away = rc.getLocation().directionTo(toAttack).opposite();
                away = Pathfinding.greedyPathfinding(rc, away);
                if (rc.senseRubble(rc.getLocation().add(away)) <= rc.senseRubble(rc.getLocation())) {
                    if (rc.canMove(away)) {
                        rc.move(away);
                    }
                }
            }
        } else {
            int visionRadius = rc.getType().visionRadiusSquared;
            RobotInfo[] enemiesInVisionRange = rc.senseNearbyRobots(visionRadius, opponent);
            RobotInfo[] alliesInVisionRange = rc.senseNearbyRobots(visionRadius, rc.getTeam());
            int targetDistance = Integer.MAX_VALUE;
            int enemyAttackersCount = 0;
            int allyAttackersCount = 0;
            boolean nearAllyArchon = false;

            if (enemiesInVisionRange.length > 0) {
                for (int i = 0; i < enemiesInVisionRange.length; i++) {
                    RobotInfo enemy = enemiesInVisionRange[i];
                    if (enemy.getType() == RobotType.ARCHON) {
                        Communication.addEnemyArconLocation(Communication.convertMapLocationToInt(enemy.getLocation()),
                                rc);
                    } else if (enemy.getType() == RobotType.SOLDIER || enemy.getType() == RobotType.SAGE) {
                        enemyAttackersCount++;
                    }

                    int enemyValue = Data.determineEnemyValue(enemiesInVisionRange[i]);
                    int distanceToEnemy = rc.getLocation().distanceSquaredTo(enemiesInVisionRange[i].getLocation());
                    if (enemyValue <= targetValue && distanceToEnemy < targetDistance) {
                        target = enemiesInVisionRange[i];
                        targetDistance = distanceToEnemy;
                        targetValue = enemyValue;
                    }
                }

                for (int i = 0; i < alliesInVisionRange.length; i++) {
                    RobotInfo ally = alliesInVisionRange[i];
                    if (ally.getType() == RobotType.SOLDIER || ally.getType() == RobotType.SAGE) {
                        allyAttackersCount++;
                    } else if (ally.getType() == RobotType.ARCHON) {
                        nearAllyArchon = true;
                    }
                }

                //run away if outnumbered
                if (allyAttackersCount < enemyAttackersCount && !nearAllyArchon) {
                    if (allyAttackersCount != 0) {
                        RobotInfo nearestAlly = getClosestAlly(rc, alliesInVisionRange);
                        if (nearestAlly != null) {
                            Direction escapeDir = Pathfinding.greedyPathfinding(rc, nearestAlly.getLocation());
                            if (rc.canMove(escapeDir)) {
                                rc.move(escapeDir);
                            }
                        }
                    } else {
                        /*MapLocation attackerLocation = target.getLocation();
                        Direction escapeDir = Pathfinding.greedyPathfinding(rc,
                                rc.getLocation().directionTo(attackerLocation).opposite());*/
                        Direction escapeDir = Pathfinding.escapeEnemies(rc);
                        if (rc.canMove(escapeDir)) {
                            rc.move(escapeDir);
                        }
                    }
                }

                MapLocation toAttack = target.getLocation();

                MapLocation[] surroundings = rc.getAllLocationsWithinRadiusSquared(toAttack, actionRadius + 2);
                MapLocation leastRubbleLocation = null;
                int rubbleAtleastRubbleLocation = Integer.MAX_VALUE;

                for (int i = 0; i < surroundings.length; i++) {
                    if (rc.canSenseLocation(surroundings[i])
                            && surroundings[i]
                                    .distanceSquaredTo(target.getLocation()) <= target.getType().actionRadiusSquared
                            && !rc.canSenseRobotAtLocation(surroundings[i])
                            && rc.senseRubble(surroundings[i]) < rubbleAtleastRubbleLocation) {
                        leastRubbleLocation = surroundings[i];
                        rubbleAtleastRubbleLocation = rc.senseRubble(surroundings[i]);
                    }
                }

                if (leastRubbleLocation != null) {
                    if (rc.canSenseLocation(toAttack) && rc.senseRubble(toAttack) < rubbleAtleastRubbleLocation) {
                        if (allyAttackersCount != 0) {
                            RobotInfo nearestAlly = getClosestAlly(rc, alliesInVisionRange);
                            if (nearestAlly != null) {
                                Direction escapeDir = Pathfinding.greedyPathfinding(rc, nearestAlly.getLocation());
                                if (rc.canMove(escapeDir)) {
                                    rc.move(escapeDir);
                                }
                            }
                        } else {
                            /*MapLocation attackerLocation = target.getLocation();
                            Direction escapeDir = Pathfinding.greedyPathfinding(rc,
                                    rc.getLocation().directionTo(attackerLocation).opposite());*/
                            Direction escapeDir = Pathfinding.escapeEnemies(rc);
                            if (rc.canMove(escapeDir)) {
                                rc.move(escapeDir);
                            }
                        }
                    } else {
                        Direction moveToOptimalLocation = Pathfinding.greedyPathfinding(rc, leastRubbleLocation);
                        if (rc.canMove(moveToOptimalLocation)) {
                            rc.move(moveToOptimalLocation);
                        }
                    }
                }
                if (rc.canAttack(toAttack)) {
                    rc.attack(toAttack);
                }
            } else {
                int closestEnemy = getClosestEnemy(rc);
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
                    }
                } else if (closestEnemy != 0) {
                    MapLocation closestEnemyLocation = Communication.convertIntToMapLocation(closestEnemy);
                    Direction dir = Pathfinding.greedyPathfinding(rc, closestEnemyLocation);
                    if (rc.canMove(dir)) {
                        rc.move(dir);
                    }
                } else if (!healing) {
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
                            //rc.setIndicatorString("MOVINGRAND");
                        }
                    } else if (closestEnemyArcon != 0) {
                        //dir = Pathfinding.basicBug(rc, closestEnemyArconLocation);
                        Direction dir = Pathfinding.greedyPathfinding(rc, closestEnemyArconLocation);
                        if (rc.canMove(dir)) {
                            rc.move(dir);
                            //rc.setIndicatorString("MOVINGTOARCON");
                        }
                    } else {
                        Direction dir = Pathfinding.wander(rc);
                        if (rc.canMove(dir)) {
                            rc.move(dir);
                            //rc.setIndicatorString("MOVINGRAND");
                        }
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
            if (rc.getLocation().distanceSquaredTo(ally.getLocation()) < distanceSquaredToClosest) {
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
        if (rc.getHealth() < 10 || healing) {
            healing = true;
            if (Data.spawnBaseLocation.distanceSquaredTo(rc.getLocation()) >= RobotType.ARCHON.actionRadiusSquared
                    - 4) {
                Direction dir = rc.getLocation().directionTo(Data.spawnBaseLocation);
                dir = Pathfinding.greedyPathfinding(rc, dir);
                if (rc.canMove(dir)) {
                    rc.move(dir);
                }
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
