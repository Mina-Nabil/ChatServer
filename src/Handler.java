import Data.Contacts;
import Data.Groups;


import javax.jws.soap.SOAPBinding;
import javax.swing.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by Mina on 19-Mar-16.
 */
public class Handler implements Runnable {


    final private String COMMAND_LOGIN = "300";
    final private String COMMAND_GETONLINE = "400";
    final private String COMMAND_GETGRPS = "500";
    final private String COMMAND_GRPENROLL = "510";
    final private String COMMAND_GRPUNENROLL = "520";
    final private String COMMAND_STARTCHAT = "610";
    final private String COMMAND_SIGNUP = "700";


    Socket socket;
    Connection conn;
    JTextArea textArea;
    DataInputStream dis;
    DataOutputStream dos;
    ObjectOutputStream oos;

    boolean checkLogin(String User, String Pass){
        try {
            String query = "SELECT COUNT(*) FROM users WHERE USER_NAME = ? AND USER_PASS = ?";
            PreparedStatement chcklogin = conn.prepareStatement(query);
            chcklogin.setString(1, User);
            chcklogin.setString(2, Pass);
            ResultSet resultSet = chcklogin.executeQuery();
            resultSet.first();
            if(resultSet.getInt(1) == 1) return true ;

        } catch (SQLException e){
            textArea.append("\nLogin Error 254: " + e.getMessage());
        }

        return false ;
    }

    void setIP(String User, String IP){

        try {
            String query = "UPDATE users SET USER_IP = ? WHERE USER_NAME = ?";
            PreparedStatement setIP = conn.prepareStatement(query);
            setIP.setString(1, IP);
            setIP.setString(2, User);
            int res = setIP.executeUpdate();

        }catch (SQLException ex)
        {
            textArea.append("\nSet IP Error 656: " + ex.getSQLState());
        }
    }

    ArrayList<Contacts> getUserList(String User){

        try {
            String query = "SELECT USER_NAME, USER_STATE, USER_IP " +
                    "       FROM users WHERE USER_NAME != ?";
            PreparedStatement getList = conn.prepareStatement(query);
            getList.setString(1, User);
            ResultSet result = getList.executeQuery();
            ArrayList<Contacts> ret = new ArrayList<Contacts>();
            result.first();
            String userName;
            boolean userState;
            String userIP;
            do {
                userName = result.getString(1);
                userState = result.getBoolean(2);
                userIP = result.getString(3);
                ret.add(new Contacts(userName, userState, userIP));
            } while(result.next());

            return ret;
        } catch (SQLException ex){
            textArea.append("User List Error: " + ex.getMessage());
        }
        return new ArrayList<Contacts>();
    }

    String getIP(String User){
        try {
            String query = "SELECT USER_IP FROM users WHERE USER_NAME = ?";
            PreparedStatement getip = conn.prepareStatement(query);
            getip.setString(1, User);
            ResultSet res = getip.executeQuery();
            res.first();
            return res.getString(1);

        } catch (SQLException ex){
            textArea.append("\nGet IP Error 653: " + ex.getMessage());
        }
        return null;
    }


    boolean kickUser(String user){
        return false ;
    }

    ArrayList<Groups> getEnrolledGrps(String User){
        return null;
    }

    public Handler(Socket s, Connection c, JTextArea a){

        socket = s;
        conn = c;
        textArea = a;
    }


    @Override
    public void run() {

        try {
            dis = new DataInputStream(socket.getInputStream());
            String frstMesg = dis.readUTF();
            String command[] ;
            command = frstMesg.split("@%@");

            if(command[0].equals(COMMAND_LOGIN)){
                dos = new DataOutputStream(socket.getOutputStream());
                if (checkLogin(command[1], command[2]) == true){
                    dos.writeBoolean(true);
                    String IP = socket.getInetAddress().getHostAddress();
                    setIP(command[1], IP);
                }
                else
                    dos.writeBoolean(false);

            }
            else if(command[0].equals(COMMAND_GETGRPS)){

            }
            else if(command[0].equals(COMMAND_GETONLINE)){
                oos = new ObjectOutputStream(socket.getOutputStream());
                ArrayList<Contacts> contacts = getUserList(command[1]);
                oos.writeObject(contacts);

            }
            else if (command[0].equals(COMMAND_STARTCHAT)){
                dos = new DataOutputStream(socket.getOutputStream());
                String IP = getIP(command[1]);
                dos.writeUTF(IP);

            }else if (command[0].equals(COMMAND_GRPENROLL)){

            }else if (command[0].equals(COMMAND_GRPUNENROLL)){

            }else if (command[0].equals(COMMAND_SIGNUP)){
                dos = new DataOutputStream(socket.getOutputStream());
                String IP = socket.getInetAddress().getHostAddress();
                boolean res = signUP(command[1], command[2], IP);
                dos.writeBoolean(res);

            }



            socket.close();

        } catch (IOException e) {
            textArea.append("\nHandler Error 12052: " + e.getMessage());
        }


    }

    private boolean signUP(String User, String Pass, String IP) {
        try {
            String query = "INSERT INTO users(USER_NAME, USER_PASS, USER_IP) VALUES (?, ?, ?)";
            PreparedStatement signup = conn.prepareStatement(query);
            signup.setString(1, User);
            signup.setString(2, Pass);
            signup.setString(3, IP);
            if(!signup.execute()) return true;



        } catch (SQLException ex) {
            textArea.append("\nSign UP Error 45: " + ex.getMessage());
        }

        return false;
    }
}
