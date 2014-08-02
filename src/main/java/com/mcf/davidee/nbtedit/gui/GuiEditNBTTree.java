package com.mcf.davidee.nbtedit.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.mcf.davidee.nbtedit.NBTEdit;
import com.mcf.davidee.nbtedit.nbt.NBTTree;
import com.mcf.davidee.nbtedit.packets.EntityNBTPacket;
import com.mcf.davidee.nbtedit.packets.TileNBTPacket;

public class GuiEditNBTTree extends GuiScreen{

	public final int entityOrX, y, z;
	private boolean entity;
	protected String screenTitle;
	private GuiNBTTree guiTree;

	public GuiEditNBTTree(int entity, NBTTagCompound tag){
		this.entity =true;
		entityOrX = entity;
		y =0;
		z =0;
		screenTitle =  "NBTEdit -- EntityId #" + entityOrX;
		guiTree = new GuiNBTTree(new NBTTree(tag));
	}
	public GuiEditNBTTree(int x, int y, int z, NBTTagCompound tag){
		this.entity = false;
		entityOrX = x;
		this.y =y;
		this.z =z;
		screenTitle =  "NBTEdit -- TileEntity at ("+x+","+y+","+z+")";
		guiTree = new GuiNBTTree(new NBTTree(tag));
	}

	@SuppressWarnings("unchecked")
	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		buttonList.clear();
		guiTree.initGUI(width,height,height-35);
		this.buttonList.add(new GuiButton(1, width / 4 - 100, this.height -27, "Save"));
		this.buttonList.add(new GuiButton(0, width *3 / 4 -100, this.height -27, "Quit"));
	}
	
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
	}

	protected void keyTyped(char par1, int key) {
		GuiEditNBT window = guiTree.getWindow();
		if (window != null)
			window.keyTyped(par1, key);
		else{
			if (key == 1){
				if (guiTree.isEditingSlot())
					guiTree.stopEditingSlot();
				else
					quitWithoutSaving();
			}
			else if (key == Keyboard.KEY_DELETE)
				guiTree.deleteSelected();
			else if (key == Keyboard.KEY_RETURN)
				guiTree.editSelected();
			else if (key == Keyboard.KEY_UP)
				guiTree.arrowKeyPressed(true);
			else if (key == Keyboard.KEY_DOWN)
				guiTree.arrowKeyPressed(false);
			else
				guiTree.keyTyped(par1, key);
		}
	}
	protected void mouseClicked(int x, int y, int t) {
		if (guiTree.getWindow() == null)
			super.mouseClicked(x, y, t);
		if (t == 0)
			guiTree.mouseClicked(x,y);
		if (t == 1)
			guiTree.rightClick(x,y);
	}
	
	public void handleMouseInput() {
		super.handleMouseInput();
		int ofs = Mouse.getEventDWheel();

		if (ofs != 0){
			guiTree.shift((ofs >= 1) ? 6 : -6);
		}

	}
	
	protected void actionPerformed(GuiButton b) {
		if (b.enabled){
			switch(b.id){
			case 1:
				quitWithSave();
				break;
			default:
				quitWithoutSaving();
				break;
			}
		}
	}
	
	public void updateScreen() {
		if (!mc.thePlayer.isEntityAlive())
			quitWithoutSaving();
		else
			guiTree.updateScreen();
	}
	
	private void quitWithSave() {
		if (entity)
			NBTEdit.DISPATCHER.sendToServer(new EntityNBTPacket(entityOrX, guiTree.getNBTTree().toNBTTagCompound()));
		else
			NBTEdit.DISPATCHER.sendToServer(new TileNBTPacket(entityOrX, y, z, guiTree.getNBTTree().toNBTTagCompound()));
		mc.displayGuiScreen(null);
		mc.setIngameFocus();

	}
	
	private void quitWithoutSaving() {
		mc.displayGuiScreen(null);
	}
	
	public void drawScreen(int x, int y, float par3) {
		this.drawDefaultBackground();
		guiTree.draw( x, y);
		this.drawCenteredString(mc.fontRenderer, this.screenTitle, this.width / 2, 5, 16777215);
		if (guiTree.getWindow() == null)
			super.drawScreen(x, y, par3);
		else
			super.drawScreen(-1, -1, par3);
	}
	
	public boolean doesGuiPauseGame() {
		return true;
	}
	
	public Entity getEntity() {
		return entity ?  mc.theWorld.getEntityByID(entityOrX) : null;
	}
	
	public boolean isTileEntity() {
		return !entity;
	}

	public int getBlockX() {
		return entity ? 0 : entityOrX;
	}

}
