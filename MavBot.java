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
import java.util.Random;

public class MavBot {
    static String logFile;
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
                String shipFo = "Ship:" + ship.id + "\t Location : (" + ship.position.x + "," + ship.position.y+")\t Halite:"+ ship.halite;
                log(shipFo);
                if (gameMap.at(ship).halite < Constants.MAX_HALITE / 10 || ship.isFull()) {
                    final Direction randomDirection = Direction.ALL_CARDINALS.get(rng.nextInt(4));
                    commandQueue.add(ship.move(randomDirection));
                } else {
                    commandQueue.add(ship.stayStill());
                }
            }

            if (
                    game.turnNumber <= 200 &&
                            me.halite >= Constants.SHIP_COST &&
                            !gameMap.at(me.shipyard).isOccupied()) {
                commandQueue.add(me.shipyard.spawn());
            }

            game.endTurn(commandQueue);
        }
    }
    private static void init(Game game, long rngSeed){
        logFile="map-" + rngSeed + ".log";
        outputBoard(game.gameMap.cells);
    }

    private static void log(String s){
        try {
            PrintWriter log = new PrintWriter(new FileOutputStream(new File(logFile),true));
            log.println(s);
            log.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void outputBoard(MapCell[][] map) {
        StringBuilder rows = new StringBuilder();
        for (MapCell[] aMap : map) {
            for (MapCell anAMap : aMap) {
                rows.append(anAMap.halite).append("\t");
            }
            rows.append("\n");
        }
        log(rows.toString());
    }
}
