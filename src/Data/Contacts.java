package Data;


import java.io.DataOutputStream;
import java.io.Serializable;

public class Contacts implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 11L;
	
	private String username;
	private boolean state;
	private String ip;



	public Contacts()
	{
		setUsername("");
		setState(false);
		setIp("");
	}
	public Contacts(String username,boolean state,String ip)
	{
		setUsername(username);
		setState(state);
		setIp(ip);
		
	}

    public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public boolean isOnline() {
		return state;
	}

	public void setState(boolean state) {
		this.state = state;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}
	

}
