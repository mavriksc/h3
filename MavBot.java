import hlt.Command;
import hlt.Constants;
import hlt.Direction;
import hlt.Game;
import hlt.GameMap;
import hlt.Log;
import hlt.MapCell;
import hlt.Player;
import hlt.Position;
import hlt.Ship;
import mav.MavShip;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MavBot {
    public static void main(final String[] args) {
        final long rngSeed;
        if (args.length > 1) {
            rngSeed = Integer.parseInt(args[1]);
        } else {
            rngSeed = System.nanoTime();
        }

        Game game = new Game();
        // At this point "game" variable is populated with initial map data.
        // This is a good place to do computationally expensive start-up pre-processing.
        // As soon as you call "ready" function below, the 2 second per turn timer will start.
        // TODO: calc 2 moves in advance of mining vs moving
        //  if cost of move + 1 turn mining is > than 2 turns in current spot. move
        // when to start moving towards drop off
        // collision distance and avoidance. 
        game.ready("MavBotV1");
        Log.log("Turns to mine 900:\t" + turnsToMine(900));
        Log.log("Turns to mine 600:\t" + turnsToMine(600));
        Log.log("Turns to mine 300:\t" + turnsToMine(300));
        Log.log("Turns to mine 100:\t" + turnsToMine(100));
        Log.log("Turns to mine 60:\t" + turnsToMine(60));
        Log.log("Turns to mine 30:\t" + turnsToMine(30));
        Log.log("Turns to mine 10:\t" + turnsToMine(10));
        Log.log("Turns to mine 6:\t" + turnsToMine(6));
        Log.log("Turns to mine 3:\t" + turnsToMine(3));
        init(game);

        Log.log("Successfully created bot! My Player ID is " + game.myId + ". Bot rng seed is " + rngSeed + ".");
        Map<Integer, MavShip> ships = new HashMap<>();

        do {
            game.updateFrame();
            final Player me = game.me;
            final GameMap gameMap = game.gameMap;

            final ArrayList<Command> commandQueue = new ArrayList<>();

            for (final Ship ship : me.ships.values()) {
                String shipFo =
                        "Ship:" + ship.id + "\t Location : (" + ship.position.x + "," + ship.position.y + ")\t Halite:"
                                + ship.halite;
                Log.log(shipFo);
                MavShip m;
                if (ships.containsKey(ship.id.id)) {
                    m = ships.get(ship.id.id);
                    m.updateShip(ship, gameMap);
                    ships.put(m.id.id, m);
                } else {
                    Log.log("new ship created ID:" + ship.id.id);
                    m = new MavShip(ship);
                    m.updateShip(ship, gameMap);
                    ships.put(m.id.id, m);
                }

                commandQueue.add(ship.move(m.getShipDirection()));
            }

            if (game.turnNumber <= 200 && me.halite >= Constants.SHIP_COST && !gameMap.at(me.shipyard).isOccupied()) {
                commandQueue.add(me.shipyard.spawn());
            }

            game.endTurn(commandQueue);
        } while (game.turnNumber < Constants.MAX_TURNS);
    }

    private boolean canMove(Ship s, GameMap gameMap) {
        return s.halite > gameMap.at(s).halite / 10;
    }

    private static void init(Game game) {
        outputConstants();
        outputBoard(game.gameMap.cells);
    }

    private static int turnsToMine(int halite) {
        if (halite <= 0) {
            return 0;
        } else {
            int getsMined = (int) Math.ceil((float) halite / Constants.EXTRACT_RATIO);
            return 1 + turnsToMine(halite - getsMined);
        }
    }

    private static void outputConstants() {
        Log.log("MAX_HALITE: " + Constants.MAX_HALITE);
        Log.log("SHIP_COST: " + Constants.SHIP_COST);
        Log.log("DROPOFF_COST: " + Constants.DROPOFF_COST);
        Log.log("MAX_TURNS: " + Constants.MAX_TURNS);
        Log.log("EXTRACT_RATIO: " + Constants.EXTRACT_RATIO);
        Log.log("MOVE_COST_RATIO: " + Constants.MOVE_COST_RATIO);
        Log.log("INSPIRATION_ENABLED: " + Constants.INSPIRATION_ENABLED);
        Log.log("INSPIRATION_RADIUS: " + Constants.INSPIRATION_RADIUS);
        Log.log("INSPIRATION_SHIP_COUNT: " + Constants.INSPIRATION_SHIP_COUNT);
        Log.log("INSPIRED_EXTRACT_RATIO: " + Constants.INSPIRED_EXTRACT_RATIO);
        Log.log("INSPIRED_BONUS_MULTIPLIER: " + Constants.INSPIRED_BONUS_MULTIPLIER);
        Log.log("INSPIRED_MOVE_COST_RATIO: " + Constants.INSPIRED_MOVE_COST_RATIO);
    }

    private static void outputBoard(MapCell[][] map) {
        long tot = 0L;
        int max = 0;
        int min = 64000;
        List<Integer> cells = new ArrayList<>();
        StringBuilder rows = new StringBuilder();
        for (MapCell[] row : map) {
            for (MapCell cell : row) {
                cells.add(cell.halite);
                rows.append(cell.halite).append("\t");
                tot += cell.halite;
                if (cell.halite > max)
                    max = cell.halite;
                if (cell.halite < min)
                    min = cell.halite;
            }
            rows.append("\n");
        }
        //Output turns to mine
        rows.append("*****************\n");
        rows.append("**Turns to mine**\n");
        rows.append("*****************\n");
        for (MapCell[] row : map) {
            for (MapCell cell : row) {
                rows.append(turnsToMine(cell.halite)).append("\t");
            }
            rows.append("\n");
        }

        cells.sort(Comparator.naturalOrder());
        int range = max - min;
        int qSize = range / 4;
        int q1 = 0, q2 = 0, q3 = 0, q4 = 0;
        for (Integer c : cells) {
            if (c < qSize) {
                q1++;
            } else if (c < qSize * 2) {
                q2++;
            } else if (c < qSize * 3) {
                q3++;
            } else {
                q4++;
            }
        }

        int rq1 = cells.get((cells.size() - 1) / 4);
        int rq2 = cells.get((cells.size() - 1) / 2);
        int rq3 = cells.get(((cells.size() - 1) * 3) / 4);
        int rq4 = cells.get((cells.size() - 1));

        Log.log(rows.toString());
        Log.log("Total: " + tot);
        Log.log("MAX: " + max);
        Log.log("AVG: " + tot / (map.length * map[0].length));
        Log.log("Dist Value  : \t<" + qSize + "\t<" + qSize * 2 + "\t<" + qSize * 3 + "\t<" + qSize * 4);
        Log.log("Distribution: \t" + q1 + "  \t" + q2 + "  \t" + q3 + "  \t" + q4);
        Log.log("Quantiles: \tQ1:<" + rq1 + "\tQ2:<" + rq2 + "\tQ3:<" + rq3 + "\tQ4:<" + rq4);

    }
}
