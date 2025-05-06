import java.net.*;

public class Player 
{
    private Socket socket;
    private String username;

    public Player(Socket socket, String username)
    {
        this.socket = socket;
        this.username = username;
    }

    public Socket getSocket()
    {
        return socket;
    }

    public String getUsername()
    {
        return username;
    }
}
