import java.io.*;

/**
 * @class MatchPlayer
 * @brief Handles matching up players and starting their game session.
 * 
 * @details Displays a message telling the player their "waiting to be matched".
 * If the waitingQueue has at least two players, it teams the first two players
 * of the queue up and passes them to the GameSession handler thread to manage
 * the game.
 */
public class MatchPlayer implements Runnable
{
    Player player;

    /**
     * @brief Initializes the matchmaker for a specific player
     * @param player The player to be matched with an opponent
     */
    public MatchPlayer(Player player)
    {
        this.player = player;
    }

    /**
     * @brief Executes the player matching process
     * 
     * @details This method:
     * 1. Notifies the player they're waiting to be matched
     * 2. Pauses briefly to allow other players to join the queue
     * 3. Continuously checks for at least two players in the waiting queue
     * 4. When two players are available, creates a game session and starts it
     * 
     * Note: This method contains a potentially blocking infinite loop
     * that waits until another player becomes available for matching.
     */
    public void run()
    {
        // phase 1: notifies the player
        try 
        {
            player.getOutputStream().writeUTF("\nWaiting to be matched...");
        }
        catch(IOException e)
        {
            System.out.println("Error sending to client: " + e.getMessage());
        }

        // phase 2: pauses to allow other players to join
        try
        {
            Thread.sleep(5000);
        }
        catch(InterruptedException e)
        {
            Thread.currentThread().interrupt();
        }

        // implement timeouts in case there isn't a match
        // return play to main menu
        
        // phase 3: continuously checks if there's at least two players
        while(true)
        {
            // phase 4: creates a game session and starts it if there's at least two players
            if (Server.waitingQueue.size() >= 2)
            {
                Player player1 = Server.waitingQueue.firstElement();
                Server.waitingQueue.removeElementAt(0);
                Player player2 = Server.waitingQueue.firstElement();
                Server.waitingQueue.removeElementAt(0);

                Server.playingQueue.addElement(player1);
                Server.playingQueue.addElement(player2);

                Server.threadPool.submit(new GameSession(player1, player2));

                System.out.println("Game session created.\n");

                break;
            }
        }
    }
}
