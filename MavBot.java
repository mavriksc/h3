import hlt.Command;
import hlt.Constants;
import hlt.Direction;
import hlt.Game;
import hlt.GameMap;
import hlt.Log;
import hlt.MapCell;
import hlt.Player;
import hlt.Ship;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class MavBot {
    public static void main(final String[] args) throws FileNotFoundException, UnsupportedEncodingException {
        final long rngSeed;
        if (args.length > 1) {
            rngSeed = Integer.parseInt(args[1]);
        } else {
            rngSeed = System.nanoTime();
        }
        final Random rng = new Random(rngSeed);

        Game game = new Game();
        // At this point "game" variable is populated with initial map data.
        // This is a good place to do computationally expensive start-up pre-processing.
        // As soon as you call "ready" function below, the 2 second per turn timer will start.
        // TODO: calc 2 moves in advance of mining vs moving
        //  if cost of move + 1 turn mining is > than 2 turns in current spot. move
        // when to start moving towards drop off
        // collision distance and avoidance. 
        game.ready("MavBotV1");
        init(game,rngSeed);

        Log.log("Successfully created bot! My Player ID is " + game.myId + ". Bot rng seed is " + rngSeed + ".");

        for (; ; ) {
            game.updateFrame();
            final Player me = game.me;
            final GameMap gameMap = game.gameMap;

            final ArrayList<Command> commandQueue = new ArrayList<>();

            for (final Ship ship : me.ships.values()) {
                String shipFo =
                        "Ship:" + ship.id + "\t Location : (" + ship.position.x + "," + ship.position.y + ")\t Halite:"
                                + ship.halite;
                Log.log(shipFo);
                if (gameMap.at(ship).halite < Constants.MAX_HALITE / 10 || ship.isFull()) {
                    final Direction randomDirection = Direction.ALL_CARDINALS.get(rng.nextInt(4));
                    commandQueue.add(ship.move(randomDirection));
                } else {
                    commandQueue.add(ship.stayStill());
                }
            }

            if (game.turnNumber <= 200 && me.halite >= Constants.SHIP_COST && !gameMap.at(me.shipyard).isOccupied()) {
                commandQueue.add(me.shipyard.spawn());
            }

            game.endTurn(commandQueue);
        }
    }

    private static void init(Game game, long rngSeed){
        outputConstants();
        outputBoard(game.gameMap.cells);

    }

    private static void outputConstants() {
        Log.log("MAX_HALITE: "+Constants.MAX_HALITE);
        Log.log("SHIP_COST: "+Constants.SHIP_COST);
        Log.log("DROPOFF_COST: "+Constants.DROPOFF_COST);
        Log.log("MAX_TURNS: "+Constants.MAX_TURNS);
        Log.log("EXTRACT_RATIO: "+Constants.EXTRACT_RATIO);
        Log.log("MOVE_COST_RATIO: "+Constants.MOVE_COST_RATIO);
        Log.log("INSPIRATION_ENABLED: "+Constants.INSPIRATION_ENABLED);
        Log.log("INSPIRATION_RADIUS: "+Constants.INSPIRATION_RADIUS);
        Log.log("INSPIRATION_SHIP_COUNT: "+Constants.INSPIRATION_SHIP_COUNT);
        Log.log("INSPIRED_EXTRACT_RATIO: "+Constants.INSPIRED_EXTRACT_RATIO);
        Log.log("INSPIRED_BONUS_MULTIPLIER: "+Constants.INSPIRED_BONUS_MULTIPLIER);
        Log.log("INSPIRED_MOVE_COST_RATIO: "+Constants.INSPIRED_MOVE_COST_RATIO);
    }

    private static void outputBoard(MapCell[][] map) {
        Long tot = 0l;
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

        Log.log(rows.toString());
        Log.log("Total: " + tot);
        Log.log("MAX: " + max);
        Log.log("AVG: " + tot / (map.length * map[0].length));
        Log.log("QUANTILE SIZE:\t<"+qSize+"\t<"+qSize*2+"\t<"+qSize*3+"\t<"+qSize*4);
        Log.log("Distribution: \tQ1:" + q1 + "\tQ2:" + q2 + "\tQ3:" + q3 + "\tQ4:" + q4);

    }
}
