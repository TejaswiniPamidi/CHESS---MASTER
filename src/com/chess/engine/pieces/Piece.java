package com.chess.engine.pieces;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Move;

import java.util.Collection;
import java.util.Objects;

public abstract class Piece {

    protected final PieceType pieceType;
    protected final int piecePosition;
    protected final Alliance pieceAlliance;
    protected final boolean isFirstMove;
    private final int cachedHashCode;

    // Constructor
    protected Piece(final PieceType pieceType, final int piecePosition, final Alliance pieceAlliance, final boolean isFirstMove) {
        this.pieceType = pieceType;
        this.piecePosition = piecePosition;
        this.pieceAlliance = pieceAlliance;
        this.isFirstMove = isFirstMove;
        this.cachedHashCode = computeHashCode();
    }

    private int computeHashCode() {
        int result = pieceType.hashCode();
        result = 31 * result + pieceAlliance.hashCode();
        result = 31 * result + piecePosition;
        result = 31 * result + (isFirstMove ? 1 : 0);
        return result;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) return true;
        if (!(other instanceof Piece)) return false;
        final Piece otherPiece = (Piece) other;
        return piecePosition == otherPiece.getPiecePosition()
                && pieceType == otherPiece.getPieceType()
                && pieceAlliance == otherPiece.getPieceAlliance()
                && isFirstMove == otherPiece.isFirstMove();
    }

    @Override
    public int hashCode() {
        return this.cachedHashCode;
    }

    // ==========================================
    // Getters
    // ==========================================
    public Alliance getPieceAlliance() {
        return this.pieceAlliance;
    }

    public boolean isFirstMove() {
        return this.isFirstMove;
    }

    public int getPiecePosition() {
        return this.piecePosition;
    }

    public PieceType getPieceType() {
        return this.pieceType;
    }

    // ==========================================
    // Abstract methods
    // ==========================================
    public abstract Collection<Move> calculateLegalMoves(final Board board);

    public abstract Collection<Move> calculatedLegalMoves(Board board);

    public abstract Piece movePiece(final Move move);

    // ==========================================
    // Piece Value (for AI evaluation)
    // ==========================================
    public int getPieceValue() {
        switch (this.pieceType) {
            case PAWN:
                return 100;
            case KNIGHT:
                return 300;
            case BISHOP:
                return 300;
            case ROOK:
                return 500;
            case QUEEN:
                return 900;
            case KING:
                return 10000;
            default:
                return 0;
        }
    }

    // ==========================================
    // Piece Types
    // ==========================================
    public enum PieceType {
        PAWN("P") {
            @Override
            public boolean isKing() { return false; }
            @Override
            public boolean isRook() { return false; }
        },
        KNIGHT("N") {
            @Override
            public boolean isKing() { return false; }
            @Override
            public boolean isRook() { return false; }
        },
        BISHOP("B") {
            @Override
            public boolean isKing() { return false; }
            @Override
            public boolean isRook() { return false; }
        },
        ROOK("R") {
            @Override
            public boolean isKing() { return false; }
            @Override
            public boolean isRook() { return true; }
        },
        QUEEN("Q") {
            @Override
            public boolean isKing() { return false; }
            @Override
            public boolean isRook() { return false; }
        },
        KING("K") {
            @Override
            public boolean isKing() { return true; }
            @Override
            public boolean isRook() { return false; }
        };

        private final String pieceName;

        PieceType(final String pieceName) {
            this.pieceName = pieceName;
        }

        public abstract boolean isKing();
        public abstract boolean isRook();

        @Override
        public String toString() {
            return this.pieceName;
        }
    }
}
