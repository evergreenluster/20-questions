import java.io.*;
import java.net.*;

// separate thread into two
// one for creating a new player and one to handle the main menu
public class LogPlayer implements Runnable
{
    private final Socket clientSocket;  // socket for server connection
    private DataInputStream in;         // reads to server
    private DataOutputStream out;       // writes to server

    public LogPlayer(Socket clientSocket)
    {
        this.clientSocket = clientSocket;

        try 
        {
            in = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
            out = new DataOutputStream(clientSocket.getOutputStream());
        }
        catch(IOException e)
        {
            System.out.println("Error creating streams: " + e.getMessage());
        }
    }
    
    public void run()
    {
        String username = ""; // used to store username from client

        try
        {
            out.writeUTF("\nEnter your username: ");
            out.flush();
        
            username = in.readUTF();  // get player's username
            
            Player player = new Player(clientSocket, username, in, out);
            Server.allPlayers.addElement(player);
            
            Server.threadPool.submit(new PlayerManager(player));
        }
        catch (IOException e)
        {
            System.out.println("Error while collecting username: " + e.getMessage());

            try
            {
                clientSocket.close();
            }
            catch (IOException c)
            {
                System.out.println("Error closing socket after connection issue.");
            }
        }
    }
}
