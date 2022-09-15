import java.io.Serializable;
import java.util.Date;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Implementation of an item to be auctioned. Keeps state of bids and the
 * owner of the auction.
 * @author Thomas Watkins
 */
public class AuctionItem implements Serializable {
	
	private int auctionID;
	private UserAccount owner;
	private String itemName;
	private String itemDescription;
	private boolean condition;
	private double startingPrice;
	private double reservePrice;
	private ArrayList<Bid> bids;
	private boolean live;
	
	/**
	 * Constructor. Creates an Auction item.
	 * @param owner           the owner of this auction
	 * @param itemName        the name of the item 
	 * @param itemDescription the description of the item 
	 * @param condition       the condition of the item 
	 * @param startingPrice   the starting price of the item 
	 * @param reservePrice    the minimum reserve price of the item
	 */
	public AuctionItem(UserAccount owner, String itemName, String itemDescription, boolean condition, double startingPrice, double reservePrice) {
		this.owner = owner;
		this.itemName = itemName;
		this.itemDescription = itemDescription;
		this.condition = condition;
		this.startingPrice = startingPrice;
		this.reservePrice = reservePrice;	
		this.bids = new ArrayList<>();
		this.live = true;
	}	
	
	/**
	 * Returns the owner of the auction.
	 * @return the owner of the auction.
	 */
	public UserAccount getOwner() {
		return this.owner;
	}
	
	/**
	 * Returns the Auction ID of the auction.
	 * @param auctionID the id of the auction
	 * @return          the auction id of the auction
	 */
	public void setAuctionID(int auctionID) {
		this.auctionID = auctionID;
	}	
	
	/**
	 * Closes an action returning message of the winner or reserve not met message.	  
	 * @return the winner or reserve not met message
	 */
	public String closeAuction() {
		this.live = false;
		
		if(bids.size() > 0 && reservePrice <= bids.get(bids.size() -1).getAmount()) 
			return "The winner of auction " + auctionID + " [" + itemName + "]" +" is " + bids.get(bids.size() -1).getBidder().getUsername() 
					+ " [" + bids.get(bids.size() -1).getBidder().getEmail() + "] for £" + bids.get(bids.size() -1).getAmount() + ".";		
		
		return "The reserve for auction " + auctionID + " [" + itemName + "]" + " was not met.";			
	}
		
	/**
	 * Creates a bid for this auction item if validated.
	 * @param bid the bid to be added
	 * @return    a success or error message to be displayed to the client
	 */
	public String bid(Bid bid) {
		if(this.live) {
			if(bids.size() < 1) {
				if(bid.getAmount() > startingPrice) {
					bids.add(bid);
					return "Bid registered.";
				}
				else 
					return "Bid not registered as it is lower than the starting price.";
			}
			else if (bids.get(bids.size()-1).getAmount() < bid.getAmount()) {
				bids.add(bid);
				return "Bid registered.";
			}
			else 
				return "Bid not registered as it is lower than the current highest bid.";
			
		}	 
		return "This auction is closed.";		
	}
	
	/**
	 * Overriding the toString method to display this auction item.
	 * @return this auction item as a string
	 */
	public String toString() { 
		double highestBid = (bids.size() > 0 ? bids.get(bids.size()-1).getAmount() : startingPrice);
		
		String auctionItem = "Auction ID: " + auctionID + "\n" + "Seller: " + owner.getUsername() + "\n" + "Item: " + itemName + "\n" + "Description: " + itemDescription + "\n" + 
				"Condition: " + (condition ? "New" : "Used") + "\n" + "Highest Bid: £" + highestBid + "\n" + "Staus: " + (live ? "LIVE" : "CLOSED");
		
		if(!this.live) {
			auctionItem += "\n";
			if(bids.size() > 0 && this.reservePrice <= bids.get(bids.size() -1).getAmount())
				auctionItem += "Winner: " + bids.get(bids.size() - 1).getBidder().getUsername();
			else 
				auctionItem += "Winner: Reserve not met";
		}
		
		return auctionItem;
	}

	/**
	 * Overriding the equals method. If all fields are equal return true, false otherwise.
	 * @param obj the AuctionItem to be compared
	 * @return true if this AuctionItem and the comparison object are equal
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AuctionItem other = (AuctionItem) obj;
		if (auctionID != other.auctionID)
			return false;
		if (bids == null) {
			if (other.bids != null)
				return false;
		} else if (!bids.equals(other.bids))
			return false;
		if (condition != other.condition)
			return false;
		if (itemDescription == null) {
			if (other.itemDescription != null)
				return false;
		} else if (!itemDescription.equals(other.itemDescription))
			return false;
		if (itemName == null) {
			if (other.itemName != null)
				return false;
		} else if (!itemName.equals(other.itemName))
			return false;
		if (live != other.live)
			return false;
		if (owner == null) {
			if (other.owner != null)
				return false;
		} else if (!owner.equals(other.owner))
			return false;
		if (Double.doubleToLongBits(reservePrice) != Double.doubleToLongBits(other.reservePrice))
			return false;
		if (Double.doubleToLongBits(startingPrice) != Double.doubleToLongBits(other.startingPrice))
			return false;
		return true;
	}		
	
}
