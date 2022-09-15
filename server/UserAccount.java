import java.io.Serializable;

/**
 * An implementation of a User Account for a client.
 * Contains state of the users username, email, ID and 
 * priveleges.
 * @author Thomas Watkins
 *
 */
public class UserAccount implements Serializable {
	
	private int clientId;
	private String username;	
	private String email;
	private String privilege;
	
	/**
	 * Constructor. Creates a new User Account.
	 * @param username  the username for this user
	 * @param email     the email address for this user
	 * @param privilege the priveleges for this user (SELLER/BUYER)
	 */
	public UserAccount(String username, String email, String privilege) {
		this.username = username;
		this.email = email;
		this.privilege = privilege;
	}
		
	/**
	 * Returns this users priveleges
	 * @return this users priveleges
	 */
	public String getPrivelige() {
		return this.privilege;
	}
	
	/**
	 * Returns this users client ID
	 * @return this users client ID
	 */
	public int getClientId() {
		return this.clientId;
	}
	
	/**
	 * Assigns this user a client ID
	 * @param clientId the ID to be assigned
	 */
	public void setClientId(int clientId) {
		this.clientId = clientId;
	}
	
	/**
	 * Returns this users username
	 * @return this users username
	 */
	public String getUsername() {
		return this.username;
	}
	
	/**
	 * Returns this users email
	 * @return this users email
	 */
	public String getEmail() {
		return this.email;
	}
	
	/**
	 * Overriding the equals method. If all fields are equal return true, false otherwise.
	 * @param obj the UserAccount to be compared
	 * @return true if this UserAccount and the comparison object are equal
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserAccount other = (UserAccount) obj;
		if (clientId != other.clientId)
			return false;
		if (email == null) {
			if (other.email != null)
				return false;
		} else if (!email.equals(other.email))
			return false;		
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		return true;
	}

	
}
