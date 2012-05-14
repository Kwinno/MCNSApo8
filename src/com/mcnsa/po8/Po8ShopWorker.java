package com.mcnsa.po8;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Scanner;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Handle shop.txt file
 * @author Kwinno
 */
public class Po8ShopWorker {
	
	private Server server = Bukkit.getServer();
	private static JavaHttpUrlConnectionReader reader = new JavaHttpUrlConnectionReader();
	private File fShop;
	private File fsTmp;
	public Po8ShopWorker(File folder) {
		fShop = new File(folder, "shop.txt");
		fsTmp = new File(folder, "shop.tmp");
	}
	
	// lists all available teleports
	
	// warps to a given stored location
	public void shopTeleTo(CommandSender sender, String itemname) {
		
		// whom to teleport
		Player player;
		String item = "";
		// has target?
		if (itemname != null /*&& sender.isOp()*/) {
			player = (Player)sender;
			item = itemname;
		}
		else{
			reader.returnMessage(sender, "&c" + "Please submit a item you want to teleport to");
			return;
		}
		
		// otherwise has to be a player
		if (!(sender instanceof Player)) {
			reader.returnMessage(sender, "&c" + "You have to be a player!");
			return;
		} else {
			player = (Player)sender;
		}
		
		if (!fShop.exists()) {
			reader.returnMessage(sender, "&c" + "No shop teleports available");
			return;
		}
		
		try {
			boolean found = false;
			Scanner scanner = new Scanner(new FileReader(fShop));
			while (scanner.hasNextLine()) {
				String[] cur = scanner.nextLine().split(":");
				if (cur.length >= 6 && cur[0].equalsIgnoreCase(item)) {
					double x = Double.parseDouble(cur[1]);
					double y = Double.parseDouble(cur[2]);
					double z = Double.parseDouble(cur[3]);
					float yaw = Float.parseFloat(cur[4]);
					float pitch = Float.parseFloat(cur[5]);
					World world;
					// backwards compatibility, use default world if not set
					if (cur.length == 7) world = server.getWorld(cur[6]);
					else world = server.getWorlds().get(0);
					// create the location and warp
					Location loc = new Location(world, x, y, z, yaw, pitch);
					player.teleport(loc);
					reader.returnMessage(sender,"&c" + "Teleported to shopchest containing: " + "&f" + item);
					found = true;
					break;
				}
			} // while hasNextLine
			scanner.close();
			if (!found) reader.returnMessage(sender,"&c" + "No shopchest containing: " + "&f" + item);
		} catch (Exception e) {
			System.out.println("Cannot parse file " + fShop.getName() + " - " + e.getMessage());
			reader.returnMessage(sender, "&c" + "Teleport failed!");
		}
	} // warpTo
	
	// adds a new location to the list of warps
	public void shopTeleAdd(CommandSender sender, String itemname) {
		
		
		// has to be a player
		if (!(sender instanceof Player)) {
			reader.returnMessage(sender, "&c" + "You have to be a player!");
			return;
		}
					
		// create new file if not exists
		if (!fShop.exists()) {
			try {
				fShop.createNewFile();
			} catch (Exception e) {
				System.out.println("Cannot create file " + fShop.getName() + " - " + e.getMessage());
				reader.returnMessage(sender, "&c" + "Setting teleport to: " + itemname + " failed!");
				return;
			}
		}
		// first remove the old warp position
		if (!shopTeleRemoveInternal(itemname)) {
			reader.returnMessage(sender, "&c" + "Setting teleport to: " + itemname + " failed!");
			return;
		}
		// get player's location and create a new warp
		Player player = (Player)sender;
		Location loc = player.getLocation();
		try {
			FileWriter wrt = new FileWriter(fShop, true);
			wrt.write(	itemname + ":" + 
						loc.getX() + ":" +
						loc.getY() + ":" +
						loc.getZ() + ":" +
						loc.getYaw() + ":" +
						loc.getPitch() + ":" +
						player.getWorld().getName() + "\n"
					 );
			wrt.close();
		} catch (Exception e) {
			System.out.println("Unexpected error " + e.getMessage());
			reader.returnMessage(sender, "&c" + "Setting teleport to: " + itemname + " failed!");
			return;
		}
		reader.returnMessage(sender,"&c" + "Teleport to shopchest containing: " + "&f" + itemname + "&c" + " set");
	} // warpAdd
	
	// removes give warp from the list of warps
	public void shopTeleRemove(CommandSender sender, String itemname) {
		// for the OP version
		//if (!sender.isOp()) {
		//	reader.returnMessage(sender, "&c" + "You have to be OP!");
		//	return;
		//}
		if (!fShop.exists()) {
			reader.returnMessage(sender, "&c" + "No shopchests available");
			return;
		}
		if (shopTeleRemoveInternal(itemname))
			reader.returnMessage(sender, "&c" + "teleport to shopchest containing: " + "&f" + itemname + "&c" + " removed");
		else 
			reader.returnMessage(sender, "&c" + "Removing shopchest containing: " + "&f" + itemname + "&f" + " failed");
	} // warpRemove
	
	public void shopTelehelp(CommandSender sender) {
		reader.returnMessage(sender, "&e" + "---" + "&2" + " Po8Shop Help " + "&e"  +" ---");
		reader.returnMessage(sender, "&2" + "/scc " + "&c" + "<itemname> " + "&e" + " Puts you on the shopchest containing: <itemname>");
		reader.returnMessage(sender, "&2" + "/scr " + "&c" + "<itemname> " + "&e" + " Removes a shopchest teleport");
		reader.returnMessage(sender, "&2" + "/scs " + "&c" + "<itemname> " + "&e" + " Sets a shopchest teleport");
		reader.returnMessage(sender, "&2" + "/po8shop " + "&e" + "displays this menu");
	}  // shopHelp
	
	// internal function for removing a warp from the text file (used in more places)
	// returns OK/error, not if warp found!
    private boolean shopTeleRemoveInternal(String itemname) {
    	try {
    		// this actually goes through the file twice (worst case scenario)
    		// should optimize, but I wait for Persistence
    		boolean found = false;
	    	Scanner scanner = new Scanner(new FileReader(fShop));
			while (scanner.hasNextLine()) {
				String[] tele = scanner.nextLine().split(":");
				if (tele.length >= 1 && tele[0].equalsIgnoreCase(itemname)) {
					found = true;
					break;
				}
			}
			scanner.close();
			// only copy file data if warp found!
			if (!found) {
				return true;
			}
			
			// copy everything except the old warp
			PrintWriter wrt = new PrintWriter(new FileWriter(fsTmp));
			BufferedReader rdr = new BufferedReader(new FileReader(fShop));
			String line;
			while ((line = rdr.readLine()) != null) {
				String[] shopTele = line.split(":");
				if (shopTele.length >= 1 && shopTele[0].equalsIgnoreCase(itemname)) {
					// nothing (i know this empty line is strange, but it's here on purpose)
				} else {
					wrt.println(line);
				}
			}
			wrt.close();
			rdr.close();
			if (!fShop.delete()) {
				System.out.println("Cannot delete " + fShop.getName());
				return false;
			}
			if (!fsTmp.renameTo(fShop)) {
				System.out.println("Cannot rename " + fsTmp.getName() + " to " + fShop.getName());
				return false;
			}
    	} catch (Exception e) {
    		System.out.println("Unexpected error " + e.getMessage());
    		return false;
    	}
    	return true;
    } // teleRemoveInternal
    
}
