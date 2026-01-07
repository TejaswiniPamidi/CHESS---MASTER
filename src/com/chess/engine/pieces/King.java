package com.chess.engine.pieces;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.Move;
import com.chess.engine.board.Tile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class King extends Piece {

    private final static int[] CANDIDATE_MOVE_OFFSETS = {-9, -8, -7, -1, 1, 7, 8, 9};

    public King(final Alliance alliance, final int position) {
        super(PieceType.KING, position, alliance, true);
    }

    public King(final Alliance alliance, final int position, final boolean isFirstMove) {
        super(PieceType.KING, position, alliance, isFirstMove);
    }

    @Override
    public Collection<Move> calculateLegalMoves(final Board board) {
        final List<Move> legalMoves = new ArrayList<>();

        // Standard King moves
        for (final int offset : CANDIDATE_MOVE_OFFSETS) {
            final int destination = this.piecePosition + offset;

            if (!BoardUtils.isValidTileCoordinate(destination)) continue;
            if (isFirstColumnExclusion(this.piecePosition, offset) ||
                    isEighthColumnExclusion(this.piecePosition, offset)) continue;

            final Tile tile = board.getTile(destination);
            if (!tile.isTileOccupied()) {
                legalMoves.add(new Move.MajorMove(board, this, destination));
            } else {
                final Piece pieceAtDest = tile.getPiece();
                if (this.pieceAlliance != pieceAtDest.getPieceAlliance()) {
                    legalMoves.add(new Move.AttackMove(board, this, destination, pieceAtDest));
                }
            }
        }

        // Castling moves (only static checks, safe at board creation)
        if (this.isFirstMove() && !board.getTile(this.piecePosition).isTileOccupied()) {
            // King-side castling
            int kingSideRookPos = this.piecePosition + 3; // typical 7th or 63
            Tile rookTile = board.getTile(kingSideRookPos);
            if (rookTile.isTileOccupied() && rookTile.getPiece().getPieceType().isRook() &&
                    rookTile.getPiece().isFirstMove()) {

                // Check empty tiles between king and rook
                boolean emptyBetween = true;
                for (int i = this.piecePosition + 1; i < kingSideRookPos; i++) {
                    if (board.getTile(i).isTileOccupied()) {
                        emptyBetween = false;
                        break;
                    }
                }

                if (emptyBetween) {
                    legalMoves.add(new Move.KingSideCastleMove(
                            board,
                            this,
                            this.piecePosition + 2,
                            (Rook) rookTile.getPiece(),
                            rookTile.getTileCoordinate(),
                            this.piecePosition + 1
                    ));
                }
            }

            // Queen-side castling
            int queenSideRookPos = this.piecePosition - 4; // typical 0 or 56
            rookTile = board.getTile(queenSideRookPos);
            if (rookTile.isTileOccupied() && rookTile.getPiece().getPieceType().isRook() &&
                    rookTile.getPiece().isFirstMove()) {

                boolean emptyBetween = true;
                for (int i = queenSideRookPos + 1; i < this.piecePosition; i++) {
                    if (board.getTile(i).isTileOccupied()) {
                        emptyBetween = false;
                        break;
                    }
                }

                if (emptyBetween) {
                    legalMoves.add(new Move.QueenSideCastleMove(
                            board,
                            this,
                            this.piecePosition - 2,
                            (Rook) rookTile.getPiece(),
                            rookTile.getTileCoordinate(),
                            this.piecePosition - 1
                    ));
                }
            }
        }

        return legalMoves;
    }

    @Override
    public Collection<Move> calculatedLegalMoves(Board board) {
        return List.of();
    }

    private static boolean isFirstColumnExclusion(final int currentPos, final int offset) {
        return BoardUtils.FIRST_COLUMN[currentPos] && (offset == -9 || offset == -1 || offset == 7);
    }

    private static boolean isEighthColumnExclusion(final int currentPos, final int offset) {
        return BoardUtils.EIGHTH_COLUMN[currentPos] && (offset == -7 || offset == 1 || offset == 9);
    }

    @Override
    public Piece movePiece(final Move move) {
        return new King(move.getMovedPiece().getPieceAlliance(), move.getDestinationCoordinate(), false);
    }

    @Override
    public String toString() {
        return PieceType.KING.toString();
    }
}
