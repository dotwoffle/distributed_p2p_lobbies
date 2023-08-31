package framework;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import util.Logger;
import util.Logger.Level;
import util.Pair;
import util.network.SimpleTcpServer;
import util.network.SocketBundle;


/**Base class for application clients implementing our framework.*/
public abstract class P2PClient extends SimpleTcpServer implements SerializableToGameState {

    /*Variables*/

    /**Logger object.*/
    private final Logger LOG;
    /**The size of the lobby.*/
    private int lobbySize;
    /**The index of this client in the lobbyClients list.*/
    private int hostIndex;
    /**Total messages sent by this client.*/
    protected int totalMessages;
    /**The most current game state known by this client.*/
    private Pair<VectorClock, GameStateMap> latestGameState;
    /**The vector clock for this process.*/
    private VectorClock clock;
    /**A list of addresses for all clients in the lobby, including this one.*/
    private InetSocketAddress[] lobbyClients;


    /*Constructor*/

    /**Initializes the client for use in the framework.
     * @param port The port this client should listen on for messages.
    */
    protected P2PClient(int port) {

        super(port, "P2PClient");

        LOG = new Logger("P2PClient");
        LOG.setLogLevel(Level.DEBUG);

    }


    /*Methods*/

    @Override
    protected void serviceClient(SocketBundle client) {
        
        String[] message = getProtocolMessage(client); //get message from client

        LOG.debug("Received message from client: " + message[0]);

        //lobby setup
        if(message[0].equals(Messages.LOBBY_SETUP)) {
            init(Integer.parseInt(message[1]), Integer.parseInt(message[2]), client);
        }

        //event message
        else if(message[0].equals(Messages.EVENT)) {
            handleEvent(message[1], message[2]);
        }

    }

    /**This function is called when this client receives the GAMESTART message from the server. Clients extending this
     * class should override this funciton to start their game simulation.
    */
    protected abstract void startGame();

    /**Broadcasts an event message to all other clients in the lobby.*/
    protected void raiseEvent() {

        totalMessages += lobbySize-1;

        LOG.debug("Application raised event");

        clock.updateFromSendEvent(); //update clock

        GameStateMap states = convertToStateMap(); //get states for this client
        JSONObject stateJson = states.serializeToJson(); //get JSON representation
        String eventMsg = Messages.buildEventMessage(clock, stateJson); //message to broadcast

        //update most recent state
        synchronized(latestGameState) {
            latestGameState = new Pair<>(clock, states);
        }

        //broadcast event
        for(int idx = 0; idx < lobbySize; idx++) {

            //skip myself
            if(idx == hostIndex) {
                continue;
            }

            try {

                //open connection
                Socket clientSocket = new Socket(lobbyClients[idx].getAddress(), lobbyClients[idx].getPort());
                SocketBundle client = new SocketBundle(clientSocket);

                client.sendMessage(eventMsg); //send message
                client.close();

            } catch (IOException e) {
                LOG.warn("Could not connect to host: " + lobbyClients[idx]);
                LOG.warn(e.getMessage());
                e.printStackTrace();
            }

        }

    }

    /**Gives the most recent game state known by this client.
     * @return The state as a GameStateMap.
    */
    protected GameStateMap getLatestGameState() {
        synchronized(latestGameState) {
            return latestGameState.second;
        }
    }

    /**Initializes this client using the provided information from the server.
     * @param lobbySize The lobby size communicated by the server.
     * @param hostIndex The host index of this client.
     * @param client The remote server socket.
    */
    private void init(int lobbySize, int hostIndex, SocketBundle client) {

        LOG.info("Initializing lobby parameters");

        //set client parameters
        this.lobbyClients = new InetSocketAddress[lobbySize];
        this.lobbySize = lobbySize;
        this.hostIndex = hostIndex;
        this.totalMessages = 0;
        this.clock = new VectorClock(lobbySize, hostIndex);
        this.latestGameState = new Pair<>(clock, convertToStateMap()); //set initial game state

        try {
            Thread.sleep(100 * hostIndex);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        //get addresses of clients from server
        for(int x = 0; x < lobbySize; x++) {

            String[] clientInfoMessage = getProtocolMessage(client);

            //attempt to store address
            try {

                //parse client info
                int otherIndex = Integer.parseInt(clientInfoMessage[1]);
                InetAddress otherHostname = InetAddress.getByName(clientInfoMessage[2]);
                int otherPort = Integer.parseInt(clientInfoMessage[3]);
                InetSocketAddress otherAddr = new InetSocketAddress(otherHostname, otherPort);

                lobbyClients[otherIndex] = otherAddr; //store address

            } catch (UnknownHostException e) {
                LOG.warn("Could not resolve host: " + clientInfoMessage[1]);
                LOG.warn(e.getMessage());
                e.printStackTrace();
            }

        }

        String gameStartMsg = client.getMessage(); //get gamestart message

        //start game
        if(gameStartMsg.equals(Messages.GAME_START)) {

            //start game simulation on new thread
            new Thread(new Runnable() {

                @Override
                public void run() {
                    LOG.info("Starting game simulation");
                    startGame();
                }
                
            }).start();

        }
        else {
            LOG.warn("Server sent unexpected message while waiting for GAMESTART: " + gameStartMsg);
        }

    }

    /**Connects to the game server and requests to join a lobby.
     * @param serverAddr The address and port of the game server.
    */
    public void joinGame(InetSocketAddress serverAddr) {

        LOG.info("Contacting server");

        SocketBundle sb = null; //remote socket

        try {
            Socket s = new Socket(serverAddr.getAddress(), serverAddr.getPort()); //connect to server
            sb = new SocketBundle(s);
        } catch (IOException e) {
            LOG.error("Could not connect to game server");
            LOG.error(e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        sb.sendMessage(Messages.buildRequestLobbyMessage(getPort())); //request to join a lobby

        String response = sb.getMessage(); //server accepts or rejects

        if(response.equals(Messages.SERVER_REJECT)) {
            LOG.error("Server refused request to join a game");
            System.exit(1);
        }
        else if(response.equals(Messages.SERVER_OK)) {
            LOG.info("Successfully queued for game");
        }
        else {
            LOG.warn("Server sent unexpected message while joining queue: " + response);
        }

        LOG.debug("Waiting for JOINLOBBY");

    }

    /**Handles an event received by a client.
     * @param eventTimestamp The serialized vector timestamp sent with the event.
     * @param eventJson The JSON string that represents the event.
    */
    private void handleEvent(String eventTimestamp, String eventJson) {

        JSONParser jParser = new JSONParser(); //parser for JSON data
        JSONObject eventJsonObject = null; //object representation

        try {
            eventJsonObject = (JSONObject) (jParser.parse(eventJson)); //parse event string
        } catch (ParseException e) {
            LOG.warn("Could not parse event JSON data, event ignored");
            LOG.warn(e.getMessage());
            e.printStackTrace();
            return;
        }

        GameStateMap eventStates = GameStateMap.constructFromJson(eventJsonObject); //load states from event
        VectorClock eventVector = new VectorClock(lobbySize, -1, eventTimestamp); //event vector timestamp

        LOG.debug("Incoming event timestamp: " + eventVector);

        //update latest state if its timestamp is greater than the last tracked event
        synchronized(latestGameState) {
            if(eventVector.compareTo(latestGameState.first) == 1) {
                latestGameState = new Pair<>(eventVector, eventStates);
            }
        }

        clock.updateFromReceiveEvent(eventVector); //update my clock

    }

    /**Retrieves a message from the given client and plits a received message into its parts based on the message
     * delimiter.
     * @param client The connected client.
     * @return An array of all parts of the message.*/
    private String[] getProtocolMessage(SocketBundle client) {
        return client.getMessage().split("\\" + Messages.DELIM); 
    }

    /**Dumps the latest game state as JSON to the given file.
     * @param outputFilePath The path to the file to output the state JSON to.
    */
    public void dumpState(String outputFilePath) {

        try {
            PrintWriter out = new PrintWriter(new File(outputFilePath));
            out.print(convertToStateMap().toJSONString());
            out.flush();
        } catch (FileNotFoundException e) {
            LOG.warn("Could not open file for JSON dump: " + outputFilePath);
            LOG.warn(e.getMessage());
            e.printStackTrace();
        }

    }

    /**Returns the host index of this client.*/
    protected int getHostIndex() {
        return hostIndex;
    }

    /**Returns the number of players in the lobby.*/
    protected int getLobbySize() {
        return lobbySize;
    }
    
}
