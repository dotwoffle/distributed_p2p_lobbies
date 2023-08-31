package tests.basicgame;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**Driver class for the basic P2P client.*/
public class BasicClientDriver {
    
    public static void main(String[] args) {

        int port = Integer.parseInt(args[0]);
        int serverPort = Integer.parseInt(args[2]);
        InetSocketAddress serverAddr = null;

        try {
            serverAddr = new InetSocketAddress(InetAddress.getByName(args[1]), serverPort);
        } catch (UnknownHostException e) {
            System.err.println("Couldn't resolve host: " + args[1]);
            e.printStackTrace();
            System.exit(1);
        }

        BasicClient c = new BasicClient(port);
        c.joinGame(serverAddr);

    }

}