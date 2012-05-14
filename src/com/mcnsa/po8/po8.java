/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mcnsa.po8;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Danny
 */
public class po8 extends JavaPlugin{

    private static JavaHttpUrlConnectionReader reader = new JavaHttpUrlConnectionReader();
    private static final Logger log = Logger.getLogger("Minecraft");
    static String apiUrl;
    static String apiKey;
    static String accountname;
    static String maxtickets;
    private FileConfiguration customConfig = null;
    private File customConfigFile = null;
    private Po8collectWorker workerPo8collect;
    private LotteryWorker workerLottery;
    private Po8ShopWorker workerPo8Shop;
    private SlotMachineWorker workerSlot;
    private Server server = Bukkit.getServer();
    static String dir = new String("plugins/MCNSApo8");
    static File newdir = new File(dir);

    @Override
    public void onEnable(){
    	//Check for existing config, create if not present
    	File config;
        config = new File(dir + "/config.yml");
        
        if(!config.exists()){
        	saveDefaultConfig();
        }
    	//Write config file down now that it's loaded
        //Client client = new Client("localhost",0,log);
        apiUrl = getCustomConfig().getString("api_url");
        apiKey = getCustomConfig().getString("api_key");
        accountname = getCustomConfig().getString("lottery_account");
        maxtickets = getCustomConfig().getString("maxtickets").trim();
        reader = new JavaHttpUrlConnectionReader();
        String testStr = apiUrl + "?key="+apiKey;
        String result  = reader.loadURL(testStr);
        //log.info(testStr);
        if (result==null || result.trim().equals("Invalid API key")){
            if (result==null)
                result = "Cannot connect to po8 server. Are you sure "+
                    "the URL in config.yml is correct?";
            log.log(Level.SEVERE, "[po8] "+result.trim()); 
            //is this an appropriate use of severe? read about bukkit levels
            getServer().getPluginManager().disablePlugin(this);
        }
        //Check for po8transactions.dat and po8shopreqs.dat
        File po8trans;
        File po8shopreqs;
        File po8lottery;
        File po8shop;
        File slotMachine;
        
        po8trans = new File(dir + "/po8transactions.dat");
        po8shopreqs = new File(dir + "/po8shopreqs.dat");
        po8lottery = new File(dir + "/Lottery.txt");
        po8shop = new File(dir + "/Shop.txt");
        slotMachine = new File(dir + "/Slots.txt");

    	
        if(!po8trans.exists()){
        	try {
        		newdir.mkdirs();
        		po8trans.createNewFile();
        		Map<String, String[]> transactionList = new HashMap<String, String[]>();
        		try {
    				slapi.save(transactionList, dir + "/po8transactions.dat");
    			} catch (Exception e) {
    			}
			} catch (IOException e) {
			}
        }
        if(!po8shopreqs.exists()){
        	try {
        		
        		newdir.mkdirs();
        		po8shopreqs.createNewFile();
        		Map<String, String[]> shopReqs = new HashMap<String, String[]>();
        		shopReqs.put("AutoNum", new String[]{"0"});
        		try {
    				slapi.save(shopReqs, dir + "/po8shopreqs.dat");
    			} catch (Exception e) {
    			}
			} catch (IOException e) {
			}
        }
        if(!po8lottery.exists()){
        	try {
        		
        		newdir.mkdirs();
        		po8lottery.createNewFile();
        		
			} catch (IOException e) {
			}
        }
        if(!slotMachine.exists()){
        	try {
        		
        		newdir.mkdirs();
        		slotMachine.createNewFile();
        		
			} catch (IOException e) {
			}
        }
        if(!po8shop.exists()){
        	try {
        		
        		newdir.mkdirs();
        		po8shop.createNewFile();
        		
			} catch (IOException e) {
			}
        }
        workerPo8collect = new Po8collectWorker(newdir);
        workerLottery = new LotteryWorker(newdir);
        workerPo8Shop = new Po8ShopWorker(newdir);
        workerSlot = new SlotMachineWorker(newdir);
        
        
    }
    
    public void message(CommandSender sender){
    	reader.returnMessage(sender,"&cYou don't have permission for this command!");
    }
    public boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException nfe) {
        	return false;
        }
    }

    public boolean onCommand(CommandSender sender, Command command, String label,
    String[] args){
    String cmd = command.getName();
    //slot commands
	if (cmd.equalsIgnoreCase("po8slot")) {
		workerSlot.po8slot(sender);
		return true;
	}
	
	if (cmd.equalsIgnoreCase("checkcoins")) {
		if(sender.hasPermission("slots.checkcoins") || sender.isOp()){
		workerSlot.checkcoins(sender);
		return true;
		}
		else
		{
			message(sender);
			return false;
		}
	}
	if (cmd.equalsIgnoreCase("play")) {
		if(sender.hasPermission("slots.play") || sender.isOp()){
			if(args.length > 0 && isInteger(args[0])){
					if((Integer.parseInt(args[0]) <= 10)){
					workerSlot.playgame(sender, args[0]);
					return true;
					}
					else
					{
						reader.returnMessage(sender,"&cPlease enter a number between 1 and 10");
						return false;
					}
			}
			else
			{
				reader.returnMessage(sender,"&cPlease enter a valid number");
				return false;
			}
		}
		else
		{
			message(sender);
			return false;
		}
	}
	if (cmd.equalsIgnoreCase("buycoins")) {
		if(sender.hasPermission("slots.buy") || sender.isOp()){
			if(args.length > 0 && isInteger(args[0])){
					workerSlot.buyCoins(sender, args[0]);
					return true;
			}
			else
			{
				reader.returnMessage(sender,"&cPlease enter a valid number");	
				return false;
			}
		}
		else
		{
			message(sender);
			return false;
		}
	}
	if (cmd.equalsIgnoreCase("checkout")) {
		if(sender.hasPermission("slots.checkout") || sender.isOp()){
		workerSlot.checkout(sender);
		return true;
		}
		else
		{
			message(sender);
			return false;
		}
	}
	
    	//Po8shop commands
	if (cmd.equalsIgnoreCase("po8shop")) {
		workerPo8Shop.shopTelehelp(sender);
		return true;
	}
	if (cmd.equalsIgnoreCase("shopteleadd")) {
		if(sender.hasPermission("po8shop.add") || sender.isOp()){
			workerPo8Shop.shopTeleAdd(sender, args[0]);
			return true;
		}
		else
		{
			message(sender);
			return false;
		}
	}
	if (cmd.equalsIgnoreCase("shopteleto")) {
		if(sender.hasPermission("po8shop.teleport") || sender.isOp()){
			workerPo8Shop.shopTeleTo(sender, args[0]);
			return true;
		}
		else
		{
			message(sender);
			return false;
		}
	}
	if (cmd.equalsIgnoreCase("shopteleremove")) {
		if(sender.hasPermission("po8shop.remove") || sender.isOp()){
			workerPo8Shop.shopTeleRemove(sender, args[0]);
			return true;
		}
		else
		{
			message(sender);
			return false;
		}
	}
	//po8Collect Commands
    	if (cmd.equalsIgnoreCase("dicks")) {
    		String judge = "JudgeAnderson is the sexiest Templar";
    		String name = sender.getName();
    		server.getPlayer(name).chat(judge);
    		return true;
    	}
    	
    	if (cmd.equalsIgnoreCase("po8collect")) {
    		workerPo8collect.telehelp(sender);
    		return true;
    	}

    	if (cmd.equalsIgnoreCase("telelist")) {
    		if(sender.hasPermission("po8collect.list") || sender.isOp()){
    		workerPo8collect.teleList(sender);
    		return true;
    		}
    		else
    		{
    			message(sender);
    			return false;
    		}
    	}
    	if (cmd.equalsIgnoreCase("teleto")) {
    		if(sender.hasPermission("po8collect.teleport") || sender.isOp()){
    		if(args.length == 0){
    		workerPo8collect.teleTo(sender,sender.getName().toString());
    		return true;}
    		else
    		workerPo8collect.teleTo(sender,args[0]);
    		return true;
    		}
    		else
    		{
    			message(sender);
    			return false;
    		}
    		
    	}


    		
    	if (cmd.equalsIgnoreCase("teleadd") && args.length > 0){
    		if(sender.hasPermission("po8collect.add") || sender.isOp()){
    		if(args.length == 2 && args[0].equalsIgnoreCase("-o")){
      		workerPo8collect.teleAdd(sender, args[1]);
      		return true;
    		}
    		else
    		{
    			if(args.length == 1 && workerPo8collect.teleExists(args[0]) == false){
    	      		workerPo8collect.teleAdd(sender, args[0]);
    	      		return true;
    	    		}
    	    		else
    	    		{
    	    			reader.returnMessage(sender,"&cThis teleports exists. Use \"-o\" to override");
    	    			return false;
    	    		}
    		}
    		}
       		else
        		{
        			message(sender);
        			return false;
        		}
    	}

    	if (cmd.equalsIgnoreCase("teleremove") && args.length > 0) {
    		if(sender.hasPermission("po8collect.remove") || sender.isOp()){
    			if(workerPo8collect.teleExists(args[0]) == true){
    				workerPo8collect.teleRemove(sender, args[0]);
    				return true;
    			}
    		}
    		else
    		{
    			message(sender);
    			return false;
    		}
    	}
    	
    	
    	
    	
    		
    	//end of PO8collect commands
    	//Lottery commands
    	
    	if (cmd.equalsIgnoreCase("Sublottery")) {
    		if(sender.hasPermission("po8lottery.sub") || sender.isOp()){

    			
	    		if(args.length > 0){
	    			if(!isInteger(args[0]))
	    			{
	    			reader.returnMessage(sender,"&cPlease submit a number");
	    			return false;
	    			}
	    			else
	    			{
	    				if(Integer.parseInt(args[0]) > Integer.parseInt(maxtickets) || Integer.parseInt(args[0]) < 1)
		    			{
		    			reader.returnMessage(sender,"&cMaximum " + maxtickets + " tickets allowed, minimum 1");
		    			return false;
		    			}
	    			}
	    			
		    		workerLottery.sublottery(sender, args[0]);
		    		return true;
	    		}
	    		else{
	    			workerLottery.showtickets(sender);
	    		return true;
	    		}
    		}
    		else
    		{
    			message(sender);
    			return false;
    		}	
    	}
    	
    	if (cmd.equalsIgnoreCase("Ticket")) {
    		if(sender.hasPermission("po8lottery.ticket") || sender.isOp()){

    			
	    		if(args.length > 0){
	    			if(!isInteger(args[0]))
	    			{
	    			reader.returnMessage(sender,"&cPlease submit a number");
	    			return false;
	    			}
	    			else
	    			{
	    				if(Integer.parseInt(args[0]) > Integer.parseInt(maxtickets) || Integer.parseInt(args[0]) < 1)
		    			{
		    			reader.returnMessage(sender,"&cMaximum " + maxtickets + " tickets allowed, minimum 1");
		    			return false;
		    			}
	    				else{
	    		    		workerLottery.ticketlottery(sender, args[0]);
	    		    		return true;
	    				}
	    			} 			
	    		}
	    		else{
	    			workerLottery.showtickets(sender);
	    		return true;
	    		}
    		}
    		else
    		{
    			message(sender);
    			return false;
    		}	
    	}

    	if (cmd.equalsIgnoreCase("Sublist")) {
    		if(sender.hasPermission("po8lottery.list") || sender.isOp()){
    		workerLottery.subList(sender);
    		return true;
    		}
    		else
    		{
    			message(sender);
    			return false;
    		}
    		
    	}
    	
    	if (cmd.equalsIgnoreCase("Lottery")) {
    		if(sender.hasPermission("po8lottery.check") || sender.isOp()){
    			workerLottery.checkPot(sender);
    			workerLottery.showtickets(sender);
    		return true;
    		}
    		else
    		{
    			message(sender);
    			return false;
    		}
    		
    	}
    	
    	if (cmd.equalsIgnoreCase("Checkpot")) {
    		if(sender.hasPermission("po8lottery.check") || sender.isOp()){
    		workerLottery.checkPot(sender);
    		return true;
    		}
    		else
    		{
    			message(sender);
    			return false;
    		}
    		
    	}
    	
    	if (cmd.equalsIgnoreCase("Runlottery")) {
    		String code = "";
    		if(sender.hasPermission("po8lottery.run") || sender.isOp()){
    			if(args.length > 0){
    				code = args[0];
    				workerLottery.runLottery(sender, code);
    				return true;
    			}
    			else
    			{
    				code = "notcorrect";
    				workerLottery.runLottery(sender, code);
    				return true;
    			}
    		}
    		else
    		{
    			message(sender);
    			return false;
    		}
    		
    	}

    	if (cmd.equalsIgnoreCase("Unsublottery")) {
    		if(sender.hasPermission("po8lottery.unsub") || sender.isOp()){
    		workerLottery.unSub(sender);
    		return true;
    		}
    		else
    		{
    			message(sender);
    			return false;
    		}
    		
    	}
	
	if (cmd.equalsIgnoreCase("po8Lottery")) {
		workerLottery.lotteryhelp(sender);
		return true;
	}
	
	// end off Lottery commands

        new HttpCommandThread(this,log,reader,apiKey,apiUrl,sender,command,label, args).start();
        return true;
    }
    
    public void reloadCustomConfig() {
        if (customConfigFile == null) {
        	customConfigFile = new File(getDataFolder(), "config.yml");
        }
        customConfig = YamlConfiguration.loadConfiguration(customConfigFile);
     
        // Look for defaults in the jar
        InputStream defConfigStream = getResource("config.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            customConfig.setDefaults(defConfig);
        }
    }
    
    public FileConfiguration getCustomConfig() {
        if (customConfig == null) {
            reloadCustomConfig();
        }
        return customConfig;
    }
    
    public void saveCustomConfig() {
        if (customConfig == null || customConfigFile == null) {
        return;
        }
        try {
            customConfig.save(customConfigFile);
        } catch (IOException ex) {
            Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE, "Could not save config to " + customConfigFile, ex);
        }
    }
    
    public void saveDefaultConfig() {
    	try {

    		newdir.mkdirs();
    		File f=new File(dir + "/config.yml");
    		InputStream inputStream= getResource("config.yml");
	    	OutputStream out=new FileOutputStream(f);
	    	byte buf[]=new byte[1024];
	    	int len;
	    	while((len=inputStream.read(buf))>0)
	    		out.write(buf,0,len);
	    	out.close();
	    	inputStream.close();
    	}
    	catch (IOException e){
            log.log(Level.SEVERE, "[][]Error Saving Default config.yml "+e); 
    	}
    }    
    
    @Override
    public void onDisable(){
        //should I have a deconstructor of some sort here?
        //what's the point of the po8 constructor?
        log.info("[po8] Goodbye, cruel server!");
    }

}