import java.io.*;
import java.net.*;
import java.util.Scanner;

/**
 * @class Client
 * @brief Client class that connects to a number guessing game server.
 *
 * This client establishes a connection with the game server, receives
 * and displays messages from the server, and sends user guesses until
 * the correct number is guessed.
 */
public class Client
{
    private Socket sock = null;             // socket for server connection
    private DataOutputStream sOutput = null; // output stream to server
    private DataInputStream sInput = null;  // input stream from server

    /**
     * @brief Constructor that connects to the server and runs the game
     * @param addr The server address to connect to
     * @param port The port number on which the server is listening
     * 
     * The constructor:
     * - Establishes a connection to the specified server
     * - Sets up input and output streams for communication
     * - Processes server messages and responds appropriately
     * - Allows the user to input guesses via the console
     * - Displays feedback from the server
     * - Closes the connection when the game ends
     */
    public Client(String addr, int port)
    {
        try
        {
            sock = new Socket(addr, port);   // create client socket
            System.out.println("Connected");

            sOutput = new DataOutputStream(sock.getOutputStream()); // create output stream
            sInput = new DataInputStream(new BufferedInputStream(sock.getInputStream())); // create input stream
            Scanner scanner = new Scanner(System.in); // scanner for reading user input
            String sIn = ""; // used to store server messages
            String in = "";  // used to store client messages

            boolean running = true; // used to control the game loop
            while (running)
            {
                try
                {
                    sIn = sInput.readUTF();
                }
                catch(IOException e)
                {
                    System.out.println("Error receiving from server: " + e.getMessage());
                }

                System.out.println(sIn);

                in = scanner.nextLine();

                try
                {
                    sOutput.writeUTF(in);
                    sOutput.flush();
                }
                catch(IOException e)
                {
                    System.out.println("Error sending to server: " + e.getMessage());
                }
            }
        }
        catch (UnknownHostException u) // handle invalid server address
        {
            System.out.println(u);
            return;
        }
        catch (IOException i) // handle connection errors
        {
            System.out.println(i);
            return;
        }
        finally
        {
            // close resources
            try 
            {
                if (sOutput != null) sOutput.close();
                if (sInput != null) sInput.close();
                if (sock != null) sock.close();
            } 
            catch (IOException e) 
            {
                System.out.println("Error closing resources: " + e);
            }
        }
    }

    /**
     * @brief Main method to start the client
     * @param args Command line arguments (not used)
     * 
     * Creates a new Client instance that connects to a server on localhost:5000
     */
    public static void main(String[] args)
    {
        Client client = new Client("localhost", 5000);
    }
}