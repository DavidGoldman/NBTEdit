package com.mcf.davidee.nbtedit;

import java.util.logging.Level;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import com.mcf.davidee.nbtedit.packets.EntityRequestPacket;
import com.mcf.davidee.nbtedit.packets.MouseOverPacket;
import com.mcf.davidee.nbtedit.packets.TileRequestPacket;


public class CommandNBTEdit extends CommandBase{

	@Override
	public String getCommandName() {
		return "nbtedit";
	}
	@Override
	public String getCommandUsage(ICommandSender par1ICommandSender)
	{
		return "/nbtedit OR /nbtedit <EntityId> OR /nbtedit <TileX> <TileY> <TileZ>";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] var2) {
		if (sender instanceof EntityPlayerMP) {
			EntityPlayerMP player = (EntityPlayerMP)sender;

			if (var2.length == 3) {
				int x = parseInt(sender,var2[0]);
				int y = parseInt(sender,var2[1]);
				int z = parseInt(sender,var2[2]);
				NBTEdit.log(Level.FINE, sender.getCommandSenderName() + " issued command \"/nbtedit " + x + " " + y + " " + z + "\"");
				new TileRequestPacket(x, y, z).handleServerSide(player);
			}
			else if (var2.length == 1) {
				int entityID = (var2[0].equalsIgnoreCase("me")) ? player.getEntityId() : parseIntWithMin(sender, var2[0], 0);
				NBTEdit.log(Level.FINE, sender.getCommandSenderName() + " issued command \"/nbtedit " + entityID +  "\"");
				new EntityRequestPacket(entityID).handleServerSide(player);
			}
			else if (var2.length == 0) {
				NBTEdit.log(Level.FINE, sender.getCommandSenderName() + " issued command \"/nbtedit\"");
				NBTEdit.DISPATCHER.sendTo(new MouseOverPacket(), player);
			}
			else  {
				String s = "";
				for (int i =0; i < var2.length; ++i) {
					s += var2[i];
					if (i != var2.length - 1)
						s += " ";
				}
				NBTEdit.log(Level.FINE, sender.getCommandSenderName() + " issued invalid command \"/nbtedit " + s + "\"");
				throw new WrongUsageException("Pass 0, 1, or 3 integers -- ex. /nbtedit", new Object[0]);
			}
		}
	}

	public boolean canCommandSenderUseCommand(ICommandSender s) {
		return s instanceof EntityPlayer && (super.canCommandSenderUseCommand(s) || !NBTEdit.opOnly && ((EntityPlayer)s).capabilities.isCreativeMode);
	}

}
