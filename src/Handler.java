import java.net.Socket;
import java.sql.Connection;

/**
 * Created by Mina on 19-Mar-16.
 */
public class Handler implements Runnable {

    Socket socket;
    Connection conn;
    public Handler(Socket s, Connection c){

        socket = s;
        conn = c;
    }


    @Override
    public void run() {

    }
}
