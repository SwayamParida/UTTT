package uttt;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;

import java.util.Scanner;

public class Controller {

    public static final int DIMENSION = 3;
    public static final String CROSS_IMAGE = "cross.png";
    public static final String NOUGHT_IMAGE = "nought.png";

    private enum Player { HUMAN, AI };
    private AI computerPlayer;
    private Player curPlayer;
    private boolean twoPlayerMode;
    private GlobalGrid globalGrid;
    @FXML private GridPane board;

    public Controller() {
        globalGrid = new GlobalGrid(DIMENSION, LocalGrid.Symbol.CROSS);
        curPlayer = Player.HUMAN;
    }

    @FXML private void initialize() {
        Scanner inputScanner = new Scanner(System.in);
        System.out.print("Two player mode? (y/n): ");
        char input = inputScanner.next().toLowerCase().charAt(0);
        twoPlayerMode = (input == 'y');

        if (!twoPlayerMode) { computerPlayer = new AI(DIMENSION, LocalGrid.Symbol.NOUGHT); }
    }

    @FXML private void mouseClicked (MouseEvent event) {
        if (!globalGrid.isGameOver()) {
            // Allow human player to make move
            if (curPlayer == Player.HUMAN) {
                humanPlayerMove(event);
                curPlayer = (twoPlayerMode) ? Player.HUMAN : Player.AI;
            }

            // Let AI make a move if one player mode is enabled
            if (curPlayer == Player.AI) {
                computerPlayerMove(false);
                curPlayer = Player.HUMAN;
            }
        }

        // Display a message when the game is over
        if (globalGrid.isGameOver()) {
            clearAllHighlightedPanes();
            String winnerMessage = new String();
            switch ((globalGrid.winner())) {
                case CROSS:
                    winnerMessage = "X wins";
                    break;
                case NOUGHT:
                    winnerMessage = "O wins";
                    break;
                default:
                    winnerMessage = "Cats game";
            }
            System.out.println(winnerMessage);
        }
    }

    private void computerPlayerMove(boolean shuffleNeeded) {
        if (!shuffleNeeded) {
            computerPlayer.computeMove(new GlobalGrid(globalGrid));
        } else {
            computerPlayer.setMoveRow((int)(Math.random() * DIMENSION));
            computerPlayer.setMoveCol((int)(Math.random() * DIMENSION));
        }

        int globalRow = globalGrid.getCurGlobalRow();
        int globalCol = globalGrid.getCurGlobalCol();
        Pane localGrid = findPane(globalRow, globalCol);

        int localRow = computerPlayer.getMoveRow();
        int localCol = computerPlayer.getMoveCol();
        Button cell = new Button();

        System.out.println("Computer move: ");
        System.out.println("Global grid: " + globalGrid.getCurGlobalRow() + ", " + globalGrid.getCurGlobalCol());
        System.out.println( "Local grid: " + localRow + ", " + localCol);

        try {
            cell = findCell(localGrid, localRow, localCol);
        } catch (IndexOutOfBoundsException e) { }

        try {
            makeMove(localRow, localCol, cell, globalRow, globalCol, localGrid);
        } catch (AlreadyOccupiedException e) {
            computerPlayerMove(true);
        } catch (NullPointerException e) {
            globalGrid.setCurLocalGridRandomly();
            computerPlayerMove(false);
        }
    }

    private void humanPlayerMove(MouseEvent event) {
        // Retrieve current game state
        Button cell = (Button) event.getSource();
        int localRow = GridPane.getRowIndex(cell);
        int localCol = GridPane.getColumnIndex(cell);

        Pane localGrid = (Pane) cell.getParent().getParent();
        int globalRow = GridPane.getRowIndex(localGrid);
        int globalCol = GridPane.getColumnIndex(localGrid);

        // When no local grid has been determined by previous move, the user may choose any local grid to make his move in
        // This condition is true for the first move of the game or when the previous move landed in a grid that has already been won or tied
        if (globalGrid.getCurLocalGrid() == null) {
            globalGrid.setCurLocalGrid(globalRow, globalCol);
        }

        // Validating that the local grid where the current player made his move is the same as the one determined by the model
        if (globalGrid.getCurLocalGrid().equals(globalGrid.getLocalGrid(globalRow, globalCol))) {
            try {
                makeMove(localRow, localCol, cell, globalRow, globalCol, localGrid);
            } catch (AlreadyOccupiedException e) { }
        } else {
            // Display an error message when move is made in a local grid that is not the one determined by the model
            System.out.println("Invalid move");
        }
    }

    private void makeMove(int localRow, int localCol, Button cell, int globalRow, int globalCol, Pane localGrid)
            throws AlreadyOccupiedException  {
        LocalGrid.Symbol curPlayer = globalGrid.getCurPlayer();

        // Make move in the data model
        globalGrid.makeMove(localRow, localCol);

        // Make move graphically
        clearAllHighlightedPanes();
        updateDisplay(cell, curPlayer);

        // When the current move results in a local game being won, update the display to replace the local grid with a mega symbol
        LocalGrid.Symbol localWinner = globalGrid.getLocalGrid(globalRow, globalCol).winner();
        if (localWinner != null)  {
            clearAllHighlightedPanes();
            updateDisplay(localGrid, localWinner);
        }

        // Highlight the local grid where the next player needs to make a move
        try {
            if (!globalGrid.getCurLocalGrid().isLocalGameOver()) {
                Pane nextLocalGrid = findPane(localRow, localCol);
                Color translucentGreen = Color.rgb(0, 255, 0, 0.25);
                highlightLocalGrid(nextLocalGrid, translucentGreen);
            }
        } catch (NullPointerException e) { }
    }

    private void setImage(ImageView imageView, LocalGrid.Symbol symbol) {
        switch (symbol) {
            case NOUGHT:
                imageView.setImage(new Image(NOUGHT_IMAGE));
                break;
            case CROSS:
                imageView.setImage(new Image(CROSS_IMAGE));
                break;
        }
    }

    private void updateDisplay(Button cell, LocalGrid.Symbol symbol) {
        ImageView imageView = new ImageView();
        imageView.setFitWidth(cell.getHeight() - 10);
        imageView.setFitHeight(cell.getWidth() - 10);
        setImage(imageView, symbol);
        cell.setGraphic(imageView);
    }

    private void updateDisplay(Pane localGrid, LocalGrid.Symbol symbol) {
        localGrid.getChildren().removeAll(localGrid.getChildren());
        ImageView imageView = new ImageView();
        imageView.setFitHeight(localGrid.getHeight());
        imageView.setFitWidth(localGrid.getWidth());
        setImage(imageView, symbol);
        localGrid.getChildren().add(imageView);
    }

    private void highlightLocalGrid(Pane localGrid, Color color) {
        localGrid.setBackground(new Background(new BackgroundFill(color, new CornerRadii(10), new Insets(10))));
    }

    private void clearAllHighlightedPanes() {
        for (Node node : board.getChildren()) {
            Pane pane = (Pane) node;
            pane.setBackground(Background.EMPTY);
        }
    }

    private Pane findPane(int row, int col) {
        return (Pane) board.getChildren().get((DIMENSION * row) + col);
    }

    private Button findCell(Pane localGrid, int row, int col) {
        return (Button)((GridPane) (localGrid.getChildren().get(4))).getChildren().get((DIMENSION * row) + col);
    }
}
