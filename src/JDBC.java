/**
 * Created by Mina on 14-Mar-15.
 */

public class JDBC { public static void main(String[] argv) {
    System.out.println("-------- JDBC Connection Testing ------");
    try {  Class.forName("com.mysql.jdbc.Driver");  }
    catch (ClassNotFoundException e)
    {  System.out.println("Cannot find JDBC Driver");
        e.printStackTrace(); return;  }
    System.out.println(" JDBC Driver Registered!"); } }
