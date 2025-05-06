import java.io.*;
import java.net.*;
import java.util.Vector;

/**
 * @class Server
 * @brief Server class that implements a number guessing game over socket connection.
 *
 * This server accepts a client connection, generates a random number,
 * and manages the game where the client tries to guess the number.
 * The server provides feedback after each guess and tracks attempts.
 */
public class Server 
{
    private Socket clientSocket = null;       // client socket
    private ServerSocket serverSocket = null; // server socket
    private DataInputStream input = null;   // input stream from client
    private DataOutputStream output = null; // output stream to client
    private Vector<Player> waitingQueue = new Vector<>();    
    private Vector<Player> playingQueue = new Vector<>();

    /**
     * @brief Constructor that initializes the server and runs the game
     * @param port The port number on which the server listens for connections
     * 
     * The constructor:
     * - Creates a server socket on the specified port
     * - Accepts a client connection
     * - Generates a random number for the client to guess
     * - Manages the game loop, processing guesses and providing feedback
     * - Cleans up resources when the game ends
     */
    public Server(int port)
    {
        try
        {
            serverSocket = new ServerSocket(port);    // create server socket
            System.out.println("Server started.");

            String username = ""; // used to store username from client

            while (true)
            {
                if (waitingQueue.size() < 2)
                {
                    System.out.println("Waiting for players...");
                }

                clientSocket = serverSocket.accept();
                System.out.println("Player connected.");

                input = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
                output = new DataOutputStream(clientSocket.getOutputStream());

                output.writeUTF("\n|| 20 Questions ||\n");
                output.flush();
    
                try
                {
                    try 
                    {
                        output.writeUTF("Enter your username: ");
                        output.flush();
                    }
                    catch (IOException m)
                    {
                        System.out.println("Failed to send message to client: " + m.getMessage());

                        try
                        {
                            clientSocket.close();
                        }
                        catch (IOException c)
                        {
                            System.out.println("Error closing socket after connection issue.");
                        }
                    }
                
                    username = input.readUTF(); // get player's username
                    
                    Player newPlayer = new Player(clientSocket, username);
                    waitingQueue.addElement(newPlayer);
                }
                catch (IOException u)
                {
                    System.out.println("Connection lost while collecting username.");

                    try
                    {
                        clientSocket.close();
                    }
                    catch (IOException c)
                    {
                        System.out.println("Error closing socket after connection issue.");
                    }

                    return;
                }

                if (waitingQueue.size() == 2)
                {
                    Player player1 = waitingQueue.firstElement();
                    waitingQueue.removeElementAt(0);
                    Player player2 = waitingQueue.firstElement();
                    waitingQueue.removeElementAt(0);

                    playingQueue.addElement(player1);
                    playingQueue.addElement(player2);

                    GameSession gameSession = new GameSession(player1, player2);

                    Thread gameThread = new Thread(gameSession);

                    gameThread.start();
                }
            }
        }
        catch(IOException i)
        {
            System.out.println(i);
        }
        finally 
        {   
            // close resources
            try 
            {
                if (output != null) output.close();
                if (input != null) input.close();
                if (clientSocket != null) clientSocket.close();
            } 
            catch (IOException e) 
            {
                System.out.println("Error closing resources: " + e);
            }
        }
    }

    /**
     * @brief Main method to start the server
     * @param args Command line arguments (not used)
     * 
     * Creates a new Server instance on port 5000 to start the game.
     */
    public static void main(String args[])
    {
        Server server = new Server(5000); // create server on port 5000
    }
}