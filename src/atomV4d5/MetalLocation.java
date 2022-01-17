package atomV4d5;

import battlecode.common.*;
import java.util.*;

public class MetalLocation {
    String type;
    int amount;
    MapLocation location;

    public MetalLocation(String type, int amount, MapLocation location) {
        this.type = type;
        this.amount = amount;
        this.location = location;
    }

    public String toString() {
        return amount + type + "@" + location.toString();
    }
}
