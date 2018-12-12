package mav;

import hlt.Constants;
import hlt.Direction;
import hlt.EntityId;
import hlt.GameMap;
import hlt.Log;
import hlt.MapCell;
import hlt.PlayerId;
import hlt.Position;
import hlt.Ship;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MavShip extends Ship {
    private Map<Direction, MapCell> adjCells = new HashMap<>();
    private List<MapCell> hist = new ArrayList<>();
    private Direction backDir;
    private Position mPos;
    private Position dropOff;
    private ShipMode mode = ShipMode.EGRESS;
    private int halite = 0;

    public MavShip(Ship s) {
        super(s.owner, s.id, s.position, s.halite);
        mPos = s.position;
        dropOff = mPos;
    }

    public void updateShip(Ship s, GameMap gameMap) {
        if (s.halite < ((Constants.MAX_HALITE * 5) / 10)) {
            Log.log("i want more minerals");
            if (s.position.equals(dropOff)) {
                Log.log("made it home");
            }
            mode = ShipMode.EGRESS;
        }
        mPos = s.position;
        halite = s.halite;
        if (!hist.contains(gameMap.at(s)))
            hist.add(gameMap.at(s));
        updateAdjCells(gameMap);
        //TODO set mode.

        if (s.halite > (Constants.MAX_HALITE * 9) / 10) {
            Log.log("time to go home");
            mode = ShipMode.INGRESS;
            backDir = gameMap.naiveNavigate(s, dropOff);
            Log.log("Back Direction is:" + backDir);
//            if (adjCells.get(backDir).isOccupied()) {
//                Log.log("i feel blocked by :"+adjCells.get(backDir).ship.id.id);
//                backDir = Direction.STILL;
//            }

        }

    }

    private void updateAdjCells(GameMap gameMap) {
        adjCells.put(Direction.NORTH,
                gameMap.at(gameMap.normalize(new Position(this.mPos.x, this.mPos.y - 1))));
        adjCells.put(Direction.EAST, gameMap.at(gameMap.normalize(new Position(this.mPos.x + 1, this.mPos.y))));
        adjCells.put(Direction.SOUTH,
                gameMap.at(gameMap.normalize(new Position(this.mPos.x, this.mPos.y + 1))));
        adjCells.put(Direction.WEST, gameMap.at(gameMap.normalize(new Position(this.mPos.x - 1, this.mPos.y))));
        adjCells.put(Direction.STILL, gameMap.at(this.mPos));
    }

    public Map<Direction, MapCell> getAdjCells() {
        return adjCells;
    }

    public Direction getShipDirection() {
        Log.log("Move Cost: " + adjCells.get(Direction.STILL).halite / Constants.MOVE_COST_RATIO);
        if ((adjCells.get(Direction.STILL).halite / Constants.MOVE_COST_RATIO) > halite) {
            Log.log("Dont have enough to move dont try");
            return Direction.STILL;
        }
        switch (mode) {
            case FARM:
                return this.getFarmDirection();
            case EGRESS:
                return this.getEgressDirection();
            case INGRESS:
                return this.getIngressDirection();
            default:
                return Direction.STILL;
        }
    }

    //A*
    private Direction getEgressDirection() {
        Log.log("getEgressDirection");
        Direction d = Direction.STILL;
        int moveCost = adjCells.get(d).halite / Constants.MOVE_COST_RATIO;
        if (moveCost > 5) {
            //Don't move if sitting on more than 50 halite
            return d;
        } else {
            int max = 0;
            for (Map.Entry<Direction, MapCell> entry : adjCells.entrySet()) {
                Direction dir = entry.getKey();
                MapCell cell = entry.getValue();
                //Log.log("Direction:" + dir + "\tHalite:" + cell.halite);
                if (dir == Direction.STILL) {
                    max = max > cell.halite / Constants.EXTRACT_RATIO ? max : cell.halite / Constants.EXTRACT_RATIO;
                } else {
                    if (cell.isEmpty()) {
                        if ((cell.halite / Constants.EXTRACT_RATIO) - moveCost > max) {
                            d = dir;
                            max = (cell.halite / Constants.EXTRACT_RATIO) - moveCost;
                        }
                    }
                }
            }
            Log.log(" I decided to go Direction:" + d + "\tHalite:" + adjCells.get(d).halite);
            return d;
        }
    }

    private Direction getIngressDirection() {
        return backDir;
    }

    private Direction getFarmDirection() {
        return Direction.STILL;
    }


}

enum ShipMode {
    EGRESS, FARM, INGRESS;
}