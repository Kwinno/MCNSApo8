package com.mcnsa.po8;

import java.util.logging.Logger;

import org.bukkit.command.CommandSender;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.ItemType;

public class itemSearch {
    private Logger log;

	
    public void searchItem(String itemString, CommandSender sender) throws WorldEditException {
		HttpCommandThread Cmd = new HttpCommandThread(null, log, null, itemString, itemString, sender, null, itemString, null);
		String query = itemString;
        boolean blocksOnly = false;
        boolean itemsOnly = false;


        try {
            int id = Integer.parseInt(query);

            ItemType type = ItemType.fromID(id);
            if (type != null) {
            	Cmd.returnMessage(sender, "&a#&c"+type.getID()+" &a("+type.getName() + ")");
            } else {
            	Cmd.returnMessage(sender, "&cNo item found by ID "+id);
            }

            return;
        } catch (NumberFormatException e) {
        }

        if (query.length() <= 2) {
        	Cmd.returnMessage(sender, "&cEnter a longer search string (len > 2).");
            return;
        }

        if (!blocksOnly && !itemsOnly) {
        	Cmd.returnMessage(sender, "&6Searching for: " + query);
        } else if (blocksOnly && itemsOnly) {
        	Cmd.returnMessage(sender, "&cYou cannot use both the 'b' and 'i' flags simultaneously.");
            return;
        } else if (blocksOnly) {
        	Cmd.returnMessage(sender, "&6Searching for blocks: " + query);
        } else {
        	Cmd.returnMessage(sender, "&6Searching for items: " + query);
        }

        int found = 0;

        for (ItemType type : ItemType.values()) {
            if (found >= 15) {
            	Cmd.returnMessage(sender, "&cToo many results");
                break;
            }

            if (blocksOnly && type.getID() > 255) {
                continue;
            }

            if (itemsOnly && type.getID() <= 255) {
                continue;
            }

            for (String alias : type.getAliases()) {
                if (alias.contains(query)) {
                	Cmd.returnMessage(sender, "&a#&c"+type.getID()+" &a("+type.getName() + ")");
                    ++found;
                    break;
                }
            }
        }

        if (found == 0) {
        	Cmd.returnMessage(sender, "&cNo items found.");
        }
    }public String[] returnItem(String[] itemString) throws WorldEditException {
		String query = itemString[0];
        boolean blocksOnly = false;
        boolean itemsOnly = false;
		if (itemString[1].equalsIgnoreCase("i")){
			itemsOnly = true;
		}
		else if (itemString[1].equalsIgnoreCase("b")){
			blocksOnly = true;
		}

        try {
            int id = Integer.parseInt(query);

            ItemType type = ItemType.fromID(id);
            if (type != null) {
            	return new String[]{""+type.getID(),""+type.getName()};
            } else {
            	return new String[]{"Error","Invalid Item Name"};
            }

        } catch (NumberFormatException e) {
        }

        if (query.length() <= 2) {
        	return new String[]{"Error","Item String too short"};
        }

        if (!blocksOnly && !itemsOnly) {
        } else if (blocksOnly && itemsOnly) {
        	return new String[]{"Error","Cannot use both b and i flags"};
        } else if (blocksOnly) {
        } else {
        }

        int found = 0;

        for (ItemType type : ItemType.values()) {
            if (found >= 15) {
            	return new String[]{"Error","Too many items for that item string"};
            }

            if (blocksOnly && type.getID() > 255) {
                continue;
            }

            if (itemsOnly && type.getID() <= 255) {
                continue;
            }

            for (String alias : type.getAliases()) {
                if (alias.contains(query)) {
                	return new String[]{""+type.getID(),""+type.getName()};
                }
            }
        }

        if (found == 0) {
        	return new String[]{"Error","No item found"};
        }
     return new String[]{"Error","No item found"};
    }
}
