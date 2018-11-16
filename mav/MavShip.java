package mav;

import hlt.Direction;
import hlt.EntityId;
import hlt.GameMap;
import hlt.MapCell;
import hlt.PlayerId;
import hlt.Position;
import hlt.Ship;

import java.util.HashMap;
import java.util.Map;

public class MavShip extends Ship {
    Map<Direction, MapCell> adjCells = new HashMap<>();
    private Position mPos;

    public MavShip(Ship s) {
        super(s.owner, s.id, s.position, s.halite);
        mPos = s.position;
    }

    public void updateShip(Ship s, GameMap gameMap) {
        mPos = s.position;
        updateAdjCells(gameMap);
    }

    private void updateAdjCells(GameMap gameMap) {
        adjCells.put(Direction.NORTH,
                gameMap.at(gameMap.normalize(new Position(this.mPos.x, this.mPos.y - 1))));
        adjCells.put(Direction.EAST, gameMap.at(gameMap.normalize(new Position(this.mPos.x + 1, this.mPos.y))));
        adjCells.put(Direction.SOUTH,
                gameMap.at(gameMap.normalize(new Position(this.mPos.x, this.mPos.y + 1))));
        adjCells.put(Direction.WEST, gameMap.at(gameMap.normalize(new Position(this.mPos.x - 1, this.mPos.y))));
        adjCells.put(Direction.STILL, gameMap.at(this));
    }

    public Map<Direction, MapCell> getAdjCells() {
        return adjCells;
    }
}
