import java.io.*;
import java.net.*;

/**
 * Represents a connected player in the 20 Questions game system.
 *
 * This class encapsulates all information and communication channels for a
 * single player, including their network connection (socket and streams)
 * and identity (username). Each Player object serves as the bridge between
 * the game logic and a specific client connection, providing organized
 * access to communication methods while maintaining player state.
 */
public class Player 
{
    private final Socket socket;
    private String username = "";
    private final DataInputStream input;
    private final DataOutputStream output;

    /**
     * Initializes a new Player with connection and identity information.
     * 
     * Creates a Player object that represents a connected client in the game
     * system. The socket and streams establish the communication channel, while
     * the username provides identification during gameplay. This constructor
     * assumes that the streams have already been properly initialized and
     * connected to the client.
     * 
     * @param socket The socket connection to the player's client.
     * @param username The player's chosen display name.
     * @param input Stream for receiving messages from the player.
     * @param output Stream for sending messages to the player.
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
     * Retrieves the player's socket connection.
     * 
     * Provides access to the underlying socket for operations like checking
     * connection status or closing the connection when the player leaves.
     * 
     * @return The Socket object connecting to the player's client.
     */
    public Socket getSocket()
    {
        return socket;
    }

    /**
     * Updates the player's display name.
     * 
     * Allows the player to change their username during their session
     * through the PlayerManager menu system. The new username takes 
     * effect immediately for all subsequent game interactions.
     * 
     * @param username The new username to assign to this player.
     */
    public void setUsername(String username)
    {
        this.username = username;
    }

    /**
     * Retrieves the player's current display name.
     * 
     * @return The player's username as it will appear to other players.
     */
    public String getUsername()
    {
        return username;
    }

    /**
     * Gets the input stream for receiving data from this player.
     * 
     * Provides access to the stream used for reading messages, menu choices,
     * and game responses from the player's client. Other classes use this
     * stream to implement the various communication protocols.
     * 
     * @return DataInputStream connected to the player's client.
     */
    public DataInputStream getInputStream()
    {
        return input;
    }

    /**
     * Gets the output stream for sending data to this player.
     * 
     * Provides access to the stream used for sending game updates, menu
     * prompts, and system messages to the player's client. Other classes
     * use this stream to implement the various communication protocols.
     * 
     * @return DataOutputStream connected to the player's client.
     */
    public DataOutputStream getOutputStream()
    {
        return output;
    }
}
