package model;

import java.util.*;
import java.util.stream.Stream;

public class GameModel {
    public static final int DECK_SIZE = 36;
    public static final int MAX_SCORE = 21;
    public static final int MAX_DEALER_TOTAL = 17;
    public static final int MAX_WEIGHT;
    public static final int MIN_WEIGHT;

    static {
        MAX_WEIGHT = Collections.max(
            CardDeckCreator.getWeights21(),
            Comparator.comparingInt(Integer::intValue)
        );

        MIN_WEIGHT = Collections.min(
            CardDeckCreator.getWeights21(),
            Comparator.comparingInt(Integer::intValue)
        );
    }

    private Stack<Card> cardDeck;
    private Vector<CardPlayer> cardPlayers;
    private boolean isRun;

    public GameModel() throws IllegalArgumentException {
        cardDeck = CardDeckCreator.createDeck(DECK_SIZE);
        cardPlayers = new Vector<>();
        isRun = false;
    }

    public Card getCardFromCardDeck() throws EmptyStackException {
        if (cardDeck.isEmpty())
            throw new EmptyStackException();

        return cardDeck.pop();
    }

    public Stack<Card> getCardDeck() {
        return cardDeck;
    }

    public int getCardDeckSize() {
        return cardDeck.size();
    }

    public boolean isCardDeckEmpty() {
        return cardDeck.isEmpty();
    }

    public Vector<CardPlayer> getCardPlayers() {
        return cardPlayers;
    }

    public CardPlayer getCardPlayer(int playerIndex) throws IndexOutOfBoundsException {
        if (playerIndex < 0 || playerIndex >= cardPlayers.size())
            throw new IndexOutOfBoundsException(
                "getCardPlayer: invalid argument 'playerIndex'");

        return cardPlayers.elementAt(playerIndex);
    }

    public boolean isRun() {
        return isRun;
    }

    public void reload() {
        cardDeck = CardDeckCreator.createDeck(DECK_SIZE);
        cardPlayers.clear();

        // poor logic because there is no way to make
        // abstract static method in CardPlayer class
        // (should probably use interface for that purpose)
        Computer.resetPlayerIndex();
        Player.resetPlayerIndex();
    }

    public long winnersAmount() {
        return getWinners().count();
    }

    public Stream<CardPlayer> getWinners() {
        return cardPlayers.stream().filter(CardPlayer::hasWon);
    }

    public void shuffleDeck() {
        Collections.shuffle(cardDeck);
    }

    public void appendPlayers() {
        cardPlayers.add(new Computer());
        cardPlayers.add(new Player());
        cardPlayers.elementAt(0).setDealer(true);
    }

    public void run() {
        shuffleDeck();
        appendPlayers();
        isRun = true;
    }

    public boolean isAllPlayersFinished() {
        for (CardPlayer cardPlayer : cardPlayers)
            if (!cardPlayer.hasFinished())
                return false;

        return true;
    }

    public void checkWinners() throws NullPointerException {
        CardPlayer dealer = cardPlayers.stream()
            .filter(CardPlayer::isDealer)
            .findFirst().orElseThrow(NullPointerException::new);

        for (CardPlayer cardPlayer : cardPlayers)
            if (!cardPlayer.isDealer() && !cardPlayer.hasWon() && !cardPlayer.hasExceeded())
                if (dealer.hasExceeded()
                    || cardPlayer.getPointsAmount() > dealer.getPointsAmount())
                    cardPlayer.setWin(true);
                else if (dealer.getPointsAmount() > cardPlayer.getPointsAmount())
                    dealer.setWin(true);
    }
}
