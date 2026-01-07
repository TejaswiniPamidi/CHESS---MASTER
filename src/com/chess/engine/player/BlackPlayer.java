package com.chess.engine.player;

import com.chess.engine.Alliance;
import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.board.Tile;
import com.chess.engine.pieces.King;
import com.chess.engine.pieces.Piece;
import com.chess.engine.pieces.Rook;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BlackPlayer extends Player {

    public BlackPlayer(final Board board,
                       final Collection<Move> playerLegalMoves,
                       final Collection<Move> opponentLegalMoves) {
        super(board, playerLegalMoves, opponentLegalMoves);
    }

    @Override
    public Collection<Piece> getActivePieces() {
        return this.board.getBlackPieces();
    }

    @Override
    public Alliance getAlliance() {
        return Alliance.BLACK;
    }

    @Override
    public Player getOpponent() {
        return this.board.whitePlayer();
    }

    /**
     * Castling logic for black. Similar to white but positions differ (king at 4, rooks at 0 and 7 in your setup).
     */
    @Override
    protected Collection<Move> calculateKingCastles(final Collection<Move> playerLegals,
                                                    final Collection<Move> opponentLegals) {
        final List<Move> kingCastles = new ArrayList<>();

        if (!this.playerKing.isFirstMove()) {
            return kingCastles;
        }

        final int kingPos = this.playerKing.getPiecePosition();

        // King-side: rook at kingPos + 3
        final Tile kingSideRookTile = this.board.getTile(kingPos + 3);
        if (kingSideRookTile != null && kingSideRookTile.isTileOccupied()) {
            final Piece rook = kingSideRookTile.getPiece();
            if (rook.getPieceType().isRook() && rook.isFirstMove()) {
                final Tile t1 = this.board.getTile(kingPos + 1);
                final Tile t2 = this.board.getTile(kingPos + 2);
                if (!t1.isTileOccupied() && !t2.isTileOccupied()) {
                    boolean tilesSafe = Player.calculateAttacksOnTile(kingPos + 1, opponentLegals).isEmpty()
                            && Player.calculateAttacksOnTile(kingPos + 2, opponentLegals).isEmpty();
                    if (tilesSafe) {
                        kingCastles.add(new Move.KingSideCastleMove(this.board,
                                this.playerKing, kingPos + 2,
                                (Rook) rook, rook.getPiecePosition(), kingPos + 1));
                    }
                }
            }
        }

        // Queen-side: rook at kingPos - 4
        final Tile queenSideRookTile = this.board.getTile(kingPos - 4);
        if (queenSideRookTile != null && queenSideRookTile.isTileOccupied()) {
            final Piece rook = queenSideRookTile.getPiece();
            if (rook.getPieceType().isRook() && rook.isFirstMove()) {
                final Tile t1 = this.board.getTile(kingPos - 1);
                final Tile t2 = this.board.getTile(kingPos - 2);
                final Tile t3 = this.board.getTile(kingPos - 3);
                if (!t1.isTileOccupied() && !t2.isTileOccupied() && !t3.isTileOccupied()) {
                    boolean tilesSafe = Player.calculateAttacksOnTile(kingPos - 1, opponentLegals).isEmpty()
                            && Player.calculateAttacksOnTile(kingPos - 2, opponentLegals).isEmpty();
                    if (tilesSafe) {
                        kingCastles.add(new Move.QueenSideCastleMove(this.board,
                                this.playerKing, kingPos - 2,
                                (Rook) rook, rook.getPiecePosition(), kingPos - 1));
                    }
                }
            }
        }

        return kingCastles;
    }
}
