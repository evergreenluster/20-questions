import java.io.*;
import java.util.Random;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane;

/**
 * @class GameSession
 * @brief Manages a game session between two players in the 20 Questions game
 * 
* @details GameSession handles the complete lifecycle of a game match between
 * two players, including role assignment (Game Master vs Guesser), message
 * passing, game state management, and win/loss conditions. It runs in its own
 * thread to allow multiple concurrent games on the server.
 */
class GameSession implements Runnable 
{
     /**
     * @enum Answer
     * @brief Represents possible answer types from the Game Master
     * 
     * @details Used to standardize communication between players. The Game Master
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
         * @brief Constructor that maps answers to their input characters
         * @param inputChar The character that represents this answer
         */
        Answer(char inputChar)
        {
            this.inputChar = inputChar;
        }

        /**
         * @brief Retrieves the character associated with this answer
         * @return The character representation of this answer
         */
        public char getInputChar()
        {
            return inputChar;
        }

        /**
         * @brief Converts a character input to the corresponding Answer
         * @param c The character to convert
         * @return The Answer enum value, or null if not recognized
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

    private final Player player1;  // first player in the game session
    private final Player player2;  // second player in the game session

    // player roles (will reference player1 and player2 once roles are assigned)
    private Player gameMaster;    // player who chooses the subject
    private Player guesser;       // player who asks the questions

    // input and output streams for both players
    private final DataInputStream inP1;
    private final DataInputStream inP2;
    private final DataOutputStream outP1;
    private final DataOutputStream outP2;

    /**
     * @brief Initializes a game session between two players
     * 
     * @param player1 The first player
     * @param player2 The second player
     * 
     * @details Sets up the communication streams and randomly assigns
     * initial player roles (Game Master vs Guesser).
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
     * @brief Randomly assigns the Game Master and Guesser roles
     * 
     * @param player1 The first player
     * @param player2 The second player
     * 
     * @details Uses a random number generator to assign roles fairly
     * between the two players.
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
     * @brief Sends a message to the Game Master
     * 
     * @param message The message to send
     * 
     * @details Determines which player is the Game Master and
     * sends the message to the appropriate output stream.
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
                System.out.println("Error sending to Game Master: " + e.getMessage());
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
                System.out.println("Error sending to Game Master: " + e.getMessage());
            }
        }
    }

    /**
     * @brief Sends a message to the Guesser
     * 
     * @param message The message to send
     * 
     * @details Determines which player is the Guesser and
     * sends the message to the appropriate output stream.
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
                System.out.println("Error sending to Guesser: " + e.getMessage());
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
                System.out.println("Error sending to Guesser: " + e.getMessage());
            }
        }
    }

    /**
     * @brief Receives a message from the Game Master
     * 
     * @return The message received
     * 
     * @details Determines which player is the Game Master and
     * receives the message from the appropriate input stream.
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
                System.out.println("Error receiving from Game Master: " + e.getMessage());
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
                System.out.println("Error receiving from Game Master: " + e.getMessage());
            }
        }

        return message;
    }

    /**
     * @brief Receives a message from the Guesser
     * 
     * @return The message received
     * 
     * @details Determines which player is the Guesser and
     * receives the message from the appropriate input stream.
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
                System.out.println("Error receiving from Guesser: " + e.getMessage());
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
                System.out.println("Error receiving from Guesser: " + e.getMessage());
            }
        }

        return message;
    }

    /**
     * @brief Executes the game session between the two players
     * 
     * @details Main game loop that:
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
        // phase 1: player introduction
        sendToGM("\nYOUR OPPONENT IS " + guesser.getUsername());
        sendToGuesser("\nYOUR OPPONENT IS " + gameMaster.getUsername());

        int decisionGM = 0;
        boolean playAgain = true;
        while(playAgain)
        {
            // phase 2: role assignment
            assignRoles(player1, player2);

            sendToGM("\nYou are the Game Master.");
            sendToGuesser("\nYou are the Guesser.");

            // phase 3: game master chooses a subject
            sendToGM("\nChoose a subject: ");
            sendToGuesser("\n" + gameMaster.getUsername() + " is thinking of a subject...");
            
            String subject = receiveFromGM();

            sendToGuesser("\n" + gameMaster.getUsername() + " has chosen a subject.");

            // phase 4: question and answer process
            boolean win = false;
            int count = 0;
            while(!win && count <= 3)
            {
                String question = "";
                char answerIn = ' ';
                Answer answerOut;

                sendToGM("\n" + guesser.getUsername() + " is thinking of a question...");

                while (question.trim().isEmpty())
                {
                    sendToGuesser("\nEnter your question: ");

                    question = receiveFromGuesser();
                }
            
                count++;

                sendToGM("\nQuestion: " + question);
                
                // fix input edge cases in GameSession and Client thread
                while (answerIn != 'y' && answerIn != 'n' && answerIn != 'm' && answerIn != 'c')
                {
                    sendToGM("\n(Y)es, (N)o, (M)aybe, (C)orrect\nEnter your answer: ");

                    answerIn = receiveFromGM().charAt(0);
                }

                answerOut = Answer.fromChar(answerIn);

                // phase 5.0: determining win/loss
                if (answerIn == 'c')
                {
                    sendToGuesser("\nYou won! The answer was " + subject + ".");
                    sendToGM("\nYou lose!");

                    win = true;
                }
                else 
                {
                    sendToGuesser("\nAnswer: " + answerOut);
                }
            }

            // phase 5.1: determining win/loss
            if (count == 3)
            {
                sendToGM("\nYou won!" + guesser.getUsername() + " ran out of questions.");
                sendToGuesser("\nYou lose! The answer was " + subject + ".");
            }

            // phase 6: play again

            // implement a proper play again section
            // asks both players simultaneously and they can change their answers within a 30 sec frame
            // playAgain thread 

            sendToGM("\nPLAY AGAINST " + guesser.getUsername() + " AGAIN?");
            sendToGuesser("\nPLAY AGAINST " + gameMaster.getUsername() + " AGAIN?");

            while (decisionGM != 'y' && decisionGM != 'n')
            {
                sendToGM("\n(Y)es, (N)o\nDecision: ");

                decisionGM = receiveFromGM().toLowerCase().charAt(0);
            }

            if (decisionGM == 'n')
            {
                sendToGuesser("\nThe Game Master has chosen to end the game.");
                playAgain = false;
            }
        }
    }
}