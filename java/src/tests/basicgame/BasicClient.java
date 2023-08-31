package tests.basicgame;

import java.util.Random;

import framework.GameStateMap;
import framework.P2PClient;


/**Basic test class that implements P2PClient.*/
public class BasicClient extends P2PClient {

    /*Variables*/
    
    private int player1Points;
    private int player2Points;
    private int turnPlayer;
    private Random rand;


    /*Constructor*/

    /**Initializes the client.*/
    protected BasicClient(int port) {

        super(port);

        //init

        this.player1Points = 0;
        this.player2Points = 0;
        this.turnPlayer = 0;
        this.rand = new Random();

        start(); //start server

    }


    /*Methods*/

    @Override
    protected void startGame() {

        System.out.println("Game starting!");
        System.out.println("Score: 0-0");

        runGame();

    }

    @Override
    public GameStateMap convertToStateMap() {
        
        GameStateMap states = new GameStateMap();

        states.addProperty("player1Points", player1Points);
        states.addProperty("player2Points", player2Points);
        states.addProperty("turnPlayer", turnPlayer);

        return states;

    }

    private void runGame() {

        while(true) {

            GameStateMap states = getLatestGameState();

            player1Points = ((Number) states.getPropertyValue("player1Points")).intValue();
            player2Points = ((Number) states.getPropertyValue("player2Points")).intValue();
            turnPlayer = ((Number) states.getPropertyValue("turnPlayer")).intValue();

            if(player1Points >= 30) {
                if(getHostIndex() == 0) {
                    System.out.println("I win!");
                }
                else {
                    System.out.println("I lost...");
                }
                System.out.println("Final score: " + player1Points + "-" + player2Points);
                break;
            }
            else if(player2Points >= 30) {
                if(getHostIndex() == 1) {
                    System.out.println("I win!");
                }
                else {
                    System.out.println("I lost...");
                }
                System.out.println("Final score: " + player1Points + "-" + player2Points);
                break;
            }

            if(turnPlayer == getHostIndex()) {

                System.out.println("My turn!");
                System.out.println("Current score: "+ player1Points + "-" + player2Points);

                int points = rand.nextInt(10);
                
                if(getHostIndex() == 0) {
                    player1Points += points;
                }
                else {
                    player2Points += points;
                }

                System.out.println("I got " + points + " points!");
                System.out.println("New score: " + player1Points + "-" + player2Points);

                if(turnPlayer == 0) {
                    turnPlayer = 1;
                }
                else {
                    turnPlayer = 0;
                }

                raiseEvent();

            }

        }

        System.out.println("Game over!");

    }
    
}
