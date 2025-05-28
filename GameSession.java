import java.io.*;
import java.util.Random;
import java.util.concurrent.*;

/*
 *  future enhancements: 
 *  - handle player disconnect mid-game 
 *  - function to handle graceful exit when playAgain is false
 *  - handle empty answer input edge case 
 *  - add gameID functionality for easier debugging
 * 
 *  new handler class:
 *  - displayManager (handles titles and visual separators)
 */

/**
 * Manages a game session between two players in the 20 Questions game.
 * 
 * GameSession handles the complete lifecycle of a game match between
 * two players, including role assignment (Game Master vs Guesser), message
 * passing, game state management, and win/loss conditions. It runs in its own
 * thread to allow multiple concurrent games on the server.
 */
class GameSession implements Runnable 
{
     /**
     * Represents possible answer types from the Game Master.
     * 
     * Used to standardize communication between players. The Game Master
     * responds to the Guesser's questions using one of these answer types.
     */
    public enum Answer
    {
        YES('y'),
        NO('n'),
        MAYBE('m'),
        CORRECT('c');

        private final char inputChar; // character representing the answer in user input

        /**
         * Constructor that maps answers to their input characters.
         * 
         * @param inputChar The character that represents this answer.
         */
        Answer(char inputChar)
        {
            this.inputChar = inputChar;
        }

        /**
         * Retrieves the character associated with this answer.
         * 
         * @return The character representation of this answer.
         */
        public char getInputChar()
        {
            return inputChar;
        }

        /**
         * Converts a character input to the corresponding Answer.
         * 
         * @param c The character to convert.
         * @return The Answer enum value, or null if not recognized.
         */
        public static Answer fromChar(char c)
        {
            char lowerChar = Character.toLowerCase(c);

            switch(lowerChar)
            {
                case 'y': return YES;
                case 'n': return NO;
                case 'm': return MAYBE;
                case 'c': return CORRECT;
                default: return null;
            }
        }
    }

    /** First player connected to this game session. */
    private final Player player1;
    /** Second player connected to this game session. */
    private final Player player2;  

    /** Player assigned the Game Master role (chooses the subject). */
    private Player gameMaster;    
    /** Player assigned the Guesser role (asks questions). */
    private Player guesser;    

    /** Input stream for receiving messages from player1. */
    private final DataInputStream inP1;
    /** Input stream for receiving messages from player2. */
    private final DataInputStream inP2;
    /** Output stream for sending messages to player1. */
    private final DataOutputStream outP1;
    /** Output stream for sending messages to player2. */
    private final DataOutputStream outP2;

    /**
     * Initializes a game session between two players.
     * 
     * Sets up the communication streams and randomly assigns
     * initial player roles (Game Master vs Guesser).
     * 
     * @param player1 The first player
     * @param player2 The second player
     */
    public GameSession(Player player1, Player player2)
    {
        this.player1 = player1;
        this.player2 = player2;
        
        this.inP1 = player1.getInputStream();
        this.inP2 = player2.getInputStream();
        this.outP1 = player1.getOutputStream();
        this.outP2 = player2.getOutputStream();

        assignRoles(player1, player2);
    }
 
    /**
     * Randomly assigns the Game Master and Guesser roles.
     * 
     * Uses a random number generator to assign roles fairly
     * between the two players.
     * 
     * @param player1 The first player.
     * @param player2 The second player.
     */
    private void assignRoles(Player player1, Player player2)
    {
        Player[] players = {player1, player2};

        Random random = new Random();

        int i = random.nextInt(2);

        gameMaster = players[i];

        guesser = i == 0 ? players[1] : players[0];
    }

    /**
     * Sends a message to the Game Master.
     * 
     * Determines which player is the Game Master and sends the message 
     * to the appropriate output stream. For now, if a network error occurs 
     * (typically due to client disconnect), the error is logged but 
     * the game continues. 
     * 
     * @param message The message to send.
     */
    private void sendToGM(String message)
    {
        if (gameMaster.getSocket() == player1.getSocket())
        {
            try
            {
                outP1.writeUTF(message);
                outP1.flush();
            }
            catch(IOException e)
            {
                System.out.println("\nError sending message to Game Master: " + e.getMessage());
            }
        }
        else
        {
            try
            {
                outP2.writeUTF(message);
                outP2.flush();
            }
            catch(IOException e)
            {
                System.out.println("\nError sending message to Game Master: " + e.getMessage());
            }
        }
    }

    /**
     * Sends a message to the Guesser.
     * 
     * Determines which player is the Guesser and sends the message 
     * to the appropriate output stream. For now, if a network error occurs 
     * (typically due to client disconnect), the error is logged but 
     * the game continues.
     * 
     * @param message The message to send.
     */
    private void sendToGuesser(String message)
    {
        if (guesser.getSocket() == player1.getSocket())
        {
            try
            {
                outP1.writeUTF(message);
                outP1.flush();
            }
            catch(IOException e)
            {
                System.out.println("\nError sending message to Guesser: " + e.getMessage());
            }
        }
        else
        {
            try
            {
                outP2.writeUTF(message);
                outP2.flush();
            }
            catch(IOException e)
            {
                System.out.println("\nError sending message to Guesser: " + e.getMessage());
            }
        }
    }

    /**
     * Sends a message to both players simultaneously.
     * 
     * @param message The message to send.
     */
    private void sendToBoth(String message)
    {
        sendToGM(message);
        sendToGuesser(message);
    }

    /**
     * Creates a visual separation between the game and other console messages.
     * 
     * Sends a message containing a long sequence of underscores to both players.
     */
    private void sendVisualSeparator()
    {
        sendToBoth("________________________________________");
    }

    /**
     * Receives a message from the Game Master.
     * 
     * Determines which player is the Game Master and
     * receives the message from the appropriate input stream.
     * 
     * @return The message received, or empty string if connection fails.
     */
    private String receiveFromGM()
    {
        String message = "";

        if (gameMaster.getSocket() == player1.getSocket())
        {
            try
            {
                message = inP1.readUTF();
            }
            catch (IOException e)
            {
                System.out.println("\nError receiving from Game Master: " + e.getMessage());
            }
        }
        else
        {
            try
            {
                message = inP2.readUTF();
            }
            catch (IOException e)
            {
                System.out.println("\nError receiving from Game Master: " + e.getMessage());
            }
        }

        return message;
    }

    /**
     * Receives a message from the Guesser.
     * 
     * Determines which player is the Guesser and
     * receives the message from the appropriate input stream.
     * 
     * @return The message received, or empty string if connection fails.
     */
    private String receiveFromGuesser()
    {
        String message = "";

        if (guesser.getSocket() == player1.getSocket())
        {
            try
            {
                message = inP1.readUTF();
            }
            catch (IOException e)
            {
                System.out.println("\nError receiving from Guesser: " + e.getMessage());
            }
        }
        else
        {
            try
            {
                message = inP2.readUTF();
            }
            catch (IOException e)
            {
                System.out.println("\nError receiving from Guesser: " + e.getMessage());
            }
        }

        return message;
    }

    /**
     * Executes the game session between the two players.
     * 
     * Main game loop phases:
     * 1. Introduces players to each other
     * 2. Assigns and communicates player roles
     * 3. Prompts the Game Master to choose a subject
     * 4. Manages the question and answer process and tracks the question count 
     * 5. Determines win/loss 
     * 6. Handles the "play again" functionality
     * 
     * The game ends when the Guesser correctly identifies the subject,
     * when 20 questions have been asked without success, or when
     * players choose not to play again.
     */
    public void run()
    {
        sendVisualSeparator();

        // phase 1: player introduction
        sendToGM("\nYOUR OPPONENT IS " + guesser.getUsername());
        sendToGuesser("\nYOUR OPPONENT IS " + gameMaster.getUsername());

        boolean playAgain = true;
        while(playAgain)
        {
            // phase 2: role assignment
            assignRoles(player1, player2);

            sendVisualSeparator();
            sendToGM("\nYou are the Game Master.");
            sendToGuesser("\nYou are the Guesser.");

            // phase 3: game master chooses a subject
            sendToGuesser("\n" + gameMaster.getUsername() + " is thinking of a subject...");
            
            String subject = "";

            // ensure we receive a non-empty question from the guesser
            // empty questions could occur from network issues or accidental sends
            while (subject.trim().isEmpty())
            {
                sendToGM("\nChoose a subject: ");

                subject = receiveFromGM();
            }


            sendToGuesser("\n" + gameMaster.getUsername() + " has chosen a subject.");

            // phase 4: question and answer process
            boolean win = false;
            int count = 0;
            while(!win && count < 20)
            {
                String question = "";
                char answerIn = ' ';  // raw input character from game master
                Answer answerOut;     // validated enum value sent to guesser

                sendToGM("\n" + guesser.getUsername() + " is thinking of a question...");

                // ensure we recieve a non-empty answer from the game master
                // empty answers could occur from network issues or accidental sends
                while (question.trim().isEmpty())
                {
                    sendToGuesser("\nEnter your question: ");

                    question = receiveFromGuesser();
                }
            
                count++;

                sendToGM("\nQuestion: " + question);

                while ((answerIn != 'y' && answerIn != 'n' && answerIn != 'm' && answerIn != 'c'))
                {
                    sendToGM("\n(Y)es, (N)o, (M)aybe, (C)orrect\nEnter your answer: ");

                    answerIn = receiveFromGM().charAt(0);
                }

                answerOut = Answer.fromChar(answerIn);

                // phase 5.0: determining win/loss (correct answer guessed)
                if (answerIn == 'c')
                {
                    sendToGuesser("\nYou won! The answer was " + subject + ".");
                    sendToGM("\nYou lost!");

                    win = true;
                }
                else 
                {
                    sendToGuesser("\nAnswer: " + answerOut);
                }
            }

            // phase 5.1: determining win/loss (question limit reached)
            if (count == 20)
            {
                sendToGM("\nYou won! " + guesser.getUsername() + " ran out of questions.");
                sendToGuesser("\nYou lost! The answer was '" + subject + "'.");
            }

            // phase 6: play again
            sendToGM("\nPLAY AGAINST " + guesser.getUsername() + " AGAIN?");
            sendToGuesser("\nPLAY AGAINST " + gameMaster.getUsername() + " AGAIN?");

            // both players are asked if they want to play against the same opponent again
            Future<Boolean> decisionGM = Server.threadPool.submit(new PlayAgain(gameMaster));
            Future<Boolean> decisionGuesser = Server.threadPool.submit(new PlayAgain(guesser));
            
            // both players have 15 seconds to submit their answers
            try 
            {
                Boolean againGM = decisionGM.get(15, TimeUnit.SECONDS);
                Boolean againGuesser = decisionGuesser.get(15, TimeUnit.SECONDS);

                playAgain = againGM && againGuesser;
            }
            // if either times out or declines, the session ends gracefully
            catch(TimeoutException e) 
            {
                System.out.println("\nPlay again frame timed out: " + e.getMessage());
                decisionGM.cancel(true);
                decisionGuesser.cancel(true);

                sendToBoth("\nPlay again timed out!");

                playAgain = false;  // main game loop is exited
            }
            catch(InterruptedException e)
            {
                System.out.println("\nThread was interrupted during play again frame: " + e.getMessage());
            }
            catch(ExecutionException e)
            {
                Throwable cause = e.getCause();

                if (cause != null)
                {
                    System.out.println("\nCause of the exception: " + cause);
                }
                else
                {
                    System.out.println("\nNo cause found for this exception.");
                }
            }
        }

        // notify players of impending disconnect
        sendToBoth("\nBoth of you didn't want to play again.\nSession ending...");

        sendVisualSeparator();

        Server.playingList.removeElement(player1);
        Server.playingList.removeElement(player2);

        Server.threadPool.submit(new PlayerManager(player1));
        Server.threadPool.submit(new PlayerManager(player2));
    }
} 