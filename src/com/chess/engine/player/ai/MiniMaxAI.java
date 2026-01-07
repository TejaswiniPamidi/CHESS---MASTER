package com.chess.engine.player.ai;

import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.player.MoveTransition;
import com.chess.engine.player.Player;

public class MiniMaxAI {

    private final int searchDepth;

    public MiniMaxAI(final int searchDepth) {
        this.searchDepth = searchDepth;
    }

    public Move execute(final Board board) {
        Move bestMove = null;
        int highestSeenValue = Integer.MIN_VALUE;

        for (final Move move : board.currentPlayer().getLegalMoves()) {
            final MoveTransition transition = board.currentPlayer().makeMove(move);
            if (transition.getMoveStatus().isDone()) {
                int captureBonus = 0;
                if (move.isAttack()) {
                    captureBonus = getPieceValue(move.getAttackedPiece().getPieceType().name()) / 2;
                }

                final int currentValue = min(transition.getTransitionBoard(),
                        this.searchDepth - 1,
                        Integer.MIN_VALUE,
                        Integer.MAX_VALUE) + captureBonus;

                if (currentValue > highestSeenValue) {
                    highestSeenValue = currentValue;
                    bestMove = move;
                }
            }
        }
        return bestMove;
    }

    private int max(final Board board, final int depth, int alpha, int beta) {
        if (depth == 0 || isEndGame(board)) {
            return evaluateBoard(board);
        }

        int highestValue = Integer.MIN_VALUE;
        for (final Move move : board.currentPlayer().getLegalMoves()) {
            final MoveTransition transition = board.currentPlayer().makeMove(move);
            if (transition.getMoveStatus().isDone()) {
                final int currentValue = min(transition.getTransitionBoard(), depth - 1, alpha, beta);
                highestValue = Math.max(highestValue, currentValue);
                alpha = Math.max(alpha, highestValue);
                if (beta <= alpha) break;
            }
        }
        return highestValue;
    }

    private int min(final Board board, final int depth, int alpha, int beta) {
        if (depth == 0 || isEndGame(board)) {
            return evaluateBoard(board);
        }

        int lowestValue = Integer.MAX_VALUE;
        for (final Move move : board.currentPlayer().getLegalMoves()) {
            final MoveTransition transition = board.currentPlayer().makeMove(move);
            if (transition.getMoveStatus().isDone()) {
                final int currentValue = max(transition.getTransitionBoard(), depth - 1, alpha, beta);
                lowestValue = Math.min(lowestValue, currentValue);
                beta = Math.min(beta, lowestValue);
                if (beta <= alpha) break;
            }
        }
        return lowestValue;
    }

    private boolean isEndGame(final Board board) {
        return board.currentPlayer().isInCheckMate() || board.currentPlayer().isInStaleMate();
    }

    private int evaluateBoard(final Board board) {
        return scorePlayer(board.whitePlayer()) - scorePlayer(board.blackPlayer());
    }

    private int scorePlayer(final Player player) {
        int score = 0;

        // Base material score
        for (final com.chess.engine.pieces.Piece piece : player.getActivePieces()) {
            score += getPieceValue(piece.getPieceType().name());
        }

        // Mobility
        score += player.getLegalMoves().size() * 5;

        // Check/Checkmate penalties
        if (player.isInCheck()) score -= 50;
        if (player.isInCheckMate()) score -= 10000;

        return score;
    }

    private int getPieceValue(String pieceType) {
        switch (pieceType) {
            case "PAWN": return 100;
            case "KNIGHT": return 320;
            case "BISHOP": return 330;
            case "ROOK": return 500;
            case "QUEEN": return 900;
            case "KING": return 20000;
            default: return 0;
        }
    }
}
