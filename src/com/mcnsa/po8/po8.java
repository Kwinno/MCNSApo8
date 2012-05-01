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
    private FileConfiguration customConfig = null;
    private File customConfigFile = null;
    private Po8collectWorker workerPo8collect;
    private LotteryWorker workerLottery;
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
        po8trans = new File(dir + "/po8transactions.dat");
        po8shopreqs = new File(dir + "/po8shopreqs.dat");
        
        
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
        workerPo8collect = new Po8collectWorker(newdir);
        workerLottery = new LotteryWorker(newdir);
        
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
    	//Po8Collect commands
    	if (cmd.equalsIgnoreCase("dicks")) {
    		String judge = "JudgeAnderson is the sexiest Templar!";
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
    	    			sender.sendMessage("&cThis teleports exists. Use \"-o\" to override");
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
	    				if(Integer.parseInt(args[0]) > 4)
		    			{
		    			reader.returnMessage(sender,"&cMaximum 3 tickets allowed");
		    			return false;
		    			}
	    			}
	    			
		    		workerLottery.sublottery(sender, args[0]);
		    		return true;
	    		}
	    		else{
	    		workerLottery.sublottery(sender, "1");
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
    		if(sender.hasPermission("po8lottery.run") || sender.isOp()){
    		workerLottery.runLottery(sender);
    		return true;
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