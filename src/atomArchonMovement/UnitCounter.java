package atomArchonMovement;

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
        } else if (round % 3 == 1) {
            Communication.setCommArrayIndexToZero(rc, 40);
            Communication.setCommArrayIndexToZero(rc, 43);
        } else {
            Communication.setCommArrayIndexToZero(rc, 41);
            Communication.setCommArrayIndexToZero(rc, 44);
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
}
