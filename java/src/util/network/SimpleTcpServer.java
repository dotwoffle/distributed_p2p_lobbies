package util.network;

import java.io.IOException;
import java.net.ServerSocket;

import util.Logger;
import util.Logger.Level;


/**Base class for an object that implements a simple TCP server.*/
public abstract class SimpleTcpServer {
    
    /*Variables*/

    /**Logger object.*/
    private final Logger LOG;
    /**Server socket that listens for client connections.*/
    private ServerSocket server;


    /*Constructor*/
    
    /**Initializes the server.
     * @param port The port this server should listen on.
     * @param logName The name of the logger to use.
    */
    protected SimpleTcpServer(int port, String logName) {

        LOG = new Logger(logName);
        LOG.setLogLevel(Level.DEBUG);

        try {
            server = new ServerSocket(port);
        } catch (IOException e) {
            LOG.error("Server couldn't be opened on port " + port);
            LOG.error(e.getMessage());
            e.printStackTrace();
        }

        LOG.info("Server socket open on port " + port);

    }


    /*Methods*/

    /**Starts the server.*/
    public void start() {

        LOG.info("Starting TCP server");

        //start listening on new thread
        new Thread(new Runnable() {

            @Override
            public void run() {
                listenLoop();
            }

        }).start();

    }

    /**Repeatedly listens for incoming conections and services them.*/
    private void listenLoop() {

        while(true) {

            try {

                SocketBundle client = new SocketBundle(server.accept()); //get next client connection

                LOG.info("Client connected: " + client.getSocketAddress());

                //start service thread
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        LOG.info("Starting service thread for client " + client.getSocketAddress());
                        serviceClient(client);
                        client.close();
                    }
                    
                }).start();

            } catch (IOException e) {
                LOG.warn("Error occurred while client was conecting");
                LOG.warn(e.getMessage());
                e.printStackTrace();
            }

        }

    }

    /**Returns the port number this server is listening on.*/
    public int getPort() {
        return server.getLocalPort();
    }

    /**Handles a specific client when they connect.*/
    protected abstract void serviceClient(SocketBundle client);

}
