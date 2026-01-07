package com.chess.gui;

import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.board.Tile;
import com.chess.engine.pieces.Piece;
import com.chess.engine.player.Player;
import com.chess.engine.player.ai.MiniMaxAI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Stack;

public class Table {

    private boolean playWithAI = false;
    private final JFrame gameFrame;
    private BoardPanel boardPanel;
    private Board chessBoard;
    private Stack<Board> boardHistory = new Stack<>();

    private static final int TILE_SIZE = 80;
    private static final int BOARD_SIZE = 8;

    private Tile sourceTile = null;
    private Collection<Move> legalMovesForPiece = null;
    private boolean gameOver = false;

    public Table() {
        this.chessBoard = Board.createStandardBoard();
        this.gameFrame = new JFrame("Chess Game");
        this.gameFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.gameFrame.setSize(BOARD_SIZE * TILE_SIZE, BOARD_SIZE * TILE_SIZE + 120);
        this.gameFrame.setResizable(false);
        this.gameFrame.setLocationRelativeTo(null);

        showMainMenu();
        this.gameFrame.setVisible(true);
    }

    // === MAIN MENU ===
    private void showMainMenu() {
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new GridBagLayout());
        menuPanel.setBackground(new Color(60, 179, 113)); // green background

        JButton playerVsPlayerButton = new JButton("Player vs Player");
        JButton playerVsAIButton = new JButton("Player vs Computer");

        styleButton(playerVsPlayerButton);
        styleButton(playerVsAIButton);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.gridx = 0;
        gbc.gridy = 0;
        menuPanel.add(playerVsPlayerButton, gbc);

        gbc.gridy = 1;
        menuPanel.add(playerVsAIButton, gbc);

        gameFrame.setContentPane(menuPanel);
        gameFrame.revalidate();

        playerVsPlayerButton.addActionListener(e -> startGame(false));
        playerVsAIButton.addActionListener(e -> startGame(true));
    }

    private void styleButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 24));
        button.setBackground(Color.WHITE);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(250, 60));
    }

    // === START GAME ===
    private void startGame(boolean vsAI) {
        this.playWithAI = vsAI;
        this.chessBoard = Board.createStandardBoard();
        this.sourceTile = null;
        this.legalMovesForPiece = null;
        this.gameOver = false;
        boardHistory.clear();

        // Parent panel contains top buttons + board
        JPanel parentPanel = new JPanel(new BorderLayout());

        // Top panel with Back and Undo
        JPanel topPanel = new JPanel();
        JButton backButton = new JButton("Back to Menu");
        backButton.setFont(new Font("Arial", Font.BOLD, 18));
        backButton.setBackground(Color.LIGHT_GRAY);
        backButton.setFocusPainted(false);
        backButton.addActionListener(e -> showMainMenu());

        JButton undoButton = new JButton("Undo");
        undoButton.setFont(new Font("Arial", Font.BOLD, 18));
        undoButton.setBackground(Color.LIGHT_GRAY);
        undoButton.setFocusPainted(false);
        undoButton.addActionListener(e -> undoMove());

        topPanel.add(backButton);
        topPanel.add(undoButton);
        parentPanel.add(topPanel, BorderLayout.NORTH);

        // Board panel
        boardPanel = new BoardPanel();
        parentPanel.add(boardPanel, BorderLayout.CENTER);

        gameFrame.setContentPane(parentPanel);
        gameFrame.revalidate();
    }

    // === BOARD PANEL ===
    private class BoardPanel extends JPanel {
        BoardPanel() {
            super(new GridLayout(BOARD_SIZE, BOARD_SIZE));
            drawBoard();
        }

        void drawBoard() {
            this.removeAll();
            for (int i = 0; i < BOARD_SIZE * BOARD_SIZE; i++) {
                TilePanel tilePanel = new TilePanel(i);
                add(tilePanel);
            }
            validate();
            repaint();
        }
    }

    // === TILE PANEL ===
    private class TilePanel extends JPanel {
        private final int tileId;

        TilePanel(int tileId) {
            super(new GridBagLayout());
            this.tileId = tileId;
            setPreferredSize(new Dimension(TILE_SIZE, TILE_SIZE));
            assignTileColor();
            assignTilePieceIcon();

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    handleClick(TilePanel.this);
                }
            });
        }

        private void assignTileColor() {
            final boolean isLightSquare = ((tileId / BOARD_SIZE) + (tileId % BOARD_SIZE)) % 2 == 0;
            final Color base = isLightSquare ? Color.decode("#F0D9B5") : Color.decode("#B58863");
            setBackground(base);

            if (sourceTile != null && sourceTile.getTileCoordinate() == this.tileId) {
                setBackground(Color.YELLOW);
            }

            if (legalMovesForPiece != null) {
                for (Move move : legalMovesForPiece) {
                    if (move.getDestinationCoordinate() == this.tileId) {
                        setBackground(Color.GREEN);
                        break;
                    }
                }
            }
        }

        private void assignTilePieceIcon() {
            this.removeAll();
            final Tile tile = chessBoard.getTile(tileId);
            if (tile.isTileOccupied()) {
                final Piece piece = tile.getPiece();
                final String pieceName = piece.getPieceType().name().toLowerCase();
                final String alliance = piece.getPieceAlliance().isWhite() ? "white" : "black";
                final String resourcePath = "/images/" + alliance + "_" + pieceName + ".png";

                ImageIcon icon = null;
                URL imageUrl = Table.class.getResource(resourcePath);
                if (imageUrl != null) {
                    icon = new ImageIcon(imageUrl);
                } else {
                    java.io.File f = new java.io.File("resources/images/" + alliance + "_" + pieceName + ".png");
                    if (f.exists()) {
                        icon = new ImageIcon(f.getAbsolutePath());
                    }
                }

                if (icon != null) {
                    final Image scaled = icon.getImage().getScaledInstance(TILE_SIZE - 10, TILE_SIZE - 10, Image.SCALE_SMOOTH);
                    add(new JLabel(new ImageIcon(scaled)));
                } else {
                    JLabel pieceLabel = new JLabel(piece.getPieceType().name().substring(0, 1).toUpperCase());
                    pieceLabel.setFont(new Font("Arial", Font.BOLD, 32));
                    add(pieceLabel);
                }
            }
        }
    }

    // === CLICK HANDLER ===
    private void handleClick(TilePanel clickedTilePanel) {
        if (gameOver) return;

        final Tile clickedTile = chessBoard.getTile(clickedTilePanel.tileId);

        if (sourceTile == null) {
            if (clickedTile.isTileOccupied() &&
                    clickedTile.getPiece().getPieceAlliance() == chessBoard.currentPlayer().getAlliance()) {
                sourceTile = clickedTile;
                legalMovesForPiece = filterLegalMoves(clickedTile);
            }
        } else {
            Move chosenMove = null;
            if (legalMovesForPiece != null) {
                for (Move m : legalMovesForPiece) {
                    if (m.getDestinationCoordinate() == clickedTile.getTileCoordinate()) {
                        chosenMove = m;
                        break;
                    }
                }
            }

            if (chosenMove == null && clickedTile.isTileOccupied() &&
                    clickedTile.getPiece().getPieceAlliance() == chessBoard.currentPlayer().getAlliance()) {
                sourceTile = clickedTile;
                legalMovesForPiece = filterLegalMoves(clickedTile);
                boardPanel.drawBoard();
                return;
            }

            if (chosenMove != null && chosenMove != Move.NULL_MOVE) {
                boardHistory.push(chessBoard); // store current board
                chessBoard = chosenMove.execute();
                updateGameBoard();

                // === AI MOVE ===
                if (playWithAI && !gameOver && chessBoard.currentPlayer().getAlliance().isBlack()) {
                    boardHistory.push(chessBoard); // store before AI move
                    MiniMaxAI ai = new MiniMaxAI(3);
                    Move aiMove = ai.execute(chessBoard);
                    if (aiMove != null) {
                        chessBoard = aiMove.execute();
                        updateGameBoard();
                    }
                }
            }

            sourceTile = null;
            legalMovesForPiece = null;
        }

        boardPanel.drawBoard();
    }

    private Collection<Move> filterLegalMoves(Tile clickedTile) {
        Collection<Move> filtered = new ArrayList<>();
        for (Move m : chessBoard.currentPlayer().getLegalMoves()) {
            if (m.getMovedPiece().equals(clickedTile.getPiece())) {
                final Board movedBoard = m.execute();
                if (!movedBoard.currentPlayer().getOpponent().isInCheck()) {
                    filtered.add(m);
                }
            }
        }
        return filtered;
    }

    // === UNDO MOVE ===
    private void undoMove() {
        if (!boardHistory.isEmpty()) {
            chessBoard = boardHistory.pop();
            sourceTile = null;
            legalMovesForPiece = null;
            gameOver = false;
            boardPanel.drawBoard();
        } else {
            JOptionPane.showMessageDialog(gameFrame,
                    "No more moves to undo!",
                    "Undo",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void updateGameBoard() {
        Player currentPlayer = chessBoard.currentPlayer();

        if (currentPlayer.isInCheckMate()) {
            String winner = currentPlayer.getOpponent().getAlliance().toString();
            JOptionPane.showMessageDialog(gameFrame,
                    "Checkmate! " + winner + " wins!",
                    "Game Over",
                    JOptionPane.INFORMATION_MESSAGE);
            gameOver = true;
            return;
        }

        if (currentPlayer.isInStalemate()) {
            JOptionPane.showMessageDialog(gameFrame,
                    "Stalemate! It's a draw.",
                    "Game Over",
                    JOptionPane.INFORMATION_MESSAGE);
            gameOver = true;
            return;
        }

        if (currentPlayer.isInCheck()) {
            JOptionPane.showMessageDialog(gameFrame,
                    "Check! " + currentPlayer.getAlliance() + " king is under attack.",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Table::new);
    }
}
