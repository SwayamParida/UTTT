package uttt;

public class LocalGrid {

    public enum Symbol { CROSS, NOUGHT };

    private Symbol[][] localGrid;
    private int movesMade;
    private int dimension;
    private Symbol curPlayer;

    public LocalGrid(int dimension, Symbol player1) {
        this.dimension = dimension;
        movesMade = 0;
        localGrid = new Symbol[dimension][dimension];
        curPlayer = player1;
    }

    public LocalGrid(LocalGrid localGrid) {
        this.dimension = localGrid.dimension;
        this.movesMade = localGrid.movesMade;
        this.localGrid = new Symbol[dimension][dimension];
        for (int row = 0; row < dimension; ++row) {
            for (int col = 0; col < dimension; ++col) {
                this.localGrid[row][col] = localGrid.localGrid[row][col];
            }
        }
        this.curPlayer = localGrid.curPlayer;
    }

    public Symbol[][] getLocalGrid() {
        return localGrid;
    }

    public Symbol getCurPlayer() {
        return curPlayer;
    }

    public static void printLocalGrid(Symbol[][] localGrid) {
        System.out.println();
        for (Symbol[] row : localGrid) {
            for (Symbol cell : row) {
                char symbol = (cell == Symbol.CROSS) ? 'x' : (cell == Symbol.NOUGHT) ? 'o' : '-' ;
                System.out.print(symbol + "\t");
            }
            System.out.println();
        }
        System.out.println();
    }

    public void makeMove(Symbol symbol, int row, int col) throws AlreadyOccupiedException {
        if (localGrid[row][col] == null) {
            localGrid[row][col] = symbol;
        } else {
            throw new AlreadyOccupiedException("Cell already occupied");
        }
        ++movesMade;
    }

    public void undoMove(int row, int col) {
        localGrid[row][col] = null;
    }

    public boolean equals(LocalGrid grid) {
        try {
            boolean arraysEqual = arraysEqual(localGrid, grid.localGrid);
            boolean movesMadeEqual = movesMade == grid.movesMade;
            boolean dimensionEqual = dimension == grid.dimension;
            boolean curPlayerEqual = curPlayer == grid.curPlayer;
            return arraysEqual && movesMadeEqual && dimensionEqual && curPlayerEqual;
        } catch (NullPointerException e) {
            return false;
        }
    }

    public boolean arraysEqual(Symbol[][] array1, Symbol[][] array2) {
        for (int row = 0; row < array1.length && row < array2.length; ++row) {
            for (int col = 0; col < array1.length && col < array2.length; ++col) {
                if (array1[row][col] != array2[row][col]) { return false; }
            }
        }
        return true;
    }

    public boolean isLocalGameOver() {
        return movesMade == Math.pow(dimension, 2) || winner() != null;
    }

    public Symbol winner() {
        Symbol winner1 = checkRowsAndCols();
        Symbol winner2 = checkDiagonals();
        return (winner1 != null) ? winner1 : winner2;
    }

    private Symbol checkRowsAndCols() {
        for (int i = 0; i < dimension; ++i) {
            boolean rowWon = checkRow(i, Symbol.CROSS);
            boolean colWon = checkCol(i, Symbol.CROSS);
            if (rowWon || colWon) { return Symbol.CROSS; }
        }
        for (int i = 0; i < dimension; ++i) {
            boolean rowWon = checkRow(i, Symbol.NOUGHT);
            boolean colWon = checkCol(i, Symbol.NOUGHT);
            if (rowWon || colWon) { return Symbol.NOUGHT; }
        }
        return null;
    }
    private boolean checkRow(int row, Symbol symbol) {
        for (int col = 0; col < dimension; ++col) {
            if (localGrid[row][col] != symbol) { return false; }
        }
        return true;
    }
    private boolean checkCol(int col, Symbol symbol) {
        for (int row = 0; row < dimension; ++row) {
            if (localGrid[row][col] != symbol) { return false; }
        }
        return true;
    }
    private Symbol checkDiagonals() {
        if (checkLeftToRightDiagonal(Symbol.CROSS) || checkRightToLeftDiagonal(Symbol.CROSS)) { return Symbol.CROSS; }
        if (checkRightToLeftDiagonal(Symbol.NOUGHT) || checkRightToLeftDiagonal(Symbol.NOUGHT)) { return Symbol.NOUGHT; }
        return null;
    }
    private boolean checkLeftToRightDiagonal(Symbol symbol) {
        for (int row = 0, col = 0; row < dimension && col < dimension; ++row, ++col ) {
            if (localGrid[row][col] != symbol) { return false; }
        }
        return true;
    }
    private boolean checkRightToLeftDiagonal(Symbol symbol) {
        for (int row = 0, col = dimension - 1; row < dimension && col >= 0; ++row, --col) {
            if (localGrid[row][col] != symbol) { return false; }
        }
        return true;
    }
 }
