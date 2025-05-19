import java.io.*;

/**
 * Manages player lifecycle and menu interactions throughout the game.
 * 
 * PlayerManager serves as the central hub for player interactions outside of 
 * active games. It displays a main menu with three core options: starting a 
 * new game (which triggers matchmaking), changing username, or exiting the 
 * game entirely. Each player has their own PlayerManager instance running 
 * in the thread pool to handle their menu interactions independently.
 */
public class PlayerManager implements Runnable
{
    private final Player player;

    private final DataInputStream in;
    private final DataOutputStream out;

    /**
     * Initializes the PlayerManager for a specific player.
     * 
     * Sets up references to the player object and extracts their I/O streams
     * for direct communication. This avoids repeated method calls during 
     * menu interactions.
     * 
     * @param player The player whose menu interactions this manager will handle.
     */
    public PlayerManager(Player player)
    {
        this.player = player;

        this.in = player.getInputStream();
        this.out = player.getOutputStream();
    }

    /**
     * Displays the main menu options to the player.
     * 
     * Sends a formatted menu showing the three available actions:
     * 1. Play Game - enter matchmaking to find an opponent
     * 2. Change Username - update display name
     * 3. Exit - disconnect from server
     */
    private void showMainMenu()
    {
        try
        {
            out.writeUTF("""
                    \n1. Play Game
                    2. Change Username
                    3. Exit
                    """);
        }
        catch(IOException e)
        {
            System.out.println("Error sending to client: " + e.getMessage());
        }
    }

    /**
     * Executes the main menu loop for player interaction.
     * 
     * This method manages the complete player experience outside of games:
     * 1. Displays menu options repeatedly until player makes a choice
     * 2. Executes the chosen action (play, change username, or exit)
     * 3. For play option: transfers player to matchmaking system
     * 4. For username change: prompts for and updates player's display name
     * 5. For exit: cleanly disconnects player and releases resources
     * 
     * The loop continues until the player chooses to play a game or exit,
     * allowing multiple username changes without reconnection.
     */
    public void run()
    {
        boolean exit = false;
        while (!exit)
        {
            showMainMenu();

            int decision = 0;
            while (decision != 1 && decision != 2 && decision != 3)
            {
                try 
                {
                    out.writeUTF("Enter your decision: ");
                } 
                catch (IOException e) 
                {
                    System.out.println("\nError sending to client: " + e.getMessage());
                }

                try
                {
                    decision = Integer.parseInt(in.readUTF());
                }
                catch(IOException e)
                {
                    System.out.println("\nError receiving from client: " + e.getMessage());
                }
            }

            switch (decision)
            {
                // Play game
                case 1 ->
                {
                    Server.threadPool.submit(new MatchPlayer(player));

                    exit = true;
                }
                // Change username
                case 2 ->
                {
                    String username = "";

                    // Ensure new username isn't empty
                    while (username.trim().isEmpty())
                    {
                        try
                        {
                            out.writeUTF("\nEnter new username: ");
                            username = in.readUTF();
                        }
                        catch(IOException e)
                        {
                            System.out.println("Error while collecting username: " + e.getMessage());
                        }
                    }

                    player.setUsername(username);
                }
                // Exit game
                case 3 ->
                {
                    Server.allPlayers.removeElement(player);

                    System.out.println("\nPlayer disconnected from server."); 
                    
                    try
                    {
                        if (in != null) in.close();
                        if (out != null) out.close();
                        if (player.getSocket() != null) player.getSocket().close();
                    }
                    catch(IOException e)
                    {
                        System.out.println("Error closing resources: " + e.getMessage());
                    }
                    
                    exit = true;
                }
            }
        }       
    }
}
