package uttt;

import java.util.ArrayList;

public class AI {

    private enum Position { WINNING, LOSING };
    private static final int MAX_DEPTH = 5;

    private int moveRow, moveCol;
    private int dimension;
    private LocalGrid.Symbol player;

    public AI(int dimension, LocalGrid.Symbol symbol) {
        this.dimension = dimension;
        this.player = symbol;
    }

    public int getMoveRow() {
        return moveRow;
    }

    public int getMoveCol() {
        return moveCol;
    }

    public void setMoveRow(int row) {
        moveRow = row;
    }

    public void setMoveCol(int col) {
        moveCol = col;
    }

    public void computeMove(GlobalGrid board) {
        findBestMove(board, 0);
    }

    private void computeAllPossibleMoves(GlobalGrid board, ArrayList<Integer> moveRows, ArrayList<Integer> moveCols) {
        if (board.getCurLocalGrid() == null) {
            board.setCurLocalGridRandomly();
        }
        for (int row = 0; row < dimension; ++row) {
            for (int col = 0; col < dimension; ++col) {
                if (board.getCurLocalGrid().getLocalGrid()[row][col] == null) {
                    moveRows.add(row);
                    moveCols.add(col);
                }
            }
        }
    }

    private int findBestMove(GlobalGrid board, int depth) {
        if (board.isGameOver() || depth >= MAX_DEPTH) {
            return evaluatePosition(board.getPrevLocalGrid(), player);
        }
        if (board.getCurPlayer() == player) {
            return getMax(board, board.getCurPlayer(), depth);
        }
        else {
            return getMin(board, board.getCurPlayer(), depth);
        }
    }

    private int getMax(GlobalGrid board, LocalGrid.Symbol player, int depth) {
        ArrayList<Integer> moveRows = new ArrayList<>();
        ArrayList<Integer> moveCols = new ArrayList<>();
        int bestMoveRating = Integer.MIN_VALUE;

        computeAllPossibleMoves(board, moveRows, moveCols);

        for (int i = 0; i < moveRows.size() && i < moveCols.size(); ++i) {
            GlobalGrid modifiedBoard = new GlobalGrid(board);
            try {
                modifiedBoard.makeMove(moveRows.get(i), moveCols.get(i));
                int moveRating = findBestMove(modifiedBoard, depth + 1);

                if (moveRating >= bestMoveRating) {
                    bestMoveRating = moveRating;
                    this.moveRow = moveRows.get(i);
                    this.moveCol = moveCols.get(i);
                }
            } catch (AlreadyOccupiedException e) { }
        }

        return bestMoveRating;
    }

    private int getMin(GlobalGrid board, LocalGrid.Symbol player, int depth) {
        ArrayList<Integer> moveRows = new ArrayList<>();
        ArrayList<Integer> moveCols = new ArrayList<>();
        int bestMoveRating = Integer.MAX_VALUE;

        computeAllPossibleMoves(board, moveRows, moveCols);

        for (int i = 0; i < moveRows.size() && i < moveCols.size(); ++i) {
            GlobalGrid modifiedBoard = new GlobalGrid(board);
            try {
                modifiedBoard.makeMove(moveRows.get(i), moveCols.get(i));
                int moveRating = findBestMove(modifiedBoard, depth + 1);

                if (moveRating <= bestMoveRating) {
                    bestMoveRating = moveRating;
                    this.moveRow = moveRows.get(i);
                    this.moveCol = moveCols.get(i);
                }
            } catch (AlreadyOccupiedException e) { }
        }

        return bestMoveRating;
    }

    private int evaluatePosition(LocalGrid localGrid, LocalGrid.Symbol curPlayer) {
        int rating = 0;

        for (int row = 0; row < dimension; ++row) { rating += evaluateRow(localGrid.getLocalGrid()[row], curPlayer); }
        for (int col = 0; col < dimension; ++col) { rating += evaluateCol(localGrid.getLocalGrid(), col, curPlayer); }
        rating += evaluateDiagonals(localGrid.getLocalGrid(), curPlayer);

        return rating;
    }

    private int evaluate(int numCrosses, int numNoughts, int numEmpty, LocalGrid.Symbol curPlayer) {
        int rating = 0;
        if (numCrosses == 1 && numEmpty == 2) { rating += 1;   }
        if (numCrosses == 2 && numEmpty == 1) { rating += 10;  }
        if (numCrosses == 3 && numEmpty == 0) { rating += 100; }

        if (numNoughts == 1 && numEmpty == 2) { rating -= 1;   }
        if (numNoughts == 2 && numEmpty == 1) { rating -= 10;  }
        if (numNoughts == 3 && numEmpty == 0) { rating -= 100; }

        return (curPlayer == LocalGrid.Symbol.CROSS) ? rating : (rating * -1);
    }

    private int evaluateRow(LocalGrid.Symbol[] row, LocalGrid.Symbol curPlayer) {
        int numCrosses = 0;
        int numNoughts = 0;
        int numEmpty = 0;

        for (int col = 0; col < dimension; ++col) {
            if (row[col] == LocalGrid.Symbol.CROSS)  { ++numCrosses; }
            if (row[col] == LocalGrid.Symbol.NOUGHT) { ++numNoughts; }
            if (row[col] == null) { ++numEmpty; }
        }

        return evaluate(numCrosses, numNoughts, numEmpty, curPlayer);
    }

    private int evaluateCol(LocalGrid.Symbol[][] localGrid, int col, LocalGrid.Symbol curPlayer) {
        int numCrosses = 0;
        int numNoughts = 0;
        int numEmpty = 0;

        for (int row = 0; row < dimension; ++row) {
            if (localGrid[row][col] == LocalGrid.Symbol.CROSS)  { ++numCrosses; }
            if (localGrid[row][col] == LocalGrid.Symbol.NOUGHT) { ++numNoughts; }
            if (localGrid[row][col] == null) { ++numEmpty; }
        }

        return evaluate(numCrosses, numNoughts, numEmpty, curPlayer);
    }

    private int evaluateDiagonals(LocalGrid.Symbol[][] localGrid, LocalGrid.Symbol symbol) {
        int numCrosses = 0;
        int numNoughts = 0;
        int numEmpty = 0;

        // First Diagonal
        for (int row = 0, col = 0; row < dimension && col < dimension; ++row, ++col) {
            if (localGrid[row][col] == LocalGrid.Symbol.CROSS)  { ++numCrosses; }
            if (localGrid[row][col] == LocalGrid.Symbol.NOUGHT) { ++numNoughts; }
            if (localGrid[row][col] == null) { ++numEmpty; }
        }

        int rating = evaluate(numCrosses, numNoughts, numEmpty, symbol);

        numCrosses = 0;
        numNoughts = 0;
        numEmpty = 0;

        // Second diagonal
        for (int row = 0, col = dimension - 1; row < dimension && col >= 0; ++row, --col) {
            if (localGrid[row][col] == LocalGrid.Symbol.CROSS)  { ++numCrosses; }
            if (localGrid[row][col] == LocalGrid.Symbol.NOUGHT) { ++numNoughts; }
            if (localGrid[row][col] == null) { ++numEmpty; }
        }

        return rating + evaluate(numCrosses, numNoughts, numEmpty, symbol);
    }

}
