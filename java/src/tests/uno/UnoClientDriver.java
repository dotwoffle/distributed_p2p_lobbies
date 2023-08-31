package tests.uno;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class UnoClientDriver {

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

        UnoClient c = new UnoClient(port);
        c.joinGame(serverAddr);

    }
    
}
