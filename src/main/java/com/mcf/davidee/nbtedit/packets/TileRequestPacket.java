package com.mcf.davidee.nbtedit.packets;

import static com.mcf.davidee.nbtedit.NBTEdit.SECTION_SIGN;

import com.mcf.davidee.nbtedit.NBTEdit;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class TileRequestPacket extends AbstractPacket {
	
	private int x, y, z;
	
	public TileRequestPacket() {
		
	}
	
	public TileRequestPacket(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
		buffer.writeInt(x);
		buffer.writeInt(y);
		buffer.writeInt(z);
	}

	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
		x = buffer.readInt();
		y = buffer.readInt();
		z = buffer.readInt();
	}

	@Override
	public void handleClientSide(EntityPlayer player) { 

	}

	@Override
	public void handleServerSide(EntityPlayerMP player) {
		TileEntity te = player.worldObj.getTileEntity(x, y, z);
		if (te != null) {
			NBTTagCompound tag = new NBTTagCompound();
			te.writeToNBT(tag);
			NBTEdit.DISPATCHER.sendTo(new TileNBTPacket(x, y, z, tag), player);
		}
		else
			sendMessageToPlayer(player, SECTION_SIGN + "cError - There is no TileEntity at ("+x+","+y+","+z+")");
	}

}
