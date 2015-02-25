package com.mcf.davidee.nbtedit.packets;

import static com.mcf.davidee.nbtedit.NBTEdit.SECTION_SIGN;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.logging.Level;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S06PacketUpdateHealth;
import net.minecraft.network.play.server.S1FPacketSetExperience;
import net.minecraft.world.WorldSettings.GameType;

import com.mcf.davidee.nbtedit.NBTEdit;
import com.mcf.davidee.nbtedit.NBTHelper;

public class EntityNBTPacket extends AbstractPacket {
	
	protected int entityID;
	protected NBTTagCompound tag;
	
	public EntityNBTPacket() {
		
	}
	
	public EntityNBTPacket(int entityID, NBTTagCompound tag) {
		this.entityID = entityID;
		this.tag = tag;
	}

	@Override
	public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer) throws IOException {
		ByteBufOutputStream bos = new ByteBufOutputStream(buffer);
		bos.writeInt(entityID);
		NBTHelper.nbtWrite(tag, bos);
	}

	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer) throws IOException {
		ByteBufInputStream bis = new ByteBufInputStream(buffer);
		DataInputStream nbt = new DataInputStream(bis);
		entityID = bis.readInt();
		tag = NBTHelper.nbtRead(nbt);
	}

	@Override
	public void handleClientSide(EntityPlayer player) {
		NBTEdit.proxy.openEditGUI(entityID, tag);
	}

	// Fairly hacky. Consider swapping to an event driven system, where classes can register to
	// receive entity edit events and provide feedback/send packets as necessary. 
	@Override
	public void handleServerSide(EntityPlayerMP player) {
		Entity e = player.worldObj.getEntityByID(entityID);
		if (e != null) {
			try {
				GameType preGameType = player.theItemInWorldManager.getGameType();
				e.readFromNBT(tag);
				NBTEdit.log(Level.FINE, player.getCommandSenderName() + " edited a tag -- Entity ID #" + entityID);
				NBTEdit.logTag(tag);
				if (e == player) { //Update player info
					player.sendContainerToPlayer(player.inventoryContainer);
					GameType type = player.theItemInWorldManager.getGameType();
					if (preGameType != type)
						player.setGameType(type);
					player.playerNetServerHandler.sendPacket(new S06PacketUpdateHealth(player.getHealth(), player.getFoodStats().getFoodLevel(), player.getFoodStats().getSaturationLevel()));
					player.playerNetServerHandler.sendPacket(new S1FPacketSetExperience(player.experience, player.experienceTotal, player.experienceLevel));
					player.sendPlayerAbilities();
				}
				sendMessageToPlayer(player, "Your changes have been saved");
			} 
			catch(Throwable t) {
				sendMessageToPlayer(player, SECTION_SIGN + "cSave Failed - Invalid NBT format for Entity");
				NBTEdit.log(Level.WARNING, player.getCommandSenderName() + " edited a tag and caused an exception");
				NBTEdit.logTag(tag);
				NBTEdit.throwing("EntityNBTPacket", "handleServerSide", t);
			}
		}
	}

}
