package tests.uno;

import framework.GameStateMap;
import framework.SerializableToGameState;

public class UnoCard implements SerializableToGameState {
    
    public enum Color {
        RED, BLUE, GREEN, YELLOW
    }

    public final int NUMBER;
    public final Color COLOR;

    public UnoCard(int number, Color color) {
        this.NUMBER = number;
        this.COLOR = color;
    }

    @Override
    public GameStateMap convertToStateMap() {
        GameStateMap map = new GameStateMap();
        map.addProperty("number", NUMBER);
        map.addProperty("color", COLOR.toString());
        return map;
    }

    @Override
    public String toString() {
        return COLOR + " " + NUMBER;
    }

    public static Color colorFromString(String colorString) {
        switch(colorString) {
            case "RED":
                return Color.RED;
            case "BLUE":
                return Color.BLUE;
            case "GREEN":
                return Color.GREEN;
            case "YELLOW":
                return Color.YELLOW;
            default:
                return null;
        }
    }

}
