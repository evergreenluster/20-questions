import java.io.*;
import java.net.*;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @class Server
 * @brief Server implementation for a multiplayer 20 Questions game
 *
 * @details This server manages client connections, player matchmaking,
 * and game sessions using a multi-threaded architecture. It accepts
 * connections on a specified port and assigns each client to a handler
 * thread from a fixed-size thread pool.
 */
public class Server 
{
    private Socket clientSocket;         
    private ServerSocket serverSocket;   

    /**
     * @brief Thread pool that manages concurrent client connections
     * 
     * @details Limited to 20 concurrent threads to prevent resource exhaustion
     * while supporting multiple simultaneous game sessions. Static to allow
     * access across all server components.
     */
    protected static ExecutorService threadPool = Executors.newFixedThreadPool(20);

    /**
     * @brief Used to keep track of all players currently connected
     * 
     * @details Makes resource cleanup easier 
     */
    protected static Vector<Player> allPlayers = new Vector<>();

    /**
     * @brief Tracks players waiting to be matched with opponents
     * 
     * @details Players are added here when they connect and removed when matched
     * with another player to form a game session. Used by the matchmaking system.
     */
    protected static Vector<Player> waitingQueue = new Vector<>();   

    /**
     * @brief Tracks players currently participating in active games
     * 
     * @details Players move here from the waitingQueue when matched and return to
     * waitingQueue when their game ends. Used to monitor active game sessions.
     */
    protected static Vector<Player> playingQueue = new Vector<>();   
    
    /**
     * @brief Constructor that initializes and runs the server
     * @param port The port number on which the server listens for connections
     * 
     * @details Creates a server socket that continuously accepts client connections
     * and passes each connected client to a LogPlayer handler running in the thread 
     * pool. The server runs indefinitely until interrupted by an exception or
     * external termination signal.
     */
    public Server(int port)
    {
        try
        {
            serverSocket = new ServerSocket(port);  // create server socket
            System.out.println("Server started.\n");

            // implement a more graceful shutdown mechanism
            // at the moment, it only terminates if there's an exception
            // or ctrl+c is pressed (my method for testing)
            while (true)
            {
                System.out.println("Waiting for players...\n");
                
                clientSocket = serverSocket.accept();            // accept client connection
                System.out.println("Player connected.\n");    

                threadPool.submit(new LogPlayer(clientSocket));  // pass client to handler thread
            }
        }
        catch(IOException e)
        {
            System.out.println("Error creating server socket: " + e.getMessage());
        }
        finally 
        {   
            // close resources
            try 
            {
                if (serverSocket != null) serverSocket.close();
                
                if (clientSocket != null) clientSocket.close();
                
                threadPool.shutdown();
                
                // close all player connections
                for (Player player : allPlayers) 
                {
                    if (player.getSocket() != null) player.getSocket().close();
                }
            } 
            catch (IOException e) 
            {
                System.out.println("Error closing resources: " + e.getMessage());
            }
        }
    }

    // receive port number from command line arguments (future version)

    /**
     * @brief Entry point for the game server application
     * @param args Command line arguments (not used)
     * 
     * @details Creates a Server instance on port 5000 to start the game server.
     */
    public static void main(String args[])
    {
        Server server = new Server(5000); // create server on port 5000
    }
}