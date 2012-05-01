package com.mcnsa.po8;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Wool;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.WorldEditException;

public class HttpCommandThread extends Thread {
    private JavaPlugin plugin;
    private Logger log;
    private JavaHttpUrlConnectionReader reader;
    private String apiKey;
    private String apiUrl;
    private CommandSender sender;
    private String label;
    private String[] args;
    private slapi slapi;

    public HttpCommandThread(po8 plugin,Logger log,
            JavaHttpUrlConnectionReader reader, String apiKey, String apiUrl,
            CommandSender sender, Command command, String label,
            String[] args){
        super("threadless");//do I need the string in here?
        this.log = log;
        this.reader = reader;
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
        this.plugin = plugin;
        this.reader = reader;
        this.sender = sender;
        this.label = label;
        this.args = args;
    }
    
    @Override
    public void run(){
        if(!(sender instanceof Player))    //prevents errors if send from console
            sender.sendMessage("Console may not operate Po8");
        else if (label.equalsIgnoreCase("ptransfer"))
            ptransfer();
        else if (label.equalsIgnoreCase("pbalance"))
            pbalance();
        else if (label.equalsIgnoreCase("paccount"))
        	paccount();
        else if (label.equalsIgnoreCase("po8"))
            showHelp("");
        else if (label.equalsIgnoreCase("po8admin") || label.equalsIgnoreCase("padmin"))
        	po8admin();
        else if (label.equalsIgnoreCase("shopkeeps"))
        	shopkeeps();
        else if(label.equalsIgnoreCase("shop"))
            shop();
        else if(label.equalsIgnoreCase("setxp"))
        	setxp();
        else if(label.equalsIgnoreCase("getxp"))
        	getxp();
        else if(label.equalsIgnoreCase("psell"))
        	peerSelling();
        else if(label.equalsIgnoreCase("paccept"))
        	paccept();
        else if(label.equalsIgnoreCase("preject"))
        	preject();
        else if(label.equalsIgnoreCase("ptoggle"))
        	ptoggle();
        else if (label.equalsIgnoreCase("phelp"))
        	showHelp("");
        else if(label.equalsIgnoreCase("psearch"))
        	psearch();
        else if(label.equalsIgnoreCase("shopreq"))
        	shopreq();
        else if(label.equalsIgnoreCase("close"))
        	closeReq();

    }
    
    private void closeReq() {
    	String adminList = reader.loadURL(apiUrl+"?key=" + apiKey +"&admin&adminlist");
    	String senderName = sender.getName();
    	String userName = null;
    	String message = null;
    	String reqNum = null;
    	
    	Map<String, String[]> shopReqs;
		//Load up hashmap
        try {
			shopReqs = (Map<String, String[]>)slapi.load(po8.dir + "/po8shopreqs.dat");
		} catch (Exception e) {
			returnMessage(sender, "&cError: Failure loading shopreqs, sorry:  " + e);
			return;
		}
        if (adminList.toLowerCase().contains(senderName.toLowerCase())){
			if (args.length >= 1){
				if (shopReqs.containsKey(args[0])){
					shopReqs.remove(args[0]);
					try {
		    			slapi.save(shopReqs, po8.dir + "/po8shopreqs.dat");
		    		} catch (Exception e) {
		    			log.info("Error saving shopreqs: " + e);
		    			return;
		    		}
					returnMessage(sender, "&aShopReq &c"+args[0]+" &aremoved");
					return;
				}
				else {
					returnMessage(sender, "&cNo ShopReq by that id number");
					return;
				}
			}
			else {
				returnMessage(sender, "&cPlease enter an id number of the shopreq you wish to close");
				return;
			}
        }
	}

	private void shopreq() {
    	String adminList = reader.loadURL(apiUrl+"?key=" + apiKey +"&admin&adminlist");
    	String senderName = sender.getName();
    	String userName = null;
    	String message = null;
    	String reqNum = null;
    	
    	Map<String, String[]> shopReqs;
		//Load up hashmap
        try {
			shopReqs = (Map<String, String[]>)slapi.load(po8.dir + "/po8shopreqs.dat");
		} catch (Exception e) {
			returnMessage(sender, "&cError: Failure loading shopreqs, sorry:  " + e);
			return;
		}
    	
    	if (args.length == 0) {
    		//If admin, display list of 
    		if (adminList.toLowerCase().contains(senderName.toLowerCase())){

    			if (shopReqs.size() <= 1){
    				returnMessage(sender, "&6No Pending ShopReqs &a:)");
    				return;
    			}
    			returnMessage(sender, "&a[&6Pending ShopReqs&a]");
    			
    			TreeSet<String> keySet = new TreeSet<String>(shopReqs.keySet());
    			for (String key : keySet) { 
    				reqNum = key;
    				if (!reqNum.equalsIgnoreCase("AutoNum")){
	    				message = shopReqs.get(reqNum)[1];
	    				String dispMsg = "";
	    				if (message.length() > 30){
	    					dispMsg = message.substring(0,29);
	    				}
	    				else {
	    					dispMsg = message;
	    				}
	    				userName = shopReqs.get(reqNum)[0];
    				
    					returnMessage(sender, "&a[&c" + reqNum + "&a] &a|&6" + userName + "&a| - &6"+dispMsg+"&6 . . .");
    				}
    			}
    			return;	
    		}
    		else {
    		returnMessage(sender, "&cUsage: /shopreq <enter text of request here>");
    		return;
    		}
    	}
    	//Read the shopreq of the given player off to the admin
    	if (args.length == 1){
    		if (args[0].equalsIgnoreCase("help")){
    			showHelp("4");
    			return;
    		}
    			
    		if (adminList.toLowerCase().contains(senderName.toLowerCase())){
    			reqNum = args[0].toString();
    			try{
	    			userName = shopReqs.get(reqNum)[0];
	    			message = shopReqs.get(reqNum)[1];
	    			returnMessage(sender, "&a[&c"+reqNum+"&a][&c"+userName+"&a] &6- " + message);
    			}catch (Exception e) {
    				returnMessage(sender, "&cNo Shopreq for that ID");
    			}
    			return;
    		}
    		else {
    			returnMessage(sender, "&cPlease enter more text");
    			return;
    		}
    	}
    	//Store new/overwrite old shopreq 
    	if(args.length >= 2) {
    		if(args.length <= 3){
    			returnMessage(sender, "&cPlease enter more text!");
    			return;
    		}
    		else{
    			int autoNum = Integer.parseInt(shopReqs.get("AutoNum")[0]);
    			autoNum++;
    			String newMessage = "";
    			for(int i=0;i<=args.length-1;i++){
    				newMessage = newMessage + " " + args[i];
    			}
    			shopReqs.put(""+autoNum, new String[]{sender.getName(),newMessage});
    			shopReqs.put("AutoNum", new String[]{""+autoNum});
    			
    			returnMessage(sender, "&aShopreq Submitted!");
    			
    			String[] adminAlert = adminList.trim().split(":");
    			for(int i=0; i<=adminAlert.length-1; i++){
    				Player player = Bukkit.getServer().getPlayer(adminAlert[i].trim());
    				if (player != null){
	    				player.sendMessage(ChatColor.GREEN + "New Shopreq Submitted!" + ChatColor.YELLOW + " use /shopreq to check");
    				}

    			}
    		}
    		try {
    			slapi.save(shopReqs, po8.dir + "/po8shopreqs.dat");
    		} catch (Exception e) {
    			log.info("Error saving shopreqs: " + e);
    			return;
    		}
    	}
		
	}

	private void psearch() {
    	itemSearch Search = new itemSearch();
		if (args.length >= 1){
			try {
				Search.searchItem(args[0], sender);
			} catch (WorldEditException e) {
			}
		}
		else {
			returnMessage(sender, "&cPlease enter a search string!");
		}
		
	}

	private void ptransfer(){
        String usage = "Usage: /ptransfer [player] [amount]";
            if(args.length != 2){
            	showHelp("1");
                //return;
            }
            //make player match non-case-sensitive = equalsIgnoreCase
            else{
                String amountStr = args[1];
                if (!isDouble(amountStr)){
                	returnMessage(sender, "&cWrong 'amount' value! " +usage);
                    return;
                }
                double amount = ((int)(100*Double.parseDouble(amountStr)+0.5))/100.0;//round it
                String recipientStr = args[0];
                Player recipient = Bukkit.getServer().getPlayer(recipientStr);
                if(recipient == null){
                	returnMessage(sender, "&cError: recipient is not currently online.");
                }
                else if (amount==0){
                	returnMessage(sender, "&cSorry, you can't transfer nothing.");
                }
                else if (amount<0){
                	returnMessage(sender, "&cYou're trying to send *negative* money?!?!");
                }
                else if(recipient == sender){
                	returnMessage(sender, "&cYou can't give yourself money!");
                }
                else{
                    String request = apiUrl+"?key="+apiKey+"&transfer&t="+
                                recipient.getName()+"&f="+sender.getName()+"&p="+amount;
                    String result = reader.loadURL(request);
                    if(result==null || !(result.contains("Success:"))|| result.contains("Error:"))
                        sender.sendMessage(ChatColor.RED + result+".");
                    else{
                        String orgName = plugin.getConfig().getString("organization_name");
                        sender.sendMessage(ChatColor.GREEN +
                                "Thank you for banking with " + orgName + "! Please allow 3-5 ms for your transaction to clear.");
                        sender.sendMessage(ChatColor.YELLOW + result+".");
                        recipient.sendMessage(ChatColor.YELLOW + sender.getName()+" has just sent you "+String.format("%.2f",amount)+" po8!");
                        log.info("Po8: "+sender.getName()+" transfered "+String.format("%.2f",amount)+" po8 to "+recipient.getName());
                    }
                }
            }
    }
    
    private void pbalance(){
    	String header = "Your";
        String playerName = sender.getName();
        String[] balance = null;
        String adminList = reader.loadURL(apiUrl+"?key=" + apiKey +"&admin&adminlist");
        //If admin and args[0] is set lookup that user's balance
    	if (args.length >=1 && adminList.toLowerCase().contains(playerName.toLowerCase())){
    		balance = reader.loadURL(apiUrl+"?key="+apiKey+"&balance&username="+args[0]).trim().split(": ");
    		header = args[0]+"'s";
    	}
    	else if (args.length >=1 && !adminList.toLowerCase().contains(playerName.toLowerCase())){
    		returnMessage(sender, "&cYou are not an admin, and cannot lookup others' balances");
    		return;
    	}
    	//else output error or balance for that user
    	else if (args.length == 0){
    		balance = getBalance().trim().split(": ");
    	}
        if (balance[0].contains("Error")){
        	if (balance[1].contains("found")){
        		returnMessage(sender, "&cError: Player not found");
        		return;
        	}
        }
        else returnMessage(sender, "&a"+header+" balance is &6" + balance[1]);

    }
    
    private void paccount(){
    	String playerName = sender.getName();
    	
    	if(args.length == 3 && args[0].equalsIgnoreCase("create")){
    		String query = apiUrl + "?key=" + apiKey + "&account&create&username=" + playerName + "&password=" + args[1] + "&email=" + args[2];
    		String[] result = reader.loadURL(query).trim().split(": ");
    		log.info(query);
    		if(result[0].equalsIgnoreCase("success")){
    			returnMessage(sender, "&aSuccess: Account Created");
    			return;
    		}
    		else {
    			returnMessage(sender, "&c"+result[0]+": "+result[1]);
    			return;
    		}
    	}
    	else if(args.length == 2 && args[0].equalsIgnoreCase("changepass")){
    		String query = apiUrl + "?key=" + apiKey + "&account&changepass&username=" + playerName + "&password=" + args[1];
			String[] result = reader.loadURL(query).trim().split(": ");
    		log.info(query);
    		if(result[0].equalsIgnoreCase("success")){
    			returnMessage(sender, "&aSuccess: Password Changed");
    			return;
    		}
    		else {
    			returnMessage(sender, "&c"+result[0]+": "+result[1]);
    			return;
    		}
    	}
    	else {
    		showHelp("");
    	}
    	return;
    	
    }
    
    private void shop(){
		if(args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("help"))) {
                showHelp("");
                return;
        }
        //shop stock <item id>  &echecks the shop's stock
        if(args.length >= 2 && (args[0].equalsIgnoreCase("stock") || args[0].equalsIgnoreCase("s"))) {				

                //I suck and this makes no sense forcing you all to use properly formatted item id's for the moment   -fusty
                //String itemId = processItemName(1);
                String[] typedItemData = getTypedItemData(args[1]);
                String itemId = typedItemData[0];
                String itemName = typedItemData[1];
                if (typedItemData[0].equalsIgnoreCase("error")){
        			returnMessage(sender, "&cError: " + typedItemData[1]);
        			return;
        		}
                if (itemId.contains("17")){
                	itemId = "17";
                }
                if (itemId.equalsIgnoreCase("35:0")){
                	itemId = "35";
                }
                String request = apiUrl+"?key="+apiKey+"&stock&itemid=" + itemId;
                String[] result = reader.loadURL(request).trim().split(": ");
                log.info(request);
                log.info(reader.loadURL(request).trim());
                String stock = result[1];
                String error = result[0];
                if(error.equalsIgnoreCase("error")) {
                        returnMessage(sender, "&cError: " + result[1]);
                }
                else {
			//API no longer returns arrays or item names.  That'll be fustyversion2.0
                        //String name = //result[1];
                        returnMessage(sender, "&6The shop currently has &e" + stock + "&a " + itemName + " &6in stock.");
                }
        }
        //shop price sell <item ID> [amount]
        else if(args.length >= 3 && (args[0].equalsIgnoreCase("price") || args[0].equalsIgnoreCase("p")) && (args[1].equalsIgnoreCase("sell") || args[1].equalsIgnoreCase("s"))) {				
                //Again, don't know how to handle names, i will leverage bukkit for that soon
                //String id = processItemName(2);
        		String[] typedItemData = getTypedItemData(args[2]);
        		String itemId = typedItemData[0];
        		String itemName = typedItemData[1];
        		if (typedItemData[0].equalsIgnoreCase("error")){
        			returnMessage(sender, "&cError: " + typedItemData[1]);
        			return;
        		}
        		if (itemId.contains("17")){
                	itemId = "17";
                }
        		if (itemId.equalsIgnoreCase("35:0")){
                	itemId = "35";
                }
                String amount = "" + "1"; 
                if(args.length >=4){
                    amount = args[3];
                }

				String query = apiUrl+"?sellprice&itemid=" + itemId + "&amount=" + amount+"&key="+apiKey;
                log.info(query);
                String[] result = reader.loadURL(query).trim().split(":");
                
                // now parse the result!
                String price = result[1];
                String error = result[0];
                // and report the results!
                if(error.equalsIgnoreCase("Error")) {
                        returnMessage(sender, "&c" + result[0] + ": " + result[1]);
                }
                else {
                        returnMessage(sender, "&6The shop will sell you &e" + amount + "&a " + itemName + " &6for: &e" + price + " &6Po8!");
                }
        }
        //shop price buy <item name> [amount]
        else if(args.length >= 3 && (args[0].equalsIgnoreCase("price") || args[0].equalsIgnoreCase("p")) && (args[1].equalsIgnoreCase("buy") || args[1].equalsIgnoreCase("b"))) {
                //Again, don't know how to handle names.
                //String id = processItemName(2);
        		String[] typedItemData = getTypedItemData(args[2]);
        		String itemId = typedItemData[0];
        		String itemName = typedItemData[1];
        		if (typedItemData[0].equalsIgnoreCase("error")){
        			returnMessage(sender, "&cError: " + typedItemData[1]);
        			return;
        		}
        		if (itemId.contains("17")){
                	itemId = "17";
                }
        		if (itemId.equalsIgnoreCase("35:0")){
                	itemId = "35";
                }
                String amount = "" + "1";
                if(args.length >=4){
                    amount = args[3];
                }
                // ok, now do the lookup
                String query = apiUrl+"?buyprice&itemid=" + itemId + "&amount=" + amount+"&key="+apiKey;
                String resultStr = reader.loadURL(query);
                log.info(query);
                // parse resultStr
                String[] result = resultStr.split(": ");
                String price = result[1];
                String error = result[0];
                // and report the results!
                if(error.contains("Error")) {
                        returnMessage(sender, "&c" + resultStr);
                }
                else if(!isDouble(price)){
                    returnMessage(sender,"&cYou have entered an incorrect price value. Usage: "+
                            "/shop price sell <item id> [amount]");
                }
                else {
                        returnMessage(sender, "&6The shop will buy &e" + amount + "&a " + itemName + " &6from you for: &e" + price + " &6Po8!");
                }
        }
    }
    
    private void po8admin(){
    	String senderName = sender.getName();
    	if(sender.hasPermission("po8.admin")){
	    	if((args.length >= 1 && args[0].equalsIgnoreCase("help")) || (args.length == 0)){
	    		showHelp("3");
	    	}
	    	else if(args.length == 1 && args[0].equalsIgnoreCase("add")){
	    		showHelp("3");
	    		return;
	    	}
	    	else if(args.length >= 2 && args[0].equalsIgnoreCase("add")){
	    		String query = apiUrl+"?key="+apiKey + "&admin&add&cadmin=" + senderName + "&madmin=" + args[1];
	    		String[] result = reader.loadURL(query).trim().split(": ");
	    		if(result[0].equalsIgnoreCase("error")){
	    			returnMessage(sender, "&cError: " + result[1]);
	    			return;
	    		}
	    		else {
	    			returnMessage(sender, "&aSuccess: " + result[1]);
	    			return;
	    		}
	    	}
	    	else if(args.length == 1 && args[0].equalsIgnoreCase("revoke")){
	    		showHelp("3");
	    		return;
	    	}
	    	else if(args.length >= 2 && args[0].equalsIgnoreCase("revoke")){
	    		if (args[1].contains(senderName)){
	    			returnMessage(sender, "&cError: Get someone else to revoke your admin status");
	    			return;
	    		}
	    		String query = apiUrl+"?key="+apiKey + "&admin&revoke&cadmin=" + senderName + "&madmin=" + args[1];
	    		String[] result = reader.loadURL(query).trim().split(": ");
	    		if(result[0].equalsIgnoreCase("error")){
	    			returnMessage(sender, "&cError: " + result[1]);
	    			return;
	    		}
	    		else {
	    			returnMessage(sender, "&aSuccess: " + result[1]);
	    		}
	    	}
    	}
    	return;
    }
    
    private void shopkeeps(){
    	String query = apiUrl+"?key="+apiKey+"&admin&shopkeep";
    	String resultString = reader.loadURL(query);
    	log.info(query);
    	String[] result = resultString.trim().split(": ");
    	if(resultString.contains("Error")){
    		returnMessage(sender, "&c" + resultString);
    		return;
    	}
    	else if(result[0].contains("Success")){
    		String[] adminArray = result[1].trim().split(":");
    		String online = "&aOnline: ";
    		String offline = "&cOffline:&8 ";
    		for(int k=0;k<adminArray.length;k++){
    			String currentAdmin = adminArray[k];
                Player onlineAdmin = Bukkit.getServer().getPlayer(currentAdmin);
                if(onlineAdmin == null){
                	offline = offline + currentAdmin + ", ";
                }
                else{
                	online = online + currentAdmin + ", ";
                }
                
    		}
            online = online.substring(0,online.length()-2);
            if(online.length() <=11){
            	returnMessage(sender, "&cSorry, no shopkeeps onlilne!");
            	return;
            }
    		returnMessage(sender, "&6[Po8 Shopkeeps] " + online);
    		return;
    	}
    	else {
    		returnMessage(sender, "&c" + resultString);
    		return;
    	}
    	
    	
    }
    
    private void setxp(){
    	/*

    	
    	if(args.length >= 2 && sender.hasPermission("po8.setxp")){
    		int amount = Integer.parseInt(args[1]);
    		Player player = Bukkit.getServer().getPlayer(args[0]);
    		if(player == null){
    			returnMessage(sender, "&cError: The player is not online.");
    		}else if(amount < 0){
    			returnMessage(sender, "&cError: You cannot set a negative XP level.");
    		}else {
    			player.setLevel(amount);
    			returnMessage(sender, "&6" + player.getName() + "'s &aXP level set to&6 " + amount + "&a.");
    			returnMessage(player, "&6" + senderName + " &ahas set your XP to &6" + amount + "&a.");
    		}
    	}
    	else if(!sender.hasPermission("po8.setxp")){
    		returnMessage(sender, "&cYou do not have permission to use this command!");
    	}
    	else {
    		returnMessage(sender, "&cUsage: /setxp [player] [level]");
    	}
    	*/
    	returnMessage(sender, "&cNOT SO FAST, SMARTY PANTS!");
    	return;    	
    }
    
    private void getxp(){
    	if(args.length >= 1 && sender.hasPermission("po8.getxp")){
    		Player player = Bukkit.getServer().getPlayer(args[0]);
    		if(player == null){
    			returnMessage(sender, "&cError: The player is not online.");
    		}else {
    			int amount = player.getLevel();
    			returnMessage(sender, "&6" + player.getName() + "'s &aXP level is&6 " + amount + "&a.");
    		}
    	}
    	else if(!sender.hasPermission("po8.getxp")){
    		returnMessage(sender, "&cYou do not have permission to use this command!");
    	}
    	else {
    		returnMessage(sender, "&cUsage: /getxp [player]");
    	}
    	return;    	
    	
    }
    
    @SuppressWarnings("unchecked")
	private void peerSelling(){
    	if(args.length == 3 && args[1].equalsIgnoreCase("held")){
    		Player recipient = Bukkit.getServer().getPlayer(args[0]);
    		Player offerer = Bukkit.getServer().getPlayer(sender.getName());
    		String itemId = null;
    		String itemName = null;
    		String amount = null;
    		if (recipient == null){
    			returnMessage(sender, "&cPlayer not online!");
    			return;
    		}
    		if (offerer.getLocation().distance(recipient.getLocation()) >= 64){
    			returnMessage(offerer, "&cYou should be able to see your customer, get within 64 blocks.");
    			return;
    		}
    		if(Integer.parseInt(args[2]) <= 0){
    			returnMessage(sender, "&cPrice must be greater than zero: " + args[2]);
    			return;
    		}
    		String[] heldStackData = getHeldStackData(offerer);
    		if (heldStackData.length == 3){
    			itemId = heldStackData[0];
    			itemName = heldStackData[1];
    			amount = heldStackData[2];
    		}
    		else {
    			returnMessage(sender, "&cError: " + heldStackData[1]);
    			return;
    		}
	        String[] transaction = {sender.getName(),itemId,amount,args[2]};
            //Load up hashmap
            Map<String, String[]> transactionList = new HashMap<String, String[]>();
            try {
    			transactionList = (Map<String, String[]>)slapi.load(po8.dir + "/po8transactions.dat");
    		} catch (Exception e) {
    			returnMessage(sender, "&cError: Failure loading transaction, please try again: " + e);
    			return;
    		}
            if (itemName.contains("AIR")){
            	returnMessage(sender, "&cNo selling of air!");
            	return;
            }
            transactionList.put(recipient.getName(), transaction);
            try {
				slapi.save(transactionList, po8.dir + "/po8transactions.dat");
			} catch (Exception e) {
				returnMessage(sender, "&cError: " + e);
				return;
			}
            returnMessage(sender, "&6You offer &a" + recipient.getName() + " &e" + transaction[2] + " &a " + itemName + " &6 for &e" + transaction[3] + " Po8");
            returnMessage(recipient, "&a" + sender.getName() + " &6offered you &e" + transaction[2] + " &a" + itemName + "&6 for &e" + transaction[3] + " Po8");
            returnMessage(recipient, "&aType /paccept to accept, /preject to reject");
            return;
    	}
    	else if(args.length <= 3){
    		showHelp("2");
    		return;
    	}
    	else if(args.length >=4){
    		Player recipient = Bukkit.getServer().getPlayer(args[0]);
    		//Error if player is not online
    		if (recipient == null){
    			returnMessage(sender, "&cPlayer is not online.");
    			return;
    		}
    		//Error if price is a decimal
    		String itemName = null;
    		int price = 0;
    		try {
    			price = Integer.parseInt(args[3]);
    		}
            catch (NumberFormatException e) {
            	returnMessage(sender, "&cPrice must be an integer, no decimals!");
            	return;
            }
    		int amount = 0;
    		try {
    			amount = Integer.parseInt(args[2]);
    		}
            catch (NumberFormatException e) {
            	returnMessage(sender, "&cQuantity must be an integer, no decimals!");
            	return;
            }
    		if (amount <= 0){
    			returnMessage(sender, "&cQuantity must be greater than zero");
    			return;
    		}
    		if (price <= 0 ){
    			returnMessage(sender, "&cPrice must be greater than zero");
    			return;
    		}
    		
    		String quantity = args[2];
    		
    		String[] typedItemData = getTypedItemData(args[1]);
    		if (typedItemData[0].equalsIgnoreCase("error")){
    			returnMessage(sender, "&cError: "+typedItemData[1]);
    			return;
    		}
    		String itemId = typedItemData[0];
    		itemName = typedItemData[1];
            //Store transaction in hashmap and save with slapi
            
            String[] transaction = {sender.getName(),itemId,quantity,args[3]};
            //Load up hashmap
            Map<String, String[]> transactionList = new HashMap<String, String[]>();
            try {
            	
    			transactionList = (Map<String, String[]>)slapi.load(po8.dir + "/po8transactions.dat");
    		} catch (Exception e) {
    			returnMessage(sender, "&cError: Failure loading transaction, please try again: " + e);
    			return;
    		}
            
            transactionList.put(recipient.getName(), transaction);
            try {
				slapi.save(transactionList, po8.dir + "/po8transactions.dat");
			} catch (Exception e) {
				returnMessage(sender, "&cError: " + e);
				return;
			}
            returnMessage(sender, "&6You offer &a" + recipient.getName() + " &e" + transaction[2] + " &a " + itemName + " &6 for &e" + transaction[3] + " Po8");
            returnMessage(recipient, "&a" + sender.getName() + " &6offered you &e" + transaction[2] + " &a" + itemName + "&6 for &e" + transaction[3] + " Po8");
            returnMessage(recipient, "&aType /paccept to accept, /preject to reject");
            return;
    	}
    		
    }
    
    @SuppressWarnings("unchecked")
	private void paccept(){
		Player recipient = Bukkit.getServer().getPlayer(sender.getName());
		//I hope this initializes a hashmap
		Map<String, String[]> transactionList;
		//Load up hashmap
        try {
			transactionList = (Map<String, String[]>)slapi.load(po8.dir + "/po8transactions.dat");
		} catch (Exception e) {
			returnMessage(sender, "&cError: Failure loading transaction, please try again: " + e);
			return;
		}
        String[] transaction;
        //Check for transaction presence
        if (transactionList.containsKey(recipient.getName())){
            transaction = transactionList.get(recipient.getName());
        }
        else {
        	returnMessage(sender, "&cNo Transaction Found!");
        	return;
        }
        
        Player offerer =  Bukkit.getServer().getPlayer(transaction[0]);
        if (offerer == null){
        	returnMessage(sender, "&cPlayer no longer online");
        	return;
        }
        Short itemDamage = null;
        int itemNumber = 0;
        Material material = null;
        
        if(transaction[1].contains(":")){
        	String[] damageItem = transaction[1].split(":");
        	itemNumber = Integer.parseInt(damageItem[0]);
        	itemDamage = Short.parseShort(damageItem[1]);
            material = new MaterialData(itemNumber, itemDamage.byteValue()).getItemType();
        }
        else{
        	itemNumber = Integer.parseInt(transaction[1]);
            material = new MaterialData(itemNumber).getItemType();
            itemDamage = 0;
        }

        		
        int amount = Integer.parseInt(transaction[2]);
        
        //Get Player Inventory objects
        PlayerInventory fromInventory = offerer.getInventory();
        PlayerInventory toInventory = recipient.getInventory();
        
        //Backup the player's inventory so they don't yell at me
        ItemStack[] toInventoryBackup = toInventory.getContents();
        ItemStack[] fromInventoryBackup = fromInventory.getContents();
        
        //Define the item Stacks
        
        ItemStack transactionStack = new ItemStack(material, amount, itemDamage);
      
        //Check if seller actually has the items, and if so deduct them
        if(fromInventory.contains(material, amount)){
	        int removeRest = removeItem(fromInventory, itemNumber, itemDamage, amount);
	        if (removeRest != amount){
	        	toInventory.clear();
	        	toInventory.setContents(toInventoryBackup);
	        	fromInventory.clear();
	        	fromInventory.setContents(fromInventoryBackup);
	        	returnMessage(sender, "&cThere is something wrong with the items in your inventory.  Make sure you have the right material/color item(s)!");
	        	return;
	        }

	        //Use the check stack as adding to inventories is the easy part
	        HashMap<Integer, ItemStack> toinventoryError = toInventory.addItem(transactionStack);       
	        //Cancel if there is no room
	        if(toinventoryError.size() > 0){
	        	//Restore inventories  NOTE: THIS IS ENCHANTMENT AND DAMAGED ITEM SAFE
	        	toInventory.clear();
	        	toInventory.setContents(toInventoryBackup);
	        	fromInventory.clear();
	        	fromInventory.setContents(fromInventoryBackup);
	        	returnMessage(sender, "&cYour inventory is full, please empty and try again.");
	        	return;
	        }
	        else{
	        	//Run the transfer
	            String query = apiUrl+"?key=" + apiKey + "&transfer&t=" + offerer.getName() + "&f=" + recipient.getName() + "&p=" + transaction[3];
	            String result = reader.loadURL(query);
	            log.info(query);
	            if(result==null || !(result.contains("Success"))|| result.contains("Error")){
	                toInventory.clear();
		        	toInventory.setContents(toInventoryBackup);
		        	fromInventory.clear();
		        	fromInventory.setContents(fromInventoryBackup);
		        	returnMessage(sender, "&c" + result);
		        	returnMessage(sender, "&cTransaction Cancelled");
		        	returnMessage(offerer, "&cTransaction Cancelled");
		        	transactionList.remove(recipient.getName());
		        	return;
	            }
	            	
	            else{
	                recipient.sendMessage(ChatColor.YELLOW + result+".");
	                returnMessage(sender, "&aTransaction complete!");
	                returnMessage(offerer, "&aTransaction complete!");
	                transactionList.remove(recipient.getName());
	            }
	        }
        }
        else {
        	returnMessage(sender, "&cThe seller has insufficient materials in their inventory.  Transaction Cancelled");
        	returnMessage(offerer, "&cItems not present in your inventory for transaction.  Transaction Cancelled");
        	transactionList.remove(recipient.getName());
        }
        try {
			slapi.save(transactionList, po8.dir + "/po8transactions.dat");
		} catch (Exception e) {
			log.info("Error saving transaction list: " + e);
			return;
		}
    }
    
    @SuppressWarnings("unchecked")
	private void preject(){
    	Player recipient = Bukkit.getServer().getPlayer(sender.getName());
		//I hope this initializes a hashmap
		Map<String, String[]> transactionList;
		//Load up hashmap
        try {
			transactionList = (Map<String, String[]>)slapi.load(po8.dir + "/po8transactions.dat");
		} catch (Exception e) {
			returnMessage(sender, "&cError: Failure loading transaction, please try again: " + e);
			return;
		}
        String[] transaction;
        if (transactionList.containsKey(recipient.getName())){
        	transaction = transactionList.get(recipient.getName());
        	returnMessage(sender, "&cTransaction Cancelled");
        	Player offerer = Bukkit.getServer().getPlayer(transaction[0]);
        	returnMessage(offerer, "&cTransaction cancelled");
        	transactionList.remove(recipient.getName());
        }
        else {
        	returnMessage(sender, "&cNo Transaction Found");
        }
        try {
			slapi.save(transactionList, po8.dir + "/po8transactions.dat");
		} catch (Exception e) {
			log.info("Error saving transaction list: " + e);
			return;
		}
        
    	
    }
    
    private void ptoggle() {
    	String senderName = sender.getName();
    	String adminList = reader.loadURL(apiUrl+"?key=" + apiKey +"&admin&adminlist");
    	if(adminList.toLowerCase().contains(senderName.toLowerCase())){
    		if (args.length >= 1){
    			String query = apiUrl+"?key="+apiKey+"&admin&onlist&player="+senderName+"&option="+args[0];
    			String[] result = reader.loadURL(query).trim().split(": ");
    			if (result[0].equalsIgnoreCase("success")){
    				returnMessage(sender, "&aSuccess: Your Shopkeeper Status is toggled &6"+args[0]);
    				return;
    			}
    			else {
    				returnMessage(sender, "&aError: "+result[1]);
    				return;
    			}
    		}
    		else {
    			returnMessage(sender, "&cUsage /ptoggle <on/off>");
    			return;
    		}
    	}
    	else {
    		returnMessage(sender, "&cYou ain't no Shopkeeper!");
    		return;
    	}
    }
    
    public String[] getTypedItemData(String itemInput) {
    	itemSearch Search = new itemSearch();
    	Integer itemNumber = null;
    	Short itemDamage = 0;
    	String itemName = null;
    	String itemId = "0";
    	Material material = null;
    	Character inputChar = itemInput.charAt(0);
    	if (itemInput.equalsIgnoreCase("wool")){
    		itemInput = "wool:white";
    	}
    	
    	if (Character.isDigit(inputChar)){
    		if (itemInput.contains(":")){
    			String[] damageItem = itemInput.split(":");
    			itemNumber = Integer.parseInt(damageItem[0]);
    			itemDamage = Short.parseShort(damageItem[1]);
    			itemName = new MaterialData(itemNumber.intValue(), itemDamage.byteValue()).getItemType().toString();
    			itemId = itemNumber.toString();
    			if (itemDamage != 0){
    	        	itemId = itemId + ":" + itemDamage.intValue();
    	        }
    			else if (itemDamage == 0 && itemInput.contains("35")){
    				itemId = itemId + ":" + "0";
    			}
    			if (itemInput.contains("35")){
    				Wool wool = new Wool(itemNumber.intValue(), itemDamage.byteValue());
    				itemName = itemName + ":" + wool.getColor().toString();
    			}
    		}
	    	else if (!itemInput.contains(":") && !itemInput.equalsIgnoreCase("held")){
	    		itemNumber = Integer.parseInt(itemInput);
	    		itemId = itemInput;
	    		itemName = new MaterialData(itemNumber.intValue()).getItemType().toString();
	    	}
    	}
    	else if (itemInput.equalsIgnoreCase("held")){
    		Player player = Bukkit.getServer().getPlayer(sender.getName());
    		String[] heldStackData = getHeldStackData(player);
    		if (heldStackData.length == 3){
    			itemId = heldStackData[0];
    			itemName = heldStackData[1];
    		}
    		else {
    			return new String[]{"Error", heldStackData[1]};
    		}
    	}
    	else if (itemInput.contains(":") && !Character.isDigit(inputChar)) {
    		String[] damageItem = itemInput.split(":");
    		material = Material.matchMaterial(damageItem[0].toString().toUpperCase());
    		if (material == null) {
    			return new String[]{"Error","Invalid Item Name"};
    		}
    		
    		itemNumber = Integer.valueOf(material.getId());
    		itemId = itemNumber.toString();
    		itemName = material.name();
    		
    		DyeColor color = DyeColor.valueOf(damageItem[1].toUpperCase());
    		if (color == null) {
    			return new String[] {"Error", "Invalid Item Color"};
    		}
    		
    		itemName = itemName + ":" + color.toString();
    		byte colorByte = color.getData();
    		itemId = itemId + ":" + colorByte;
    	}
    	else {
    		String[] results = null;
    		String[] query = new String[]{itemInput.toString(),"i"};
    		try {
				results = Search.returnItem(query);
			} catch (WorldEditException e) {
			}
    		if (results[0].equalsIgnoreCase("error")) {
    			query = new String[]{itemInput.toString(),""};
    			try {
    				results = Search.returnItem(query);
    			} catch (WorldEditException e) {
    			}
    		}
    		if (results == null) {
    			return new String[] { "Error", "Invalid Item Name" };
    		}
    		else if (results[0].equalsIgnoreCase("error")){
    			return new String[] {results[0],results[1]};
    		}
    		
    	    itemId = results[0];
    	    itemName = results[1];
    	    
    	    if (itemId.equalsIgnoreCase("75")){
    	    	itemId = "76";
    	    	itemName = "Redstone Torch (on)";
    	    }
    	}
    	return new String[]{itemId,itemName};
    }
    
    
    public String[] getHeldStackData(Player holder){
    	String itemId = "";
    	int itemNumber = 0;
		Short itemDamage = 0;
        PlayerInventory holderInventory = holder.getInventory();
        ItemStack heldStack = holderInventory.getItemInHand();
        itemId = Integer.toString(heldStack.getTypeId());
        String amount = Integer.toString(heldStack.getAmount());
        Map<Enchantment, Integer> enchant = heldStack.getEnchantments();
        if(!enchant.isEmpty()){
        	String[] heldStackData = {"Error: ","You cannot trade enchanted items this way."};
        	return heldStackData;
        }
        itemNumber = Integer.parseInt(itemId);
        itemDamage = heldStack.getDurability();
        if (itemDamage != 0){
        	itemId = itemId + ":" + itemDamage.intValue();
        }
        String itemName = new MaterialData(itemNumber, itemDamage.byteValue()).getItemType().toString();
        if (itemId.contains("35")){
        	Wool wool = new Wool(itemNumber, itemDamage.byteValue());
        	itemName = itemName + ":" + wool.getColor().toString();
        }
        

        String[] heldStackData = {itemId, itemName, amount};
    	return heldStackData;
    }
    
    public String getBalance(){
        String queryString = apiUrl+"?balance&key="+apiKey+"&username="+sender.getName();
        String balance = reader.loadURL(queryString);
		String[] splitbalance = balance.split(": ");
		if(splitbalance[0].contains("Error")){
			return balance;
		}
		else{
			balance = "Success: " + splitbalance[1];
			if(splitbalance[1]!=null && isDouble(splitbalance[1])){
				return "Success: " + String.format("%.2f",Double.parseDouble(splitbalance[1]));
			}
	        	return balance;
		}
	    }
    
    
    public void returnMessage(CommandSender sender, String message) {
		if(sender instanceof Player) {
			sender.sendMessage(processColours(message));
		}
		else {
			sender.sendMessage(message);//plugin.stripColours(message));
		}
	}

	public void showHelp(String page) {
		returnMessage(sender, "&e--- &6Po8 Help &e---");
		String senderName = sender.getName();
    	String adminList = reader.loadURL(apiUrl+"?key=" + apiKey +"&admin&adminlist");
    	if ((args.length >=1 && args[0].equalsIgnoreCase("4")) || page.equalsIgnoreCase("4")){
			if(adminList.toLowerCase().contains(senderName.toLowerCase())) {
				returnMessage(sender, "&6/shopreq  &echecks the pending ShopReqs");
				returnMessage(sender, "&6/shopreq &a<id>  &ereads that specific ShopReq");
				returnMessage(sender, "&6/close &a<id> &eClose's the ShopReq of that id");
				returnMessage(sender, "&6/padmin add &a<player> &eadd's the player to the Po8 Admin list");
				returnMessage(sender, "&6/padmin revoke &a<player> &eremove the player from the Po8 Admin list");
				returnMessage(sender, "&6/ptoggle &a<on/off>  &etoggle if you show up in the /shopkeers list");
			}
			else {
				returnMessage(sender, "&6/shopreq &a<start typing your request here>   &eSubmits a ShopRequest");
			}
		}
    	else if ((args.length >=1 && args[0].equalsIgnoreCase("3")) || page.equalsIgnoreCase("3")){
			returnMessage(sender, "&6/shopkeeps  &edisplays a list of available Shopkeepers");
			returnMessage(sender, "&6/shop price sell &a<item name> [amount]  &ehow much the shop will sell for.");
			returnMessage(sender, "&6/shop price buy &a<item name> [amount]  &ehow much the shop will buy for.");
			returnMessage(sender, "&6/shop stock &a<item id>  &echecks the shop's stock.");
			if(adminList.toLowerCase().contains(senderName.toLowerCase())) {
				returnMessage(sender, "&e[&6/phelp 4 for page 4&e]");
			}
		}
		else if ((args.length >=1 && args[0].equalsIgnoreCase("2")) || page.equalsIgnoreCase("2")){
			returnMessage(sender, "&6All /psell cmds must be done within 64 blocks of the player!");
			returnMessage(sender, "&6/psell &a<player> &6held &a<price>  &ewill offer the <player> whatever is in your hand for <price>");
			returnMessage(sender, "&6/psell &a<player> &6held &a<quantity> <price>  &ewill offer the <player> <quantity> of whatever is in yoru hand for <price>");
			returnMessage(sender, "&6/psell &a<player> <itemId> <quantity> <price> &ewill offer the <player> the items for the price.");
			returnMessage(sender, "&e[&6/phelp 3 for page 3&e]");
		}
		else {
			returnMessage(sender, "&6/pbalance  &edisplays your current Po8 balance");
			if(adminList.toLowerCase().contains(senderName.toLowerCase())) {
				returnMessage(sender, "&6/pbalance &a<player>  &echecks the player's Po8 balance");
			}			
			returnMessage(sender, "&6/ptransfer &a<player> <amount>  &ewill transfer the given funds into the player's account");
			returnMessage(sender, "&6/paccount create &a<password> <email>  &eCreate a new Po8 account for this username");
			returnMessage(sender, "&6/paccount changepass &a<password> &eChange your Po8 account's password");
			returnMessage(sender, "&e[&6/phelp 2 for page 2&e]");
			return;
		}
	}
    
    public boolean isInteger( String input ) {
        try {
            Integer.parseInt( input );
            return true;
        }
        catch( Exception e ) {
            return false;
        }
    }

    public boolean isDouble( String input ) {
        try {
            Double.parseDouble( input );
            return true;
        }
        catch( Exception e ) {
            return false;
        }
    }
    
    // allow for colour tags to be used in strings..
	public String processColours(String str) {
		return str.replaceAll("(&([a-f0-9]))", "\u00A7$2");
	}
	
	public static int removeItem(Inventory inventory, int id, short meta, int quantity) {
        int rest = quantity;
        for( int i = 0 ; i < inventory.getSize() ; i++ ){
            ItemStack stack = inventory.getItem(i); 
            if( stack == null || stack.getTypeId() != id )
                continue;
            if( hasDataValue(id) && stack.getDurability() != meta ){
                continue;
            }
            if( rest >= stack.getAmount() ){
                rest -= stack.getAmount();
                inventory.clear(i);
            } else if( rest>0 ){
                    stack.setAmount(stack.getAmount()-rest);
                    rest = 0;
            } else {
                break;
            }
        }
        return quantity-rest;
    }
	
	public static boolean hasDataValue(int id){
        if( id == 6 )
            return true;
        if( id == 17 )
            return true;
        if( id == 18 )
            return true;
        if( id == 35 )
            return true;
        if( id == 43 )
            return true;
        if( id == 44 )
            return true;
        if( id == 263 )
            return true;
        if( id == 351 )
            return true;
        return false;
    }
 
}

