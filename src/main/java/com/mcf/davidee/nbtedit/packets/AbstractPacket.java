package com.mcf.davidee.nbtedit.packets;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;


/**
 * Packet stuff. This stuff is repetitive, but generally makes sense.
 * @author sirgingalot, DavidGoldman
 */
public abstract class AbstractPacket {

    /**
     * Encode the packet data into the ByteBuf stream. Complex data sets may need specific data handlers (See @link{cpw.mods.fml.common.network.ByteBuffUtils})
     *
     * @param ctx    channel context
     * @param buffer the buffer to encode into
     */
    public abstract void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer) throws IOException;

    /**
     * Decode the packet data from the ByteBuf stream. Complex data sets may need specific data handlers (See @link{cpw.mods.fml.common.network.ByteBuffUtils})
     *
     * @param ctx    channel context
     * @param buffer the buffer to decode from
     */
    public abstract void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer) throws IOException;

    /**
     * Handle a packet on the client side. Note this occurs after decoding has completed.
     *
     * @param player the player reference
     */
    public abstract void handleClientSide(EntityPlayer player);

    /**
     * Handle a packet on the server side. Note this occurs after decoding has completed.
     *
     * @param player the player reference
     */
    public abstract void handleServerSide(EntityPlayerMP player);
    
    public void sendMessageToPlayer(EntityPlayer player, String msg) {
    	player.addChatMessage(new ChatComponentText(msg));
    }
}