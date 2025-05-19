import java.io.*;
import java.net.*;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 *  future enhancement: 
 *  - implement graceful shutdown of server with proper resource cleanup
 *  - receive port number from command line arguments
 */

/**
 * Server implementation for a multiplayer 20 Questions game.
 *
 * This server manages client connections. It accepts connections 
 * on a specified port and assigns each client to a handler thread 
 * from a fixed-size thread pool.
 */
public class Server 
{
    private Socket clientSocket;         
    private ServerSocket serverSocket;   

    /**
     * Thread pool that manages concurrent client connections.
     * 
     * Limited to 20 concurrent threads to prevent resource exhaustion while 
     * supporting multiple simultaneous game sessions. Static to allow access 
     * across all server components.
     */
    protected static ExecutorService threadPool = Executors.newFixedThreadPool(20);

    /**
     * Tracks all players currently connected, making resource cleanup easier.
     */
    protected static Vector<Player> allPlayers = new Vector<>();

    /**
     * Tracks players waiting to be matched with opponents.
     * 
     * Players are added here when they connect and removed when matched
     * with another player to form a game session. Used by the matchmaking system.
     */
    protected static Vector<Player> waitingQueue = new Vector<>();   

    /**
     * Tracks players currently participating in active games.
     * 
     * Players move here from the waitingQueue when matched and return to the
     * waitingQueue when their game ends. Used to monitor active game sessions.
     */
    protected static Vector<Player> playingList = new Vector<>();   
    
    /**
     * Constructor that initializes and runs the server.
     * 
     * Creates a server socket that continuously accepts client connections
     * and passes each connected client to a LogPlayer handler running in the thread 
     * pool. The server runs indefinitely until interrupted by an exception or
     * external termination signal.
     * 
     * @param port The port number on which the server listens for connections.
     */
    public Server(int port)
    {
        try
        {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started.");

            // at the moment, it only terminates if ctrl+c is pressed
            while (true)
            {
                System.out.println("\nWaiting for players...");
                
                clientSocket = serverSocket.accept();  
                System.out.println("\nPlayer connected.");    

                threadPool.submit(new LogPlayer(clientSocket));
            }
        }
        catch(IOException e)
        {
            System.out.println("\nError creating server socket: " + e.getMessage());
        }
        finally 
        {   
            // Clean up resources
            try 
            {
                if (serverSocket != null) serverSocket.close();
                if (clientSocket != null) clientSocket.close();
                
                threadPool.shutdown();
                
                // Close all player connections
                for (Player player : allPlayers) 
                {
                    if (player.getSocket() != null) player.getSocket().close();
                }
            } 
            catch (IOException e) 
            {
                System.out.println("\nError closing resources: " + e.getMessage());
            }
        }
    }

    /**
     * Entry point for the game server application.
     * 
     * Creates a Server instance on port 5000 to start the game server.
     * 
     * @param args Command line arguments (not used)
     */
    public static void main(String args[])
    {
        Server server = new Server(5000);  // create server on port 5000
    }
}