package framework;

import org.json.simple.JSONObject;

/**Class to define message constants to send between nodes.*/
public class Messages {
    
    /*Variables*/

    /**Deliminator between different parts of messages.*/
    public static final String DELIM = "<DELIM>";
    /**Prefix for message game server sends to client preceding lobby formation.*/
    public static final String LOBBY_SETUP = "JOINLOBBY";
    /**Prefix for messages containing a client address and index.*/
    public static final String CLIENT_INFO = "CLIENTINFO";
    /**Prefix for message client sends to game server to ask to be put in a lobby.*/
    public static final String REQUEST_LOBBY = "REQUESTLOBBY";
    /**Message server sends to client upon connecting to acknowledge the client is in queue.*/
    public static final String SERVER_OK = "SERVEROK";
    /**Message server sends to client to reject request to join queue.*/
    public static final String SERVER_REJECT = "SERVERREJECT";
    /**Message server sends to client to tell them to start game simulation.*/
    public static final String GAME_START = "GAMESTART";
    /**Prefix for an event message.*/
    public static final String EVENT = "EVENT";


    /*Methods*/

    /**Builds a JOINLOBBY message from the given parameters.
     * @param lobbySize The number of clients in the lobby.
     * @param hostIndex The host index of the client this message is being sent to.
    */
    public static String buildJoinLobbyMessage(int lobbySize, int hostIndex) {
        return LOBBY_SETUP + DELIM + lobbySize + DELIM + hostIndex;
    }

    /**Builds a CLIENTINFO message from the given parameters.
     * @param hostname The IP address or hostname of this client.
     * @param port The port of this client.
     * @param hostIndex The id of this client.
    */
    public static String buildClientInfoMessage(String hostname, int port, int hostIndex) {
        return CLIENT_INFO + DELIM + hostIndex + DELIM + hostname + DELIM + port;
    }

    /**Builds a REQUESTLOBBY message from the given parameters.
     * @param port The public port that the client will listen on for messages from other clients.
    */
    public static String buildRequestLobbyMessage(int port) {
        return REQUEST_LOBBY + DELIM + port;
    }

    /**Builds an EVENT message from the given parameters.
     * @param timestamp The vector clock timestamp when the event happens.
     * @param eventStates The JSON object representing the game states being sent with the event.
    */
    public static String buildEventMessage(VectorClock timestamp, JSONObject eventStates) {
        return EVENT + DELIM + timestamp + DELIM + eventStates.toJSONString();
    }

}
