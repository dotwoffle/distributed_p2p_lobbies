package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

import framework.Messages;
import util.Logger;
import util.Logger.Level;
import util.network.SimpleTcpServer;
import util.network.SocketBundle;


/**A simple game server to set up lobbies and record game results.*/
public class GameServer extends SimpleTcpServer{

    /*Variables*/

    /**Whether or not to shut down after creating one lobby.*/
    private final boolean STOP_ON_LOBBY_CREATION;
    /**The total number of clients in each game lobby.*/
    private final int LOBBY_SIZE;
    /**Logger object.*/
    private final Logger LOG;
    /**The queue of clients waiting for a lobby. Each entry is the public endpoint of the client.*/
    private Queue<InetSocketAddress> clientQueue;


    /*Constructor*/

    /**Initializes the game server.
     * @param port The port the server will run on.
     * @param lobbySize The maximum number of clients in a game lobby.
     * @param stopOnLobbyCreation If this is true, the server will shut down after forming a single lobby. This is for testing purposes.
    */
    protected GameServer(int port, int lobbySize, boolean stopOnLobbyCreation) {

        super(port, "GameServer");

        LOG = new Logger("GameServer");
        LOG.setLogLevel(Level.DEBUG);

        //init

        this.STOP_ON_LOBBY_CREATION = stopOnLobbyCreation;
        this.LOBBY_SIZE = lobbySize;
        this.clientQueue = new LinkedList<>();

    }


    /*Methods*/

    @Override
    protected void serviceClient(SocketBundle client) {

        //get client request parameters
        String clientRequestMsg = client.getMessage();
        String[] clientRequest = clientRequestMsg.split("\\" + Messages.DELIM);
        int clientPublicPort = Integer.parseInt(clientRequest[1]);

        synchronized(clientQueue) {

            //put client in queue
            clientQueue.add(new InetSocketAddress(client.getSocketAddress().getAddress(), clientPublicPort)); 
            LOG.debug("Client " + client.getSocketAddress() + " added to queue");

        }

        client.sendMessage(Messages.SERVER_OK); //ack client

    }

    @Override
    public void start() {

        LOG.info("Starting game server");

        super.start();

        queueLoop(); //wait for clients to connect

    }

    /**Continually waits for clients to queue. When enough clients have joined, a lobby is started.*/
    private void queueLoop() {

        while(true) {

            //required to force updates
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                LOG.error("Thread interrupted while sleeping");
                LOG.error(e.getMessage());
                e.printStackTrace();
            }

            //fire lobby when enough clients have joined
            if(clientQueue.size() >= LOBBY_SIZE) {
                setupLobby();
            }

        }

    }

    /**Sets up a lobby when enough clients have connected.*/
    private void setupLobby() {

        LOG.info(LOBBY_SIZE + " clients connected, initializing lobby");

        SocketBundle[] lobbyClients = new SocketBundle[LOBBY_SIZE]; //list of clients in lobby
        
        //connect to clients and put them in lobby
        for(int idx = 0; idx < LOBBY_SIZE; idx++) {

            InetSocketAddress clientAddr = clientQueue.remove(); //get next client
            Socket clientSocket = null;

            //attempt to open connection
            try {
                clientSocket = new Socket(clientAddr.getAddress(), clientAddr.getPort());
            } catch (IOException e) {
                LOG.error("Could not establish connection with client: " + clientAddr);
                LOG.error(e.getMessage());
                e.printStackTrace();
            }

            lobbyClients[idx] = new SocketBundle(clientSocket); //add client to lobby

        }

        //send client addresses to all clients
        for(int idx1 = 0; idx1 < LOBBY_SIZE; idx1++) {

            String joinLobbyMessage = Messages.buildJoinLobbyMessage(LOBBY_SIZE, idx1); //message to initiate clients
            lobbyClients[idx1].sendMessage(joinLobbyMessage); //tell client lobby is starting

            //send info for each client
            for(int idx2 = 0; idx2 < LOBBY_SIZE; idx2++) {

                InetSocketAddress clientAddr = lobbyClients[idx2].getSocketAddress(); //client address
                String hostname = clientAddr.getHostString(); //client address
                int port = clientAddr.getPort(); //client port

                //send address info
                lobbyClients[idx1].sendMessage(Messages.buildClientInfoMessage(hostname, port, idx2));

            }

        }

        //tell clients to start game
        for(SocketBundle client : lobbyClients) {
            client.sendMessage(Messages.GAME_START);
        }

        if(STOP_ON_LOBBY_CREATION) {
            System.exit(0);
        }

    }
    
}
