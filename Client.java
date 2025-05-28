import java.io.*;
import java.net.*;
import java.util.Scanner;

/**
 * Client class that connects to the 20 Questions game server.
 */
public class Client
{
    private Socket clientSocket;
    private DataInputStream in;    
    private DataOutputStream out;   

    /**
     * Constructor that connects to the server and runs the game.
     * 
     * @param addr The server address to connect to.
     * @param port The port number on which the server is listening.
     */
    public Client(String addr, int port)
    {
        Scanner scanner = new Scanner(System.in);  // scanner for reading user input
        String message = "";                       // used to store server messages
        String input = "";                         // used to store client messages

        // attempts to connect to the server
        try
        {
            clientSocket = new Socket(addr, port); 

            out = new DataOutputStream(clientSocket.getOutputStream());
            in = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream())); 

            // main communication loop: receive message, check if input needed, respond 
            while (true)
            {
                try
                {
                    message = in.readUTF();
                }
                // connection lost or server server disconnected
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

                // all messages requiring input end in ": "
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
                // outputs a newline for consistent formatting when not prompting for input
                else 
                {
                    System.out.print("\n");  
                }    
            }
        }
        catch (UnknownHostException e) 
        {
            System.out.println(e);
        }
        catch (IOException e)  
        {
            System.out.println(e);
        }
    }

    /**
     * Main method to start the client.
     * 
     * Creates a new Client instance that connects to a server on localhost:5000.
     * 
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args)
    {
        Client client = new Client("localhost", 5000);
    }
}