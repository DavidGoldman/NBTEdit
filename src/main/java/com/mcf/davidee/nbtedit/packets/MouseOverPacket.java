package com.mcf.davidee.nbtedit.packets;

import static com.mcf.davidee.nbtedit.NBTEdit.SECTION_SIGN;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;

import com.mcf.davidee.nbtedit.NBTEdit;

public class MouseOverPacket extends AbstractPacket {
	
	@Override
	public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer) throws IOException {

	}

	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer) throws IOException {

	}

	@Override
	public void handleClientSide(EntityPlayer player) {
		MovingObjectPosition pos = Minecraft.getMinecraft().objectMouseOver;
		AbstractPacket packet = null;
		if (pos != null)
			if (pos.entityHit != null)
				packet = new EntityRequestPacket(pos.entityHit.getEntityId());
			else if (pos.typeOfHit == MovingObjectType.BLOCK)
				packet = new TileRequestPacket(pos.blockX, pos.blockY, pos.blockZ);
		if (packet == null) 
			sendMessageToPlayer(player, SECTION_SIGN + "cError - No tile or entity selected");
		else
			NBTEdit.DISPATCHER.sendToServer(packet);
	}

	@Override
	public void handleServerSide(EntityPlayerMP player) {
		
	}

}
