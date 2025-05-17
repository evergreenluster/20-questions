import java.io.*;
import java.net.*;
import java.util.Scanner;

// handle case where one player disconnects 
// pass them back to the main menu handler thread

/**
 * @class Client
 * @brief Client class that connects to the 20 Questions game server.
 */
public class Client
{
    private Socket clientSocket;    // socket for server connection
    private DataInputStream in;     // reads from client
    private DataOutputStream out;   // writes to client

    /**
     * @brief Constructor that connects to the server and runs the game
     * @param addr The server address to connect to
     * @param port The port number on which the server is listening
     */
    public Client(String addr, int port)
    {
        Scanner scanner = new Scanner(System.in);    // scanner for reading user input
        String message = "";                         // used to store server messages
        String input = "";                           // used to store client messages

        try
        {
            clientSocket = new Socket(addr, port);   // create client socket
            System.out.println("Connected");         // delete once game is running smoothly

            out = new DataOutputStream(clientSocket.getOutputStream());
            in = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream())); 

            while (true)
            {
                try
                {
                    message = in.readUTF();
                }
                catch(IOException e)
                {
                    System.out.println("\nDisconnected from server.");  

                    if (out != null) out.close();
                    if (in != null) in.close();
                    if (clientSocket != null) clientSocket.close();
                    scanner.close();

                    break;
                }

                System.out.print(message);

                if (message.charAt(message.length() - 2) == ':')
                {
                    input = scanner.nextLine();

                    try
                    {
                        out.writeUTF(input);
                        out.flush();
                    }
                    catch(IOException e)
                    {
                        System.out.println("Error sending to server: " + e.getMessage());
                    }
                }
                else
                {
                    System.out.print("\n");
                }    
            }
        }
        catch (UnknownHostException e)  // handle invalid server address
        {
            System.out.println(e);
        }
        catch (IOException e)  // handle connection errors
        {
            System.out.println(e);
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