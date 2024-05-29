package com.game.app;

import java.util.ArrayList;
import java.util.List;

import com.game.app.enums.MoveType;
import com.game.app.enums.PieceType;

import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;

public class Board {

    public static final int TILE_SIZE = 100;
    public static final int WIDTH = 8;
    public static final int HEIGHT = 8;
    private static Board instance;

    private Tile[][] board;
    private Group tileGroup;
    private Group pieceGroup;
    private List<Piece> pieces;
    private Scene scene;
    private CheckersApp checkersApp;

    public CheckersApp getCheckersApp() {
        return checkersApp;
    }

    public void setCheckersApp(CheckersApp checkersApp) {
        this.checkersApp = checkersApp;
    }

    private Board() {
        board = new Tile[WIDTH][HEIGHT];
        tileGroup = new Group();
        pieceGroup = new Group();
        pieces = new ArrayList<Piece>();
    };

    public static Board getInstance() {
        if (instance == null)
            instance = new Board();
        return instance;
    }

    public static void setInstance(Board newInstance) {
        Scene currentScene = instance.getScene();
        Parent newContent = newInstance.createContentFromPieces(newInstance.getPieces());
        currentScene.setRoot(newContent);
        newInstance.setScene(currentScene);
        instance = newInstance;
    }

    public Parent createContentFromPieces(List<Piece> pieces) {

        Pane root = new Pane();
        pieceGroup = new Group();
        tileGroup = new Group();
        root.setPrefSize(WIDTH * TILE_SIZE, HEIGHT * TILE_SIZE);
        root.getChildren().addAll(tileGroup, pieceGroup);

        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                Tile tile = new Tile((x + y) % 2 == 0, x, y);
                board[x][y] = tile;

                tileGroup.getChildren().add(tile);
            }
        }

        for (Piece piece : pieces) {
            int x = piece.getPoint().getX();
            int y = piece.getPoint().getY();
            Tile tile = board[x][y];
            // Piece newPiece = makePiece(piece.getType(), piece.getPoint().getX(), piece.getPoint().getY());
            tile.setPiece(piece);
            pieceGroup.getChildren().add(piece);
        }

        return root;
    }

    public Parent createContent() {

        Pane root = new Pane();
        root.setPrefSize(WIDTH * TILE_SIZE, HEIGHT * TILE_SIZE);
        root.getChildren().addAll(tileGroup, pieceGroup);

        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                Tile tile = new Tile((x + y) % 2 == 0, x, y);
                board[x][y] = tile;

                tileGroup.getChildren().add(tile);

                Piece piece = null;

                if (y <= 2 && (x + y) % 2 != 0) {
                    piece = makePiece(PieceType.RED, x, y);
                }

                if (y >= 5 && (x + y) % 2 != 0) {
                    piece = makePiece(PieceType.WHITE, x, y);
                }

                if (piece != null) {
                    pieces.add(piece);
                    tile.setPiece(piece);
                    pieceGroup.getChildren().add(piece);
                }
            }
        }

        return root;
    }

    public void printBoard() {
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                Tile tile = board[x][y];
                Piece piece = tile.getPiece();
                if (piece != null) {
                    if (piece.getType().equals(PieceType.RED))
                        System.out.print("R ");
                    else
                        System.out.print("W ");
                } else
                    System.out.print("0 ");
            }
            System.out.println();
        }
    }

    public MoveResult tryMove(Piece piece, int newX, int newY) {
        if (board[newX][newY].hasPiece() || (newX + newY) % 2 == 0) {
            return new MoveResult(MoveType.NONE);
        }

        int x0 = piece.getPoint().getX();
        int y0 = piece.getPoint().getY();

        if (Math.abs(newX - x0) == 1 && newY - y0 == piece.getType().getMoveDir()) {
            return new MoveResult(MoveType.NORMAL);
        } else if (Math.abs(newX - x0) == 2 && newY - y0 == piece.getType().getMoveDir() * 2) {

            int x1 = x0 + (newX - x0) / 2;
            int y1 = y0 + (newY - y0) / 2;

            if (board[x1][y1].hasPiece() && board[x1][y1].getPiece().getType() != piece.getType()) {
                return new MoveResult(MoveType.KILL, board[x1][y1].getPiece());
            }
        }

        return new MoveResult(MoveType.NONE);
    }

    private boolean isValidPoint(int x, int y) {
        return isValidX(x) && isValidY(y);
    }

    private boolean isValidX(int x) {
        return x >= 0 && x < WIDTH;
    }

    private boolean isValidY(int y) {
        return y >= 0 && y < HEIGHT;
    }

    /**
     * <h4>This method will get a list of all possible moves that a piece can move
     * in future</h4>
     * 
     * @param piece
     * @return List
     */

    public List<Point> getAllPossibleMoves(Piece piece) {
        List<Point> result = new ArrayList<Point>();

        int currentX = piece.getPoint().getX();
        int currentY = piece.getPoint().getY();

        int nextY = piece.getType().equals(PieceType.RED) ? currentY + 1 : currentY - 1;
        int[] nextXArray = new int[] { currentX - 1, currentX + 1 };

        for (int nextX : nextXArray) {
            if (isValidPoint(nextX, nextY)) {
                MoveResult moveResult = tryMove(piece, nextX, nextY);
                if (moveResult.isValidMoveType()) {
                    result.add(new Point(nextX, nextY));
                }
            }
        }

        return result;
    }

    public int toBoard(double pixel) {
        return (int) (pixel + TILE_SIZE / 2) / TILE_SIZE;
    }

    private Piece makePiece(PieceType type, int x, int y) {
        Piece piece = new Piece(type, x, y);
        return piece;
    }

    public List<Piece> copyPieces() {
        List<Piece> newPieces = new ArrayList<Piece>();
        for (Piece piece : pieces) {
            Piece newPiece = makePiece(piece.getType(), piece.getPoint().getX(), piece.getPoint().getY());
            newPieces.add(newPiece);
        }
        return newPieces;
    }

    public Board copy() {
        Board board = new Board();
        board.setPieces(this.copyPieces());
        board.createContentFromPieces(board.getPieces());
        return board;
    }

    public static int getTileSize() {
        return TILE_SIZE;
    }

    public static int getWidth() {
        return WIDTH;
    }

    public static int getHeight() {
        return HEIGHT;
    }

    public Tile[][] getBoard() {
        return board;
    }

    public void setBoard(Tile[][] board) {
        this.board = board;
    }

    public Group getTileGroup() {
        return tileGroup;
    }

    public void setTileGroup(Group tileGroup) {
        this.tileGroup = tileGroup;
    }

    public Group getPieceGroup() {
        return pieceGroup;
    }

    public void setPieceGroup(Group pieceGroup) {
        this.pieceGroup = pieceGroup;
    }

    public List<Piece> getPieces() {
        return pieces;
    }

    public void setPieces(List<Piece> pieces) {
        this.pieces = pieces;
    }

    public Scene getScene() {
        return scene;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }
}
