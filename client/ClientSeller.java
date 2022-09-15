import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.registry.*;
import java.util.Scanner;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * An implementation for a client seller to be used in an Auctioning System.
 * Sellers can open and close auction listings once authenticated.
 * @author Thomas Watkins
 */
public class ClientSeller {

	public static void main(String[] args) {		
		try {
			// Locate server and create server object
			String name = "myserver";
			Registry registry = LocateRegistry.getRegistry("localhost");
			IAuction server = (IAuction) registry.lookup(name);
			Scanner scanner = new Scanner(System.in); //String scanner
			Scanner optScan = new Scanner(System.in); //Int scanner
			Scanner doubScan = new Scanner(System.in);//Double scanner
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
					System.out.println(server.register(new UserAccount(username, email, "SELLER")));					
					System.out.println();
					System.out.print("Press enter to continue...");
					wait = scanner.nextLine();					
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
					break;
				case 3:
					for(int i = 0; i < 50; i++) System.out.println();	
					System.exit(0);
					break;
				default:					
					break;
				}
				for(int i = 0; i < 50; i++) System.out.println();	
			}			
			
			//Auction system user interface post authentication
			while(true) {
				System.out.println("Logged in as " + thisUser.getUsername() + " [" + thisUser.getPrivelige() + "]");
				System.out.println("----------------------------------");
				System.out.println("           Auction System");
				System.out.println("----------------------------------");
				System.out.println("1] Create a new listing.");
				System.out.println("2] Close a listing.");
				System.out.println("3] Exit program.");
				System.out.println("----------------------------------");
				System.out.print("Enter option: ");
				option = optScan.nextInt();	
				for(int i = 0; i < 50; i++) System.out.println();
				
				System.out.println("Logged in as " + thisUser.getUsername() + " [" + thisUser.getPrivelige() + "]");
				System.out.println("----------------------------------");
				
				switch(option) {
				case 1: 
					System.out.println("       Create a new listing");					
					System.out.println("----------------------------------");
					System.out.print("Enter name for the item: ");
					String itemName = scanner.nextLine();
					System.out.print("Enter description for the item: ");
					String itemDesc = scanner.nextLine();
					System.out.print("Is the item new? [Y/N]:  ");
					String itemCond = scanner.nextLine();
					boolean condition = (itemCond.equals("Y") ? true : false);
					System.out.print("Enter starting price for the item: £");
					double startingPrice = doubScan.nextDouble();
					System.out.print("Enter reserve price for the item: £");
					double reservePrice = doubScan.nextDouble();
					
					AuctionItem item = new AuctionItem(thisUser, itemName, itemDesc, condition, startingPrice, reservePrice);
					int id = server.createAuction(item);					 
										
					if(id > 0) 
						System.out.println("Item put up for auction under ID " + id + ".");
					else
						System.out.println("You do not have permissions to create a new listing.");
					
					System.out.println();
					System.out.print("Press enter to continue...");
					wait = scanner.nextLine();
					for(int i = 0; i < 50; i++) System.out.println();
					break;					
				case 2: 
					System.out.println("        Close a listing");					
					System.out.println("----------------------------------");				
					System.out.print("Enter listing to close: ");
					int listingClose = optScan.nextInt();					
					System.out.println(server.closeAuction(listingClose, thisUser));						
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
