package controller;

import model.Card;
import model.CardPlayer;
import model.GameModel;
import view.Application;
import view.CardImage;
import view.LayeredPane;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

public class Controller {
    private GameModel model;
    private Application view;

    public Controller(GameModel model, Application view) {
        this.model = model;
        this.view = view;

        this.view.addButtonClickListener(new ButtonClickListener());
    }

    public class ButtonClickListener implements ActionListener {
        public static final String NEW_GAME = "NEW_GAME";
        public static final String EXIT_GAME = "EXIT_GAME";
        public static final String EXIT_GAME_NO_DIALOG = "EXIT_GAME_NO_DIALOG";
        public static final String GET_CARD = "GET_CARD";
        public static final String PASS_ROUND = "PASS_ROUND";
        public static final String HOW_TO_PLAY = "HOW_TO_PLAY";
        public static final String ABOUT = "ABOUT";

        private void pushCardToDeck(LayeredPane targetPane, Vector<Card> targetDeck) {
            if (model.isCardDeckEmpty()) {
                view.showEmptyDeckWarningDialog();
                return;
            }

            Card card = model.getCardFromCardDeck();
            ImageIcon cardImage = CardImage.getCardImage(card);

            int layerBoundX = targetPane.getBoundX();
            int layerBoundY = targetPane.getBoundY();

            JLabel label = new JLabel(cardImage);
            label.setBounds(layerBoundX, layerBoundY,
                cardImage.getIconWidth(), cardImage.getIconHeight());

            targetDeck.add(card); // send card to Model
            targetPane.add(label); // send card to View

            if (layerBoundX >= LayeredPane.OFFSET)
                targetPane.increaseWidth();
        }

        private void invokeGameRoutine() {
            if (view.containsInitPanel())
                view.launchApplication();

            if (model.isRun() && view.interactButtonsAreEnabled()) {
                int response = view.showNewGameDialog();

                switch (response) {
                    // if user wants to reload the game:
                    case 0:
                        model.reload();
                        view.reloadPanes();
                        break;
                    // if user does not want to do so:
                    case 1:
                        return;
                }
            }
            else if (!view.interactButtonsAreEnabled()) {
                model.reload();
                view.reloadPanes();
            }

            model.run();

            pushCardToDeck(
                view.getDealerCardsPane(),
                model.getCardPlayer(0).getCardDeck()
            );

            pushCardToDeck(
                view.getPlayerCardsPane(),
                model.getCardPlayer(1).getCardDeck()
            );

            refreshDynamicFields();

            view.switchInteractButtons(true);
        }

        private void refreshDynamicFields() {
            view.getDealerTotalPtsLabel().setText(
                Integer.toString(model.getCardPlayer(0).getPointsAmount())
            );

            view.getPlayerTotalPtsLabel().setText(
                Integer.toString(model.getCardPlayer(1).getPointsAmount())
            );

            view.getCardDeckSizeLabel().setText(
                Integer.toString(model.getCardDeckSize())
            );
        }

        // Apparently, this method is used here temporarily (may be it's not)
        private void processUserResponse(int response) {
            switch (response) {
                // if user wants to play the game again
                case 0:
                    invokeGameRoutine();
                    break;
                // if user wants to quit
                case 1:
                    System.exit(0);
            }
        }

        private void winDialog(CardPlayer wonPlayer) {
            int response = view.showWinGameDialog(wonPlayer.getPlayerName());

            processUserResponse(response);
        }

        private void exceedDialog(CardPlayer exceededPlayer) {
            int response = view.showLoseGameDialog(exceededPlayer.getPlayerName());

            processUserResponse(response);
        }

        private void exitDialog() {
            int response = view.showExitDialog();

            switch (response) {
                case 0:
                    System.exit(0);
                case 1:
                    return;
            }
        }

        private void checkComputerCondition(CardPlayer computer) {
            Timer timer = new Timer(650, null);

            timer.addActionListener(actionEvent -> {
                computer.analyzeTurn();

                if (computer.hasPassed() || computer.hasWon() || computer.hasExceeded()) {
                    timer.stop();

                    if (computer.hasPassed())
                        view.showComputerPassedDialog(computer.getPlayerName());

                    else if (computer.hasExceeded())
                        view.showComputerExceededDialog(computer.getPlayerName());

                    else if (computer.hasWon())
                        view.showComputerWinDialog(computer.getPlayerName());
                }
                else
                    addCardFromDeck(0);

                getGameResults();
            });

            timer.start();
        }

        private void checkCondition(CardPlayer cardPlayer) {
            cardPlayer.analyzeTurn();

            if (cardPlayer.hasExceeded() || cardPlayer.hasWon())
                view.switchInteractButtons(false);

            if (cardPlayer.hasExceeded())
                exceedDialog(cardPlayer);
            else if (cardPlayer.hasWon())
                winDialog(cardPlayer);

            getGameResults();
        }

        private void addCardFromDeck(int playerIndex) {
            // lame logic (temporarily):
            if (playerIndex == 0)
                pushCardToDeck(
                    view.getDealerCardsPane(),
                    model.getCardPlayer(playerIndex).getCardDeck());
            else
                pushCardToDeck(
                    view.getPlayerCardsPane(),
                    model.getCardPlayer(playerIndex).getCardDeck());

            refreshDynamicFields();
        }

        private void getGameResults() {
            if (!model.isAllPlayersFinished())
                return;

            model.checkWinners();
            if (model.winnersAmount() == 0) {
                view.showNoWinnersDialog();
                return;
            }

            model.getWinners().forEach(cardPlayer ->
                view.showWinnerDialog(cardPlayer.getPlayerName(), Integer.toString(cardPlayer.getPointsAmount()))
            );
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            String command = actionEvent.getActionCommand();

            switch (command) {
                case NEW_GAME:
                    invokeGameRoutine();
                    break;
                case EXIT_GAME:
                    exitDialog();
                    break;
                case EXIT_GAME_NO_DIALOG:
                    System.exit(0);
                    break;
                case GET_CARD:
                    addCardFromDeck(1);
                    checkCondition(model.getCardPlayer(1));
                    break;
                case PASS_ROUND:
                    view.switchInteractButtons(false);
                    model.getCardPlayer(1).setPass(true);
                    checkComputerCondition(model.getCardPlayer(0));
                    break;
                case HOW_TO_PLAY:
//                    view.showHowToPlayDialog();
                    break;
                case ABOUT:
                    view.showAboutDialog();
                    break;
            }
        }
    }
}