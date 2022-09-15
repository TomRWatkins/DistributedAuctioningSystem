import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.registry.*;
import java.util.ArrayList;
import java.util.Scanner;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * An implementation for a client buyer to be used in an Auctioning System.
 * Buyers can view and bid on auctions once authenticated.
 * @author Thomas Watkins
 */
public class ClientBuyer {

	public static void main(String[] args) {		
		try {
			// Locate server and create server object
			String name = "myserver";
			Registry registry = LocateRegistry.getRegistry("localhost");
			IAuction server = (IAuction) registry.lookup(name);
			Scanner scanner = new Scanner(System.in);
			Scanner optScan = new Scanner(System.in);
			Scanner doubScan = new Scanner(System.in);
			String wait;
			int option = -1;		
						
			//Login variables
			UserAccount thisUser = null;
			String username = "";
			String email = "";
			boolean loggedIn = false;
			
			//Login system
			while(!loggedIn) {
				for(int i = 0; i < 50; i++) System.out.println("");
				System.out.println("           Auction System");
				System.out.println("----------------------------------");
				System.out.println("1] Register new user account.");
				System.out.println("2] Login.");
				System.out.println("3] Exit program.");
				System.out.println("----------------------------------");
				System.out.print("Enter option: ");
				option = optScan.nextInt();	
				for(int i = 0; i < 50; i++) System.out.println();
				
				switch(option) {
				case 1: 
					System.out.println("          Register new user");
					System.out.println("----------------------------------");
					System.out.print("Enter Username: ");
					username = scanner.nextLine();
					System.out.print("Enter Email Address: ");
					email = scanner.nextLine();
					System.out.println(server.register(new UserAccount(username, email, "BUYER")));					
					System.out.println();
					System.out.print("Press enter to continue...");
					wait = scanner.nextLine();
					for(int i = 0; i < 50; i++) System.out.println();	
					
					break;
				case 2: 
					System.out.println("               Login");
					System.out.println("----------------------------------");
					System.out.print("Enter Username: ");
					username = scanner.nextLine();	
					
					//Retrieve challenge from server
					String challenge = server.getChallenge(username);
					if(challenge == null) {
						System.out.println("User doesn't exist.");
						System.out.println();
						System.out.print("Press enter to continue...");
						wait = scanner.nextLine();
						break; 
					}								
					
					//Find key
					String path = "../UsersKeyStore/" + username + ".txt";					
					byte[] encodedChallenge = null;
					try {
						byte[] encoded = Files.readAllBytes(Paths.get(path));			
						SecretKey key = new SecretKeySpec(encoded, "DES");
						
						//Encrypt challenge 
						Cipher cipher = Cipher.getInstance("DES");
						cipher.init(Cipher.ENCRYPT_MODE,key);
						encodedChallenge = cipher.doFinal(challenge.getBytes());							
					} 
					catch (Exception e) {			
						e.printStackTrace();
					} 
					
					//Validate the challenge and authenticate self
					thisUser = server.validateChallenge(encodedChallenge, username);					
					if(thisUser != null) {
						System.out.println("Login Successful.");
						loggedIn = true;						
					}
					else 
						System.out.println("Login Failed.");
					
					System.out.println();
					System.out.print("Press enter to continue...");
					wait = scanner.nextLine();
					for(int i = 0; i < 50; i++) System.out.println();						
					break;
				case 3:
					for(int i = 0; i < 50; i++) System.out.println();	
					System.exit(0);						
					break;
				default:
					for(int i = 0; i < 50; i++) System.out.println();	
					break;
				}
			}									
			
			//Auction system user interface post authentication
			while(true) {			
				System.out.println("Logged in as " + thisUser.getUsername() + " [" + thisUser.getPrivelige() + "]");
				System.out.println("----------------------------------");
				System.out.println("           Auction System");
				System.out.println("----------------------------------");
				System.out.println("1] Browse Auctions.");
				System.out.println("2] Bid on a listing.");
				System.out.println("3] Exit program.");
				System.out.println("----------------------------------");
				System.out.print("Enter option: ");
				option = optScan.nextInt();				
				for(int i = 0; i < 50; i++) System.out.println();
				
				System.out.println("Logged in as " + thisUser.getUsername() + " [" + thisUser.getPrivelige() + "]");
				System.out.println("----------------------------------");
				
				switch(option) {
				case 1: 
					System.out.println("            Auctions");
					System.out.println("----------------------------------");
					ArrayList<AuctionItem> auctions = server.browseAuctions();
					for(AuctionItem s: auctions) { 
						System.out.println(s);
						System.out.println("----------------------------------");
					}
					
					System.out.println();
					System.out.print("Press enter to continue...");
					wait = scanner.nextLine();
					for(int i = 0; i < 50; i++) System.out.println();
					
					break;					
				case 2: 
					System.out.println("        Bid on a listing");
					System.out.println("----------------------------------");
					System.out.print("Enter the Auction ID of the item to bid on: ");
					int aucId = optScan.nextInt();
					System.out.print("Enter the amount you would like to bid: £");
					double amount = doubScan.nextDouble();	
					Bid bid = new Bid(thisUser,amount);
					String response = server.bidOnItem(aucId, bid);
					System.out.println(response);
					
					System.out.println();
					System.out.print("Press enter to continue...");
					wait = scanner.nextLine();
					for(int i = 0; i < 50; i++) System.out.println();					
					break;
				case 3: 
					for(int i = 0; i < 50; i++) System.out.println();		
					System.exit(0);					
					break;
				default: 
					for(int i = 0; i < 50; i++) System.out.println();	
					break;
				}				
			}			
		} catch (Exception e) {
			System.err.println("Exception");
			e.printStackTrace();
			System.exit(1);
		}
	}
}
