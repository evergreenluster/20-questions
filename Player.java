import java.io.*;
import java.net.*;

/**
 * @class Player
 * @brief Manages player information and communication
 *
 * @details This class represents a player in the 20 Questions game system.
 * It encapsulates the player's connection details (socket and streams),
 * identity (username), and provides methods to access and modify this 
 * information. Each Player object maintains its own communication channels
 * with the client through input and output streams.
 */
public class Player 
{
    private final Socket socket;
    private String username = " ";
    private final DataInputStream input;
    private final DataOutputStream output;

    /**
     * @brief Initializes a player with connection and identity information
     * 
     * @param socket The socket connection to the player's client
     * @param username The player's chosen display name
     * @param input Stream for receiving messages from the player
     * @param output Stream for sending messages to the player
     * 
     * @details Creates a Player object that represents a connected client
     * in the game system. The socket and streams provide the communication
     * channel, while the username provides identification during gameplay.
     */
    public Player(Socket socket, String username, 
        DataInputStream input, DataOutputStream output)
    {
        this.socket = socket;   
        this.username = username;
        this.input = input;
        this.output = output;
    }

    /**
     * @brief Retrieves the player's socket connection
     * @return The Socket object connecting to the player's client
     */
    public Socket getSocket()
    {
        return socket;
    }

    /**
     * @brief Updates the player's display name
     * @param username The new username to assign to this player
     */
    public void setUsername(String username)
    {
        this.username = username;
    }

    /**
     * @brief Retrieves the player's current username
     * @return The player's display name
     */
    public String getUsername()
    {
        return username;
    }

    /**
     * @brief Gets the input stream for receiving data from this player
     * @return DataInputStream connected to the player's client
     */
    public DataInputStream getInputStream()
    {
        return input;
    }

    /**
     * @brief Gets the output stream for sending data to this player
     * @return DataOutputStream connected to the player's client
     */
    public DataOutputStream getOutputStream()
    {
        return output;
    }
}
