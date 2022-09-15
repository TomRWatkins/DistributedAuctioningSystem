import java.rmi.*;
import java.util.ArrayList;

/**
 * The interface in which an auctioning server will implement.
 * Provides methods to view, create and close auctions. As well
 * as bidding on auctions. Also provides methods for registering 
 * and authentication. 
 * @author Thomas Watkins
 *
 */
public interface IAuction extends Remote {
	
	/**
	 * Creates a new auction for an item.
	 * @param item the item to be put up for auction
	 * @return     the auction ID for this auction
	 */
	public int createAuction(AuctionItem item) throws RemoteException;	
	
	/**
	 * Closes an auction. This returns a string consisting of
	 * the winner of the auction, or an error message.
	 * @param auctionId the auction ID of the auction to be closed
	 * @param user      the user attempting to close the auction
	 * @return     		a string message with the winner of the auction
	 */
	public String closeAuction(int auctionId, UserAccount user) throws RemoteException;
	
	/**
	 * Enables a buyer to bid on an item. 
	 * @param auctionId the auction ID of the auction to be bid on
	 * @param bid       the bid to be placed
	 * @return     		a string message with the success of the bid
	 */
	public String bidOnItem(int auctionId, Bid bid) throws RemoteException;		
	
	/**
	 * Returns a list of all auctioned items for display.	 
	 * @return a list of all auctioned items
	 */
	public ArrayList<AuctionItem> browseAuctions() throws RemoteException;	
	
	/**
	 * Registers a new account ensuring a user with the same username
	 * doesn't already exist.
	 * @param user the user to be registered
	 * @return     a success or error message	  
	 */
	public String register(UserAccount user) throws RemoteException;
	
	/**
	 * Returns a string challenge to a requesting client.
	 * @param username the clients username requesting the challenge
	 * @return         the string challenge
	 */
	public String getChallenge(String username) throws RemoteException;
	
	/**
	 * Returns a UserAccount if the string challenge is successfully validated, null otherwise.
	 * @param encodedChallenge the encrypted challenge sent by the client
	 * @param username         the username of the client requesting authentication
	 * @return                 the UserAccount of the user sucessfully authenticated
	 */
	public UserAccount validateChallenge(byte[] encodedChallenge, String username) throws RemoteException;
}
