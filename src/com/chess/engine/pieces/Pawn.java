package com.chess.engine.pieces;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.Move;
import com.chess.engine.board.Tile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Pawn extends Piece {

    private static final int[] CANDIDATE_MOVE_OFFSETS = {8, 16, 7, 9};

    public Pawn(final Alliance alliance, final int position) {
        super(PieceType.PAWN, position, alliance, true);
    }

    public Pawn(final Alliance alliance, final int position, final boolean isFirstMove) {
        super(PieceType.PAWN, position, alliance, isFirstMove);
    }

    @Override
    public Collection<Move> calculateLegalMoves(final Board board) {
        final List<Move> legalMoves = new ArrayList<>();
        final int direction = this.getPieceAlliance().getDirection();

        // Single forward move
        final int forwardOne = this.piecePosition + (direction * 8);
        if (BoardUtils.isValidTileCoordinate(forwardOne) && !board.getTile(forwardOne).isTileOccupied()) {
            if (isPromotionSquare(forwardOne)) {
                legalMoves.add(new Move.PawnPromotion(new Move.PawnMove(board, this, forwardOne)));
            } else {
                legalMoves.add(new Move.PawnMove(board, this, forwardOne));
            }

            // Two squares forward from starting position
            final int forwardTwo = this.piecePosition + (direction * 16);
            final int startingRow = this.getPieceAlliance().isWhite() ? 6 : 1;
            if (this.isFirstMove()
                    && BoardUtils.isValidTileCoordinate(forwardTwo)
                    && BoardUtils.getRow(this.piecePosition) == startingRow
                    && !board.getTile(forwardTwo).isTileOccupied()
                    && !board.getTile(forwardOne).isTileOccupied()) {
                legalMoves.add(new Move.PawnJump(board, this, forwardTwo));
            }
        }

        // Diagonal captures (left + right)
        for (final int offset : new int[]{7, 9}) {
            final int destination = this.piecePosition + (direction * offset);
            if (!BoardUtils.isValidTileCoordinate(destination)) continue;

            // Prevent wrap-around
            if (offset == 7 && ((this.pieceAlliance.isWhite() && BoardUtils.FIRST_COLUMN[this.piecePosition]) ||
                    (this.pieceAlliance.isBlack() && BoardUtils.EIGHTH_COLUMN[this.piecePosition]))) {
                continue;
            }
            if (offset == 9 && ((this.pieceAlliance.isWhite() && BoardUtils.EIGHTH_COLUMN[this.piecePosition]) ||
                    (this.pieceAlliance.isBlack() && BoardUtils.FIRST_COLUMN[this.piecePosition]))) {
                continue;
            }

            final Tile destinationTile = board.getTile(destination);

            // ✅ Normal diagonal capture
            if (destinationTile.isTileOccupied()) {
                final Piece pieceAtDestination = destinationTile.getPiece();
                if (this.pieceAlliance != pieceAtDestination.getPieceAlliance()) {
                    if (isPromotionSquare(destination)) {
                        legalMoves.add(new Move.PawnPromotion(new Move.PawnAttackMove(board, this, destination, pieceAtDestination)));
                    } else {
                        legalMoves.add(new Move.PawnAttackMove(board, this, destination, pieceAtDestination));
                    }
                }
            }

            // ✅ En-passant capture
            else if (board.getEnPassantPawn() != null) {
                final Pawn enPassantPawn = board.getEnPassantPawn();
                int pawnPos = enPassantPawn.getPiecePosition();
                if ((offset == 7 && pawnPos == (this.piecePosition - 1)) ||
                        (offset == 9 && pawnPos == (this.piecePosition + 1))) {
                    legalMoves.add(new Move.PawnEnPassantAttack(board, this, destination, enPassantPawn));
                }
            }
        }

        return List.copyOf(legalMoves);
    }

    @Override
    public Collection<Move> calculatedLegalMoves(Board board) {
        return List.of();
    }

    private boolean isPromotionSquare(final int destination) {
        return (this.pieceAlliance.isWhite() && destination >= 0 && destination <= 7) ||
                (this.pieceAlliance.isBlack() && destination >= 56 && destination <= 63);
    }

    @Override
    public Piece movePiece(final Move move) {
        return new Pawn(move.getMovedPiece().getPieceAlliance(), move.getDestinationCoordinate(), false);
    }

    @Override
    public String toString() {
        return PieceType.PAWN.toString();
    }
}
