package com.mcf.davidee.nbtedit.packets;

import static com.mcf.davidee.nbtedit.NBTEdit.SECTION_SIGN;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.logging.Level;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import com.mcf.davidee.nbtedit.NBTEdit;
import com.mcf.davidee.nbtedit.NBTHelper;

public class TileNBTPacket extends AbstractPacket {
	
	protected int x, y, z;
	protected NBTTagCompound tag;
	
	public TileNBTPacket() {
		
	}
	
	public TileNBTPacket(int x, int y, int z, NBTTagCompound tag) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.tag = tag;
	}

	@Override
	public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer) throws IOException {
		ByteBufOutputStream bos = new ByteBufOutputStream(buffer);
		bos.writeInt(x);
		bos.writeInt(y);
		bos.writeInt(z);
		NBTHelper.nbtWrite(tag, bos);
	}

	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer) throws IOException {
		ByteBufInputStream bis = new ByteBufInputStream(buffer);
		DataInputStream nbt = new DataInputStream(bis);
		x = bis.readInt();
		y = bis.readInt();
		z = bis.readInt();
		tag = NBTHelper.nbtRead(nbt);
	}

	@Override
	public void handleClientSide(EntityPlayer player) {
		NBTEdit.proxy.openEditGUI(x, y, z, tag);
	}

	@Override
	public void handleServerSide(EntityPlayerMP player) {
		TileEntity te = player.worldObj.getTileEntity(x, y, z);
		if (te != null) {
			try {
				te.readFromNBT(tag);
				NBTEdit.DISPATCHER.sendToDimension(new TileNBTUpdatePacket(x, y, z, tag), player.dimension); //Broadcast changes
				NBTEdit.log(Level.FINE, player.getCommandSenderName() + " edited a tag -- Tile Entity at ("+x+","+y+","+z+")");
				NBTEdit.logTag(tag);
				sendMessageToPlayer(player, "Your changes have been saved");
			}
			catch(Throwable t) {
				sendMessageToPlayer(player, SECTION_SIGN + "cSave Failed - Invalid NBT format for Tile Entity");
				NBTEdit.log(Level.WARNING, player.getCommandSenderName() + " edited a tag and caused an exception");
				NBTEdit.logTag(tag);
				NBTEdit.throwing("TileNBTPacket", "handleServerSide", t);
			}
		}
		else {
			NBTEdit.log(Level.WARNING, player.getCommandSenderName() + " tried to edit a non-existant TileEntity at ("+x+","+y+","+z+")");
			sendMessageToPlayer(player, SECTION_SIGN + "cSave Failed - There is no TileEntity at ("+x+","+y+","+z+")");
		}
	}

}
