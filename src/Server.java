import javax.swing.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by Mina on 19-Mar-16.
 */
public class Server extends JFrame {

    final private int SERVER_PORT = 44556;

    Connection conn;
    JTextArea textArea;
    ServerSocket server;

    private void gui_Init(){
        //GUI Initialization
        textArea = new JTextArea();
        setTitle("Chat Server");
        setSize(800, 600);
        add(new JScrollPane(textArea));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);
        //End of GUI Init

    }

    private void DB_Init(){
        //Connection to DB
        try {
            connect_DB();
            textArea.append("Database Connected!");
        } catch (IOException e) {
            textArea.append("IO Error: " + e.getMessage());
        } catch (SQLException e) {
            textArea.append("SQL Error: " + e.getMessage());
        }
        //End of Conn to DB
    }

    public Server(){

        gui_Init();
        DB_Init();
        try {
            server = new ServerSocket(SERVER_PORT);


        //Port Listening
        while(true){
            try {
                Socket s = server.accept();
                new Thread(new Handler(s, conn, textArea)).start();
            } catch (IOException e) {
                textArea.append("IO Error(6325): " + e.getMessage());
            }
        }
        } catch (IOException e) {
            textArea.append("Critical Error: " + e.getMessage());
        }


    }



    public static void main(String[] args){

        new Server();
    }


    public void connect_DB() throws IOException, SQLException {
        // Load JDBC Driver
        try {

            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Cannot find JDBC Driver");
        }
        //Connect to the database

        conn = DriverManager.getConnection("jdbc:mysql://localhost:3309/chat_server" +
                "?user=root&password=MINA4ever" + "&autoReconnect=true");


    }
}
