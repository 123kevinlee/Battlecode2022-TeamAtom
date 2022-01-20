package atomV4d5random;

import battlecode.common.*;
import java.util.*;

public class Sage {
    static void runSage(RobotController rc) throws GameActionException {
        int actionRadius = rc.getType().actionRadiusSquared;
        Team opponent = rc.getTeam().opponent();

        RobotInfo[] enemies = rc.senseNearbyRobots(actionRadius, opponent);

        RobotInfo target = null;
        int targetValue = Integer.MAX_VALUE; //sage = 1, soldier = 2, builder = 3, archon = 4, miner = 5

        //UnitCounter.addSage(rc);

        // Try to attack someone
        if (enemies.length > 0) {
            if (rc.canEnvision(AnomalyType.CHARGE)) {
                rc.envision(AnomalyType.CHARGE);
            }
        } else {
            int visionRadius = rc.getType().visionRadiusSquared;
            enemies = rc.senseNearbyRobots(visionRadius, opponent);
            int targetDistance = Integer.MAX_VALUE;

            if (enemies.length > 0) {
                for (int i = 0; i < enemies.length; i++) {
                    int enemyValue = Data.determineEnemyValue(enemies[i]);
                    int distanceToEnemy = rc.getLocation().distanceSquaredTo(enemies[i].getLocation());
                    if (enemyValue <= targetValue && distanceToEnemy < targetDistance) {
                        target = enemies[i];
                        targetDistance = distanceToEnemy;
                        targetValue = enemyValue;
                    }
                }
                MapLocation toAttack = target.location;

                String x = String.format("%02d", target.location.x);
                String y = String.format("%02d", target.location.y);
                String locationS = x + y;
                Communication.addEnemyLocation(rc, Integer.parseInt(locationS));

                //Direction dir = Pathfinding.basicBug(rc, toAttack);
                Direction dir = Pathfinding.greedyPathfinding(rc, toAttack);
                if (rc.canMove(dir)) {
                    rc.move(dir);
                }
            } else {
                Direction dir = null;
                int closestEnemyArcon = Soldier.getClosestEnemyArcon(rc);
                MapLocation closestEnemyArconLocation = null;
                if (closestEnemyArcon != 0) {
                    closestEnemyArconLocation = Communication.convertIntToMapLocation(closestEnemyArcon);
                }

                if (closestEnemyArcon != 0) {
                    //dir = Pathfinding.basicBug(rc, closestEnemyArconLocation);
                    dir = Pathfinding.greedyPathfinding(rc, closestEnemyArconLocation);
                    if (rc.canMove(dir)) {
                        rc.move(dir);
                        //rc.setIndicatorString("MOVINGTOARCON");
                    }
                } else {
                    int closestEnemy = Soldier.getClosestEnemy(rc);
                    if (closestEnemy != 0) {
                        MapLocation closestEnemyLocation = Communication.convertIntToMapLocation(closestEnemy);
                        dir = Pathfinding.greedyPathfinding(rc, closestEnemyLocation);
                        if (rc.canMove(dir)) {
                            rc.move(dir);
                        }
                    } else {
                        dir = Pathfinding.wander(rc);
                        if (rc.canMove(dir)) {
                            rc.move(dir);
                            //rc.setIndicatorString("MOVINGRAND");
                            Data.randCounter++;
                        }
                    }
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
    }
}
