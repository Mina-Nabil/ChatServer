import Data.Contacts;
import Data.Groups;
import sun.security.acl.GroupImpl;


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
import java.util.Map;

/**
 * Created by Mina on 19-Mar-16.
 */
public class Handler implements Runnable {


    final private String COMMAND_LOGIN = "300";
    final private String COMMAND_GETONLINE = "400";
    final private String COMMAND_SENDMSG = "410";
    final private String COMMAND_GETGRPS = "500";
    final private String COMMAND_GRPENROLL = "510";
    final private String COMMAND_GRPUNENROLL = "520";
    final private String COMMAND_GRPADD = "540";
    final private String COMMAND_STARTCHAT = "610";
    final private String COMMAND_SIGNUP = "700";
    final private String COMMAND_GETSTREAMS = "800";
    final private String COMMAND_SENDGRP = "530";

    Socket socket;
    Connection conn;
    JTextArea textArea;
    DataInputStream dis;
    DataOutputStream dos;
    ObjectOutputStream oos;


    private boolean checkLogin(String User, String Pass){
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

    private void sendToGrp(String User, String Grp , String Msg){
        try {
            String query = "SELECT USER_NAME FROM users,groups, user_grp_rel " +
                    "       WHERE users.USER_ID = user_grp_rel.USER_ID " +
                    "       AND groups.GRP_ID = user_grp_rel.GRP_ID " +
                    "       AND groups.GRP_NAME = ?";

            PreparedStatement getGrps = conn.prepareStatement(query);
            getGrps.setString(1, Grp);
            ResultSet users = getGrps.executeQuery();
            users.first();
            String usr_name;
            do {
                usr_name = users.getString(1);
                Socket s = Server.Tracker.get(usr_name);
                if(s!=null && !usr_name.equalsIgnoreCase(User)) {
                    try {
                        new DataOutputStream(s.getOutputStream()).writeUTF(Msg);
                    } catch (IOException e){

                    }
                }

            } while (users.next());

        } catch (SQLException ex){
            textArea.append("\nGroup Message Error 0121: " + ex.getMessage());
        }
    }


    private void setOnline(String User){

        try {
            String query = "UPDATE users SET USER_STATE = True WHERE USER_NAME = ?";
            PreparedStatement setIP = conn.prepareStatement(query);
            setIP.setString(1, User);
            int res = setIP.executeUpdate();

        }catch (SQLException ex)
        {
            textArea.append("\nSet IP Error 656: " + ex.getMessage());
        }
    }

    private ArrayList<Contacts> getUserList(String User){

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
                Contacts x = new Contacts(userName, userState, userIP);
                ret.add(x);
            } while(result.next());

            return ret;
        } catch (SQLException ex){
            textArea.append("\nUser List Error: " + ex.getMessage());
        }
        return new ArrayList<Contacts>();
    }

    private String getIP(String User){
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

    private boolean EnrollUser(String Grp, String User){
        try {
            int grp_id, user_id;

            grp_id = GetGRP_ID(Grp);
            user_id = GetUser_ID(User);

            String Enroll = "INSERT INTO user_grp_rel VALUES (?, ?) ";
            PreparedStatement preparedStatement = conn.prepareStatement(Enroll);
            preparedStatement.setInt(1, grp_id);
            preparedStatement.setInt(2, user_id);
            return !preparedStatement.execute();


        } catch (SQLException ex){
            textArea.append("\nEnroll User 474: " + ex.getMessage());
        }
        return true ;
    }

    private ArrayList<Groups> getEnrolledGrps(String User) {
        ArrayList<Groups> ret = new ArrayList<Groups>();
        try {
            String query = "SELECT GRP_NAME " +
                    "       FROM groups, users, user_grp_rel " +
                    "       WHERE groups.GRP_ID = user_grp_rel.GRP_ID " +
                    "       AND users.USER_ID = user_grp_rel.USER_ID " +
                    "       AND users.USER_NAME = ?";

            PreparedStatement enrollgrps = conn.prepareStatement(query);
            enrollgrps.setString(1, User);

            ResultSet resultSet = enrollgrps.executeQuery();
            resultSet.first();
            String name;
            do {
                name = resultSet.getString(1);
                ret.add(new Groups(name, true));
            } while (resultSet.next());

        } catch(SQLException ex){
            textArea.append("\nGet Groups Error: 566" + ex.getMessage());
        }

        try{
            String query2 = "SELECT DISTINCT(GRP_NAME) " +
                    "       FROM groups, users, user_grp_rel " +
                    "       WHERE groups.GRP_ID = user_grp_rel.GRP_ID " +
                    "       AND users.USER_ID = user_grp_rel.USER_ID " +
                    "       AND groups.GRP_NAME NOT IN ( " +
                    "    SELECT GRP_NAME " +
                    "       FROM groups, users, user_grp_rel " +
                    "       WHERE groups.GRP_ID = user_grp_rel.GRP_ID " +
                    "       AND users.USER_ID = user_grp_rel.USER_ID " +
                    "      AND users.USER_NAME = ?  " +
                    " ) ";

            PreparedStatement unenrollgrps = conn.prepareStatement(query2);
            unenrollgrps.setString(1, User);

            ResultSet resultSet2 = unenrollgrps.executeQuery();
            resultSet2.first();
            String unname;
            do {
                unname = resultSet2.getString(1);
                ret.add(new Groups(unname, false));
            } while(resultSet2.next());



        } catch (SQLException ex) {
            textArea.append("\nGet Groups Error 565: " + ex.getMessage());
        }
        return ret;
    }

    private boolean UNENROLL (String User, String GRP){

        try {
            int grp_id, user_id;
            grp_id = GetGRP_ID(GRP);
            user_id = GetUser_ID(User);

            String query = "DELETE FROM user_grp_rel WHERE GRP_ID = ? AND USER_ID = ?";
            PreparedStatement unenroll = conn.prepareStatement(query);
            unenroll.setInt(1, grp_id);
            unenroll.setInt(2, user_id);
            return !unenroll.execute();
        }catch (SQLException ex) {
            textArea.append("UNENROLL Error 4141: " + ex.getMessage());
        }
        return false;
    }

    private int GetUser_ID(String User){
        try {
            String S1 = "SELECT USER_ID FROM users WHERE USER_NAME = ?";
            PreparedStatement UsrID_Query = conn.prepareStatement(S1);
            UsrID_Query.setString(1, User);
            ResultSet Usr_ID_Result = UsrID_Query.executeQuery();
            Usr_ID_Result.first();
            return Usr_ID_Result.getInt(1);
        } catch (SQLException ex){
            textArea.append("\nGet User ID Error 7858: " + ex.getMessage());
        }

    return 0;
    }

    private int GetGRP_ID(String Grp){
        try {
            String S2 = "SELECT GRP_ID FROM groups WHERE GRP_NAME = ?";
            PreparedStatement Grp_Query = conn.prepareStatement(S2);
            Grp_Query.setString(1, Grp);
            ResultSet Grp_Result = Grp_Query.executeQuery();
            Grp_Result.first();
            return Grp_Result.getInt(1);
        } catch (SQLException ex)
        {
            textArea.append("\nGet Grp Error 7447: " + ex.getMessage());
        }
        return 0;
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

    private boolean createGrp(String Name){
        try {
            String query = "INSERT INTO groups (GRP_NAME) VALUES (?)";
            PreparedStatement create = conn.prepareStatement(query);
            create.setString(1, Name);
            return !create.execute();

        } catch (SQLException ex ){
            textArea.append("\nCreate GRP Error 563: " + ex.getMessage());
        }
        return false;
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
                    setOnline(command[1]);
                }
                else
                    dos.writeBoolean(false);

            }else if (command[0].equals(COMMAND_SENDMSG)) {
                Socket s = Server.Tracker.get(command[2].toLowerCase());
                if(s != null) {
                    DataOutputStream os = new DataOutputStream(s.getOutputStream());
                    os.writeUTF(frstMesg);
                }
            }else if(command[0].equals(COMMAND_SENDGRP)){

                sendToGrp(command[1], command[2], frstMesg);


            }
            else if(command[0].equals(COMMAND_GETGRPS)){
                    oos = new ObjectOutputStream(socket.getOutputStream());
                    oos.writeObject(getEnrolledGrps(command[1]));
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

                dos = new DataOutputStream(socket.getOutputStream());
                dos.writeBoolean(EnrollUser(command[1], command[2]));

            }else if (command[0].equals(COMMAND_GRPUNENROLL)){

               dos = new DataOutputStream(socket.getOutputStream());
               dos.writeBoolean(UNENROLL(command[2], command[1]));

            }else if (command[0].equals(COMMAND_SIGNUP)){
                dos = new DataOutputStream(socket.getOutputStream());
                String IP = socket.getInetAddress().getHostAddress();
                boolean res = signUP(command[1], command[2], IP);
                dos.writeBoolean(res);

            }else if (command[0].equals(COMMAND_GRPADD)){
                dos = new DataOutputStream(socket.getOutputStream());
                dos.writeBoolean(createGrp(command[1]));
                EnrollUser(command[1], command[2] );
            }else if (command[0].equals(COMMAND_GETSTREAMS)){

                Server.Tracker.put(command[1].toLowerCase(), socket);
            }






        } catch (IOException e) {
            textArea.append("\nHandler Error 12052: " + e.getMessage());
        }


    }


}
