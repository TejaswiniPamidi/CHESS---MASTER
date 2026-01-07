package com.chess.engine.player;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.pieces.King;
import com.chess.engine.pieces.Piece;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Common player functionality.
 * Subclasses must implement getActivePieces(), getAlliance(), getOpponent(),
 * and calculateKingCastles(...) which returns the player's castling moves.
 */
public abstract class Player {

    protected final Board board;
    protected final King playerKing;
    protected final Collection<Move> legalMoves;
    private final boolean isInCheck;

    /**
     * Constructor: note that playerLegalMoves and opponentLegalMoves are passed in
     * by the Board while the board and players are constructed.
     */
    Player(final Board board,
           final Collection<Move> playerLegalMoves,
           final Collection<Move> opponentLegalMoves) {
        this.board = board;
        this.playerKing = establishKing();
        // Start with the moves already computed for the player's pieces
        final List<Move> combined = new ArrayList<>(playerLegalMoves);
        // Add castling moves computed with the help of opponent moves
        combined.addAll(calculateKingCastles(playerLegalMoves, opponentLegalMoves));
        this.legalMoves = ImmutableList.copyOf(combined);
        this.isInCheck = !calculateAttacksOnTile(this.playerKing.getPiecePosition(), opponentLegalMoves).isEmpty();
    }

    public King getPlayerKing() {
        return this.playerKing;
    }

    protected static Collection<Move> calculateAttacksOnTile(final int piecePosition,
                                                             final Collection<Move> moves) {
        final List<Move> attackMoves = new ArrayList<>();
        for (final Move move : moves) {
            if (piecePosition == move.getDestinationCoordinate()) {
                attackMoves.add(move);
            }
        }
        return ImmutableList.copyOf(attackMoves);
    }

    private King establishKing() {
        for (final Piece piece : getActivePieces()) {
            if (piece.getPieceType().isKing()) {
                return (King) piece;
            }
        }
        throw new RuntimeException("Board does not contain a King for alliance: " + getAlliance());
    }

    public boolean isMoveLegal(final Move move) {
        return this.legalMoves.contains(move);
    }

    public boolean isInCheck() {
        return this.isInCheck;
    }

    public boolean isInCheckMate() {
        return this.isInCheck && !hasEscapeMoves();
    }

    public boolean isInStaleMate() {
        return !this.isInCheck && !hasEscapeMoves();
    }

    public boolean hasEscapeMoves() {
        for (final Move move : this.legalMoves) {
            final MoveTransition transition = makeMove(move);
            if (transition.getMoveStatus().isDone()) {
                return true;
            }
        }
        return false;
    }

    public boolean isCastled() {
        return false; // can be overridden if castled status is tracked
    }

    public MoveTransition makeMove(final Move move) {
        if (!isMoveLegal(move)) {
            return new MoveTransition(this.board, move, MoveStatus.ILLEGAL_MOVE);
        }

        final Board transitionBoard = move.execute();

        // After making the move, make sure opponent cannot attack our king
        final Collection<Move> kingAttacks = calculateAttacksOnTile(
                transitionBoard.currentPlayer().getOpponent().getPlayerKing().getPiecePosition(),
                transitionBoard.currentPlayer().getLegalMoves()
        );

        if (!kingAttacks.isEmpty()) {
            return new MoveTransition(this.board, move, MoveStatus.LEAVES_PLAYER_IN_CHECK);
        }

        return new MoveTransition(transitionBoard, move, MoveStatus.DONE);
    }

    public Collection<Move> getLegalMoves() {
        return this.legalMoves;
    }

    // Abstract methods to be implemented by concrete players (WhitePlayer, BlackPlayer)
    public abstract Collection<Piece> getActivePieces();
    public abstract Alliance getAlliance();
    public abstract Player getOpponent();

    /**
     * Compute castling moves for this player.
     * Implemented in WhitePlayer and BlackPlayer.
     */
    protected abstract Collection<Move> calculateKingCastles(Collection<Move> playerLegals,
                                                             Collection<Move> opponentLegals);

    // ✅ Fixed missing return statement
    public boolean isInStalemate() {
        return !this.isInCheck && !hasEscapeMoves();
    }
}
