import java.io.*;
import java.net.*;

/*
 *  future enhancements: 
 *  - generate a random default username so players have immediate access to the main menu
 *  - no duplicate usernames
 */

/**
 * Handles initial client connection and user authentication for the 20 Questions game.
 * 
 * LogPlayer manages when a new client connects to the server. It collects the player's 
 * username, creates a Player object to represent them in the system, and then hands them 
 * off to PlayerManager for menu interaction and game matchmaking. Each LogPlayer instance 
 * runs in its own thread from the server's thread pool to handle multiple simultaneous 
 * connections.
 */
public class LogPlayer implements Runnable
{
    private final Socket clientSocket;  
    private DataInputStream in;        
    private DataOutputStream out;       

    /**
     * Initializes a new LogPlayer for a connected client.
     * 
     * Sets up the input and output streams for communication with the client.
     * If stream creation fails, the error is logged but the LogPlayer instance
     * is still created to allow for graceful error handling in the run() method.
     * 
     * @param clientSocket The socket connection to the newly connected client.
     */
    public LogPlayer(Socket clientSocket)
    {
        this.clientSocket = clientSocket;

        try 
        {
            // wrap streams in buffered versions for better performance
            in = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
            out = new DataOutputStream(clientSocket.getOutputStream());
        }
        catch(IOException e)
        {
            System.out.println("Error creating streams: " + e.getMessage());
        }
    }
    
    /**
     * Executes the client onboarding process.
     * 
     * This method handles the complete flow from initial connection to transferring
     * the client to the main game system:
     * 1. Sends welcome message and prompts for username
     * 2. Receives and stores the player's chosen username
     * 3. Creates a Player object to represent this client in the system
     * 4. Adds the player to the server's tracking collection
     * 5. Hands off the player to PlayerManager for menu interaction
     * 
     * If any step fails due to network issues, the connection is properly closed
     * to prevent resource leaks.
     */
    public void run()
    {
        String username = "";

        try
        {
            out.writeUTF("\n| 20  Questions |");
            out.writeUTF("\nEnter your username: ");
            out.flush();
        
            username = in.readUTF();
            
            // create player object with connection details and username
            Player player = new Player(clientSocket, username, in, out);

            Server.allPlayers.addElement(player);
            
            // transfer control to playermanager for menu and matchmaking
            Server.threadPool.submit(new PlayerManager(player));
        }
        catch (IOException e)
        {
            System.out.println("Error while collecting username: " + e.getMessage());

            // clean up connection if onboarding fails
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
