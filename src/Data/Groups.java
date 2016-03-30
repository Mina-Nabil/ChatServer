package Data;

import java.io.Serializable;

public class Groups implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 12L;
	
	private String groupName;
	private boolean rolled;
	
	public Groups()
	{
		setGroupName("");
		setRolled(false);
	}
	
	public Groups(String groupName,boolean rolled)
	{
		setGroupName(groupName);
		setRolled(rolled);
	}
	
	public String getGroupName() {
		return groupName;
	}
	
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	
	public boolean isRolled() {
		return rolled;
	}
	
	public void setRolled(boolean rolled) {
		this.rolled = rolled;
	}
	
	

}
