package uttt;

public class GlobalGrid {

    private LocalGrid[][] localGrids;
    private LocalGrid curLocalGrid, prevLocalGrid;
    private int curGlobalRow, curGlobalCol;
    private LocalGrid.Symbol curPlayer;
    private int dimension;
    private int movesMade;

    public GlobalGrid(int dimension, LocalGrid.Symbol player1) {
        this.dimension = dimension;
        curPlayer = player1;
        movesMade = 0;
        localGrids = new LocalGrid[dimension][dimension];
        for (int row = 0; row < dimension; ++row) {
            for (int col = 0; col < dimension; ++col) {
                localGrids[row][col] = new LocalGrid(dimension, player1);
            }
        }
    }

    public GlobalGrid(GlobalGrid globalGrid) {
        dimension = globalGrid.dimension;
        localGrids = new LocalGrid[dimension][dimension];
        for (int row = 0; row < dimension; ++row) {
            for (int col = 0; col < dimension; ++col) {
                localGrids[row][col] = new LocalGrid(globalGrid.localGrids[row][col]);
            }
        }
        curLocalGrid = matchLocalGrid(globalGrid.getCurLocalGrid());
        curGlobalRow = globalGrid.curGlobalRow;
        curGlobalCol = globalGrid.curGlobalCol;
        curPlayer = globalGrid.curPlayer;
        movesMade = globalGrid.movesMade;
        prevLocalGrid = matchLocalGrid(globalGrid.prevLocalGrid);
    }

    private LocalGrid matchLocalGrid(LocalGrid grid) {
        for (LocalGrid[] localGridRow : localGrids) {
            for (LocalGrid localGrid : localGridRow) {
                if (localGrid.equals(grid)) {
                    return localGrid;
                }
            }
        }
        return null;
    }

    public void makeMove(int localRow, int localCol) throws AlreadyOccupiedException {
        curLocalGrid.makeMove(curPlayer, localRow, localCol);
        setCurLocalGrid(localRow, localCol);
        curPlayer = (curPlayer == LocalGrid.Symbol.CROSS) ? LocalGrid.Symbol.NOUGHT : LocalGrid.Symbol.CROSS;
        ++movesMade;

        // When the local grid determined by the previous move has already been won or tied
        // the board's current local grid is set to null so that the current player is allowed to choose any local grid to make his move
        try {
            if (curLocalGrid.isLocalGameOver()) {
                curLocalGrid = null;
            }
        } catch (NullPointerException e) { }
    }

    public LocalGrid getLocalGrid(int row, int col) {
        return localGrids[row][col];
    }

    public void setCurLocalGrid(int row, int col) {
        prevLocalGrid = curLocalGrid;
        curLocalGrid = localGrids[row][col];
        curGlobalRow = row;
        curGlobalCol = col;
    }

    public void setCurLocalGridRandomly() {
        int row = (int)(Math.random() * dimension);
        int col = (int)(Math.random() * dimension);

        while (localGrids[row][col].isLocalGameOver()) {
            row = (int)(Math.random() * dimension);
            col = (int)(Math.random() * dimension);
        }

        setCurLocalGrid(row, col);
    }

    public LocalGrid[][] getLocalGrids() {
        return localGrids;
    }

    public LocalGrid getCurLocalGrid() {
        return curLocalGrid;
    }

    public LocalGrid getPrevLocalGrid() {
        return prevLocalGrid;
    }

    public LocalGrid.Symbol getCurPlayer() {
        return curPlayer;
    }

    public int getCurGlobalRow() {
        return curGlobalRow;
    }

    public int getCurGlobalCol() {
        return curGlobalCol;
    }

    public boolean isGameOver() {
        return movesMade == Math.pow(dimension, 4) || winner() != null;
    }

    public LocalGrid.Symbol winner() {
        LocalGrid.Symbol winner1 = checkRowsAndCols();
        LocalGrid.Symbol winner2 = checkDiagonals();
        return (winner1 != null) ? winner1 : winner2;
    }

    private LocalGrid.Symbol checkRowsAndCols() {
        for (int i = 0; i < dimension; ++i) {
            boolean rowWon = checkRow(i, LocalGrid.Symbol.CROSS);
            boolean colWon = checkCol(i, LocalGrid.Symbol.CROSS);
            if (rowWon || colWon) { return LocalGrid.Symbol.CROSS; }
        }
        for (int i = 0; i < dimension; ++i) {
            boolean rowWon = checkRow(i, LocalGrid.Symbol.NOUGHT);
            boolean colWon = checkCol(i, LocalGrid.Symbol.NOUGHT);
            if (rowWon || colWon) { return LocalGrid.Symbol.NOUGHT; }
        }
        return null;
    }
    private boolean checkRow(int row, LocalGrid.Symbol symbol) {
        for (int col = 0; col < dimension; ++col) {
            if (localGrids[row][col].winner() != symbol) { return false; }
        }
        return true;
    }
    private boolean checkCol(int col, LocalGrid.Symbol symbol) {
        for (int row = 0; row < dimension; ++row) {
            if (localGrids[row][col].winner() != symbol) { return false; }
        }
        return true;
    }
    private LocalGrid.Symbol checkDiagonals() {
        if (checkLeftToRightDiagonal(LocalGrid.Symbol.CROSS) || checkRightToLeftDiagonal(LocalGrid.Symbol.CROSS)) { return LocalGrid.Symbol.CROSS; }
        if (checkRightToLeftDiagonal(LocalGrid.Symbol.NOUGHT) || checkRightToLeftDiagonal(LocalGrid.Symbol.NOUGHT)) { return LocalGrid.Symbol.NOUGHT; }
        return null;
    }
    private boolean checkLeftToRightDiagonal(LocalGrid.Symbol symbol) {
        for (int row = 0, col = 0; row < dimension && col < dimension; ++row, ++col ) {
            if (localGrids[row][col].winner() != symbol) { return false; }
        }
        return true;
    }
    private boolean checkRightToLeftDiagonal(LocalGrid.Symbol symbol) {
        for (int row = 0, col = dimension - 1; row < dimension && col >= 0; ++row, --col) {
            if (localGrids[row][col].winner() != symbol) { return false; }
        }
        return true;
    }
}
