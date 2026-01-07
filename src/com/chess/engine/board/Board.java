package com.chess.engine.board;

import com.chess.engine.Alliance;
import com.chess.engine.pieces.*;
import com.chess.engine.player.BlackPlayer;
import com.chess.engine.player.Player;
import com.chess.engine.player.WhitePlayer;

import java.util.*;

public class Board {

    private final List<Tile> gameBoard;
    private final Collection<Piece> whitePieces;
    private final Collection<Piece> blackPieces;
    private final WhitePlayer whitePlayer;
    private final BlackPlayer blackPlayer;
    private final Player currentPlayer;
    private final Pawn enPassantPawn;

    private Board(Builder builder) {
        this.gameBoard = createGameBoard(builder);
        this.whitePieces = calculateActivePieces(gameBoard, Alliance.WHITE);
        this.blackPieces = calculateActivePieces(gameBoard, Alliance.BLACK);

        final Collection<Move> whiteLegalMoves = calculateLegalMoves(this.whitePieces);
        final Collection<Move> blackLegalMoves = calculateLegalMoves(this.blackPieces);

        // IMPORTANT: pass the correct move lists to each player (playerMoves, opponentMoves)
        this.whitePlayer = new WhitePlayer(this, whiteLegalMoves, blackLegalMoves);
        this.blackPlayer = new BlackPlayer(this, blackLegalMoves, whiteLegalMoves);

        this.currentPlayer = builder.nextMoveMaker.choosePlayer(whitePlayer, blackPlayer);
        this.enPassantPawn = builder.enPassantPawn;
    }

    public Tile getTile(final int coordinate) {
        return gameBoard.get(coordinate);
    }

    public Collection<Piece> getWhitePieces() { return whitePieces; }
    public Collection<Piece> getBlackPieces() { return blackPieces; }
    public Player whitePlayer() { return whitePlayer; }
    public Player blackPlayer() { return blackPlayer; }
    public Player currentPlayer() { return currentPlayer; }
    public Pawn getEnPassantPawn() { return enPassantPawn; }

    private static List<Tile> createGameBoard(final Builder builder) {
        final Tile[] tiles = new Tile[BoardUtils.NUM_TILES];
        for (int i = 0; i < BoardUtils.NUM_TILES; i++) {
            tiles[i] = Tile.createTile(i, builder.boardConfig.get(i));
        }
        return Collections.unmodifiableList(Arrays.asList(tiles));
    }

    private static Collection<Piece> calculateActivePieces(final List<Tile> gameBoard, final Alliance alliance) {
        final List<Piece> activePieces = new ArrayList<>();
        for (Tile tile : gameBoard) {
            if (tile.isTileOccupied()) {
                Piece piece = tile.getPiece();
                if (piece.getPieceAlliance() == alliance) {
                    activePieces.add(piece);
                }
            }
        }
        return Collections.unmodifiableList(activePieces);
    }

    private Collection<Move> calculateLegalMoves(final Collection<Piece> pieces) {
        final List<Move> legalMoves = new ArrayList<>();
        for (Piece piece : pieces) {
            legalMoves.addAll(piece.calculateLegalMoves(this));
        }
        return Collections.unmodifiableList(legalMoves);
    }

    public Iterable<Move> getAllLegalMoves() {
        List<Move> allMoves = new ArrayList<>();
        allMoves.addAll(this.whitePlayer.getLegalMoves());
        allMoves.addAll(this.blackPlayer.getLegalMoves());
        return Collections.unmodifiableList(allMoves);
    }

    /**
     * Is the given alliance's king in check?
     */
    public boolean isKingInCheck(final Alliance alliance) {
        final Player player = alliance.isWhite() ? whitePlayer : blackPlayer;
        final King king = player.getPlayerKing();
        if (king == null) return false; // defensive
        final Collection<Move> opponentMoves = player.getOpponent().getLegalMoves();
        for (final Move move : opponentMoves) {
            if (move.getDestinationCoordinate() == king.getPiecePosition()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Is the given alliance checkmated?
     */
    public boolean isCheckMate(final Alliance alliance) {
        final Player player = alliance.isWhite() ? whitePlayer : blackPlayer;
        // if not in check, cannot be checkmate
        if (!player.isInCheck()) return false;
        // if any legal move avoids check, not mate
        for (final Move move : player.getLegalMoves()) {
            final Board transitioned = move.execute();
            if (!transitioned.isKingInCheck(alliance)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Human-friendly status message; call after a move to display status.
     */
    public String gameStatusMessage() {
        // check opponent: after a move, board.currentPlayer() is the player to move
        // we will check current player (the one to move) whether they are in check/mate
        final Alliance current = this.currentPlayer.getAlliance();
        if (isCheckMate(current)) {
            // winner is opponent
            return "Checkmate! Winner: " + this.currentPlayer.getOpponent().getAlliance();
        }
        if (isKingInCheck(current)) {
            return "Check!";
        }
        return "OK";
    }

    public static Board createStandardBoard() {
        Builder builder = new Builder();

        // Black pieces
        builder.setPiece(new Rook(Alliance.BLACK, 0));
        builder.setPiece(new Knight(Alliance.BLACK, 1));
        builder.setPiece(new Bishop(Alliance.BLACK, 2));
        builder.setPiece(new Queen(Alliance.BLACK, 3));
        builder.setPiece(new King(Alliance.BLACK, 4));
        builder.setPiece(new Bishop(Alliance.BLACK, 5));
        builder.setPiece(new Knight(Alliance.BLACK, 6));
        builder.setPiece(new Rook(Alliance.BLACK, 7));
        for (int i = 8; i <= 15; i++) builder.setPiece(new Pawn(Alliance.BLACK, i));

        // White pieces
        for (int i = 48; i <= 55; i++) builder.setPiece(new Pawn(Alliance.WHITE, i));
        builder.setPiece(new Rook(Alliance.WHITE, 56));
        builder.setPiece(new Knight(Alliance.WHITE, 57));
        builder.setPiece(new Bishop(Alliance.WHITE, 58));
        builder.setPiece(new Queen(Alliance.WHITE, 59));
        builder.setPiece(new King(Alliance.WHITE, 60));
        builder.setPiece(new Bishop(Alliance.WHITE, 61));
        builder.setPiece(new Knight(Alliance.WHITE, 62));
        builder.setPiece(new Rook(Alliance.WHITE, 63));

        builder.setMoveMaker(Alliance.WHITE);
        return builder.build();
    }

    public static class Builder {
        Map<Integer, Piece> boardConfig = new HashMap<>();
        Alliance nextMoveMaker;
        Pawn enPassantPawn;

        public Builder setPiece(Piece piece) { this.boardConfig.put(piece.getPiecePosition(), piece); return this; }
        public Builder setMoveMaker(Alliance alliance) { this.nextMoveMaker = alliance; return this; }
        public Builder setEnPassantPawn(Pawn pawn) { this.enPassantPawn = pawn; return this; }
        public Board build() { return new Board(this); }
    }
}
