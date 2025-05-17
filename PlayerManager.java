import java.io.*;
import java.net.*;

public class PlayerManager implements Runnable
{
    private final Player player;

    private final DataInputStream in;
    private final DataOutputStream out;

    public PlayerManager(Player player)
    {
        this.player = player;

        this.in = player.getInputStream();
        this.out = player.getOutputStream();
    }

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
                case 1 ->
                {
                    Server.threadPool.submit(new MatchPlayer(player));

                    exit = true;
                }
                case 2 ->
                {
                    String username = "";

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
