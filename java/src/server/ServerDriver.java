package server;


/**Driver class for the game server.*/
public class ServerDriver {
    
    public static void main(String[] args) {

        int port = Integer.parseInt(args[0]);
        int lobbySize = Integer.parseInt(args[1]);
        boolean stopOnLobbyCreation = args[2].equals("single-lobby");
        
        GameServer s = new GameServer(port, lobbySize, stopOnLobbyCreation);
        s.start();

    }

}