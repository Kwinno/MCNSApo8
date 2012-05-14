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
public class SlotMachineWorker {
	
	private static JavaHttpUrlConnectionReader reader = new JavaHttpUrlConnectionReader();
	private Server server = Bukkit.getServer();
	private File fSlot;
	private File slotTmp;
	private String accountname = po8.accountname.trim();
	private static String apiUrl;
    private static String apiKey;
	public SlotMachineWorker(File folder) {
		fSlot = new File(folder, "Slots.txt");
		slotTmp = new File(folder, "Slot.tmp");
		//Change to real key before uploading to server
		apiUrl = po8.apiUrl;
		apiKey = po8.apiKey;
		
	}
	public void po8slot(CommandSender sender) {
		reader.returnMessage(sender, "&e---" + "&a Po8 Slotmachine Help " + "&e ---");
		reader.returnMessage(sender, "&a/checkout " + "&e Turns all coins in po8 equivalent");
		reader.returnMessage(sender, "&a/play " + "&c <# of coins> " + "&e Play the slots for <# of coins> times");
		reader.returnMessage(sender, "&a/buycoins " + "&c <# of coins> " + "&e Buys coins at the rate of 1 coin for 1 po8");
		reader.returnMessage(sender, "&a/checkcoins " + "&e Shows how many coins you have");
		reader.returnMessage(sender, "&a/po8slot " + "&e Displays this menu");
	}  // help
	// lists all available subscriptions
	public void checkout(CommandSender sender) {
		if (!fSlot.exists()) {
			reader.returnMessage(sender, "&c Can't check out, no one has coins.");
			return;
		}
		try {
			String name = sender.getName();
			boolean found = false;
	    	Scanner scanner = new Scanner(new FileReader(fSlot));
			while (scanner.hasNextLine()) {
				String[] SlotMachine = scanner.nextLine().split(":");
				if (SlotMachine.length >= 1 && SlotMachine[0].equalsIgnoreCase(name)) {
					found = true;
					break;
				}
			}
			scanner.close();
			// if false = nothing to check out
			if (found == false) 
			{
				reader.returnMessage(sender, "&9 You have nothing to check out!");
				return;
			}
			else
			{
			// copy everything except the old name
			PrintWriter wrt = new PrintWriter(new FileWriter(slotTmp));
			BufferedReader rdr = new BufferedReader(new FileReader(fSlot));
			String line;
			while ((line = rdr.readLine()) != null) {
				String[] SlotMachine = line.split(":");
				int coins = Integer.parseInt(SlotMachine[1]);
				if (SlotMachine.length >= 1 && SlotMachine[0].equalsIgnoreCase(name)) {
					if(coins > 0){
						if(!setBalance("Slotmachine",name,coins).contains("Success"))
						{
							System.out.print("Error in transfering Po8 from Slotmachine account to " + name);
						}
						else
						{
							wrt.println(name + ":0");
							reader.returnMessage(sender, "&9 You have checked out. " + coins + " po8 added to your po8 account.");
						}
						
					}
					else
					{
						reader.returnMessage(sender, "&9 You have nothing to check out!");
						wrt.println(line);
					}
				}

				else {
					wrt.println(line);
				}
			}
			wrt.close();
			rdr.close();
			if (!fSlot.delete()) {
				System.out.println("Cannot delete " + fSlot.getName());
				return;
			}
			if (!slotTmp.renameTo(fSlot)) {
				System.out.println("Cannot rename " + slotTmp.getName() + " to " + fSlot.getName());
				return;
			}
			}
		} catch (Exception e) {
			System.out.println("Cannot create file " + fSlot.getName() + " - " + e.getMessage());
			reader.returnMessage(sender, "&c Checkout failed!");
		}
	}  // checkout
  
	
public void buyCoins(CommandSender sender, String coins) {
		
		
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
	    	Scanner scanner = new Scanner(new FileReader(fSlot));
			while (scanner.hasNextLine()) {
				String[] slotmachine = scanner.nextLine().split(":");
				if (slotmachine.length >= 1 && slotmachine[0].equalsIgnoreCase(name)) {
					found = true;
					break;
				}
			}
			scanner.close();
			// only copy file data if name found!
			if (found == false) {
				PrintWriter wrt = new PrintWriter(new FileWriter(slotTmp));
				BufferedReader rdr = new BufferedReader(new FileReader(fSlot));
				String line;
				while ((line = rdr.readLine()) != null) {
					wrt.println(line);
				}
				int coin = Integer.parseInt(coins);
				if(!setBalance(name,"Slotmachine",coin).contains("Success"))
				{
					System.out.print("Error in transfering Po8 from " + name + " to Slotmachine account");
				}
				else
				{
					wrt.println(name + ":" + coins);
					reader.returnMessage(sender, "&9 You have bought " + coins + " coins and now have a total of: " + coins + " coins");
				}
				
				wrt.close();
				rdr.close();
				if (!fSlot.delete()) {
					System.out.println("Cannot delete " + fSlot.getName());
					return;
				}
				if (!slotTmp.renameTo(fSlot)) {
					System.out.println("Cannot rename " + slotTmp.getName() + " to " + fSlot.getName());
					return;
				}
			}
			else{
			// copy everything except the old name
			PrintWriter wrt = new PrintWriter(new FileWriter(slotTmp));
			BufferedReader rdr = new BufferedReader(new FileReader(fSlot));
			String line;
			while ((line = rdr.readLine()) != null) {
				String[] SlotMachine = line.split(":");
				int coin = Integer.parseInt(coins);
				int total = Integer.parseInt(coins) + Integer.parseInt(SlotMachine[1]);
				if (SlotMachine.length >= 1 && SlotMachine[0].equalsIgnoreCase(name)) {
					if(!setBalance(name,"Slotmachine",coin).contains("Success"))
					{
						System.out.print("Error in transfering Po8 from " + name+ " to Slotmachine account");
					}
					else
					{
						wrt.println(SlotMachine[0] + ":" + total );
						reader.returnMessage(sender, "&9 You have bought " + coins + " coins and now have a total of: " + total + " coins");
					}
						

				} else {
					wrt.println(line);
				}
			}
			wrt.close();
			rdr.close();
			if (!fSlot.delete()) {
				System.out.println("Cannot delete " + fSlot.getName());
				return;
			}
			if (!slotTmp.renameTo(fSlot)) {
				System.out.println("Cannot rename " + slotTmp.getName() + " to " + fSlot.getName());
				return;
			}
			}
    	} catch (Exception e) {
    		System.out.println("Unexpected error " + e.getMessage());
    		return;
    	}
	} // buycoins


public void checkcoins(CommandSender sender) {
	if (!fSlot.exists()) {
		reader.returnMessage(sender, "&c No one has coins.");
		return;
	}
	try {
		String name = sender.getName();
		boolean found = false;
    	Scanner scanner = new Scanner(new FileReader(fSlot));
		while (scanner.hasNextLine()) {
			String[] SlotMachine = scanner.nextLine().split(":");
			if (SlotMachine.length >= 1 && SlotMachine[0].equalsIgnoreCase(name)) {
				String coins = SlotMachine[1].trim();
				reader.returnMessage(sender, "&9 You have " + coins + " coins!");
				found = true;
				break;
			}
		}
		scanner.close();
		// if false = nothing to check
		if (found == false) 
		{
			reader.returnMessage(sender, "&9 You have no coins!");
			return;
		}	
	} catch (Exception e) {
		System.out.println("Cannot check coins " + e.getMessage());
		reader.returnMessage(sender, "&c Checking coins failed!");
	}
}  // checkcoins


public void playgame(CommandSender sender, String number) {
	
	
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
		boolean enoughcoins = false;
    	Scanner scanner = new Scanner(new FileReader(fSlot));
		while (scanner.hasNextLine()) {
			String[] SlotMachine = scanner.nextLine().split(":");
			if (SlotMachine.length >= 1 && SlotMachine[0].equalsIgnoreCase(name)) {
				found = true;
				if(Integer.parseInt(SlotMachine[1].trim()) > 0){
					int total = Integer.parseInt(SlotMachine[1].trim()) - Integer.parseInt(number);
					if(total >= 0){
						enoughcoins = true;
						break;
					}
					else
					{
						reader.returnMessage(sender, "&9 You don't have enough coins to play with");
						scanner.close();
						return;
					}
				}
				else
				{
					reader.returnMessage(sender, "&9 You havn't bought any coins to play with");
					scanner.close();
					return;
				}
			}
		}
		scanner.close();
		// only copy file data if name found!
		if (found == false) {
			reader.returnMessage(sender, "&9 You havn't bought any coins to play with");
			return;
		}
		else{
		// copy everything except the old name
		PrintWriter wrt = new PrintWriter(new FileWriter(slotTmp));
		BufferedReader rdr = new BufferedReader(new FileReader(fSlot));
		String line;
		while ((line = rdr.readLine()) != null) {
			String[] SlotMachine = line.split(":");
			
			if (SlotMachine.length >= 1 && SlotMachine[0].trim().equalsIgnoreCase(name)) 
			{
			

				int total = Integer.parseInt(SlotMachine[1].trim()) - Integer.parseInt(number);
				/*
					String[] reel1 = {"Lapis","Lapis","Lapis","Stone","Stone","Redstone","Redstone","Redstone","Stone","Stone","Gold","Gold","Gold","Stone","Stone","Iron","Iron","Iron","Iron","Stone","Stone","Coal","Coal","Coal","Coal","Stone","Stone","Stone","Stone","Stone","Diamond","Diamond","Diamond","Diamond","Stone","Stone","Stone","Stone","Stone","Lapis","Lapis","Stone","Stone","Redstone","Redstone","Redstone","Stone","Stone","Gold","Gold","Gold","Stone","Stone","Iron","Iron","Iron","Stone","Stone","Coal","Coal","Coal","Coal","Stone","Stone"};
					String[] reel2 = {"Lapis","Lapis","Stone","Stone","Stone","Redstone","Redstone","Stone","Stone","Stone","Gold","Gold","Gold","Stone","Stone","Stone","Iron","Iron","Iron","Stone","Stone","Stone","Coal","Coal","Coal","Stone","Stone","Stone","Stone","Stone","Diamond","Diamond","Diamond","Stone","Stone","Stone","Stone","Stone","Lapis","Lapis","Stone","Stone","Stone","Redstone","Redstone","Stone","Stone","Stone","Gold","Gold","Stone","Stone","Stone","Iron","Iron","Stone","Stone","Stone","Coal","Coal","Coal","Stone","Stone","Stone"};
					String[] reel3 = {"Lapis","Stone","Stone","Stone","Redstone","Redstone","Stone","Stone","Stone","Gold","Gold","Stone","Stone","Stone","Iron","Iron","Iron","Stone","Stone","Stone","Coal","Coal","Coal","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Diamond","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Lapis","Stone","Stone","Stone","Redstone","Stone","Stone","Stone","Gold","Gold","Stone","Stone","Stone","Iron","Iron","Iron","Stone","Stone","Stone","Coal","Coal","Coal","Stone","Stone","Stone"};		
				*/
				
				String[] reel1= {"Diamond","Diamond","Diamond","Diamond","Lapis","Lapis","Lapis","Lapis","Lapis","Lapis","Lapis","Lapis","Redstone","Redstone","Redstone","Redstone","Redstone","Redstone","Gold","Gold","Gold","Gold","Gold","Gold","Gold","Gold","Iron","Iron","Iron","Iron","Iron","Iron","Iron","Iron","Iron","Iron","Coal","Coal","Coal","Coal","Coal","Coal","Coal","Coal","Coal","Coal","Coal","Coal","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone"};
				String[] reel2= {"Diamond","Diamond","Diamond","Lapis","Lapis","Lapis","Redstone","Redstone","Redstone","Redstone","Gold","Gold","Gold","Gold","Gold","Gold","Iron","Iron","Iron","Iron","Iron","Iron","Iron","Iron","Coal","Coal","Coal","Coal","Coal","Coal","Coal","Coal","Coal","Coal","Coal","Coal","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone"};
				String[] reel3= {"Diamond","Diamond","Lapis","Redstone","Redstone","Gold","Gold","Gold","Iron","Iron","Iron","Iron","Iron","Coal","Coal","Coal","Coal","Coal","Coal","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone","Stone"};
					int winnings = 0;
					reader.returnMessage(sender, "&b~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
					for(int i = Integer.parseInt(number);i>0;i--){
					Random reel1roll = new Random();
					int roll1 = reel1roll.nextInt(reel1.length);
					Random reel2roll = new Random();
					int roll2 = reel2roll.nextInt(reel2.length);
					Random reel3roll = new Random();
					int roll3 = reel3roll.nextInt(reel3.length);
					reader.returnMessage(sender, "&9" + "---" + "&e" +reel1[roll1] + "&9" + " - " + "&e" + reel2[roll2] + "&9\t" + " - " + "&e" + reel3[roll3] + "&9\t" + "---" );
					if(reel1[roll1].equalsIgnoreCase("Diamond") && reel2[roll2].equalsIgnoreCase("Diamond") && reel3[roll3].equalsIgnoreCase("Diamond")){
						total += 2500;
						winnings += 2500;
						server.broadcastMessage("Jackpot! " + name + " has won 2500 po8");
					}
					else
					{
						if(reel1[roll1].equalsIgnoreCase("Redstone") && reel2[roll2].equalsIgnoreCase("Redstone") && reel3[roll3].equalsIgnoreCase("Redstone")){
							total += 200;
							winnings += 200;
						}
						else
						{
							if(reel1[roll1].equalsIgnoreCase("Gold") && reel2[roll2].equalsIgnoreCase("Gold") && reel3[roll3].equalsIgnoreCase("Gold")){
								total += 100;
								winnings += 100;
							}
							else
							{
								if(reel1[roll1].equalsIgnoreCase("Iron") && reel2[roll2].equalsIgnoreCase("Iron") && reel3[roll3].equalsIgnoreCase("Iron")){
									total += 50;
									winnings += 50;
								}
								else
								{
									if(reel1[roll1].equalsIgnoreCase("Coal") && reel2[roll2].equalsIgnoreCase("Coal") && reel3[roll3].equalsIgnoreCase("Coal")){
										total += 25;
										winnings += 25;
									}
									else
									{
										if(reel1[roll1].equalsIgnoreCase("Lapis") && reel2[roll2].equalsIgnoreCase("Lapis") && reel3[roll3].equalsIgnoreCase("Lapis")){
											total += 750;
											winnings += 750;
										}
										else
										{
											if(reel1[roll1].equalsIgnoreCase("Lapis") && reel2[roll2].equalsIgnoreCase("Lapis")){
												total += 10;
												winnings += 10;
											}
											else
											{
												if(reel1[roll1].equalsIgnoreCase("Lapis") && reel3[roll3].equalsIgnoreCase("Lapis")){
													total += 10;
													winnings += 10;
												}
												else
												{
													if(reel2[roll2].equalsIgnoreCase("Lapis") && reel3[roll3].equalsIgnoreCase("Lapis")){
														total += 10;
														winnings += 10;
													}
													else{	
														if(reel1[roll1].equalsIgnoreCase("Lapis") || reel2[roll2].equalsIgnoreCase("Lapis") || reel3[roll3].equalsIgnoreCase("Lapis")){
															total += 2;
															winnings += 2;
														}															
																
															
														
													}
												}
											}
										}
									}
								}
							}
						}
					}
					}//end for
					reader.returnMessage(sender, "&9 In " + number + " plays, you won " + winnings + " coins and now you have " + total + " coins");
					reader.returnMessage(sender, "&b~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
					wrt.println(SlotMachine[0] + ":" + total);
			
			
			} 
			else 
			{
				wrt.println(line);
			}
		}
		wrt.close();
		rdr.close();
		if (!fSlot.delete()) {
			System.out.println("Cannot delete " + fSlot.getName());
			return;
		}
		if (!slotTmp.renameTo(fSlot)) {
			System.out.println("Cannot rename " + slotTmp.getName() + " to " + fSlot.getName());
			return;
		}

		
		
		}
	} catch (Exception e) {
		System.out.println("Unexpected error " + e.getMessage());
		return;
	}
} // play


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
