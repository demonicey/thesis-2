package com.example.sudoku;

import java.util.Random;

public class SudokuGame {
    private int[][] board = new int[9][9];
    private int[][] originalBoard = new int[9][9];

    public SudokuGame() {
        generatePuzzle();
    }

    public void generatePuzzle() {
        // Simple puzzle generation (for demo, create a valid Sudoku)
        clearBoard();
        // Fill diagonal 3x3 boxes
        fillDiagonal();
        // Fill remaining cells
        solveSudoku(0, 0);
        // Remove some numbers for puzzle
        removeNumbers(40); // Remove 40 numbers for easy puzzle
        // Copy to original board
        copyBoard(board, originalBoard);
    }

    private void clearBoard() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                board[i][j] = 0;
            }
        }
    }

    private void fillDiagonal() {
        for (int i = 0; i < 9; i += 3) {
            fillBox(i, i);
        }
    }

    private void fillBox(int row, int col) {
        Random rand = new Random();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int num;
                do {
                    num = rand.nextInt(9) + 1;
                } while (!isSafe(row + i, col + j, num));
                board[row + i][col + j] = num;
            }
        }
    }

    private boolean solveSudoku(int row, int col) {
        if (row == 9) return true;
        if (col == 9) return solveSudoku(row + 1, 0);
        if (board[row][col] != 0) return solveSudoku(row, col + 1);

        for (int num = 1; num <= 9; num++) {
            if (isSafe(row, col, num)) {
                board[row][col] = num;
                if (solveSudoku(row, col + 1)) return true;
                board[row][col] = 0;
            }
        }
        return false;
    }

    private void removeNumbers(int count) {
        Random rand = new Random();
        while (count > 0) {
            int i = rand.nextInt(9);
            int j = rand.nextInt(9);
            if (board[i][j] != 0) {
                board[i][j] = 0;
                count--;
            }
        }
    }

    public boolean isSafe(int row, int col, int num) {
        // Check row
        for (int j = 0; j < 9; j++) {
            if (board[row][j] == num) return false;
        }
        // Check column
        for (int i = 0; i < 9; i++) {
            if (board[i][col] == num) return false;
        }
        // Check 3x3 box
        int boxRow = row - row % 3;
        int boxCol = col - col % 3;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[boxRow + i][boxCol + j] == num) return false;
            }
        }
        return true;
    }

    public boolean isValidMove(int row, int col, int num) {
        return isSafe(row, col, num);
    }

    public void setCell(int row, int col, int num) {
        board[row][col] = num;
    }

    public int[][] getBoard() {
        return board;
    }

    public int[][] getOriginalBoard() {
        return originalBoard;
    }

    private void copyBoard(int[][] source, int[][] dest) {
        for (int i = 0; i < 9; i++) {
            System.arraycopy(source[i], 0, dest[i], 0, 9);
        }
    }

    public boolean isSolved() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (board[i][j] == 0 || !isSafe(i, j, board[i][j])) return false;
            }
        }
        return true;
    }
}
