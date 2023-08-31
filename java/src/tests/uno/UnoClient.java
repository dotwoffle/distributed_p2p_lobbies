package tests.uno;

import java.util.ArrayList;

import framework.GameStateMap;
import framework.P2PClient;

public class UnoClient extends P2PClient {

    private final int HAND_SIZE = 7;
    private int turnNumber;
    private UnoDeck deck;
    private ArrayList<UnoCard> discard;
    private ArrayList<ArrayList<UnoCard>> hands;

    protected UnoClient(int port) {

        super(port);

        this.turnNumber = -1;
        this.deck = new UnoDeck();
        this.discard = new ArrayList<>();
        this.hands = new ArrayList<>();
        
        start(); //start local server

    }

    @Override
    public GameStateMap convertToStateMap() {

        GameStateMap map = new GameStateMap();

        ArrayList<GameStateMap> discardState = new ArrayList<>();
        for(UnoCard card : discard) {
            discardState.add(card.convertToStateMap());
        }
        ArrayList<ArrayList<GameStateMap>> handState = new ArrayList<>();
        for(ArrayList<UnoCard> hand : hands) {
            ArrayList<GameStateMap> cardsInHand = new ArrayList<>();
            handState.add(cardsInHand);
            for(UnoCard card : hand) {
                cardsInHand.add(card.convertToStateMap());
            }
        }

        map.addProperty("deck", deck.convertToStateMap());
        map.addProperty("turnNumber", turnNumber);
        map.addProperty("discard", discardState);
        map.addProperty("hands", handState);
        map.addProperty("totalMessages", totalMessages);

        return map;

    }

    @Override
    protected void startGame() {

        System.out.println("Game starting!");

        for(int player = 0; player < getLobbySize(); player++) {
            hands.add(new ArrayList<>());
        }

        if(getHostIndex() == 0) {
            deck.shuffle();
            drawStartingHands();
            discard.add(deck.drawCard());
            turnNumber = 0;
            raiseEvent();
        }
        else {
            while(turnNumber == -1) {
                GameStateMap state = getLatestGameState();
                turnNumber = state.getInt("turnNumber");
            }
        }

        runGame();

    }

    private void runGame() {

        updateFromGameState(getLatestGameState());

        while(getWinningPlayer() == -1 && deck.size() != 0) {

            if(turnNumber % getLobbySize() == getHostIndex()) {

                System.out.println("My turn!");
                System.out.println("Top card is: " + discard.get(discard.size()-1));

                takeTurn();

                if(hands.get(getHostIndex()).size() == 1) {
                    System.out.println("UNO!");
                }

                turnNumber++;
                raiseEvent();

            }

            updateFromGameState(getLatestGameState());

        }

        System.out.println("Game over!");
        System.out.println("Player " + getWinningPlayer() + " won!");

        dumpState("../../tests/temp/uno_state_player_" + getHostIndex() + ".json");

        System.exit(0);

    }

    private void updateFromGameState(GameStateMap state) {

        turnNumber = state.getInt("turnNumber");

        ArrayList<GameStateMap> discardState = (ArrayList<GameStateMap>) state.getPropertyValue("discard");
        discard.clear();
        for(GameStateMap cardState : discardState) {
            UnoCard card = new UnoCard(
                cardState.getInt("number"),
                UnoCard.colorFromString((String) cardState.getPropertyValue("color"))
            );
            discard.add(card);
        }

        ArrayList<ArrayList<GameStateMap>> handState =
            (ArrayList<ArrayList<GameStateMap>>) state.getPropertyValue("hands");
        for(int idx = 0; idx < getLobbySize(); idx++) {
            hands.get(idx).clear();
            for(GameStateMap cardState : handState.get(idx)) {
                UnoCard card = new UnoCard(
                    cardState.getInt("number"),
                    UnoCard.colorFromString((String) cardState.getPropertyValue("color"))
                );
                hands.get(idx).add(card);
            }
        }

        deck.updateFromGameState((GameStateMap) state.getPropertyValue("deck"));

    }

    private void drawStartingHands() {

        for(int x = 0; x < HAND_SIZE; x++) {
            for(int player = 0; player < getLobbySize(); player++) {
                hands.get(player).add(deck.drawCard());
            }
        }

    }

    private int getWinningPlayer() {

        for(int idx = 0; idx < getLobbySize(); idx++) {
            if(hands.get(idx).isEmpty()) {
                return idx;
            }
        }

        return -1;

    }

    private void takeTurn() {

        UnoCard topCard = discard.get(discard.size()-1);

        for(UnoCard card : hands.get(getHostIndex())) {
            if(card.COLOR == topCard.COLOR || card.NUMBER == topCard.NUMBER) {
                System.out.println("I play: " + card);
                hands.get(getHostIndex()).remove(card);
                discard.add(card);
                return;
            }
        }

        System.out.println("I couldn't play anything...");
        hands.get(getHostIndex()).add(deck.drawCard());

    }
    
}
