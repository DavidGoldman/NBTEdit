package com.mcf.davidee.nbtedit.forge;

import java.io.File;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;

import org.lwjgl.opengl.GL11;

import com.mcf.davidee.nbtedit.NBTEdit;
import com.mcf.davidee.nbtedit.gui.GuiEditNBTTree;
import com.mcf.davidee.nbtedit.nbt.SaveStates;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class ClientProxy extends CommonProxy {

	@Override
	public void registerInformation(){
		MinecraftForge.EVENT_BUS.register(this);
		SaveStates save = NBTEdit.getSaveStates();
		save.load();
		save.save();
	}

	@Override
	public File getMinecraftDirectory(){
		return FMLClientHandler.instance().getClient().mcDataDir;
	}

	@Override
	public void openEditGUI(int entityID, NBTTagCompound tag) {
		Minecraft.getMinecraft().displayGuiScreen(new GuiEditNBTTree(entityID, tag));
	}
	
	@Override
	public void openEditGUI(int x, int y, int z, NBTTagCompound tag) {
		Minecraft.getMinecraft().displayGuiScreen(new GuiEditNBTTree(x, y, z, tag));
	}

	@SubscribeEvent
	public void renderWorldLast(RenderWorldLastEvent event){
		GuiScreen curScreen = Minecraft.getMinecraft().currentScreen;
		if (curScreen instanceof GuiEditNBTTree){
			GuiEditNBTTree screen = (GuiEditNBTTree)curScreen;
			Entity e = screen.getEntity();
			
			if (e != null && e.isEntityAlive())
				drawBoundingBox(event.context, event.partialTicks,e.boundingBox);
			else if (screen.isTileEntity()){
				int x = screen.getBlockX();
				int y = screen.y;
				int z = screen.z;
				World world = Minecraft.getMinecraft().theWorld;
				Block b = world.getBlock(x, y, z);
				if (b != null) {
					b.setBlockBoundsBasedOnState(world, x,y,z);
					drawBoundingBox(event.context,event.partialTicks, b.getSelectedBoundingBoxFromPool(world,x, y,z));
				}
			}
		}
	}

	private void drawBoundingBox(RenderGlobal r, float f, AxisAlignedBB aabb) {
		if (aabb == null)
			return;

		EntityLivingBase player = Minecraft.getMinecraft().renderViewEntity;

		double var8 = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double)f;
		double var10 = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double)f;
		double var12 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double)f;

		aabb = aabb.getOffsetBoundingBox(-var8, -var10, -var12);

		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glColor4f(1.0F, 0.0F, 0.0F, .5F);
		GL11.glLineWidth(3.5F);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDepthMask(false);

		Tessellator var2 = Tessellator.instance;

		var2.startDrawing(3);
		var2.addVertex(aabb.minX, aabb.minY, aabb.minZ);
		var2.addVertex(aabb.maxX, aabb.minY, aabb.minZ);
		var2.addVertex(aabb.maxX, aabb.minY, aabb.maxZ);
		var2.addVertex(aabb.minX, aabb.minY, aabb.maxZ);
		var2.addVertex(aabb.minX, aabb.minY, aabb.minZ);
		var2.draw();
		var2.startDrawing(3);
		var2.addVertex(aabb.minX, aabb.maxY, aabb.minZ);
		var2.addVertex(aabb.maxX, aabb.maxY, aabb.minZ);
		var2.addVertex(aabb.maxX, aabb.maxY, aabb.maxZ);
		var2.addVertex(aabb.minX, aabb.maxY, aabb.maxZ);
		var2.addVertex(aabb.minX, aabb.maxY, aabb.minZ);
		var2.draw();
		var2.startDrawing(1);
		var2.addVertex(aabb.minX, aabb.minY, aabb.minZ);
		var2.addVertex(aabb.minX, aabb.maxY, aabb.minZ);
		var2.addVertex(aabb.maxX, aabb.minY, aabb.minZ);
		var2.addVertex(aabb.maxX, aabb.maxY, aabb.minZ);
		var2.addVertex(aabb.maxX, aabb.minY, aabb.maxZ);
		var2.addVertex(aabb.maxX, aabb.maxY, aabb.maxZ);
		var2.addVertex(aabb.minX, aabb.minY, aabb.maxZ);
		var2.addVertex(aabb.minX, aabb.maxY, aabb.maxZ);
		var2.draw();

		GL11.glDepthMask(true);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_BLEND);

	}
}
