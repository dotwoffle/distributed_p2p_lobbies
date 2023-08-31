package tests.uno;

import java.util.ArrayList;
import java.util.Collections;

import framework.GameStateMap;
import framework.SerializableToGameState;
import tests.uno.UnoCard.Color;

public class UnoDeck implements SerializableToGameState {

    private ArrayList<UnoCard> cards;

    public UnoDeck() {
        
        cards = new ArrayList<>();

        buildDeck();

    }

    @Override
    public GameStateMap convertToStateMap() {
        GameStateMap map = new GameStateMap();
        ArrayList<GameStateMap> cardStates = new ArrayList<>();
        for(UnoCard card : cards) {
            cardStates.add(card.convertToStateMap());
        }
        map.addProperty("cards", cardStates);
        return map;
    }

    private void buildDeck() {

        for(int x = 0; x <= 9; x++) {
            for(int y = 0; y < 5; y++) {
                cards.add(new UnoCard(x, Color.RED));
                cards.add(new UnoCard(x, Color.GREEN));
                cards.add(new UnoCard(x, Color.BLUE));
                cards.add(new UnoCard(x, Color.YELLOW));
            }
        }

    }

    public void updateFromGameState(GameStateMap state) {

        cards.clear();

        for(GameStateMap cardState : (ArrayList<GameStateMap>) state.getPropertyValue("cards")) {
            UnoCard card = new UnoCard(
                cardState.getInt("number"),
                UnoCard.colorFromString((String) cardState.getPropertyValue("color"))
            );
            cards.add(card);
        }

    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public UnoCard drawCard() {
        return cards.remove(cards.size()-1);
    }

    public int size() {
        return cards.size();
    }

}
