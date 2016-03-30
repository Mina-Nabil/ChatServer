import Data.Contacts;

import javax.sound.midi.Track;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by Mina on 19-Mar-16.
 */
public class Server extends JFrame {

    final private int SERVER_PORT = 44556;
    final private String COMMAND_KICK = "5555";
    Connection conn;
    JTextArea textArea;
    JTextField textField;
    JButton button;
    JPanel panel;
    ServerSocket server;
    static HashMap<String, Socket> Tracker;



    private void kickUser(String user){
        try {
            String query = "DELETE FROM users WHERE USER_NAME = ?";
            PreparedStatement kick = conn.prepareStatement(query);
            kick.setString(1, user);
            if(!kick.execute())
            {
                textArea.append("\n" + user + "'s Account is deleted");
                Socket os = Tracker.get(user);
                if(os != null) {
                    new DataOutputStream(os.getOutputStream()).writeUTF(COMMAND_KICK);
                    Tracker.remove(user);
                }
            }
            else textArea.append("\nAccount Not Found");

        } catch (SQLException ex ){
            textArea.append("\nKick Error 5550: " + ex.getMessage());
        } catch (IOException e) {
            textArea.append("\nKick Error 5551: " + e.getMessage());
        }
    }


    private void gui_Init(){
        //GUI Initialization
        textArea = new JTextArea();
        setTitle("Chat Server");
        setSize(800, 600);
        panel = new JPanel();
        textField = new JTextField();
        textField.setSize(500,100);
        JLabel label = new JLabel("Enter Username: ");
        button = new JButton("Kick");
        button.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 kickUser(textField.getText());
             }
         });

                panel.setLayout(new BorderLayout());
        panel.setSize(800, 100);
        panel.add(textField, BorderLayout.CENTER);
        panel.add(button, BorderLayout.EAST);
        panel.add(label, BorderLayout.WEST);
        add(panel, BorderLayout.NORTH);
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
        Tracker = new HashMap<String, Socket>() ;
        try {
            //State Checker
            new StateTracker(conn).start();




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

        conn = DriverManager.getConnection("jdbc:mysql://localhost:33662/chat_server" +
                "?user=root&password=Smart_12345" + "&autoReconnect=true");


    }



    class StateTracker extends Thread {

        Connection cn;
        String tobeRemoved = "";
        StateTracker(Connection c) {
            cn = c;
        }

        void setOffline(String user){
            try {
                String query = "UPDATE users SET USER_STATE = false WHERE USER_NAME = ?";

                PreparedStatement set = conn.prepareStatement(query);
                set.setString(1,user);
                set.executeUpdate();

            } catch (SQLException e){
                textArea.append("\nSet Offline Error 011: " + e.getMessage());
            }
        }


        @Override
        public void run() {

            try {
                sleep(1000);
            } catch (InterruptedException e){
                textArea.append("\nTracker Error: " + e.getMessage());
            }
            while(!this.isInterrupted()) {

                    for (Map.Entry<String, Socket> item : Tracker.entrySet()) {
                        try {


                            if (item.getValue() == null)
                                setOffline(item.getKey());
                            new DataOutputStream(item.getValue().getOutputStream()).writeUTF("1144");
                        } catch (IOException ex) {
                            setOffline(item.getKey());
                            Tracker.remove(item.getKey());
                            try {
                                this.interrupt();
                            } catch (Throwable throwable) {
                                textArea.append("\nInterrupt Error");
                            }
                            break;



                        }
                    }

                try {
                    sleep(5000);
                } catch (InterruptedException e){
                    textArea.append("\nTracker Error: " + e.getMessage());
                }

            }
            new StateTracker(conn).start();
        }
    }
}
