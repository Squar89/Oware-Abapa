import java.util.*;
import java.io.*;
import java.math.*;

// class Score implements Cloneable {
//     public int scorePlayer;
//     public int scoreOpponent;
//     public boolean isGameOver;

//     public Score() {
//         this.scorePlayer = 0;
//         this.scoreOpponent = 0;
//         this.isGameOver = false;
//     }

//     public Object clone() throws CloneNotSupportedException
//     {
//         return super.clone();
//     }

//     public String toString() {
//         return "player: " + this.scorePlayer + " opponent: " + this.scoreOpponent + " isGameOver: " + this.isGameOver;
//     }
// };

class GameState {
    public int scorePlayer;
    public int scoreOpponent;
    public boolean isGameOver;
    int[] board;

    public GameState(int scorePlayer, int scoreOpponent, int[] board) {
        this.scorePlayer = scorePlayer;
        this.scoreOpponent = scoreOpponent;
        this.isGameOver = scorePlayer >= 25  || scoreOpponent >= 25;
        this.board = board;
    }

    public GameState simulateMove(int move) throws CloneNotSupportedException {
        GameState newGameState = new GameState(this.scorePlayer, this.scoreOpponent, this.board.clone());

        int seeds = board[move];
        int i = 0;
        newGameState.board[move] = 0;
        while (seeds > 0) {
            i++;
            if ((move + i) % 12 != move % 12) {
                newGameState.board[(move + i) % 12]++;
                seeds--;
            }
        }
        while (isOpponentIndex((move + i) % 12, move)
                && (newGameState.board[(move + i) % 12] == 2
                    || newGameState.board[(move + i) % 12] == 3)) {
            int addToScore = newGameState.board[(move + i) % 12];
            newGameState.board[(move + i) % 12] = 0;
            if (move < 6) {
                newGameState.scorePlayer += addToScore;
            }
            else {
                newGameState.scoreOpponent += addToScore;
            }
            if (newGameState.scorePlayer >= 25  || newGameState.scoreOpponent >= 25) {
                newGameState.isGameOver = true;
            }
            i--;
        }

        return newGameState;
    }

    private boolean isOpponentIndex(int index, int moveIndex) {
        return (moveIndex < 6 && index >= 6) || (moveIndex >= 6 && index < 6);
    }

    private String printBoard() {
        String res = "";
        for (int i = 0; i < 12; i++) {
            res += i + ": " + this.board[i] + "; ";
        }
        return res;
    }

    public String toString() {
        return "player: " + this.scorePlayer + " opponent: " + this.scoreOpponent + " isGameOver: " + this.isGameOver
               + "\n" + printBoard();
    }
}

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Player {
    static int INT_MAX = 2147483647;
    static int INT_MIN = -2147483647;
    static int startingAlpha = INT_MIN;
    static int startingBeta = INT_MAX;
    static int startingDepth = 9;

    public static void main(String args[]) throws CloneNotSupportedException {
        Scanner in = new Scanner(System.in);
        int[] board = new int[12];
        int score1 = 0;
        int score2 = 0;
        int previousCount = 48;

        // game loop
        while (true) {
            int currentCount = 0;
            for (int i = 0; i < 12; i++) {
                int seeds = in.nextInt();
                board[i] = seeds;
                currentCount += seeds;
            }
            if (previousCount > currentCount) {
                score2 += previousCount - currentCount;
                previousCount = currentCount;
            }

            GameState currentGameState = new GameState(score1, score2, board);
            int nextMove = pickNextMove(currentGameState);
            int newScore1 = currentGameState.simulateMove(nextMove).scorePlayer;
            if (score1 < newScore1) {
                previousCount -= (newScore1 - score1);
            }
            score1 = newScore1;

            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");
            while(board[nextMove] == 0) {
                nextMove++;
            }
            System.out.println(nextMove);
        }
    }

    public static int pickNextMove(GameState gameState) throws CloneNotSupportedException {
        int maxResult = INT_MIN;
        int nextMove = -1;

        for (int i = 0; i < 6; i++) {
            if (gameState.board[i] != 0) {
                int scoreChange = minimax(gameState.simulateMove(i), startingDepth, startingAlpha, startingBeta, false);
                if (nextMove == -1 || scoreChange > maxResult) {
                    maxResult = scoreChange;
                    nextMove = i;
                }
                // System.err.println("Move: " + i + " Minimax: " + scoreChange);
            }
        }

        return nextMove;
    }

    public static int minimax(GameState state, int depth, int alpha, int beta, boolean maximizingPlayer)
        throws CloneNotSupportedException {
        
        if (depth == 0 || state.isGameOver) {
            if (state.scoreOpponent >= 25) {
                return INT_MIN;
            }
            else if (state.scorePlayer >= 25) {
                return INT_MAX;
            }
            return state.scorePlayer - state.scoreOpponent;
        }

        if (maximizingPlayer) {
            boolean hasNoMoveInstaWin = true;
            int maxEval = INT_MIN;
            for (int i = 0; i < 6; i++) {
                if (state.board[i] != 0) {
                    hasNoMoveInstaWin = false;
                    int eval = minimax(state.simulateMove(i), depth - 1, alpha, beta, false);
                    // System.err.println("  Depth: " + depth + " state:" + state.toString() + " eval: " + eval + " ");
                    maxEval = Math.max(maxEval, eval);
                    alpha = Math.max(alpha, eval);
                    if (beta <= alpha) {
                        break;
                    }
                }
            }
            if (hasNoMoveInstaWin) {
                return INT_MAX;
            }
            return maxEval;
        }
        else {
            boolean hasNoMoveInstaWin = true;
            int minEval = INT_MAX;
            for (int i = 6; i < 12; i++) {
                if (state.board[i] != 0) {
                    hasNoMoveInstaWin = false;
                    int eval = minimax(state.simulateMove(i), depth - 1, alpha, beta, true);
                    // System.err.println("  Depth: " + depth + " state:" + state.toString() + " eval: " + eval + " ");
                    minEval = Math.min(minEval, eval);
                    beta = Math.min(beta, eval);
                    if (beta <= alpha) {
                        break;
                    }
                }
            }
            if (hasNoMoveInstaWin) {
                return INT_MIN;
            }
            return minEval;
        }
    }
}