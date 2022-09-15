import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.stream.Stream;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.blocks.RspFilter;
import org.jgroups.util.RspList;

/**
 * A Backend server for an auctioning system used to communicate with a Frontend via JGroups.
 * @author Thomas Watkins
 *
 */
public class Backend {
	private JChannel groupChannel;
	private RpcDispatcher dispatcher;	

	private ArrayList<UserAccount> registeredUsers;
	private Hashtable<Integer, String> challenges;
	private Hashtable<Integer, AuctionItem> listings;
	private int auctionCounter;
	private int clientCounter;

	/**
	 * Constructor. Creates a Backend. Connects to the JGroups channel and instantiates state.
	 * Also pre registers 4 users and ensures state consistency upon creation.
	 */
	public Backend() {
		// Connect to the group (channel)
		this.groupChannel = GroupUtils.connect();
		if (this.groupChannel == null) {
			System.exit(1); // error to be printed by the 'connect' function
		}

		// Make this instance of Backend a dispatcher in the channel (group)
		this.dispatcher = new RpcDispatcher(this.groupChannel, this);

		listings = new Hashtable<>();
		challenges = new Hashtable<>();
		registeredUsers = new ArrayList<>();
		auctionCounter = 1;
		clientCounter = 1;

		// Hard code 4 clients and register them
		UserAccount user1 = new UserAccount("tom", "tom@hotmail.com", "SELLER");
		UserAccount user2 = new UserAccount("fred", "fred@hotmail.com", "SELLER");
		UserAccount user3 = new UserAccount("max", "max@hotmail.com", "BUYER");
		UserAccount user4 = new UserAccount("shaun", "shaun@hotmail.com", "BUYER");
		registerReq(user1);
		registerReq(user2);
		registerReq(user3);
		registerReq(user4);

		this.update();
	}

	/**
	 * Generates a DES secret key for a new user upon registration. This key is then
	 * stored in a file named USERNAME.txt.
	 * 
	 * @param user the new user to generate the secret key for
	 */
	private void generateKeyReq(UserAccount user) {
		try {
			KeyGenerator kGen = KeyGenerator.getInstance("DES");
			SecretKey desKey = kGen.generateKey();
			byte[] encodedKey = desKey.getEncoded();
			String path = "../UsersKeyStore/" + user.getUsername() + ".txt";
			FileOutputStream fos = new FileOutputStream(path);
			fos.write(encodedKey);
			fos.close();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see IAuction#register()
	 */
	public String registerReq(UserAccount user) {		
		for (UserAccount u : registeredUsers) {
			if (user.getUsername().equals(u.getUsername()) || user.getEmail().equals(u.getEmail())) {
				return "User with that username or email address already exists.";
			}
		}
		user.setClientId(clientCounter++);
		registeredUsers.add(user);
		generateKeyReq(user);
		return "Account registered.";
	}

	/**
	 * Returns a random string of size n constructed of upper and lower case letters
	 * and numbers.
	 * 
	 * @param n the length of the string to be returned
	 * @return the random alphanumeric string
	 */
	private String getRandomAlphaNumStringReq(int n) {
		String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "0123456789" + "abcdefghijklmnopqrstuvxyz";
		StringBuilder sb = new StringBuilder(n);
		for (int i = 0; i < n; i++) {
			int index = (int) (AlphaNumericString.length() * Math.random());
			sb.append(AlphaNumericString.charAt(index));
		}

		return sb.toString();
	}

	/**
	 * @see IAuction#getChallenge()
	 */
	public String getChallengeReq(String username) {		
		// Generate random 10 digit string challenge
		String challenge = getRandomAlphaNumStringReq(10);

		// Find user requesting challenge
		UserAccount user = null;
		for (UserAccount u : registeredUsers) {
			if (u.getUsername().equals(username))
				user = u;
		}
		if (user == null)
			return null;

		// Store the challenge and the client ID for retrieval later
		challenges.put(user.getClientId(), challenge);

		// Return challenge to client
		return challenge;
	}

	/**
	 * @see IAuction#validateChallenge()
	 */
	public UserAccount validateChallengeReq(byte[] encodedChallenge, String username) {		
		// Find user requesting authentication
		UserAccount user = null;
		for (UserAccount u : registeredUsers) {
			if (u.getUsername().equals(username))
				user = u;
		}
		if (user == null)
			return null;

		String challenge = null;

		// Find the plaintext challenge sent to the user
		if (challenges.containsKey(user.getClientId()))
			challenge = challenges.get(user.getClientId());
		else
			return null;

		// Find key for the user
		String path = "../UsersKeyStore/" + user.getUsername() + ".txt";
		byte[] plainTextChallenge = null;
		byte[] encoded;
		try {
			encoded = Files.readAllBytes(Paths.get(path));
			SecretKey key = new SecretKeySpec(encoded, "DES");

			// Decrypt encrypted challenge sent by user
			Cipher cipher = Cipher.getInstance("DES");
			cipher.init(Cipher.DECRYPT_MODE, key);
			plainTextChallenge = cipher.doFinal(encodedChallenge);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
		// If challenge is correct return the user account
		return challenge.equals(new String(plainTextChallenge)) ? user : null;
	}

	/**
	 * @see IAuction#createAuction()
	 */
	public int createAuctionReq(AuctionItem item) {		
		if (item.getOwner().getPrivelige().equals("SELLER")) {
			item.setAuctionID(auctionCounter);
			listings.put(auctionCounter, item);
			return this.auctionCounter++;
		}
		return -1;
	}

	/**
	 * @see IAuction#closeAuction()
	 */
	public String closeAuctionReq(int auctionId, UserAccount user) {		
		AuctionItem item;

		// Find the Auction Item to close
		if (listings.containsKey(auctionId))
			item = listings.get(auctionId);
		else
			return "Item does not exist.";

		// If user is owner of the auction close the auction
		if (item.getOwner().equals(user)) {
			return item.closeAuction();
		}

		// Otherwise return error message
		return "You are not authorised to close this auction";
	}

	/**
	 * Ensures concurrent bidding.
	 * 
	 * @see IAuction#bidOnItem()
	 */
	public synchronized String bidOnItemReq(int auctionId, Bid bid) {		
		// Ensure privileges
		if (bid.getBidder().getPrivelige().equals("SELLER"))
			return "You do not have the correct privileges to make a bid.";

		AuctionItem item;
		// Find auction item to bid on and place bid
		if (listings.containsKey(auctionId)) {
			item = listings.get(auctionId);
			return item.bid(bid);
		}

		return "Item not found.";
	}

	/**
	 * @see IAuction#browseAuctions()
	 */
	public ArrayList<AuctionItem> browseAuctionsReq() {		
		ArrayList<AuctionItem> list = new ArrayList<>();
		listings.forEach((k, v) -> {
			list.add(v);
		});
		return list;
	}

	/**
	 * A function to update the state of this Backend. Obtains all current states
	 * from other Backends on the channel and updates this Backends state.
	 */
	public void update() {
		HashMap<String, Object> state = null;
		try {
			//Obtain list of all states
			RequestOptions opts = new RequestOptions(ResponseMode.GET_ALL, (long) 2000);
			RspList<HashMap<String, Object>> responses = this.dispatcher.callRemoteMethods(null, "getAllState",
					new Object[] {}, new Class[] {}, opts);

			//If first Backend server don't update state
			if (responses.getResults().isEmpty()) {
				System.out.println("Not updating state as i'm first to join.");
				return;
			}

			state = new HashMap<>();
			//If responses are conistent use first, otherwise use majority state
			if (allEqual(responses))
				state = responses.getResults().get(0);
			else
				state = (HashMap<String, Object>) findMajority(responses.getResults());

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//Assign new state
		this.registeredUsers = (ArrayList<UserAccount>) state.get("registeredUsers");
		this.challenges = (Hashtable<Integer, String>) state.get("challenges");
		this.listings = (Hashtable<Integer, AuctionItem>) state.get("listings");
		this.auctionCounter = (int) state.get("auctionCounter");
		this.clientCounter = (int) state.get("clientCounter");

		System.out.println("State Updated.");
	}

	/**
	 * Returns this Backends current state.
	 * @return this Backends state
	 */
	public HashMap<String, Object> getAllState() {
		HashMap<String, Object> state = new HashMap<>();
		state.put("registeredUsers", this.registeredUsers);
		state.put("challenges", this.challenges);
		state.put("listings", this.listings);
		state.put("auctionCounter", this.auctionCounter);
		state.put("clientCounter", this.clientCounter);
		return state;
	}
	
	/**
	 * A helper function to return whether elements in a list are identical.
	 * @param list the list of values to compare
	 * @return all values are identical
	 */
	private boolean allEqual(RspList list) {
		List results = list.getResults();
		//If list has less than 2 elements return true
		if (results.size() <= 1)
			return true;

		//Otherwise compare all combinations of elements
		for (int i = 0; i < results.size(); i++) {
			for (int j = 0; j < results.size(); j++) {
				if (!results.get(i).equals(results.get(j)))
					return false;

			}
		}
		return true;
	}

	/**
	 * A helper function to find the majority element in a list. 
	 * @param list the list of elements
	 * @return the majority element
	 */
	private <T> Object findMajority(List<T> list) {
		System.out.println("Finding majority state.");
		
		//Obtain distinct elements
		Stream<T> dList = list.stream().distinct();
		Object[] dListObj = dList.toArray();

		//Create corresponding counts array
		int[] counts = new int[dListObj.length];
		for (int i = 0; i < counts.length; i++) {
			counts[i] = 0;
		}

		//Count number of times distinct element appears in list
		for (int i = 0; i < dListObj.length; i++) {
			for (int j = 0; j < list.size(); j++) {
				if (dListObj[i].equals(list.get(j))) 
					counts[i]++;				
			}
		}

		//Return the most common distinct element
		int modeIndex = 0;
		for (int i = 0; i < counts.length; i++) {
			if (counts[i] > counts[modeIndex])
				modeIndex = i;
		}
		return dListObj[modeIndex];
	}

	/**
	 * A function to update the challenge data for this Backend.
	 * @param challengeState the state to be assigned
	 */
	public void updateChallengeState(Hashtable<Integer, String> challengeState) {
		System.out.println("Updating Challenge State");
		this.challenges = challengeState;
	}

	/**
	 * Returns this Backends challenge state
	 * @return this Backends challenge state
	 */
	public Hashtable<Integer, String> getChallengeState() {
		return this.challenges;
	}

	/**
	 * Main Method. Instantiates a Backend.
	 * @param args N/A
	 */
	public static void main(String args[]) {
		new Backend();
	}
}
