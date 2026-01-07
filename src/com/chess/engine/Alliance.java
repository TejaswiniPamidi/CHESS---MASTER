package com.chess.engine;

import com.chess.engine.player.BlackPlayer;
import com.chess.engine.player.Player;
import com.chess.engine.player.WhitePlayer;

public enum Alliance {

    WHITE {
        @Override
        public int getDirection() {
            // White pieces move upward on the board (from bottom to top)
            return -1;
        }

        @Override
        public int getOppositeDirection() {
            return 1;
        }

        @Override
        public boolean isWhite() {
            return true;
        }

        @Override
        public boolean isBlack() {
            return false;
        }

        @Override
        public Player choosePlayer(final WhitePlayer whitePlayer, final BlackPlayer blackPlayer) {
            return whitePlayer;
        }

        @Override
        public String toString() {
            return "White";
        }
    },

    BLACK {
        @Override
        public int getDirection() {
            // Black pieces move downward (from top to bottom)
            return 1;
        }

        @Override
        public int getOppositeDirection() {
            return -1;
        }

        @Override
        public boolean isWhite() {
            return false;
        }

        @Override
        public boolean isBlack() {
            return true;
        }

        @Override
        public Player choosePlayer(final WhitePlayer whitePlayer, final BlackPlayer blackPlayer) {
            return blackPlayer;
        }

        @Override
        public String toString() {
            return "Black";
        }
    };

    /**
     * Returns +1 or -1 depending on the alliance.
     * Useful for calculating pawn move direction and coordinate changes.
     */
    public abstract int getDirection();

    /**
     * Returns the opposite direction, helpful for pawn attack diagonals.
     */
    public abstract int getOppositeDirection();

    public abstract boolean isWhite();
    public abstract boolean isBlack();

    /**
     * Returns which player (white or black) corresponds to this alliance.
     */
    public abstract Player choosePlayer(WhitePlayer whitePlayer, BlackPlayer blackPlayer);
}
