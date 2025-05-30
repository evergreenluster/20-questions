import java.io.*;

/*
 *  future enhancements:
 *  - implement timeout in case there isn't a match (return control to playermanager)
 */

/**
 * Handles matching up players and starting their game session.
 * 
 * Displays a message telling the player their "waiting to be matched".
 * If the waitingQueue has at least two players, it teams the first two players
 * of the queue up and passes them to the GameSession handler thread to manage
 * the game.
 */
public class MatchPlayer implements Runnable
{
    Player player;

    /**
     * Initializes the matchmaker for a specific player
     * 
     * @param player The player to be matched with an opponent
     */
    public MatchPlayer(Player player)
    {
        this.player = player;
    }

    /**
     * Executes the player matching process
     * 
     * This method:
     * 1. Adds player to the waiting queue
     * 2. Notifies the player they're waiting to be matched
     * 3. Pauses briefly to allow other players to join the queue
     * 4. Continuously checks for at least two players in the waiting queue
     * 5. When two players are available, creates a game session and starts it
     * 
     * Note: This method contains a potentially blocking infinite loop
     * that waits until another player becomes available for matching.
     */
    public void run()
    {
        // phase 1: add player to waiting queue
        Server.waitingQueue.addElement(player);

        // phase 2: notify the player of matchmaking
        try 
        {
            player.getOutputStream().writeUTF("\nWaiting to be matched...");
        }
        catch(IOException e)
        {
            System.out.println("Error sending to client: " + e.getMessage());
        }

        // phase 3: pauses briefly to allow other players to join 
        // also used as a visual buffer if there is already a match available
        try
        {
            Thread.sleep(5000);
        }
        catch(InterruptedException e)
        {
            Thread.currentThread().interrupt();
        }

        // phase 4: continuously checks for a match
        while(true)
        {
            if (Server.waitingQueue.size() >= 2)
            {
                Player player1 = Server.waitingQueue.firstElement();
                Server.waitingQueue.removeElementAt(0);
                Player player2 = Server.waitingQueue.firstElement();
                Server.waitingQueue.removeElementAt(0);

                Server.playingList.addElement(player1);
                Server.playingList.addElement(player2);

                // phase 5: create gamesession when a match is made
                Server.threadPool.submit(new GameSession(player1, player2));

                System.out.println("\nGame session created.");

                break;
            }
        }
    }
}
