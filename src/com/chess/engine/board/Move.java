package com.chess.engine.board;

import com.chess.engine.pieces.*;

import java.util.ArrayList;
import java.util.Collection;

public abstract class Move {

    protected final Board board;
    protected final Piece movedPiece;
    protected final int destinationCoordinate;

    public static final Move NULL_MOVE = new NullMove();

    public Move(final Board board, final Piece movedPiece, final int destinationCoordinate) {
        this.board = board;
        this.movedPiece = movedPiece;
        this.destinationCoordinate = destinationCoordinate;
    }

    public Piece getMovedPiece() { return this.movedPiece; }
    public int getDestinationCoordinate() { return this.destinationCoordinate; }
    public boolean isAttack() { return false; }
    public Piece getAttackedPiece() { return null; }

    public Board execute() {
        final Board.Builder builder = new Board.Builder();

        // Keep all current player's pieces except moved piece
        for (final Piece piece : this.board.currentPlayer().getActivePieces()) {
            if (!this.movedPiece.equals(piece)) builder.setPiece(piece);
        }

        // Keep opponent pieces
        for (final Piece piece : this.board.currentPlayer().getOpponent().getActivePieces()) {
            builder.setPiece(piece);
        }

        // Move piece
        builder.setPiece(this.movedPiece.movePiece(this));
        builder.setMoveMaker(this.board.currentPlayer().getOpponent().getAlliance());
        return builder.build();
    }

    // ==========================================
    // NullMove
    // ==========================================
    public static final class NullMove extends Move {
        public NullMove() { super(null, null, -1); }
        @Override
        public Board execute() { throw new RuntimeException("Cannot execute null move!"); }
    }

    // ==========================================
    // Regular Moves
    // ==========================================
    public static class MajorMove extends Move {
        public MajorMove(final Board board, final Piece movedPiece, final int destinationCoordinate) {
            super(board, movedPiece, destinationCoordinate);
        }
    }

    public static class AttackMove extends Move {
        private final Piece attackedPiece;
        public AttackMove(final Board board, final Piece movedPiece, final int destinationCoordinate, final Piece attackedPiece) {
            super(board, movedPiece, destinationCoordinate);
            this.attackedPiece = attackedPiece;
        }

        @Override
        public boolean isAttack() { return true; }
        @Override
        public Piece getAttackedPiece() { return this.attackedPiece; }

        @Override
        public Board execute() {
            final Board.Builder builder = new Board.Builder();

            for (final Piece piece : this.board.currentPlayer().getActivePieces()) {
                if (!this.movedPiece.equals(piece)) builder.setPiece(piece);
            }

            for (final Piece piece : this.board.currentPlayer().getOpponent().getActivePieces()) {
                if (!piece.equals(this.getAttackedPiece())) builder.setPiece(piece);
            }

            builder.setPiece(this.movedPiece.movePiece(this));
            builder.setMoveMaker(this.board.currentPlayer().getOpponent().getAlliance());
            return builder.build();
        }
    }

    // ==========================================
    // Pawn Moves
    // ==========================================
    public static class PawnMove extends Move {
        public PawnMove(final Board board, final Pawn movedPawn, final int destinationCoordinate) {
            super(board, movedPawn, destinationCoordinate);
        }
    }

    public static class PawnAttackMove extends AttackMove {
        public PawnAttackMove(final Board board, final Pawn movedPawn, final int destinationCoordinate, final Piece attackedPiece) {
            super(board, movedPawn, destinationCoordinate, attackedPiece);
        }
    }

    public static class PawnJump extends Move {
        public PawnJump(final Board board, final Pawn movedPawn, final int destinationCoordinate) {
            super(board, movedPawn, destinationCoordinate);
        }

        @Override
        public Board execute() {
            final Board.Builder builder = new Board.Builder();

            for (final Piece piece : this.board.currentPlayer().getActivePieces()) {
                if (!this.movedPiece.equals(piece)) builder.setPiece(piece);
            }

            for (final Piece piece : this.board.currentPlayer().getOpponent().getActivePieces()) {
                builder.setPiece(piece);
            }

            final Pawn movedPawn = (Pawn) this.movedPiece.movePiece(this);
            builder.setPiece(movedPawn);
            builder.setEnPassantPawn(movedPawn);
            builder.setMoveMaker(this.board.currentPlayer().getOpponent().getAlliance());
            return builder.build();
        }
    }

    public static class PawnEnPassantAttack extends AttackMove {
        public PawnEnPassantAttack(final Board board, final Pawn movedPawn, final int destinationCoordinate, final Pawn attackedPawn) {
            super(board, movedPawn, destinationCoordinate, attackedPawn);
        }

        @Override
        public Board execute() {
            final Board.Builder builder = new Board.Builder();

            for (final Piece piece : this.board.currentPlayer().getActivePieces()) {
                if (!this.movedPiece.equals(piece)) builder.setPiece(piece);
            }

            for (final Piece piece : this.board.currentPlayer().getOpponent().getActivePieces()) {
                if (!piece.equals(this.getAttackedPiece())) builder.setPiece(piece);
            }

            builder.setPiece(this.movedPiece.movePiece(this));
            builder.setMoveMaker(this.board.currentPlayer().getOpponent().getAlliance());
            return builder.build();
        }
    }

    public static class PawnPromotion extends Move {
        private final Move decoratedMove;
        private final Pawn promotedPawn;

        public PawnPromotion(final Move decoratedMove) {
            super(decoratedMove.board, decoratedMove.getMovedPiece(), decoratedMove.getDestinationCoordinate());
            this.decoratedMove = decoratedMove;
            this.promotedPawn = (Pawn) decoratedMove.getMovedPiece();
        }

        @Override
        public Board execute() {
            final Board pawnMovedBoard = this.decoratedMove.execute();
            final Board.Builder builder = new Board.Builder();

            for (final Piece piece : pawnMovedBoard.currentPlayer().getOpponent().getActivePieces()) {
                builder.setPiece(piece);
            }

            for (final Piece piece : pawnMovedBoard.currentPlayer().getActivePieces()) {
                if (!this.promotedPawn.equals(piece)) builder.setPiece(piece);
            }

            builder.setPiece(new Queen(this.promotedPawn.getPieceAlliance(), this.destinationCoordinate));
            builder.setMoveMaker(pawnMovedBoard.currentPlayer().getOpponent().getAlliance());
            return builder.build();
        }

        @Override
        public boolean isAttack() { return this.decoratedMove.isAttack(); }
        @Override
        public Piece getAttackedPiece() { return this.decoratedMove.getAttackedPiece(); }
    }

    // ==========================================
    // Castling Moves
    // ==========================================
    public static class KingSideCastleMove extends Move {
        private final Rook castleRook;
        private final int castleRookStart;
        private final int castleRookDestination;

        public KingSideCastleMove(final Board board, final King king, final int destinationCoordinate,
                                  final Rook castleRook, final int castleRookStart, final int castleRookDestination) {
            super(board, king, destinationCoordinate);
            this.castleRook = castleRook;
            this.castleRookStart = castleRookStart;
            this.castleRookDestination = castleRookDestination;
        }

        @Override
        public Board execute() {
            final Board.Builder builder = new Board.Builder();

            for (final Piece piece : this.board.currentPlayer().getActivePieces()) {
                if (!this.movedPiece.equals(piece) && !this.castleRook.equals(piece)) builder.setPiece(piece);
            }

            for (final Piece piece : this.board.currentPlayer().getOpponent().getActivePieces()) {
                builder.setPiece(piece);
            }

            builder.setPiece(this.movedPiece.movePiece(this));
            builder.setPiece(new Rook(this.castleRook.getPieceAlliance(), this.castleRookDestination));
            builder.setMoveMaker(this.board.currentPlayer().getOpponent().getAlliance());
            return builder.build();
        }
    }

    public static class QueenSideCastleMove extends KingSideCastleMove {
        public QueenSideCastleMove(final Board board, final King king, final int destinationCoordinate,
                                   final Rook castleRook, final int castleRookStart, final int castleRookDestination) {
            super(board, king, destinationCoordinate, castleRook, castleRookStart, castleRookDestination);
        }
    }

    // ==========================================
    // Move Factory
    // ==========================================
    public static class MoveFactory {
        public static Move createMove(final Board board, final int currentCoordinate, final int destinationCoordinate) {
            for (final Move move : board.currentPlayer().getLegalMoves()) {
                if (move.getMovedPiece().getPiecePosition() == currentCoordinate &&
                        move.getDestinationCoordinate() == destinationCoordinate) {
                    return move;
                }
            }
            return NULL_MOVE;
        }
    }
}
