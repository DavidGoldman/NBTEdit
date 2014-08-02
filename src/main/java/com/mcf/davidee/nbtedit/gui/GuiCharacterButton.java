package com.mcf.davidee.nbtedit.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

import org.lwjgl.opengl.GL11;

public class GuiCharacterButton extends Gui{


	public static final int WIDTH = 14, HEIGHT = 14;

	private Minecraft mc = Minecraft.getMinecraft();
	private byte id;
	private int x, y;
	private boolean enabled;


	public GuiCharacterButton(byte id, int x, int y){
		this.id = id;
		this.x = x; 
		this.y = y;
	}
	public void draw(int mx, int my){
		mc.renderEngine.bindTexture(GuiNBTNode.WIDGET_TEXTURE);
		
		if(inBounds(mx,my))
			Gui.drawRect(x, y, x+WIDTH, y+HEIGHT, 0x80ffffff);
		
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		drawTexturedModalRect(x, y, id * WIDTH, 27, WIDTH, HEIGHT);
		if (!enabled){
			drawRect(x, y, x+WIDTH, y+HEIGHT, 0xc0222222);
		}
	}
	
	public void setEnabled(boolean aFlag){
		enabled = aFlag;
	}
	
	public boolean isEnabled(){
		return enabled;
	}
	
	public boolean inBounds(int mx, int my){
		return enabled && mx >= x && my >= y && mx < x + WIDTH && my < y + HEIGHT;
	}
	
	public byte getId(){
		return id;
	}
}
 