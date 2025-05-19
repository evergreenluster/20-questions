import java.io.*; 
import java.util.concurrent.Callable;

/**
 * Handles collecting play-again decisions from individual players with timeout support.
 * 
 * This Callable implementation allows the GameSession to query multiple players
 * simultaneously about whether they want to play another round. Each PlayAgain
 * instance runs in its own thread, enabling the Future.get() pattern with timeouts
 * to gracefully handle players who don't respond within the allowed time frame.
 */
public class PlayAgain implements Callable<Boolean>
{
    private final Player player;

    /**
     * Initializes a PlayAgain task for a specific player.
     * 
     * @param player The player who will be prompted for their play-again decision.
     */
    public PlayAgain(Player player)
    {
        this.player = player;
    }

    /**
     * Prompts the player to decide whether they want to play again.
     * 
     * Continuously prompts the player until they provide a valid yes/no response.
     * GameSession handles the timeout behavior by using Future.get() with a timeout
     * parameter. If this method doesn't return within the timeout period, the Future
     * will be cancelled.
     * 
     * @return true if the player chooses to play again, false otherwise.
     * @throws Exception If communication with the player fails.
     */
    @Override
    public Boolean call() throws Exception
    {
        char decision = ' ';

        while (decision != 'y' && decision != 'n')
        {
            try
            {
                player.getOutputStream().writeUTF("\n(Y)es, (N)o | 15 sec. to decide\nEnter your decision: ");
                String input = player.getInputStream().readUTF();

                if (!input.isEmpty()) 
                {
                    decision = input.toLowerCase().charAt(0);
                }
            }
            catch(IOException e)
            {
                throw new Exception("Player communication failed during play-again query", e);
            }
        }

        return decision == 'y';
    }
}
