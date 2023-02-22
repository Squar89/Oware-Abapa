import java.util.*;
import java.io.*;
import java.math.*;

// MCTS Node
class Node {
    public GameState gameState;
    public int visitedCount;
    public int nodeScore;
    public Node[] nextNodes;
    public Node previousNode;

    public Node(Node previousNode, GameState gameState) {
        this.previousNode = previousNode;
        this.gameState = gameState;

        this.visitedCount = 0;
        this.nodeScore = 0;
        this.nextNodes = new Node[6];
    }

    public boolean isFullyExpanded() {
        for (int i = 0; i < 6; i++) {
            if (nextNodes[i] == null) {
                return false;
            }
        }
        return true;
    }

    public void expandNode() {
        for (int i = 0; i < 6; i++) {
            nextNodes[i] = new Node(this, gameState.simulateMove(i + (this.gameState.opponentTurn ? 6 : 0)));
        }
    }

    public Node findMaxUCBChild() {
        double maxUCB = Double.NEGATIVE_INFINITY;
        Node maxChild = null;

        for (int i = 0; i < 6; i++) {
            double currentUCB = nextNodes[i].calculateUCB();
            if (maxChild == null || currentUCB > maxUCB) {
                maxUCB = currentUCB;
                maxChild = nextNodes[i];
            }
        }
        return maxChild;
    }

    public double calculateUCB() {
        if (this.visitedCount == 0) {
            return Double.POSITIVE_INFINITY;
        }

        double C = 2;
        return Double.valueOf(this.nodeScore) + C * (Math.log(this.previousNode.visitedCount) / this.visitedCount);
    }

    public boolean isTerminal() {
        return this.gameState.isGameOver;
    }
}

class GameState {
    public int scorePlayer;
    public int scoreOpponent;
    public boolean isGameOver;
    public int turnCount;
    public boolean hasNoMoves;
    public boolean opponentTurn;
    int[] board;

    public GameState(int scorePlayer, int scoreOpponent, int[] board, int turnCount, boolean opponentTurn) {
        this.scorePlayer = scorePlayer;
        this.scoreOpponent = scoreOpponent;
        this.board = board;
        this.turnCount = turnCount;
        this.hasNoMoves = false;
        this.opponentTurn = opponentTurn;

        this.isGameOver = checkIfGameOver();
    }

    public GameState simulateMove(int move) {
        GameState newGameState = new GameState(this.scorePlayer, this.scoreOpponent, this.board.clone(),
                                               this.turnCount + 1, !this.opponentTurn);

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
        while (board[move] != 0
                && isOpponentIndex((move + i) % 12, move)
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
            i--;
        }

        newGameState.checkIfGameOver();

        return newGameState;
    }

    private boolean checkIfGameOver() {
        if (turnCount > 200 || scorePlayer >= 25 || scoreOpponent >= 25) {
            return true;
        }

        boolean playerHasNoMoves = true;
        boolean opponentHasNoMoves = true;
        for (int i = 0; i < 6; i++) {
            if (board[i] != 0) {
                playerHasNoMoves = false;
            }
        }
        for (int i = 6; i < 12; i++) {
            if (board[i] != 0) {
                opponentHasNoMoves = false;
            }
        }
        if (playerHasNoMoves || opponentHasNoMoves) {
            hasNoMoves = true;
            return true;
        }

        return false;
    }

    public int getOutcomeScore() {
        if (this.scorePlayer >= 25) {
            return 1;
        }
        if (this.scoreOpponent >= 25) {
            return -1;
        }

        if (this.turnCount > 200) {
            if (this.scorePlayer > this.scoreOpponent) {
                return 1;
            }
            else if (this.scorePlayer < this.scoreOpponent) {
                return -1;
            }
            else {
                return 0;
            }
        }

        if (this.hasNoMoves && this.opponentTurn) {
            return -1;
        }
        else if (this.hasNoMoves && !this.opponentTurn) {
            return 1;
        }

        // unfinished game, return whoever has more points
        if (this.scorePlayer > this.scoreOpponent) {
            return 1;
        }
        else if (this.scorePlayer < this.scoreOpponent) {
            return -1;
        }
        else {
            return 0;
        }
    }

    public int getRandomValidNextMove() {
        Random rand = new Random();

        while(true) {
            int r = rand.nextInt(6);
            int boardIndex = r + (this.opponentTurn ? 6 : 0);
            if (this.board[boardIndex] != 0) {
                return boardIndex;
            }
        }
    }

    private boolean isOpponentIndex(int index, int moveIndex) {
        return (moveIndex < 6 && index >= 6) || (moveIndex >= 6 && index < 6);
    }

    public String printBoard() {
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
    static int INT_MIN = -2147483647;
    static final int TURN_LIMIT = 200;
    static final int EXPAND_LIMIT = 5;
    static final int SIMULATION_LIMIT = 1;
    static final int DEPTH_LIMIT = 20;

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        int[] board = new int[12];
        int score1 = 0;
        int score2 = 0;
        int previousCount = 48;
        int turnCount = 0;
        Node root = null;

        // game loop
        while (true) {
            int currentCount = 0;
            for (int i = 0; i < 12; i++) {
                int seeds = in.nextInt();
                board[i] = seeds;
                currentCount += seeds;
            }
            // find out who started the game
            if (turnCount == 0) {
                boolean gameStart = true;
                for (int i = 0; i < 12; i++) {
                    if (board[i] != 4) {
                        gameStart = false;
                    }
                }
                if (!gameStart) { // opponent started the game
                    turnCount = 2;
                }
                else {
                    turnCount = 1;
                }
            }
            else {
                turnCount += 2;
            }


            if (previousCount > currentCount) {
                score2 += previousCount - currentCount;
                previousCount = currentCount;
            }

            GameState currentGameState = new GameState(score1, score2, board, turnCount, false);
            currentGameState.printBoard();


            root = findCurrentNode(root, currentGameState);
            
            int expandCounter = EXPAND_LIMIT;
            while (expandCounter > 0) {
                expandCounter--;
                expandTree(root);
            }
            int nextMove = pickNextMove(root);
            int newScore1 = currentGameState.simulateMove(nextMove).scorePlayer;
            if (score1 < newScore1) {
                previousCount -= (newScore1 - score1);
            }
            score1 = newScore1;

            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");
            while(board[nextMove % 6] == 0) {
                //System.err.println("[ERR] Returned 0 field move " + nextMove);
                nextMove++;
            }
            System.out.println(nextMove);
            root = root.nextNodes[nextMove];
            root.expandNode();
        }
    }

    public static Node findCurrentNode(Node node, GameState gameState) {
        if (node == null) { // starting point - build first node
            return new Node(null, gameState);
        }
        // System.err.println("Node x " + gameState.printBoard());

        for (int i = 0; i < 6; i++) {
            // System.err.println("Node " + i + " " + node.nextNodes[i].gameState.printBoard());
            boolean boardEqual = true;
            for (int j = 0; j < 12; j++) {
                if (node.nextNodes[i].gameState.board[j] != gameState.board[j]) {
                    boardEqual = false;
                }
            }
            if (boardEqual) {
                node.nextNodes[i].gameState = gameState;
                return node.nextNodes[i];
            }
        }

        //System.err.println("[ERR] couldn't find current game state. Constructing new MCTS.");
        return new Node(null, gameState);
    }

    public static Node selectLeaf(Node node) {
        if (!node.isFullyExpanded()) {
            node.expandNode();
            return node.isTerminal() ? node : node.nextNodes[node.gameState.getRandomValidNextMove() % 6];
        }
        return node.findMaxUCBChild();
    }

    public static int runSimulation(GameState simulationState) {
        int result = 0;
        
        int simulationsCountLimit = SIMULATION_LIMIT;
        int depthCounter = DEPTH_LIMIT;
        while (simulationsCountLimit > 0) {
            simulationsCountLimit--;
            while (!simulationState.isGameOver && depthCounter > 0) {
                depthCounter--;
                simulationState = simulationState.simulateMove(simulationState.getRandomValidNextMove());
            }
            result += simulationState.getOutcomeScore();
        }

        return result;
    }

    public static void expandTree(Node node) {
        Node leaf = selectLeaf(node);

        int simulationOutcomeScore = runSimulation(leaf.gameState);
        
        // backpropagate both score and visit counter
        while (leaf != node) {
            leaf.nodeScore += simulationOutcomeScore;
            leaf.visitedCount += 1;
            leaf = leaf.previousNode;
        }
        node.nodeScore += simulationOutcomeScore;
        node.visitedCount += 1;
    }

    public static int pickNextMove(Node node) {
        int maxResult = INT_MIN;
        int nextMove = -1;

        for (int i = 0; i < 6; i++) {
            if (nextMove == -1 || (node.nextNodes[i].visitedCount > 0 && node.nextNodes[i].nodeScore > maxResult)) {
                maxResult = node.nextNodes[i].nodeScore;
                nextMove = i;
            }
        }
        
        return nextMove;
    }
}