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
 * Handle tele.txt file
 * @author Kwinno
 */
public class Po8collectWorker {
	
	private Server server = Bukkit.getServer();
	private static JavaHttpUrlConnectionReader reader = new JavaHttpUrlConnectionReader();
	private File fTele;
	private File fTmp;
	public Po8collectWorker(File folder) {
		fTele = new File(folder, "tele.txt");
		fTmp = new File(folder, "tele.tmp");
	}
	
	// lists all available teleports
	public void teleList(CommandSender sender) {
		if (!fTele.exists()) {
			reader.returnMessage(sender, "&c" + "No playerchests available");
			return;
		}
		try {
			Scanner scanner = new Scanner(new FileReader(fTele));
			String buffer = "&c" + "Available playerchests: " + "&f";
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
			System.out.println("Cannot create file " + fTele.getName() + " - " + e.getMessage());
			reader.returnMessage(sender, "&c" + "Listing playerchests failed!");
		}
	}  // teleList

	// warps to a given stored location
	public void teleTo(CommandSender sender, String teleloc) {
		
		// whom to teleport
		Player player;
		String teleport = "";
		// has target?
		if (teleloc != null /*&& sender.isOp()*/) {
			player = (Player)sender;
			teleport = teleloc;
		}
		else{
			player = (Player)sender;
			teleport = player.getName();
		}
		
		// otherwise has to be a player
		if (!(sender instanceof Player)) {
			reader.returnMessage(sender, "&c" + "You have to be a player!");
			return;
		} else {
			player = (Player)sender;
		}
		
		if (!fTele.exists()) {
			reader.returnMessage(sender, "&c" + "No playerchests available");
			return;
		}
		
		try {
			boolean found = false;
			Scanner scanner = new Scanner(new FileReader(fTele));
			while (scanner.hasNextLine()) {
				String[] cur = scanner.nextLine().split(":");
				if (cur.length >= 6 && cur[0].equalsIgnoreCase(teleport)) {
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
					reader.returnMessage(sender,"&c" + "Teleported to playerchest: " + "&f" + teleport);
					found = true;
					break;
				}
			} // while hasNextLine
			scanner.close();
			if (!found) reader.returnMessage(sender,"&c" + "No such playerchest: " + "&f" + teleport);
		} catch (Exception e) {
			System.out.println("Cannot parse file " + fTele.getName() + " - " + e.getMessage());
			reader.returnMessage(sender, "&c" + "Teleport failed!");
		}
	} // warpTo
	
	// adds a new location to the list of warps
	public void teleAdd(CommandSender sender, String warp) {
		
		
		// has to be a player
		if (!(sender instanceof Player)) {
			reader.returnMessage(sender, "&c" + "You have to be a player!");
			return;
		}
		
		//see if the name represents a player
		OfflinePlayer[] players = server.getOfflinePlayers();
		int total = players.length;
		boolean found = false;
		for(int i = 0; i < total; i++) {
			if(players[i].getName().equalsIgnoreCase(warp)){
				found = true;
			}
		}
		//did it find a player?
		if(found != true){
			reader.returnMessage(sender, "&c" + "Teleport name must be a player's name!");
			return;
		}
		
					
		// create new file if not exists
		if (!fTele.exists()) {
			try {
				fTele.createNewFile();
			} catch (Exception e) {
				System.out.println("Cannot create file " + fTele.getName() + " - " + e.getMessage());
				reader.returnMessage(sender, "&c" + "Setting teleport failed!");
				return;
			}
		}
		// first remove the old warp position
		if (!teleRemoveInternal(warp)) {
			reader.returnMessage(sender, "&c" + "Setting teleport failed!");
			return;
		}
		// get player's location and create a new warp
		Player player = (Player)sender;
		Location loc = player.getLocation();
		try {
			FileWriter wrt = new FileWriter(fTele, true);
			wrt.write(	warp + ":" + 
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
			reader.returnMessage(sender, "&c" + "Setting teleport failed!");
			return;
		}
		reader.returnMessage(sender,"&c" + "Teleport to playerchest: " + "&f" + warp + "&c" + " set");
	} // warpAdd
	
	// removes give warp from the list of warps
	public void teleRemove(CommandSender sender, String warp) {
		// for the OP version
		//if (!sender.isOp()) {
		//	reader.returnMessage(sender, "&c" + "You have to be OP!");
		//	return;
		//}
		if (!fTele.exists()) {
			reader.returnMessage(sender, "&c" + "No playerchests available");
			return;
		}
		if (teleRemoveInternal(warp))
			reader.returnMessage(sender, "&c" + "teleport to playerchest: " + "&f" + warp + "&c" + " removed");
		else 
			reader.returnMessage(sender, "&c" + "Removing playerchest: " + "&f" + " failed");
	} // warpRemove
	
	public void telehelp(CommandSender sender) {
		reader.returnMessage(sender, "&e" + "---" + "&2" + " Po8Collect Help " + "&e"  +" ---");
		reader.returnMessage(sender, "&2" + "/tclist or /listtc " + "&e" + " Lists all defined warp location names.");
		reader.returnMessage(sender, "&2" + "/tc or /tc " + "&c" + "<name> " + "&e" + " Puts you on the player's chest. yours if blank");
		reader.returnMessage(sender, "&2" + "/tr " + "&c" + "<name> " + "&e" + " Removes a teleport location");
		reader.returnMessage(sender, "&2" + "/ts " + "&c" + "<name> " + "&e" + " Sets a teleport location");
		reader.returnMessage(sender, "&2" + "/po8collect " + "&e" + "displays this menu");
	}  // warpList
	
	// internal function for removing a warp from the text file (used in more places)
	// returns OK/error, not if warp found!
    private boolean teleRemoveInternal(String name) {
    	try {
    		// this actually goes through the file twice (worst case scenario)
    		// should optimize, but I wait for Persistence
    		boolean found = false;
	    	Scanner scanner = new Scanner(new FileReader(fTele));
			while (scanner.hasNextLine()) {
				String[] tele = scanner.nextLine().split(":");
				if (tele.length >= 1 && tele[0].equalsIgnoreCase(name)) {
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
			PrintWriter wrt = new PrintWriter(new FileWriter(fTmp));
			BufferedReader rdr = new BufferedReader(new FileReader(fTele));
			String line;
			while ((line = rdr.readLine()) != null) {
				String[] tele = line.split(":");
				if (tele.length >= 1 && tele[0].equalsIgnoreCase(name)) {
					// nothing (i know this empty line is strange, but it's here on purpose)
				} else {
					wrt.println(line);
				}
			}
			wrt.close();
			rdr.close();
			if (!fTele.delete()) {
				System.out.println("Cannot delete " + fTele.getName());
				return false;
			}
			if (!fTmp.renameTo(fTele)) {
				System.out.println("Cannot rename " + fTmp.getName() + " to " + fTele.getName());
				return false;
			}
    	} catch (Exception e) {
    		System.out.println("Unexpected error " + e.getMessage());
    		return false;
    	}
    	return true;
    } // teleRemoveInternal
    
    public boolean teleExists(String name) {
    	try {
    		// this actually goes through the file twice (worst case scenario)
    		// should optimize, but I wait for Persistence
    		boolean found = false;
	    	Scanner scanner = new Scanner(new FileReader(fTele));
			while (scanner.hasNextLine()) {
				String[] tele = scanner.nextLine().split(":");
				if (tele.length >= 1 && tele[0].equalsIgnoreCase(name)) {
					found = true;
					break;
				}
			}
			scanner.close();
			// only copy file data if warp found!
			if (found) {
				return true;
			}
			
    	} catch (Exception e) {
    		System.out.println("Unexpected error " + e.getMessage());
    		return false;
    	}
    	return false;
    } // teleRemoveInternal
    
}
