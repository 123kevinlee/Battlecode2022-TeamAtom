package atomAlpha;

import battlecode.common.*;
import java.util.*;

public class Builder {
    static String role = "";
    static Direction scoutDir = null;

    static void runBuilder(RobotController rc) throws GameActionException {
        scout(rc);
    }

    static void init(RobotController rc) {
        if (rc.canSenseRadiusSquared(3)) {
            for (RobotInfo robot : rc.senseNearbyRobots(3, rc.getTeam())) {
                if (robot.getType() == RobotType.ARCHON) {
                    scoutDir = rc.getLocation().directionTo(robot.getLocation()).opposite();
                }
            }
        }
    }

    static void scout(RobotController rc) throws GameActionException {
        if (rc.canMove(scoutDir)) {
            rc.move(scoutDir);
        }
    }
}
