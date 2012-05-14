package com.mcnsa.po8;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Random;
import java.util.Scanner;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Handle Lottery.txt file
 * @author Octavian0, Kwinno, Fusty
 */
public class LotteryWorker {
	
	private static JavaHttpUrlConnectionReader reader = new JavaHttpUrlConnectionReader();
	private Server server = Bukkit.getServer();
	private File fLottery;
	private File fTmp;
	private String accountname = po8.accountname;
	private static String apiUrl;
    private static String apiKey;
    private static String maxtickets;
	public LotteryWorker(File folder) {
		fLottery = new File(folder, "Lottery.txt");
		fTmp = new File(folder, "lottery.tmp");
		//Change to real key before uploading to server
		apiUrl = po8.apiUrl;
		apiKey = po8.apiKey;
		maxtickets = po8.maxtickets;
		
	}
	
	// lists all available subscriptions
	public void subList(CommandSender sender) {
		if (!fLottery.exists()) {
			reader.returnMessage(sender, "&c No subscriptions running");
			return;
		}
		try {
			Scanner scanner = new Scanner(new FileReader(fLottery));
			String buffer = "&c Running subscriptions: &f";
			while (scanner.hasNextLine()) {
				String[] items = scanner.nextLine().split(":");
				if (items.length > 0 && !items[1].trim().equalsIgnoreCase("0")) {
					if (buffer.length() + items[0].length() + 2 >= 256) {
						reader.returnMessage(sender, buffer);
						buffer = items[0] + " ";
					} else {
						buffer += items[0] + " ";    							
					}
				}
			}
			reader.returnMessage(sender, buffer);
			scanner.close();
		} catch (Exception e) {
			System.out.println("Cannot create file " + fLottery.getName() + " - " + e.getMessage());
			reader.returnMessage(sender, "&c Listing subscriptions failed!");
		}
	}  // sublist
	
	//check the pot
	public void checkPot(CommandSender sender) {
		if (!fLottery.exists()) {
			reader.returnMessage(sender, "&c No one has bought a ticket!");
			return;
		}
		//Get current lottery balance or don't continue
		int currentBalance = -1;
		String balance = getBalance(accountname).trim();
		String[] result = balance.split(": ");
		if(result[0].equalsIgnoreCase("Error")){
			reader.returnMessage(sender, "&c Can't show pot right now! ");
			return;
		}else{
			
			try{
				currentBalance = Integer.parseInt(result[1].trim());
			}catch (Exception e){
				return;
			}
			
		}
		//If it didn't grab an int
		if(currentBalance == -1){
			reader.returnMessage(sender, "&c Can't show pot right now!");
			return;
		}

		//Ok, fine, get the pot now that we have all the info
		try {
			int totalTickets = 0;
			Scanner scanner = new Scanner(new FileReader(fLottery));
			//search through and get everyone's name
			while (scanner.hasNextLine()) {
				String[] cur = scanner.nextLine().split(":");
				if (cur.length >= 1) {
					//see their total of tickets
					int number = Integer.parseInt(cur[1].trim()) + Integer.parseInt(cur[2].trim());
					//Add that total to the total total of all totaled tickets, totally
					totalTickets = totalTickets+number;
				}
			} // while hasNextLine
			int pot = totalTickets*100;
			pot = pot + currentBalance;
			reader.returnMessage(sender, "&c The current pot is at "+pot+" po8.");
			scanner.close();
		} catch (Exception e) {
			reader.returnMessage(sender, "&c Can't show pot right now!");
		}
	}  //checkPot

	//run the lottery
	public void runLottery(CommandSender sender, String code) {
		
		String octcode = code;
		
		//can't be a player unless he has the code
		if(octcode.equalsIgnoreCase("hel64tha") && sender.isOp()){
		}
		else
		{
			if ((sender instanceof Player)) {
				reader.returnMessage(sender, "&c You're a player. You're not allowed to do that!");
				return;
			}
		}
		System.out.println("Running the Po8 Lottery!");
		if (!fLottery.exists()) {
			reader.returnMessage(sender, "&c No tickets bought running, can't do a lottery");
			return;
		}
		//see if there is ANY tickets bought
		try {
			Scanner scanner = new Scanner(new FileReader(fLottery));			
			int playercount = server.getOfflinePlayers().length *2;
			String[] players = new String[playercount];
			
			int count = 0;
			//search through and get everyone's name
			while (scanner.hasNextLine()) {
				String[] cur = scanner.nextLine().split(":");
				if (cur[1].trim().equalsIgnoreCase("0") && cur[2].trim().equalsIgnoreCase("0")) {
					players[count] = "none";
					count ++;
				}
				else
				{
					players[count] = "tickets";
					count ++;	
				}
			}
			scanner.close();
			int tickets = 0;
			for(int i = 0;i < count; i++){
				if(players[i].equalsIgnoreCase("tickets")){
					tickets++;
				}
			}
			if(tickets == 0){
				if (!(sender instanceof Player)) {
					System.out.print("Can't do a lottery, no tickets bought");
					return;
				}
				else{
				reader.returnMessage(sender, "&c No tickets bought, can't do a lottery");
				return;
				}
			}
		}
		catch (Exception e) {
			System.out.println("Cannot parse file " + fLottery.getName() + " - " + e.getMessage());
			reader.returnMessage(sender, "Lottery failed @see if there is any ticket");
		}
		
		//get players who bought tickets
		try {
			Scanner scanner = new Scanner(new FileReader(fLottery));
			//max 1000 people can join at 3 per person
			
			int unsubcount = server.getOfflinePlayers().length;
			int playercount = unsubcount *10;
			String[] players = new String[playercount];
			String[] playersEnough = new String[playercount];
			String unsubplayers[] = new String[unsubcount];
			String unticketplayers[] = new String[unsubcount];
			int count = 0;
			int enough = 0;
			int unsub = 0;
			int unticket = 0;
			String maburl = "";
			//search through and get everyone's name
			while (scanner.hasNextLine()) {
				String[] cur = scanner.nextLine().split(":");
				if (cur.length >= 1) {
					String name = cur[0].trim();
					//see their total of tickets
					int number = Integer.parseInt(cur[1].trim()) + Integer.parseInt(cur[2].trim());
					//check if they have enough, if not unsubscribe them
					int totalpo8 = number*100;
					
					maburl += name + ":" + totalpo8 + ",";					
				}
			}
			scanner.close();
			String url = maburl.substring(0, maburl.length() -1);
			String result = hasEnough(url);
			result = result.trim();
			String[] playersresult = new String[playercount];
			if(result.contains(",") == true){
				playersresult = result.split(",");
				for(int i = 0; i < playersresult.length; i++){
					String playersSplit[] = playersresult[i].split(":");

						if(playersSplit[1].trim().equalsIgnoreCase("1"))
						{
							playersEnough[enough] = playersSplit[0].trim();
							enough++;
						}
						else
						{
							unticketplayers[unticket] = playersSplit[0].trim();
							unticket++;
							unsubplayers[unsub] = playersSplit[0].trim();
							unsub++;
							server.getPlayer(playersSplit[0]).sendMessage(ChatColor.RED + "You do not have enough po8 for this lottery. Please resubscribe.");
						}
					
				}	
			}
			else
			{
				
				String[] playerEnough = result.split(":");
				if(playerEnough[1].trim().equals("1"))
				{
					playersEnough[enough] = playerEnough[0].trim();
					enough++;
				}
				else
				{
					unticketplayers[unticket] = playerEnough[0].trim();
					unticket++;
					unsubplayers[unsub] = playerEnough[0].trim();
					unsub++;
					server.getPlayer(playerEnough[0]).sendMessage(ChatColor.RED + "You do not have enough po8 for this lottery. Please resubscribe.");
				}
			}
			scanner = new Scanner(new FileReader(fLottery));
			while (scanner.hasNextLine()) {
				String[] cur = scanner.nextLine().split(":");
				if (cur.length >= 1) {
					String name = cur[0].trim();
					for(int i = 0; i < playersEnough.length; i++){
						int number = Integer.parseInt(cur[1].trim()) + Integer.parseInt(cur[2].trim());
						if(name.equalsIgnoreCase(playersEnough[i])){	
							if(number <= Integer.parseInt(maxtickets)){
								if(number > 0){
									int totalpo8 = number*100;
									if(!setBalance(name,accountname, totalpo8).contains("Success")){
										System.out.println("Error in transfering po8 from " + name + " to " + accountname);
									}	
																		
									for(int j = 0; j < number; j++){
										players[count] = name;
										count++;
										players[count] = name;
										count++;								
									}
								}
							}
							else{
								unticketplayers[unticket] = name;
								unticket++;
								unsubplayers[unsub] = name;
								unsub++;
								server.getPlayer(name).sendMessage(ChatColor.RED + "You had too many tickets and you have been removed from the lottery");
							}
						}

					}	
				}
			}	// while hasNextLine
			scanner.close();
			//unsub the ones who dont have enough

			
			//all players are in the list and have paid. Time to draw
			Random diceRoller = new Random();
			int total = count + (count/2);
			int roll = diceRoller.nextInt(total);
			System.out.println("Out of " + total + ", " + roll + " came out");
			String winner = "";
			if(roll < count)
			{
				winner = players[roll];
				System.out.println("And the winner is: " + winner);
				String lotterybalance = getBalance(accountname);
				String[] lotteryresult = lotterybalance.split(": ");
				int totalpo8 = Integer.parseInt(lotteryresult[1].trim());
				System.out.println(totalpo8);
				server.broadcastMessage(reader.processColours("&e " + winner + " has won " + lotteryresult[1].trim() + " po8 in the lottery draw!"));
				
				if(!setBalance(accountname, winner, totalpo8).contains("Success")){
					System.out.println("Error in transfering po8 from " + accountname + " to " + winner);
				}
				
				
				if(!setBalance("Admin",accountname,100).contains("Success")){
					System.out.println("Error in transfering po8 from Admin to " + accountname);
				}	
				
				
			}
			else
			{
				winner = accountname;
				System.out.println("And the winner is: " + winner);
				server.broadcastMessage(reader.processColours("&eNobody has won the lottery, pot is transfering to the next day"));
			}
			
			//remove all tickets
			
			for(int i = 0; i < unsub; i++) {
			String name = unsubplayers[i];
			unSubLottery(name);
			}
			for(int i = 0; i < unticket; i++) {
			String name = unticketplayers[i];
			unTicketLottery(name);
			}
			
			if(RemoveTickets()){
				System.out.println("All tickets successfully removed");
				server.broadcastMessage(reader.processColours("&eAll one use tickets have been removed."));
			}
			else{
				System.out.println("Problem with removing tickets");
			}
		} catch (Exception e) {
			System.out.println("Cannot parse file " + fLottery.getName() + " - " + e.toString());
			System.out.println(e.getCause());
			reader.returnMessage(sender, "Lottery failed @ runlottery");
		}
	} //runLottery
	
	//subcribe to the lottery
	public void sublottery(CommandSender sender, String tickets) {
		
		
		// has to be a player
		if (!(sender instanceof Player)) {
			reader.returnMessage(sender, "&9You have to be a player, you derp!");
			return;
		}
		
		String name = sender.getName();
		//does the player have a po8 account?
		if(!hasAccount(name)){
			reader.returnMessage(sender, "&c Type /po8 for instructions on how to create a po8 account!");
			return;
		}
		
					
		try {
    		// this actually goes through the file twice (worst case scenario)
    		// should optimize, but I wait for Persistence
    		boolean found = false;
	    	Scanner scanner = new Scanner(new FileReader(fLottery));
			while (scanner.hasNextLine()) {
				String[] lottery = scanner.nextLine().split(":");
				if (lottery.length >= 1 && lottery[0].trim().equalsIgnoreCase(name)) {
					found = true;
					break;
				}
			}
			scanner.close();
			// only copy file data if name found!
			if (found == false) {
				PrintWriter wrt = new PrintWriter(new FileWriter(fTmp));
				BufferedReader rdr = new BufferedReader(new FileReader(fLottery));
				String line;
				while ((line = rdr.readLine()) != null) {
					wrt.println(line);
				}
					wrt.println(name + ":" + tickets + ":0");
					reader.returnMessage(sender, "&9 You have been added to the subscription list!");
				wrt.close();
				rdr.close();
				if (!fLottery.delete()) {
					System.out.println("Cannot delete " + fLottery.getName());
					return;
				}
				if (!fTmp.renameTo(fLottery)) {
					System.out.println("Cannot rename " + fTmp.getName() + " to " + fLottery.getName());
					return;
				}
			}
			else{
			// copy everything except the old name
			PrintWriter wrt = new PrintWriter(new FileWriter(fTmp));
			BufferedReader rdr = new BufferedReader(new FileReader(fLottery));
			String line;
			while ((line = rdr.readLine()) != null) {
				String[] Lottery = line.split(":");
				int total = Integer.parseInt(tickets) + Integer.parseInt(Lottery[2].trim());
				if (Lottery.length >= 1 && Lottery[0].trim().equalsIgnoreCase(name)) {
					if(total <= Integer.parseInt(maxtickets))
					{
						wrt.println(Lottery[0].trim() + ":" + tickets + ":" + Lottery[1].trim() );
						reader.returnMessage(sender, "&9 You have bought tickets for the upcoming lottery!");
					}
					else{
						wrt.println(Lottery[0].trim()  + ":" + tickets + ":" + Lottery[1].trim());
						reader.returnMessage(sender, "&9 You have too many tickets and will be removed from the lottery when it starts.");
					}
				} else {
					wrt.println(line);
				}
			}
			wrt.close();
			rdr.close();
			if (!fLottery.delete()) {
				System.out.println("Cannot delete " + fLottery.getName());
				return;
			}
			if (!fTmp.renameTo(fLottery)) {
				System.out.println("Cannot rename " + fTmp.getName() + " to " + fLottery.getName());
				return;
			}
			}
    	} catch (Exception e) {
    		System.out.println("Unexpected error " + e.getMessage());
    		return;
    	}
	} // subAdd
	//subcribe to the lottery
	public void ticketlottery(CommandSender sender, String tickets) {
		
		
		// has to be a player
		if (!(sender instanceof Player)) {
			reader.returnMessage(sender, "&9You have to be a player, you derp!");
			return;
		}
		
		String name = sender.getName();
		//does the player have a po8 account?
		if(!hasAccount(name)){
			reader.returnMessage(sender, "&c Type /po8 for instructions on how to create a po8 account!");
			return;
		}
		
					
		try {
    		// this actually goes through the file twice (worst case scenario)
    		// should optimize, but I wait for Persistence
			
    		boolean found = false;
	    	Scanner scanner = new Scanner(new FileReader(fLottery));
			while (scanner.hasNextLine()) {
				String[] lottery = scanner.nextLine().split(":");
				if (lottery.length >= 1 && lottery[0].trim().equalsIgnoreCase(name)) {
					found = true;
					break;
				}
			}
			
			scanner.close();
			System.out.print(found);
			// only copy file data if name found!
			if (found == false) {
				PrintWriter wrt = new PrintWriter(new FileWriter(fTmp));
				BufferedReader rdr = new BufferedReader(new FileReader(fLottery));
				String line;
				while ((line = rdr.readLine()) != null) {
					wrt.println(line);
				}
					wrt.println(name  + ":0" + ":" + tickets);
					reader.returnMessage(sender, "&9 You have bought tickets for the upcoming lottery!");
				wrt.close();
				rdr.close();
				if (!fLottery.delete()) {
					System.out.println("Cannot delete " + fLottery.getName());
					return;
				}
				if (!fTmp.renameTo(fLottery)) {
					System.out.println("Cannot rename " + fTmp.getName() + " to " + fLottery.getName());
					return;
				}
				
			}
			else{
			// copy everything except the old name
			PrintWriter wrt = new PrintWriter(new FileWriter(fTmp));
			BufferedReader rdr = new BufferedReader(new FileReader(fLottery));
			String line;
			while ((line = rdr.readLine()) != null) {
				String[] Lottery = line.split(":");
				int total = Integer.parseInt(tickets) + Integer.parseInt(Lottery[1]);

				if (Lottery.length >= 1 && Lottery[0].trim().equalsIgnoreCase(name)) {
					if(total <= (Integer.parseInt(maxtickets)))
					{
						wrt.println(Lottery[0].trim() + ":" + Lottery[1].trim() + ":" + tickets);
						reader.returnMessage(sender, "&9 You have bought tickets for the upcoming lottery!");
					}
					else{
						wrt.println(Lottery[0].trim() + ":" + Lottery[1].trim() + ":" + tickets);
						reader.returnMessage(sender, "&9 You have too many tickets and will be removed from the lottery when it starts.");
					}					
				}
				else {
					wrt.println(line);
				}
			}
			wrt.close();
			rdr.close();
			if (!fLottery.delete()) {
				System.out.println("Cannot delete " + fLottery.getName());
				return;
			}
			if (!fTmp.renameTo(fLottery)) {
				System.out.println("Cannot rename " + fTmp.getName() + " to " + fLottery.getName());
				return;
			}
			}
    	} catch (Exception e) {
    		System.out.println("Unexpected error " + e.getMessage());
    		return;
    	}
	} // subAdd
	
	// unsubscribes from lottery
	public void unSubLottery(String name) {
		if (!fLottery.exists()) {
			return;
		}
		String playername = name;
		if (RemovesubInternal(playername)){
			server.getPlayer(name).sendMessage(ChatColor.RED + "You have been removed from the lottery");
		}
		else 
			System.out.print("Removing subscription: "+ name + " failed");
	} // unsub
	
	//removes tickets
	public void unTicketLottery(String name) {
		if (!fLottery.exists()) {
			return;
		}
		String playername = name;
		if(RemoveTicketsInternal(playername)){
			
		}
		else 
			System.out.print("Removing tickets: "+ name + " failed");
	} // unticket
	
	public void unSub(CommandSender sender) {
		if (!fLottery.exists()) {
			reader.returnMessage(sender, "&c No subscriptions available");
			return;
		}
		String playername = sender.getName();
		if (RemovesubInternal(playername))
			reader.returnMessage(sender, "&c Subscription of &f"+ playername + "&c  removed");
		else 
			reader.returnMessage(sender, "&c Removing subscription: &6" + playername + "&c failed");
	} // subRemove
	
	public void showtickets(CommandSender sender) {
		
		if (!fLottery.exists()) {
			reader.returnMessage(sender, "&c No tickets bought");
			return;
		}
		String name = sender.getName();
		Scanner scanner;
		try {
			
			scanner = new Scanner(new FileReader(fLottery));
			boolean found = false;
			String playername = "";
			int tickets = 0;
			while (scanner.hasNextLine()) {
				String[] lottery = scanner.nextLine().split(":");
				if (lottery.length >= 1 && lottery[0].trim().equalsIgnoreCase(name)) {
					found = true;
					playername = lottery[0].trim();
					tickets = Integer.parseInt(lottery[1].trim()) + Integer.parseInt(lottery[2].trim());
					if(tickets > Integer.parseInt(maxtickets)){
						reader.returnMessage(sender, "&cYou have too many tickets and will be removed from the lottery");
						reader.returnMessage(sender, "&cPlease lower or remove your tickets");
						break;
					}
					else
					{
						if(tickets > 0){
							reader.returnMessage(sender, "&cYou have " + tickets + " ticket(s) for the next lottery");
							break;
						}
						else
						{
							reader.returnMessage(sender, "&cYou have no tickets for the next lottery");
							break;
						}
					}
				}

		}
		if(found == false){
			reader.returnMessage(sender, "&cYou have no tickets for the next lottery");
			scanner.close();
			return;	
		}
		scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	} // showtickets
	
	// lottery help
	public void lotteryhelp(CommandSender sender) {
		reader.returnMessage(sender, "&e---" + "&a Po8lottery Help " + "&e ---");
		reader.returnMessage(sender, "&a/sublist " + "&e Lists all subscribed accounts, and how many tickets they are subbed for");
		reader.returnMessage(sender, "&a/checkpot " + "&e Tells the sender the total value of Lottery's account");
		reader.returnMessage(sender, "&a/unsublottery " + "&e Unsubscribes the sender");
		reader.returnMessage(sender, "&a/sublottery " + "&c <# of tickets> " + "&e Subscribes the sender daily to the lottery");
		reader.returnMessage(sender, "&a/ticket " + "&c <# of tickets> " + "&e one time tickets to the lottery. Once bought the ticket stays.");
		reader.returnMessage(sender, "&a/lottery " + "&e Shows your total tickets for the upcoming lottery and the current pot");
		reader.returnMessage(sender, "&a/runlottery " + "&e Runs the lottery - only available through the console");
	}  // help
	
	// internal function for removing a warp from the text file (used in more places)
	// returns OK/error, not if name found!
    private boolean RemovesubInternal(String name) {
    	try {
    		// this actually goes through the file twice (worst case scenario)
    		// should optimize, but I wait for Persistence
    		boolean found = false;
	    	Scanner scanner = new Scanner(new FileReader(fLottery));
			while (scanner.hasNextLine()) {
				String[] lottery = scanner.nextLine().split(":");
				if (lottery.length >= 1 && lottery[0].trim().equalsIgnoreCase(name)) {
					found = true;
					break;
				}
			}
			scanner.close();
			// only copy file data if name found!
			if (!found) {
				return true;
			}
			
			// copy everything except the old name
			PrintWriter wrt = new PrintWriter(new FileWriter(fTmp));
			BufferedReader rdr = new BufferedReader(new FileReader(fLottery));
			String line;
			while ((line = rdr.readLine()) != null) {
				String[] Lottery = line.split(":");
				if (Lottery.length >= 1 && Lottery[0].trim().equalsIgnoreCase(name)) {
					wrt.println(Lottery[0].trim() + ":0:" + Lottery[2].trim());
				} else {
					wrt.println(line);
				}
			}
			wrt.close();
			rdr.close();
			if (!fLottery.delete()) {
				System.out.println("Cannot delete " + fLottery.getName());
				return false;
			}
			if (!fTmp.renameTo(fLottery)) {
				System.out.println("Cannot rename " + fTmp.getName() + " to " + fLottery.getName());
				return false;
			}
    	} catch (Exception e) {
    		System.out.println("Unexpected error " + e.getMessage());
    		return false;
    	}
    	return true;
    } // RemovesubInternal
    
    private boolean RemoveTicketsInternal(String name) {
    	try {
    		// this actually goes through the file twice (worst case scenario)
    		// should optimize, but I wait for Persistence
    		boolean found = false;
	    	Scanner scanner = new Scanner(new FileReader(fLottery));
			while (scanner.hasNextLine()) {
				String[] lottery = scanner.nextLine().split(":");
				if (lottery.length >= 1 && lottery[0].trim().equalsIgnoreCase(name)) {
					found = true;
					break;
				}
			}
			scanner.close();
			// only copy file data if name found!
			if (found == false) {
				System.out.print("Player " + name + " not found");
				return true;
			}
			
			// copy everything except the old name
			PrintWriter wrt = new PrintWriter(new FileWriter(fTmp));
			BufferedReader rdr = new BufferedReader(new FileReader(fLottery));
			String line;
			while ((line = rdr.readLine()) != null) {
				String[] Lottery = line.split(":");
				if (Lottery[0].trim().equalsIgnoreCase(name)) {
					wrt.println(Lottery[0].trim()  + ":" + Lottery[1].trim() + ":0");
				} else {
					wrt.println(line);
				}
			}
			wrt.close();
			rdr.close();
			if (!fLottery.delete()) {
				System.out.println("Cannot delete " + fLottery.getName());
				return false;
			}
			if (!fTmp.renameTo(fLottery)) {
				System.out.println("Cannot rename " + fTmp.getName() + " to " + fLottery.getName());
				return false;
			}
    	} catch (Exception e) {
    		System.out.println("Unexpected error " + e.getMessage());
    		return false;
    	}
    	System.out.println("tickets for " + name + " successfully removed!");
    	return true;
    } // RemoveticketInternal
    
    private boolean RemoveTickets() {
    	try {
    		// this actually goes through the file twice (worst case scenario)
    		// should optimize, but I wait for Persistence
			
			// copy everything except the old name
			PrintWriter wrt = new PrintWriter(new FileWriter(fTmp));
			BufferedReader rdr = new BufferedReader(new FileReader(fLottery));
			String line;
			while ((line = rdr.readLine()) != null) {
				String[] Lottery = line.split(":");
					wrt.println(Lottery[0].trim()  + ":" + Lottery[1].trim() + ":0");
			}
			wrt.close();
			rdr.close();
			if (!fLottery.delete()) {
				System.out.println("Cannot delete " + fLottery.getName());
				return false;
			}
			if (!fTmp.renameTo(fLottery)) {
				System.out.println("Cannot rename " + fTmp.getName() + " to " + fLottery.getName());
				return false;
			}
    	} catch (Exception e) {
    		System.out.println("Unexpected error " + e.getMessage());
    		return false;
    	}
    	return true;
    } // RemoveticketInternal
  
    public String getBalance(String player){
        String queryString = apiUrl+"?balance&key="+apiKey+"&username="+player;
        String balance = reader.loadURL(queryString);
		return balance;
    }
    
    public boolean hasAccount(String player){
    	
    	String queryString = apiUrl+"?balance&key="+apiKey+"&username="+player;
        String balance = reader.loadURL(queryString);
		String[] splitbalance = balance.split(": ");
		if(splitbalance[0].contains("Error")){
			return false;
		}
		else{
	        	return true;
		}

    }
    
    public String setBalance(String sender, String recipient, int amount){
    	String request = apiUrl+"?key="+apiKey+"&transfer&t="+recipient+"&f="+sender+"&p="+amount;
    	String result = reader.loadURL(request);
    	return result;
    }
    
    public String hasEnough(String url){
    	String request = apiUrl+"?key="+apiKey+"&lotto&players=" + url;
    	String result = reader.loadURL(request);
    	return result;
    }
    public String removePo8(String url){
    	String request = apiUrl+"?key="+apiKey+"&lotto&transfer&r="+ accountname +"&players=" + url;
    	String result = reader.loadURL(request);
    	return result;
    // url build up => name:po8toberemoved,name:po8toberemoved,
    //success:  kwinno:1,maboughey:1
    //if error : Error
    }
}
