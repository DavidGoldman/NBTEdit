package com.mcf.davidee.nbtedit.packets;

import com.mcf.davidee.nbtedit.NBTEdit;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class TileNBTUpdatePacket extends TileNBTPacket {

	public TileNBTUpdatePacket() {
		super();
	}

	public TileNBTUpdatePacket(int x, int y, int z, NBTTagCompound tag) {
		super(x, y, z, tag);
	}

	@Override
	public void handleClientSide(EntityPlayer player) {
		//TODO Work on this
		TileEntity te = player.worldObj.getTileEntity(x, y, z);
		if (te != null) {
			NBTTagCompound backup = new NBTTagCompound();
			te.writeToNBT(backup);

			try {
				te.readFromNBT(tag);
			}
			catch(Throwable t) {
				te.readFromNBT(backup);
				NBTEdit.throwing(te.toString(), "readFromNBT", t);
			}
		}
	}

	@Override
	public void handleServerSide(EntityPlayerMP player) {

	}
}
