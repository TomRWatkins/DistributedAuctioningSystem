import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.MembershipListener;
import org.jgroups.View;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.util.RspList;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.stream.Stream;

/**
 * A Frontend Server for an auctioning system that uses RMI to communicate with a Client.
 * Routes client requests to Backend using JGroups.
 * 
 * @author Thomas Watkins
 */
public class Frontend implements MembershipListener, IAuction {
	
	public static final long serialVersionUID = 42069;
	public final String SERVER_NAME = "myserver";
	public final int REGISTRY_PORT = 1099;
	private JChannel groupChannel;
	private RpcDispatcher dispatcher;
	private final int DISPATCHER_TIMEOUT = 2000;

	/**
	 * Constructor. Creates a Frontend.
	 * Connects to the JGroups channel and binds the server to RMI registry.
	 * 
	 * @throws RemoteException
	 */
	public Frontend() throws RemoteException {		
		this.groupChannel = GroupUtils.connect();
		if (this.groupChannel == null)			
			System.exit(1); // error to be printed by the 'connect' function
		
		this.bind(this.SERVER_NAME);
		
		// Make this instance of Frontend a dispatcher in the channel (group)
		this.dispatcher = new RpcDispatcher(this.groupChannel, this);
		this.dispatcher.setMembershipListener(this);
	}

	/**
	 * @see IAuction#createAuction()
	 */
	public int createAuction(AuctionItem item) throws RemoteException {
		try {
			RspList<Integer> responses = this.dispatcher.callRemoteMethods(null, "createAuctionReq",
					new Object[] { item }, new Class[] { AuctionItem.class },
					new RequestOptions(ResponseMode.GET_ALL, this.DISPATCHER_TIMEOUT));

			// If responses are all equal then return first response
			if (allEqual(responses))
				return responses.getResults().get(0);

			// Otherwise take majority response and update erroneous replica
			int majority = (int) findMajority(responses.getResults());
			List<Address> dests = new ArrayList<Address>(groupChannel.getView().getMembers());
			// Remove this member
			dests.remove(groupChannel.getAddress());

			// Check all address responses if it doesn't match the majority response then update			
			for (Address a : dests) {
				if (!(responses.getValue(a).equals(majority))) {
					// Update A
					this.dispatcher.callRemoteMethod(a, "update", new Object[] {}, new Class[] {},
							new RequestOptions(ResponseMode.GET_ALL, this.DISPATCHER_TIMEOUT));
				}
			}
			return majority;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	/**
	 * @see IAuction#closeAuction()
	 */
	public String closeAuction(int auctionId, UserAccount user) throws RemoteException {
		try {
			RspList<String> responses = this.dispatcher.callRemoteMethods(null, "closeAuctionReq",
					new Object[] { auctionId, user }, new Class[] { int.class, UserAccount.class },
					new RequestOptions(ResponseMode.GET_ALL, this.DISPATCHER_TIMEOUT));

			// If responses are all equal then return first response
			if (allEqual(responses))
				return responses.getResults().get(0);

			// Otherwise take majority response and update erroneous replica
			String majority = (String) findMajority(responses.getResults());
			List<Address> dests = new ArrayList<Address>(groupChannel.getView().getMembers());
			// Remove this member
			dests.remove(groupChannel.getAddress());

			// Check all address responses if it doesn't match the majority response then update			
			for (Address a : dests) {
				if (!(responses.getValue(a).equals(majority))) {
					// Update A
					this.dispatcher.callRemoteMethod(a, "update", new Object[] {}, new Class[] {},
							new RequestOptions(ResponseMode.GET_ALL, this.DISPATCHER_TIMEOUT));
				}
			}
			return majority;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "Error";
	}

	/**
	 * @see IAuction#bidOnItem()
	 */
	public String bidOnItem(int auctionId, Bid bid) throws RemoteException {
		try {
			RspList<String> responses = this.dispatcher.callRemoteMethods(null, "bidOnItemReq",
					new Object[] { auctionId, bid }, new Class[] { int.class, Bid.class },
					new RequestOptions(ResponseMode.GET_ALL, this.DISPATCHER_TIMEOUT));

			// If responses are all equal then return first response
			if (allEqual(responses))
				return responses.getResults().get(0);

			// Otherwise take majority response and update erroneous replica
			String majority = (String) findMajority(responses.getResults());
			List<Address> dests = new ArrayList<Address>(groupChannel.getView().getMembers());
			// Remove this member
			dests.remove(groupChannel.getAddress());

			// Check all address responses if it doesn't match the majority response then update			
			for (Address a : dests) {
				if (!(responses.getValue(a).equals(majority))) {
					// Update A
					this.dispatcher.callRemoteMethod(a, "update", new Object[] {}, new Class[] {},
							new RequestOptions(ResponseMode.GET_ALL, this.DISPATCHER_TIMEOUT));
				}
			}
			return majority;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "Error";
	}

	/**
	 * @see IAuction#browseAuctions()
	 */
	public ArrayList<AuctionItem> browseAuctions() throws RemoteException {
		try {
			RspList<ArrayList<AuctionItem>> responses = this.dispatcher.callRemoteMethods(null, "browseAuctionsReq",
					new Object[] {}, new Class[] {}, new RequestOptions(ResponseMode.GET_ALL, this.DISPATCHER_TIMEOUT));

			// If responses are all equal then return first response
			if (allEqual(responses))
				return responses.getResults().get(0);

			// Otherwise take majority response and update erroneous replica
			ArrayList<AuctionItem> majority = (ArrayList<AuctionItem>) findMajority(responses.getResults());
			List<Address> dests = new ArrayList<Address>(groupChannel.getView().getMembers());
			// Remove this member
			dests.remove(groupChannel.getAddress());

			// Check all address responses if it doesn't match the majority response then update			
			for (Address a : dests) {
				if (!(responses.getValue(a).equals(majority))) {
					// Update A
					this.dispatcher.callRemoteMethod(a, "update", new Object[] {}, new Class[] {},
							new RequestOptions(ResponseMode.GET_ALL, this.DISPATCHER_TIMEOUT));
				}
			}
			return majority;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @see IAuction#register()
	 */
	public String register(UserAccount user) throws RemoteException {
		try {
			RspList<String> responses = this.dispatcher.callRemoteMethods(null, "registerReq", new Object[] { user },
					new Class[] { UserAccount.class },
					new RequestOptions(ResponseMode.GET_ALL, this.DISPATCHER_TIMEOUT));

			// If responses are all equal then return first response
			if (allEqual(responses))
				return responses.getResults().get(0);

			// Otherwise take majority response and update erroneous replica
			String majority = (String) findMajority(responses.getResults());
			List<Address> dests = new ArrayList<Address>(groupChannel.getView().getMembers());
			// Remove this member
			dests.remove(groupChannel.getAddress());

			// Check all address responses if it doesn't match the majority response then update			
			for (Address a : dests) {
				if (!(responses.getValue(a).equals(majority))) {
					// Update A
					this.dispatcher.callRemoteMethod(a, "update", new Object[] {}, new Class[] {},
							new RequestOptions(ResponseMode.GET_ALL, this.DISPATCHER_TIMEOUT));
				}
			}
			return majority;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "Error";
	}

	/**
	 * @see IAuction#getChallenge()
	 */
	public String getChallenge(String username) throws RemoteException {
		try {
			RspList<String> responses = this.dispatcher.callRemoteMethods(null, "getChallengeReq",
					new Object[] { username }, new Class[] { String.class },
					new RequestOptions(ResponseMode.GET_ALL, this.DISPATCHER_TIMEOUT));

			if (responses.getResults().isEmpty())
				return null;

			String challenge = responses.getResults().get(0);
			List<Address> dests = new ArrayList<Address>(groupChannel.getView().getMembers());
			dests.remove(groupChannel.getAddress());
			Address stateToKeep = null;
			for (Address a : dests) {
				if (responses.getValue(a).equals(challenge)) {
					stateToKeep = a;
					break;
				}
			}

			// Get state from state to keep
			Hashtable<Integer, String> state = this.dispatcher.callRemoteMethod(stateToKeep, "getChallengeState",
					new Object[] {}, new Class[] {}, new RequestOptions(ResponseMode.GET_ALL, this.DISPATCHER_TIMEOUT));

			// Update states in all other replicas
			dests.remove(stateToKeep);
			for (Address a : dests) {
				this.dispatcher.callRemoteMethod(a, "updateChallengeState", new Object[] { state },
						new Class[] { Hashtable.class },
						new RequestOptions(ResponseMode.GET_ALL, this.DISPATCHER_TIMEOUT));
			}

			return challenge;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @see IAuction#validateChallenge()
	 */
	public UserAccount validateChallenge(byte[] encodedChallenge, String username) throws RemoteException {
		try {
			RspList<UserAccount> responses = this.dispatcher.callRemoteMethods(null, "validateChallengeReq",
					new Object[] { encodedChallenge, username }, new Class[] { byte[].class, String.class },
					new RequestOptions(ResponseMode.GET_ALL, this.DISPATCHER_TIMEOUT));

			// If responses are all equal then return first response
			if (allEqual(responses))
				return responses.getResults().get(0);

			// Otherwise take majority response and update erroneous replica
			UserAccount majority = (UserAccount) findMajority(responses.getResults());
			List<Address> dests = new ArrayList<Address>(groupChannel.getView().getMembers());
			// Remove this member
			dests.remove(groupChannel.getAddress());

			// Check all address responses if it doesn't match the majority response then
			// update
			for (Address a : dests) {
				if (!(responses.getValue(a).equals(majority))) {
					// Update A
					this.dispatcher.callRemoteMethod(a, "update", new Object[] {}, new Class[] {},
							new RequestOptions(ResponseMode.GET_ALL, this.DISPATCHER_TIMEOUT));
				}
			}
			return majority;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Binds this server to the RMI registry.
	 * @param serverName the name of this server
	 */
	private void bind(String serverName) {
		try {
			// Create server and stub
			IAuction stub = (IAuction) UnicastRemoteObject.exportObject(this, 0);
			Registry registry = LocateRegistry.getRegistry();

			// Bind server to registry
			registry.rebind(serverName, stub);
			System.out.println("Frontend ready");

		} catch (Exception e) {
			System.err.println("Error");
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * A helper function to return whether elements in a list are identical.
	 * @param list the list of values to compare
	 * @return all values are identical
	 */
	private boolean allEqual(RspList list) {
		List results = list.getResults();
		//If list is only one item return true
		if (results.size() == 1)
			return true;

		//Otherwise loop through all combinations of items are compare
		for (int i = 0; i < results.size(); i++) {
			for (int j = 0; j < results.size(); j++) {
				if (!results.get(i).equals(results.get(j))) {
					return false;
				}
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
		//Obtain distinct elements
		Stream<T> dList = list.stream().distinct();
		Object[] dListObj = dList.toArray();
		
		//Create corresponding counts list
		int[] counts = new int[dListObj.length];
		for (int i = 0; i < counts.length; i++) 
			counts[i] = 0;
		

		//Count number of times distinct element appears in list
		for (int i = 0; i < dListObj.length; i++) {
			for (int j = 0; j < list.size(); j++) {
				if (dListObj[i].equals(list.get(j))) 
					counts[i]++;				
			}
		}

		//Return the element that appears the most
		int modeIndex = 0;
		for (int i = 0; i < counts.length; i++) {
			if (counts[i] > counts[modeIndex])
				modeIndex = i;
		}

		return dListObj[modeIndex];
	}
	
	public void viewAccepted(View newView) {
		System.out.printf("jgroups view changed\n    new view: %s\n", newView.toString());
	}

	public void suspect(Address suspectedMember) {
		System.out.printf("jgroups view suspected member crash: %s\n", suspectedMember.toString());
	}

	public void block() {
		System.out.printf("jgroups view block indicator\n");
	}

	public void unblock() {
		System.out.printf("jgroups view unblock indicator\n");
	}

	/**
	 * Main method. Instantiates a Frontend.
	 * @param args N/A
	 */
	public static void main(String args[]) {
		try {
			new Frontend();
		} catch (RemoteException e) {
			System.err.println("remote exception:");
			e.printStackTrace();
			System.exit(1);
		}
	}
}
