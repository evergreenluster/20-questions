import java.io.*;

class GameSession implements Runnable 
{
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

    private void sendToPlayer1(String message)
    {
        try
        {
            outputP1.writeUTF(message);
            outputP1.flush();
        }
        catch(IOException e)
        {
            System.out.println("Error sending to player 1: " + e.getMessage());
        }
    }

    private void sendToPlayer2(String message)
    {
        try
        {
            outputP2.writeUTF(message);
            outputP2.flush();
        }
        catch(IOException e)
        {
            System.out.println("Error sending to player 2: " + e.getMessage());
        }
    }

    public void run()
    {
        System.out.println("GAME RUNNING !");
    }
}