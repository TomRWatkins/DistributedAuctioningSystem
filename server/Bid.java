import java.io.Serializable;

/**
 * An implementation for a Bid to be used by a client buyer.
 * @author Thomas Watkins
 */
public class Bid implements Serializable {
	private UserAccount bidder;
	private double amount;
	
	/**
	 * Constructor. Creates a Bid.
	 * @param bidder the client placing the bid
	 * @param amount the amount of money placed
	 */
	public Bid(UserAccount bidder, double amount) {
		this.bidder = bidder;
		this.amount = amount;
	}
	
	/**
	 * Returns this bids user account.
	 * @return this bids user account.
	 */
	public UserAccount getBidder() { 
		return this.bidder;
	}

	/**
	 * Returns the amount of money placed on this bid.
	 * @return the amount of money placed on this bid
	 */
	public double getAmount() {
		return amount;
	}
	
	/**
	 * Overriding the equals method. If all fields are equal return true, false otherwise.
	 * @param obj the Bid to be compared
	 * @return true if this Bid and the comparison object are equal
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Bid other = (Bid) obj;
		if (Double.doubleToLongBits(amount) != Double.doubleToLongBits(other.amount))
			return false;
		if (bidder == null) {
			if (other.bidder != null)
				return false;
		} else if (!bidder.equals(other.bidder))
			return false;
		return true;
	}
	
	
}
