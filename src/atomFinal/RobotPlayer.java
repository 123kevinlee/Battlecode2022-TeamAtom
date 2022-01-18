package atomFinal;

import battlecode.common.*;

public strictfp class RobotPlayer {
    public static void run(RobotController rc) throws GameActionException {

        RobotType type = rc.getType();

        switch (type) {
            case ARCHON:
                Archon.init(rc);
                break;
            case MINER:
                Miner.init(rc);
                break;
            case SOLDIER:
                Soldier.init(rc);
                break;
            case LABORATORY:
                break;
            case WATCHTOWER:
                break;
            case BUILDER:
                Builder.init(rc);
                break;
            case SAGE:
                Sage.init(rc);
                break;
        }

        while (true) {
            try {
                checkForChargeAnomaly(rc);
                switch (type) {
                    case ARCHON:
                        Archon.runArchon(rc);
                        break;
                    case MINER:
                        Miner.runMiner(rc);
                        break;
                    case SOLDIER:
                        Soldier.runSoldier(rc);
                        break;
                    case LABORATORY:
                        break;
                    case WATCHTOWER:
                        break;
                    case BUILDER:
                        Builder.runBuilder(rc);
                        break;
                    case SAGE:
                        Sage.runSage(rc);
                        break;
                }
            } catch (GameActionException e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            } catch (Exception e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            } finally {
                Clock.yield();
            }
        }
    }

    static void checkForChargeAnomaly(RobotController rc) throws GameActionException {
        AnomalyScheduleEntry[] anomalySchedule = rc.getAnomalySchedule();
        for (int i = 0; i < anomalySchedule.length; i++) {
            if (anomalySchedule[i].anomalyType == AnomalyType.CHARGE
                    && anomalySchedule[i].roundNumber - rc.getRoundNum() >= 0
                    && anomalySchedule[i].roundNumber - rc.getRoundNum() <= 5) {
                //System.out.println("CHARGE");
                RobotInfo[] nearbyFriendlies = rc.senseNearbyRobots(rc.getType().visionRadiusSquared,
                        rc.getTeam());

                RobotInfo closestFriendly = null;
                int distance = Integer.MAX_VALUE;
                for (int j = 0; j < nearbyFriendlies.length; j++) {
                    if (rc.getLocation().distanceSquaredTo(nearbyFriendlies[j].getLocation()) < distance) {
                        closestFriendly = nearbyFriendlies[j];
                        distance = rc.getLocation().distanceSquaredTo(nearbyFriendlies[j].getLocation());
                    }
                }
                if (closestFriendly != null) {
                    Direction dir = rc.getLocation().directionTo(closestFriendly.getLocation()).opposite();
                    dir = Pathfinding.greedyPathfinding(rc, dir);
                    if (rc.canMove(dir)) {
                        rc.move(dir);
                    }
                }
            }
        }
    }
}
