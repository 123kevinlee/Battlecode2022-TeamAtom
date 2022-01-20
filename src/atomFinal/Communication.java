package atomFinal;

import battlecode.common.*;
import java.util.*;

//0-3 enemyArchon
//4-9 metalLocation
//10-19 enemyLocation
//20 archon moving
//49 = rand
//50-53 archonId
//54 spawnIndex
//55-58 distressSignals
//59-62 archonLocations
//63 lastLeadAmount
public class Communication {
    static int ownBaseLocationIndex = 0;
    static int distressIndex = -1;

    static void setCommArrayIndexToZero(RobotController rc, int index) throws GameActionException {
        rc.writeSharedArray(index, 0);
    }

    static void addEnemyArconLocation(int location, RobotController rc) throws GameActionException {
        int[] locations = new int[] { rc.readSharedArray(0), rc.readSharedArray(1), rc.readSharedArray(2),
                rc.readSharedArray(3) };
        for (int i = 0; i < locations.length; i++) {
            if (locations[i] == location) {
                break;
            }
            if (locations[i] == 0) {
                rc.writeSharedArray(i, location);
                break;
            }
        }
    }

    static void removeEnemyArconLocation(int location, RobotController rc) throws GameActionException {
        int[] locations = new int[] { rc.readSharedArray(0), rc.readSharedArray(1), rc.readSharedArray(2),
                rc.readSharedArray(3) };
        for (int i = 0; i < locations.length; i++) {
            if (locations[i] == location) {
                setCommArrayIndexToZero(rc, i);
            }
        }
    }

    static int[] getEnemyArconLocations(RobotController rc) throws GameActionException {
        int[] locations = new int[] { rc.readSharedArray(0), rc.readSharedArray(1), rc.readSharedArray(2),
                rc.readSharedArray(3) };
        return locations;
    }

    static void addMetalLocation(RobotController rc, int location) throws GameActionException {
        int[] locations = new int[] { rc.readSharedArray(4), rc.readSharedArray(5), rc.readSharedArray(6),
                rc.readSharedArray(7), rc.readSharedArray(8), rc.readSharedArray(9) };
        for (int i = 0; i < locations.length; i++) {
            if (locations[i] != 0) {
                MapLocation mapLocation = convertIntToMapLocation(locations[i]);
                MapLocation thisLocation = convertIntToMapLocation(location);
                if (mapLocation.distanceSquaredTo(thisLocation) < 20) {
                    break;
                }
            } else if (locations[i] == 0) {
                rc.writeSharedArray(i + 4, location);
                break;
            }
        }
    }

    static void removeMetalLocation(int location, RobotController rc) throws GameActionException {
        int[] locations = new int[] { rc.readSharedArray(4), rc.readSharedArray(5), rc.readSharedArray(6),
                rc.readSharedArray(7), rc.readSharedArray(8), rc.readSharedArray(9) };
        for (int i = 0; i < locations.length; i++) {
            if (locations[i] == location) {
                setCommArrayIndexToZero(rc, i + 4);
            }
        }
    }

    static int[] getMetalLocations(RobotController rc) throws GameActionException {
        int[] locations = new int[] { rc.readSharedArray(4), rc.readSharedArray(5), rc.readSharedArray(6),
                rc.readSharedArray(7), rc.readSharedArray(8), rc.readSharedArray(9) };
        return locations;
    }

    static void addEnemyLocation(RobotController rc, int location) throws GameActionException {
        int[] locations = new int[] { rc.readSharedArray(10), rc.readSharedArray(11), rc.readSharedArray(12),
                rc.readSharedArray(13), rc.readSharedArray(14), rc.readSharedArray(15), rc.readSharedArray(16),
                rc.readSharedArray(17), rc.readSharedArray(18), rc.readSharedArray(19) };
        for (int i = 0; i < locations.length; i++) {
            if (locations[i] != 0) {
                MapLocation mapLocation = convertIntToMapLocation(locations[i]);
                MapLocation thisLocation = convertIntToMapLocation(location);
                if (mapLocation.distanceSquaredTo(thisLocation) < RobotType.SOLDIER.visionRadiusSquared * 2) {
                    break;
                }
            } else if (locations[i] == 0) {
                rc.writeSharedArray(i + 10, location);
                break;
            }
        }
    }

    static int[] getEnemyLocations(RobotController rc) throws GameActionException {
        int[] locations = new int[] { rc.readSharedArray(10), rc.readSharedArray(11), rc.readSharedArray(12),
                rc.readSharedArray(13), rc.readSharedArray(14), rc.readSharedArray(15), rc.readSharedArray(16),
                rc.readSharedArray(17), rc.readSharedArray(18), rc.readSharedArray(19) };
        return locations;
    }

    static void clearEnemyLocations(RobotController rc) throws GameActionException {
        setCommArrayIndexToZero(rc, 10);
        setCommArrayIndexToZero(rc, 11);
        setCommArrayIndexToZero(rc, 12);
        setCommArrayIndexToZero(rc, 13);
        setCommArrayIndexToZero(rc, 14);
        setCommArrayIndexToZero(rc, 15);
        setCommArrayIndexToZero(rc, 16);
        setCommArrayIndexToZero(rc, 17);
        setCommArrayIndexToZero(rc, 18);
        setCommArrayIndexToZero(rc, 19);
    }

    static void addArchonId(RobotController rc, int id) throws GameActionException {
        int[] ids = new int[] { rc.readSharedArray(50), rc.readSharedArray(51), rc.readSharedArray(52),
                rc.readSharedArray(53) };
        for (int i = 0; i < ids.length; i++) {
            if (ids[i] == 0) {
                rc.writeSharedArray(i + 50, id);
                break;
            }
        }
    }

    static int[] getArchonIds(RobotController rc) throws GameActionException {
        int[] ids = new int[] { rc.readSharedArray(50), rc.readSharedArray(51), rc.readSharedArray(52),
                rc.readSharedArray(53) };
        return ids;
    }

    static void addArchonLocation(RobotController rc, int location) throws GameActionException {
        int[] locations = new int[] { rc.readSharedArray(59), rc.readSharedArray(60), rc.readSharedArray(61),
                rc.readSharedArray(62) };
        for (int i = 0; i < locations.length; i++) {
            if (locations[i] == 0) {
                rc.writeSharedArray(i + 59, location);
                ownBaseLocationIndex = i + 59;
                break;
            }
        }
    }

    static void changeArchonLocation(RobotController rc, int location) throws GameActionException {
        rc.writeSharedArray(ownBaseLocationIndex, location);
    }

    static int[] getArchonLocations(RobotController rc) throws GameActionException {
        int[] locations = new int[] { rc.readSharedArray(59), rc.readSharedArray(60), rc.readSharedArray(61),
                rc.readSharedArray(62) };
        return locations;
    }

    static int getArchonSpawnIndex(RobotController rc) throws GameActionException {
        return rc.readSharedArray(54);
    }

    static void increaseArchonSpawnIndex(RobotController rc) throws GameActionException {
        if (rc.readSharedArray(54) >= rc.getArchonCount() - 1) {
            rc.writeSharedArray(54, 0);
        } else {
            rc.writeSharedArray(54, rc.readSharedArray(54) + 1);
        }

    }

    static void setLastLeadAmnt(RobotController rc, int value) throws GameActionException {
        rc.writeSharedArray(63, value);
    }

    static int getLastLeadAmnt(RobotController rc) throws GameActionException {
        return rc.readSharedArray(63);
    }

    static MapLocation convertIntToMapLocation(int location) {
        String locationS = Integer.toString(location);
        int x = 0, y = 0;
        if (locationS.length() == 3) {
            x = Integer.parseInt(locationS.substring(0, 1));
            y = Integer.parseInt(locationS.substring(1));
        } else if (locationS.length() == 4) {
            x = Integer.parseInt(locationS.substring(0, 2));
            y = Integer.parseInt(locationS.substring(2));
        }
        return new MapLocation(x, y);
    }

    static void sendDistressSignal(RobotController rc, int location) throws GameActionException {
        if (distressIndex == -1) {
            int[] locations = new int[] { rc.readSharedArray(55), rc.readSharedArray(56), rc.readSharedArray(57),
                    rc.readSharedArray(58) };
            for (int i = 0; i < locations.length; i++) {
                if (locations[i] == 0) {
                    rc.writeSharedArray(i + 55, location);
                    distressIndex = i + 55;
                    break;
                }
            }
        } else {
            rc.writeSharedArray(distressIndex, location);
        }
    }

    static void endDistressSignal(RobotController rc, int location) throws GameActionException {
        if (distressIndex != -1) {
            setCommArrayIndexToZero(rc, distressIndex);
            distressIndex = -1;
        }
    }

    static void clearDistressSignals(RobotController rc) throws GameActionException {
        setCommArrayIndexToZero(rc, 55);
        setCommArrayIndexToZero(rc, 56);
        setCommArrayIndexToZero(rc, 57);
        setCommArrayIndexToZero(rc, 58);
    }

    static int[] checkDistressSignal(RobotController rc) throws GameActionException {
        int[] locations = new int[] { rc.readSharedArray(55), rc.readSharedArray(56), rc.readSharedArray(57),
                rc.readSharedArray(58) };
        return locations;
    }

    static void signalMovingArchon(RobotController rc) throws GameActionException {
        rc.writeSharedArray(20, 1);
    }

    static void signalMovingArchonEnd(RobotController rc) throws GameActionException {
        rc.writeSharedArray(20, 0);
    }

    static boolean anArchonIsMoving(RobotController rc) throws GameActionException {
        if (rc.readSharedArray(20) == 1) {
            return true;
        } else {
            return false;
        }
    }

    static void seenEnemy(RobotController rc) throws GameActionException{
        rc.writeSharedArray(49, 1);
    }

    static int convertMapLocationToInt(MapLocation location) {
        String x = String.format("%02d", location.x);
        String y = String.format("%02d", location.y);
        String locationS = x + y;
        return Integer.parseInt(locationS);
    }
}
