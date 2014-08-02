package com.mcf.davidee.nbtedit.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

import org.lwjgl.opengl.GL11;

import com.mcf.davidee.nbtedit.NBTStringHelper;

public class GuiNBTButton extends Gui{
	
	public static final int WIDTH = 9, HEIGHT = 9;
	
	private Minecraft mc = Minecraft.getMinecraft();
	
	private byte id;
	private int x, y;
	private boolean enabled;
	
	private long hoverTime;
	
	public GuiNBTButton(byte id, int x, int y){
		this.id = id;
		this.x = x; 
		this.y = y;
	}
	public void draw(int mx, int my){
		mc.renderEngine.bindTexture(GuiNBTNode.WIDGET_TEXTURE);
		
		if(inBounds(mx,my)){
			Gui.drawRect(x, y, x+WIDTH, y+HEIGHT, 0x80ffffff);
			if (hoverTime == -1)
				hoverTime = System.currentTimeMillis();
		}
		else
			hoverTime = -1;
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		drawTexturedModalRect(x, y, (id-1) * 9, 18, WIDTH, HEIGHT);
		if (!enabled){
			drawRect(x, y, x+WIDTH, y+HEIGHT, 0xc0222222);
		}
		else if (hoverTime != -1 && System.currentTimeMillis() - hoverTime > 300){
			drawToolTip(mx,my);
		}
	}
	private void drawToolTip(int mx, int my){
		String s = NBTStringHelper.getButtonName(id);
		int width = mc.fontRenderer.getStringWidth(s);
		drawRect(mx+4,my+7,mx+5+width,my+17, 0xff000000);
		mc.fontRenderer.drawString(s, mx+5, my+8, 0xffffff);
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
