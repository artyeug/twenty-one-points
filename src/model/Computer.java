package model;

import java.util.Random;

public class Computer extends CardPlayer {
    private static int _computerIndex;

    static {
        _computerIndex = 0;
    }

    Computer() {
        super("Computer " + ++_computerIndex);
    }

    @Override
    public int getPlayerIndex() {
        return _computerIndex;
    }

    private TurnStatement calculatePossibility(double percentage) {
        Random random = new Random();
        int maxPossibility = 100;

        int possibility = random.nextInt(maxPossibility);
        if (possibility <= percentage)
            return TurnStatement.HIT;
        else
            return TurnStatement.STAND;
    }

    @Override
    public TurnStatement analyzeTurn() {
        String playerName = this.getPlayerName();
        int pointsAmount = this.getPointsAmount();

        if (pointsAmount > TwentyOnePoints.MAX_SCORE) {
            System.out.println(playerName + " has exceeded the maximum score. It's lost.");
            return TurnStatement.EXCEED;
        }
        else if (pointsAmount == TwentyOnePoints.MAX_SCORE) {
            System.out.println(playerName + " has earned 21 points!");
            return TurnStatement.WIN;
        }

        if (this.isDealer() && pointsAmount >= TwentyOnePoints.MAX_DEALER_TOTAL)
            return TurnStatement.STAND;

        // Below is the main decision determination:

        int upperBound = TwentyOnePoints.MAX_SCORE - TwentyOnePoints.MIN_WEIGHT + 1;
        int lowerBound = TwentyOnePoints.MAX_SCORE - TwentyOnePoints.MAX_WEIGHT;

        if (pointsAmount == upperBound)
            return TurnStatement.STAND;

        if (pointsAmount <= lowerBound)
            return TurnStatement.HIT;

        int firstBreakpoint = lowerBound + (upperBound - lowerBound) / 5;
        int secondBreakpoint = lowerBound + (upperBound - lowerBound) / 2;
        int thirdBreakpoint = upperBound - 1;

        if (pointsAmount <= firstBreakpoint)
            return calculatePossibility(85);
        else if (pointsAmount <= secondBreakpoint)
            return calculatePossibility(60);
        else if (pointsAmount <= thirdBreakpoint)
            return calculatePossibility(35);


        return TurnStatement.STAND;
    }
}