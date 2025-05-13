import java.io.*;
import java.util.Random;

class GameSession implements Runnable 
{
    public enum Answer
    {
        YES('y'),
        NO('n'),
        MAYBE('m'),
        CORRECT('c');

        private final char inputChar;

        Answer(char inputChar)
        {
            this.inputChar = inputChar;
        }

        public char getInputChar()
        {
            return inputChar;
        }

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

    private Player gameMaster;
    private Player guesser;
    private Player player1;
    private Player player2;
    private DataInputStream inputP1;  // output stream from client
    private DataInputStream inputP2;  // output stream to client
    private DataOutputStream outputP1;  // output stream from client
    private DataOutputStream outputP2;  // output stream to client

    public GameSession(Player player1, Player player2)
    {
        this.player1 = player1;
        this.player2 = player2;

        try
        {
            inputP1 = new DataInputStream(new BufferedInputStream(player1.getSocket().getInputStream()));
            inputP2 = new DataInputStream(new BufferedInputStream(player2.getSocket().getInputStream()));
            outputP1 = new DataOutputStream(player1.getSocket().getOutputStream());
            outputP2 = new DataOutputStream(player2.getSocket().getOutputStream());
        }
        catch (IOException e)
        {
            System.out.println("Error creating streams: " + e.getMessage());
        }
    }
 
    private void assignRoles(Player player1, Player player2)
    {
        Player[] players = {player1, player2};

        Random random = new Random();

        int i = random.nextInt(2);

        gameMaster = players[i];

        guesser = i == 0 ? players[1] : players[0];
    }

    private void sendToGM(String message)
    {
        if (gameMaster.getSocket() == player1.getSocket())
        {
            try
            {
                outputP1.writeUTF(message + "\n");
                outputP1.flush();
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
                outputP2.writeUTF(message + "\n");
                outputP2.flush();
            }
            catch(IOException e)
            {
                System.out.println("Error sending to Game Master: " + e.getMessage());
            }
        }
    }

    private void sendToGuesser(String message)
    {
        if (guesser.getSocket() == player1.getSocket())
        {
            try
            {
                outputP1.writeUTF(message + "\n");
                outputP1.flush();
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
                outputP2.writeUTF(message + "\n");
                outputP2.flush();
            }
            catch(IOException e)
            {
                System.out.println("Error sending to Guesser: " + e.getMessage());
            }
        }
    }

    // get rid of if it goes unused
    private void sendToBoth(String message)
    {
        sendToGM(message + "\n");
        sendToGuesser(message + "\n");
    }

    private String receiveFromGM()
    {
        String message = "";

        if (gameMaster.getSocket() == player1.getSocket())
        {
            try
            {
                message = inputP1.readUTF();
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
                message = inputP2.readUTF();
            }
            catch (IOException e)
            {
                System.out.println("Error receiving from Game Master: " + e.getMessage());
            }
        }

        return message;
    }

    private String receiveFromGuesser()
    {
        String message = "";

        if (guesser.getSocket() == player1.getSocket())
        {
            try
            {
                message = inputP1.readUTF();
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
                message = inputP2.readUTF();
            }
            catch (IOException e)
            {
                System.out.println("Error receiving from Guesser: " + e.getMessage());
            }
        }

        return message;
    }

    public void run()
    {
        sendToGM("YOUR OPPONENT IS " + guesser.getUsername());
        sendToGuesser("YOUR OPPONENT IS " + gameMaster.getUsername());

        int decisionGM = 0;
        boolean playAgain = true;
        while(playAgain)
        {
            assignRoles(player1, player2);

            sendToGM("\nYou are the Game Master.");
            sendToGuesser("\n You are the Guesser.");

            sendToGM("\nChoose a subject: ");
            sendToGuesser("\n" + gameMaster.getUsername() + " is thinking of a subject...");
            
            String subject = receiveFromGM();

            sendToGuesser("\n" + gameMaster.getUsername() + " has chosen a subject.");

            String question = "";
            char answerIn = ' ';
            Answer answerOut;
            boolean win = false;
            int count = 0;
            while(!win && count < 20)
            {
                sendToGM("\n" + guesser.getUsername() + " is thinking...");
                sendToGuesser("\nEnter your question: ");

                question = receiveFromGuesser();
                count++;

                sendToGM("\nQuestion: " + question);
                
                while (answerIn != 'y' || answerIn != 'n' || answerIn != 'm' || answerIn != 'c')
                {
                    sendToGM("\n(Y)es, (N)o, (M)aybe, (C)orrect\nEnter your answer: ");

                    answerIn = receiveFromGM().charAt(0);
                }

                answerOut = Answer.fromChar(answerIn);

                if (answerIn == 'c')
                {
                    sendToGuesser("\nYou won! The answer was " + subject + ".");
                    sendToGM("\n...You lost. Better luck next time!");

                    win = true;
                }

                sendToGuesser("\nAnswer: " + answerOut);
            }

            // implement a proper play again section
            // asks both players simultaneously and they can change their answers within a 30 sec frame
            // multithread 

            sendToGM("PLAY AGAINST " + guesser.getUsername() + " AGAIN?");
            sendToGuesser("PLAY AGAINST " + gameMaster.getUsername() + " AGAIN?");

            while (decisionGM != 'y' || decisionGM != 'n')
            {
                sendToGM("\n(Y)es, (N)o\nDecision: ");

                decisionGM = receiveFromGM().toLowerCase().charAt(0);
            }

            if (decisionGM == 'n')
            {
                sendToGuesser("The Game Master has chosen to end the game.");
                playAgain = false;
            }
        }
    }
}