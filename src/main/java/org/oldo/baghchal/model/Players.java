package org.oldo.baghchal.model;

import org.guppy4j.log.Log;
import org.guppy4j.log.LogProvider;
import org.oldo.baghchal.control.Player;

import java.util.Arrays;
import java.util.List;

import static org.guppy4j.log.Log.Level.debug;

/**
 * Manages the (user or computer-based) players
 */
public final class Players {

    private final Log log;

    private final List<Piece> pieces = Arrays.asList(Piece.values());

    private final Player preyStrategy;
    private final Player preyUser;
    private final Player predatorStrategy;
    private final Player predatorUser;

    private Player preyPlayer;
    private Player predatorPlayer;

    public Players(LogProvider logProvider,
                   Player preyStrategy, Player preyUser,
                   Player predatorStrategy, Player predatorUser) {
        log = logProvider.getLog(getClass());
        this.preyStrategy = preyStrategy;
        this.preyUser = preyUser;
        this.predatorStrategy = predatorStrategy;
        this.predatorUser = predatorUser;
        // initially assume human players
        preyPlayer = preyUser;
        predatorPlayer = predatorUser;
    }

    public Iterable<? extends Piece> getPieces() {
        return pieces;
    }

    public void setPlayedByComputer(Piece piece, boolean computer) {
        if (piece == Piece.PREDATOR) {
            predatorPlayer = computer ? predatorStrategy : predatorUser;
        }
        if (piece == Piece.PREY) {
            preyPlayer = computer ? preyStrategy : preyUser;
        }
    }

    public  boolean isPlayedByComputer(Piece piece) {
        return getPlayer(piece).isComputer();
    }

    public Move play(GameTable gameTable, Piece piece) {
        final Player player = getPlayer(piece);
        if (player.isComputer()) {
            sleepSeconds(3);
        }
        return player.play(gameTable);
    }

    private void sleepSeconds(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            log.as(debug, e);
        }
    }

    private Player getPlayer(Piece piece) {
        switch (piece) {
            case PREDATOR:
                return predatorPlayer;
            case PREY:
                return preyPlayer;
            default:
                throw new IllegalArgumentException("Unknown piece: " + piece);
        }
    }
}
