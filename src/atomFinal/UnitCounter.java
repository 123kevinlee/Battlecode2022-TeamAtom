package atomFinal;

import battlecode.common.*;
import java.util.*;

/*
40 = soldier 0
41 = soldier 1
42 = soldier 2
43 = miner 0
44 = miner 1
45 = miner 2
*/

public class UnitCounter {
    static void reset(RobotController rc) throws GameActionException {
        int round = rc.getRoundNum();
        if (round % 3 == 0) {
            Communication.setCommArrayIndexToZero(rc, 42);
            Communication.setCommArrayIndexToZero(rc, 45);
            Communication.setCommArrayIndexToZero(rc, 22);
            Communication.setCommArrayIndexToZero(rc, 25);
            Communication.setCommArrayIndexToZero(rc, 28);
        } else if (round % 3 == 1) {
            Communication.setCommArrayIndexToZero(rc, 40);
            Communication.setCommArrayIndexToZero(rc, 43);
            Communication.setCommArrayIndexToZero(rc, 20);
            Communication.setCommArrayIndexToZero(rc, 23);
            Communication.setCommArrayIndexToZero(rc, 26);
        } else {
            Communication.setCommArrayIndexToZero(rc, 41);
            Communication.setCommArrayIndexToZero(rc, 44);
            Communication.setCommArrayIndexToZero(rc, 21);
            Communication.setCommArrayIndexToZero(rc, 24);
            Communication.setCommArrayIndexToZero(rc, 27);
        }
    }

    static void addMiner(RobotController rc) throws GameActionException {
        int round = rc.getRoundNum();
        if (round % 3 == 0) {
            int miners = rc.readSharedArray(44);
            rc.writeSharedArray(44, miners + 1);
        } else if (round % 3 == 1) {
            int miners = rc.readSharedArray(45);
            rc.writeSharedArray(45, miners + 1);
        } else {
            int miners = rc.readSharedArray(43);
            rc.writeSharedArray(43, miners + 1);
        }
    }

    static void addSoldier(RobotController rc) throws GameActionException {
        int round = rc.getRoundNum();
        if (round % 3 == 0) {
            int soldiers = rc.readSharedArray(41);
            rc.writeSharedArray(41, soldiers + 1);
        } else if (round % 3 == 1) {
            int soldiers = rc.readSharedArray(42);
            rc.writeSharedArray(42, soldiers + 1);
        } else {
            int soldiers = rc.readSharedArray(40);
            rc.writeSharedArray(40, soldiers + 1);
        }
    }

    static void addBuilders(RobotController rc) throws GameActionException {
        int round = rc.getRoundNum();
        if (round % 3 == 0) {
            int builders = rc.readSharedArray(24);
            rc.writeSharedArray(24, builders + 1);
        } else if (round % 3 == 1) {
            int builders = rc.readSharedArray(25);
            rc.writeSharedArray(25, builders + 1);
        } else {
            int builders = rc.readSharedArray(23);
            rc.writeSharedArray(23, builders + 1);
        }
    }

    static void addWatchtower(RobotController rc) throws GameActionException {
        int round = rc.getRoundNum();
        if (round % 3 == 0) {
            int watchtowers = rc.readSharedArray(21);
            rc.writeSharedArray(21, watchtowers + 1);
        } else if (round % 3 == 1) {
            int watchtowers = rc.readSharedArray(22);
            rc.writeSharedArray(22, watchtowers + 1);
        } else {
            int watchtowers = rc.readSharedArray(20);
            rc.writeSharedArray(20, watchtowers + 1);
        }
    }

    static void addLaboratory(RobotController rc) throws GameActionException {
        int round = rc.getRoundNum();
        if (round % 3 == 0) {
            int laboratories = rc.readSharedArray(27);
            rc.writeSharedArray(21, laboratories + 1);
        } else if (round % 3 == 1) {
            int laboratories = rc.readSharedArray(28);
            rc.writeSharedArray(22, laboratories + 1);
        } else {
            int laboratories = rc.readSharedArray(26);
            rc.writeSharedArray(20, laboratories + 1);
        }
    }

    static int getMiners(RobotController rc) throws GameActionException {
        int round = rc.getRoundNum();
        if (round % 3 == 0) {
            return rc.readSharedArray(40);
        } else if (round % 3 == 1) {
            return rc.readSharedArray(41);
        } else {
            return rc.readSharedArray(42);
        }
    }

    static int getSoldiers(RobotController rc) throws GameActionException {
        int round = rc.getRoundNum();
        if (round % 3 == 0) {
            return rc.readSharedArray(43);
        } else if (round % 3 == 1) {
            return rc.readSharedArray(44);
        } else {
            return rc.readSharedArray(45);
        }
    }

     static void getBuilders(RobotController rc) throws GameActionException {
        int round = rc.getRoundNum();
        if (round % 3 == 0) {
            return rc.readSharedArray(24);
        } else if (round % 3 == 1) {
            return rc.readSharedArray(25);
        } else {
            int builders = rc.readSharedArray(23);
            rc.writeSharedArray(23, builders + 1);
        }
    }

    static void getWatchtower(RobotController rc) throws GameActionException {
        int round = rc.getRoundNum();
        if (round % 3 == 0) {
            return rc.readSharedArray(21);
        } else if (round % 3 == 1) {
            return rc.readSharedArray(22);
        } else {
            return rc.readSharedArray(20);
        }
    }

    static void getLaboratory(RobotController rc) throws GameActionException {
        int round = rc.getRoundNum();
        if (round % 3 == 0) {
            return rc.readSharedArray(27);
        } else if (round % 3 == 1) {
            return rc.readSharedArray(28);
        } else {
            return rc.readSharedArray(26);
        }
    }
}
