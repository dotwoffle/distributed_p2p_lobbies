package util.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;


/**Utility class to hold a Socket along with some other useful information.*/
public class SocketBundle {
    
    /*Variables*/

    /**The socket connection.*/
    private Socket socket;
    /**The remote to local input stream.*/
    private BufferedReader in;
    /**The local to remote output stream.*/
    private PrintWriter out;


    /*Constructor*/

    /**Creates a new SocketBundle object.
     * @param socket The socket to represent.
    */
    public SocketBundle(Socket socket) {

        //init

        this.socket = socket;
        try {
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            System.err.println("Error creating socket");
            e.printStackTrace();
        }

    }


    /*Methods*/

    /**Reads a message from the socket.
     * @return The message that is read.
    */
    public String getMessage() {
        
        String message = null;
        try {
            message = in.readLine();
        } catch (IOException e) {
            System.err.println("Could not read message");
            e.printStackTrace();
        }

        return message;

    }

    /**Prints a line to the output stream.
     * @param message The message to send.
    */
    public void sendMessage(String message) {
        out.println(message);
    }

    /**Closes the socket.*/
    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            System.err.println("Error closing socket");
            e.printStackTrace();
        }
    }

    /**Returns the address and port of this socket.*/
    public InetSocketAddress getSocketAddress() {
        return new InetSocketAddress(socket.getInetAddress(), socket.getPort());
    }

}
