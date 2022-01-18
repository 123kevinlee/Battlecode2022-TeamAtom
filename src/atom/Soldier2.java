package atom;

import battlecode.common.*;
import java.util.*;

public class Soldier2 {
    static boolean healing = false;
    static int escapeCounter = 0;

    static void runSoldier(RobotController rc) throws GameActionException {
        int actionRadius = rc.getType().actionRadiusSquared;
        Team opponent = rc.getTeam().opponent();
        int visionRadius = rc.getType().visionRadiusSquared;
        MapLocation current = rc.getLocation();

        UnitCounter.addSoldier(rc);
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

        RobotInfo[] nearbyRobots = rc.senseNearbyRobots();
        RobotInfo target = null;
        int targetHealth = Integer.MAX_VALUE;
        int targetValue = Integer.MAX_VALUE; //sage = 1, soldier = 2, builder = 3, archon = 4, miner = 5
        //int targetDistance = Integer.MAX_VALUE;
        int enemyAttackersCount = 0;
        int allyAttackersCount = 0;
        boolean nearAllyArchon = false;

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
                    //targetDistance = current.distanceSquaredTo(robot.getLocation());
                    targetValue = enemyValue;
                } /*else if (enemyValue == targetValue
                        && targetDistance < current.distanceSquaredTo(robot.getLocation())) {
                    target = robot;
                    targetHealth = robot.getHealth();
                    targetDistance = current.distanceSquaredTo(robot.getLocation());
                    targetValue = enemyValue;
                  } else if (enemyValue == targetValue
                        && targetDistance == current.distanceSquaredTo(robot.getLocation())
                        && robot.health < targetHealth) {
                    target = robot;
                    targetHealth = robot.getHealth();
                    targetDistance = current.distanceSquaredTo(robot.getLocation());
                    targetValue = enemyValue;
                  }*/else if (enemyValue == targetValue
                        && robot.health < targetHealth) {
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

        if (target != null) {
            /*       
                not in attacking range:
                        more enemies < allies - backup
                        get all blocks where u can attack enemy on the boundary of attack radius:
                            check for most optimal:
                                if most optimal is more optimal than enenmy - move there
                                try attacking
                                else find most optimal outside of attack range and go there
                in attacking range:
                        attack
                        more enemies < allies - backup
                        can't attack on next turn against enemy fighters - backup
                        if can attack - check for more optimal location to go to
            */
            int allyAttackersNear = 1;
            //int enemyAttackersNear = 0;
            RobotInfo[] allys = rc.senseNearbyRobots(-1, rc.getTeam());
            for (int i = 0; i < allys.length; i++) {
                if (allys[i].getType() == RobotType.SOLDIER && allys[i].getLocation()
                        .distanceSquaredTo(target.getLocation()) <= RobotType.SOLDIER.actionRadiusSquared + 7) {
                    allyAttackersNear++;
                }
            }
            /*RobotInfo[] enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
            for (int i = 0; i < enemies.length; i++) {
                if (enemies[i].getType() == RobotType.SOLDIER && enemies[i].getLocation()
                        .distanceSquaredTo(current) <= RobotType.SOLDIER.actionRadiusSquared) {
                    enemyAttackersNear++;
                }
            }*/

            System.out.println(target.getLocation());
            if (!rc.canAttack(target.getLocation()) && rc.isActionReady()) {
                //System.out.println("VISION" + rc.isMovementReady());
                //System.out.println("ALLY" + allyAttackersNear + "ENEMY" + enemyAttackersCount);
                if (allyAttackersNear < enemyAttackersCount && !nearAllyArchon) {
                    Direction escapeDir = Pathfinding.escapeEnemies(rc);
                    if (rc.canMove(escapeDir)) {
                        rc.move(escapeDir);
                        //System.out.println("ORUNNINGAWAY:" + escapeDir);
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

                    if (rubbleAtleastRubbleLocation <= rc.senseRubble(target.getLocation())
                            && !(allyAttackersNear > enemyAttackersCount + 1)) {
                        //System.out.println("SHOULDMOVETOINSIDE");
                        Direction to = Pathfinding.greedyPathfinding(rc, leastRubbleLocation);
                        if (rc.canMove(to)) {
                            rc.move(to);
                            //System.out.println("OMOVETOOPTIMALINSIDE:" + leastRubbleLocation);
                        }
                    } else {
                        //System.out.println("SHOULDMOVETOOUTSIDE");
                        surroundings = rc.getAllLocationsWithinRadiusSquared(current,
                                RobotType.SOLDIER.visionRadiusSquared);
                        leastRubbleLocation = null;
                        rubbleAtleastRubbleLocation = Integer.MAX_VALUE;
                        leastRubbleDistance = Integer.MAX_VALUE;
                        for (int i = 0; i < surroundings.length; i++) {
                            if (rc.canSenseLocation(surroundings[i]) && !rc.canSenseRobotAtLocation(surroundings[i])
                                    && surroundings[i].distanceSquaredTo(
                                            target.getLocation()) > RobotType.SOLDIER.actionRadiusSquared) {
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
                        Direction to = Pathfinding.greedyPathfinding(rc, leastRubbleLocation);
                        if (rc.canMove(to)) {
                            rc.move(to);
                            //System.out.println("OMOVETOOPTIMALOUTSIDE:" + leastRubbleLocation);
                        }
                    }
                }
                if (rc.canAttack(target.getLocation())) {
                    rc.attack(target.getLocation());
                    //System.out.println("OATTACK");
                }
            } else {
                //System.out.println("ACTION" + rc.isMovementReady());
                if (rc.canAttack(target.getLocation())) {
                    rc.attack(target.getLocation());
                    //System.out.println("IATTACK");
                }
                //System.out.println("ALLY" + allyAttackersNear + "ENEMY" + enemyAttackersCount);
                if (allyAttackersNear < enemyAttackersCount && !nearAllyArchon) {
                    Direction escapeDir = Pathfinding.escapeEnemies(rc);
                    if (rc.canMove(escapeDir)) {
                        rc.move(escapeDir);
                        //System.out.println("IRUNNINGAWAY:" + escapeDir);
                    }
                } else if (allyAttackersNear > enemyAttackersCount + 1) {
                    Direction towards = Pathfinding.greedyPathfinding(rc, target.getLocation());
                    if (rc.canMove(towards)) {
                        rc.move(towards);
                    }
                } else {
                    if ((rc.senseRubble(current) > rc.senseRubble(target.getLocation())
                            && !(allyAttackersNear > enemyAttackersCount + 1))
                            || ((target.getType() == RobotType.SOLDIER || target.getType() == RobotType.SAGE)
                                    && !rc.isActionReady())) {
                        //System.out.println("SHOULDMOVETOOUTSIDE");
                        MapLocation[] surroundings = rc.getAllLocationsWithinRadiusSquared(target.getLocation(),
                                RobotType.SOLDIER.visionRadiusSquared);
                        MapLocation leastRubbleLocation = null;
                        int rubbleAtleastRubbleLocation = Integer.MAX_VALUE;
                        int leastRubbleDistance = Integer.MAX_VALUE;
                        for (int i = 0; i < surroundings.length; i++) {
                            if (rc.canSenseLocation(surroundings[i]) && !rc.canSenseRobotAtLocation(surroundings[i])
                                    && surroundings[i].distanceSquaredTo(
                                            target.getLocation()) > RobotType.SOLDIER.actionRadiusSquared) {
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
                        Direction to = Pathfinding.greedyPathfinding(rc, leastRubbleLocation);
                        if (rc.canMove(to)) {
                            rc.move(to);
                            //System.out.println("IMOVETOOPTIMALOUTSIDE:" + leastRubbleLocation);
                        }
                    } else {
                        //System.out.println("SHOULDMOVETOINSIDE");
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

                        if (rubbleAtleastRubbleLocation <= rc.senseRubble(target.getLocation())) {
                            Direction to = Pathfinding.greedyPathfinding(rc, leastRubbleLocation);
                            if (rc.canMove(to)) {
                                rc.move(to);
                                //System.out.println("IMOVETOOPTIMALINSIDE:" + leastRubbleLocation);
                            }
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
            } else {/*
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
                    } else */
                if (closestEnemyArcon != 0) {
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
            if (Data.spawnBaseLocation.distanceSquaredTo(rc.getLocation()) > RobotType.ARCHON.actionRadiusSquared - 4) {
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

/*
if (current.distanceSquaredTo(target.getLocation()) <= RobotType.SOLDIER.actionRadiusSquared) {
                MapLocation toAttack = target.getLocation();
                if (target.getType() == RobotType.SOLDIER || target.getType() == RobotType.SAGE) {
                    Communication.addEnemyLocation(rc, Communication.convertMapLocationToInt(toAttack));
                }
                if (rc.canAttack(toAttack)) {
                    System.out.println("ATTACK:" + toAttack);
                    rc.attack(toAttack);
                }
                if ((target.getType() == RobotType.SOLDIER || target.getType() == RobotType.SAGE)
                        && !rc.isActionReady()) {
                    Direction away = rc.getLocation().directionTo(toAttack).opposite();
                    away = Pathfinding.greedyPathfinding(rc, away);
                    if (rc.senseRubble(rc.getLocation().add(away)) <= rc.senseRubble(rc.getLocation())) {
                        if (rc.canMove(away)) {
                            rc.move(away);
                            System.out.println("BACKINGUP:" + away);
                        }
                    }
                } else {
                    MapLocation[] surroundings = new MapLocation[] { toAttack, toAttack.add(Direction.NORTH),
                            toAttack.add(Direction.WEST), toAttack.add(Direction.EAST),
                            toAttack.add(Direction.SOUTH),
                            toAttack.add(Direction.NORTHEAST), toAttack.add(Direction.NORTHWEST),
                            toAttack.add(Direction.SOUTHEAST), toAttack.add(Direction.SOUTHWEST) };
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
                    Direction to = Pathfinding.greedyPathfinding(rc, leastRubbleLocation);
                    if (rc.canMove(to)) {
                        rc.move(to);
                        System.out.println("MOVETOMOREOPTIMAL:" + leastRubbleLocation);
                    }
                }
            } else {
                RobotInfo[] alliesInVisionRange = rc.senseNearbyRobots(visionRadius, rc.getTeam());
                if (allyAttackersCount < enemyAttackersCount && !nearAllyArchon) {
                    if (allyAttackersCount != 0) {
                        RobotInfo nearestAlly = getClosestAlly(rc, alliesInVisionRange);
                        if (nearestAlly != null) {
                            Direction escapeDir = Pathfinding.greedyPathfinding(rc, nearestAlly.getLocation());
                            if (rc.canMove(escapeDir)) {
                                rc.move(escapeDir);
                                System.out.println("RUNNINGTOALLY:" + escapeDir);
                            }
                        }
                    } else {
                        Direction escapeDir = Pathfinding.escapeEnemies(rc);
                        if (rc.canMove(escapeDir)) {
                            rc.move(escapeDir);
                            System.out.println("RUNNINGAWAY:" + escapeDir);
                        }
                    }
                }

                MapLocation toAttack = target.getLocation();
                MapLocation[] surroundings = rc.getAllLocationsWithinRadiusSquared(toAttack, actionRadius);
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
                                    System.out.println("RUNNINGAWAYBCRUBBLETOALLY:" + escapeDir);
                                }
                            }
                        } else {
                            Direction escapeDir = Pathfinding.escapeEnemies(rc);
                            if (rc.canMove(escapeDir)) {
                                rc.move(escapeDir);
                                System.out.println("RUNNINGAWAYBCRUBBLE:" + escapeDir);
                            }
                        }
                    } else {
                        Direction moveToOptimalLocation = Pathfinding.greedyPathfinding(rc, leastRubbleLocation);
                        if (current.add(moveToOptimalLocation)
                                .distanceSquaredTo(target.getLocation()) <= RobotType.SOLDIER.actionRadiusSquared) {
                            if (rc.isActionReady()) {
                                if (rc.canMove(moveToOptimalLocation)) {
                                    rc.move(moveToOptimalLocation);
                                    System.out.println("MOVINGTOATTACK:" + moveToOptimalLocation);
                                }
                            }
                        } else {
                            if (rc.canMove(moveToOptimalLocation)) {
                                rc.move(moveToOptimalLocation);
                                System.out.println("MOVINGTOWARDSTARGET:" + moveToOptimalLocation);
                            }
                        }
                    }
                }
            }
*/