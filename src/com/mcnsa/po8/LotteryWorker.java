package com.mcnsa.po8;

import java.io.BufferedReader;
import java.io.File;
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

/**
 * Handle Lottery.txt file
 * @author Octavian0, Kwinno, Fusty
 */
public class LotteryWorker {
	
	private static JavaHttpUrlConnectionReader reader = new JavaHttpUrlConnectionReader();
	private Server server = Bukkit.getServer();
	private File fLottery;
	private File fTmp;
	private static String apiUrl;
    private static String apiKey;
	
	public LotteryWorker(File folder) {
		fLottery = new File(folder, "Lottery.txt");
		fTmp = new File(folder, "lottery.tmp");
		//Change to real key before uploading to server
		apiUrl = po8.apiUrl;
		apiKey = po8.apiKey;
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
				if (items.length > 0) {
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
		String balance = getBalance("lottery").trim();
		String[] result = balance.split(": ");
		if(result[0].equalsIgnoreCase("Error")){
			reader.returnMessage(sender, "&c Can't show pot right now! ");
			return;
		}else{
			
			try{
				currentBalance = Integer.parseInt(result[1]);
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
			//max 1000 people can join at 3 per person
			String[] players;
			players = new String[3000];
			int count = 0;
			//search through and get everyone's name
			while (scanner.hasNextLine()) {
				String[] cur = scanner.nextLine().split(":");
				if (cur.length >= 1) {
					String name = cur[0];
					//see their total of tickets
					int number = Integer.parseInt(cur[1]);
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
		if (!fLottery.exists()) {
			reader.returnMessage(sender, "&c No subscriptions running, can't do a lottery");
			return;
		}
		
		try {
			Scanner scanner = new Scanner(new FileReader(fLottery));
			//max 1000 people can join at 3 per person
			
			int unsubcount = server.getOfflinePlayers().length;
			int playercount = unsubcount *3;
			String[] players = new String[playercount];
			String unsubplayers[] = new String[unsubcount];
			int count = 0;
			int unsub = 0;
			//search through and get everyone's name
			while (scanner.hasNextLine()) {
				String[] cur = scanner.nextLine().split(":");
				if (cur.length >= 1) {
					String name = cur[0];
					//see their total of tickets
					int number = Integer.parseInt(cur[1]);
					//check if they have enough, if not unsubscribe them
					int totalpo8 = number*100;
					String playerbalance = getBalance(name).trim();
					String[] playerresult = playerbalance.split(": ");
					int  playerpo8 = (int) Math.floor(Double.parseDouble(playerresult[1]));
					if(playerpo8 >= totalpo8 && playerpo8 > 0)
					for(int i = 0; i < number; i++) {
						players[count] = name;
						count++;
						setBalance(name,"Lottery", "100");
					}
					else
					{
						unsubplayers[unsub] = name;
						unsub++;
					}
				}
			} // while hasNextLine
			scanner.close();
			//unsub the ones who dont have enough
			for(int i = 0; i < unsub; i++) {
			String name = unsubplayers[i];
			unSubLottery(name);
			}
			//all players are in the list and have paid. Time to draw
			Random diceRoller = new Random();
				int total = count * 2;
				int roll = diceRoller.nextInt(total);
				String winner = "";
				if(roll < count)
				{
					winner = players[roll];
				}
				else
				{
					winner = "Lottery";
				}
				
				//pay up!
				String lotterybalance = getBalance("Lottery");
				String[] lotteryresult = lotterybalance.split(": ");
				if(winner != "Lottery"){
					server.broadcastMessage(reader.processColours("&e " + winner + " has won " + lotteryresult[1] + " po8 in the lottery draw!"));
				setBalance("Lottery", winner, lotteryresult[1]);
				setBalance("Admin","Lottery","100");
				}
				else
				{
					
				server.broadcastMessage(reader.processColours("&eNobody has won the lottery, pot is transfering to the next day"));
				}

		} catch (Exception e) {
			System.out.println("Cannot parse file " + fLottery.getName() + " - " + e.getMessage());
			reader.returnMessage(sender, "Lottery failed");
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
		
					
		// create new file if not exists
		if (!fLottery.exists()) {
			try {
				fLottery.createNewFile();
			} catch (Exception e) {
				System.out.println("Cannot create file " + fLottery.getName() + " - " + e.getMessage());
				reader.returnMessage(sender, "&c Subscription Failure!");
				return;
			}
		}
		//remove an old sub if it's there
		if (!RemovesubInternal(name)) {
			reader.returnMessage(sender, "&c Subscription Failed!");
			return;
		}
		//make the new sub
		try {
			FileWriter wrt = new FileWriter(fLottery, true);
			wrt.write(	name + ":" + tickets + "\n"
					 );
			wrt.close();
		} catch (Exception e) {
			System.out.println("Unexpected error " + e.getMessage());
			System.out.println("Flargle?" + e.getMessage());
			reader.returnMessage(sender, "&c Subscription Failure!");
			return;
		}
		reader.returnMessage(sender, "&9You have been added to the subscription list");
	} // subAdd
	
	// unsubscribes from lottery
	public void unSubLottery(String name) {
		if (!fLottery.exists()) {
			return;
		}
		String playername = name;
		if (RemovesubInternal(playername)){
			server.getPlayer(name).sendMessage(ChatColor.RED + "You have insufficient funds for the lottery. Please resubscribe when you have enough");
		}
		else 
			System.out.print("Removing subscription: "+ name + " failed");
	} // unsub
	
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
	
	// lists all available warps
	public void lotteryhelp(CommandSender sender) {
		reader.returnMessage(sender, "&e---" + "&a Po8lottery Help " + "&e ---");
		reader.returnMessage(sender, "&a/sublist " + "&e Lists all subscribed accounts, and how many tickets they are subbed for");
		reader.returnMessage(sender, "&a/checkpot " + "&e Tells the sender the total value of Lottery's account");
		reader.returnMessage(sender, "&a/unsublottery " + "&c <# of tickets> " + "&e Unsubscribes the sender of the specified number of tickets");
		reader.returnMessage(sender, "&a/sublottery " + "&c <# of tickets> " + "&e Subscribes the sender to the lottery");
		reader.returnMessage(sender, "&a/runlottery " + "&e Runs the lottery - only available through the console");
	}  // warpList
	
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
				if (lottery.length >= 1 && lottery[0].equalsIgnoreCase(name)) {
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
				if (Lottery.length >= 1 && Lottery[0].equalsIgnoreCase(name)) {
					// nothing (i know this empty line is strange, but it's here on purpose)
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
    
    public String setBalance(String sender, String recipient, String amount){
    	String request = apiUrl+"?key="+apiKey+"&transfer&t="+recipient+"&f="+sender+"&p="+amount;
    	String result = reader.loadURL(request);
    	return result;
    }
    
}
