package com.mcf.davidee.nbtedit.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.mcf.davidee.nbtedit.nbt.SaveStates;

public class GuiSaveSlotButton extends Gui {

	public static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/widgets.png");
	private static final int X_SIZE = 14, HEIGHT = 20, MAX_WIDTH = 150, MIN_WIDTH = 82, GAP = 3;
	private final Minecraft mc;
	public final SaveStates.SaveState save;
	private final int rightX;

	private int x, y;
	private int width;
	private String text;
	private boolean xVisible;

	private int tickCount;

	public GuiSaveSlotButton(SaveStates.SaveState save, int rightX, int y){
		this.save = save;
		this.rightX = rightX;
		this.y = y;
		mc = Minecraft.getMinecraft();
		xVisible = !save.tag.hasNoTags();
		text = (save.tag.hasNoTags() ? "Save " : "Load ") + save.name;
		tickCount = -1;
		updatePosition();
	}


	public void draw(int mx, int my){
		/*
		int color = (inBounds(mx,my)) ? 0x80ffffff : 0x80000000;
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.drawRect(x, y, x+width, y+HEIGHT, color);*/

		int textColor = ((inBounds(mx,my))) ? 16777120 : 0xffffff;
		renderVanillaButton(x,y,0,66,width,HEIGHT);
		drawCenteredString(mc.fontRenderer, text, x + width/2, y + 6, textColor);
		if (tickCount != -1 && tickCount / 6 % 2 == 0){
			mc.fontRenderer.drawStringWithShadow("_", x+(width+mc.fontRenderer.getStringWidth(text))/2+1, y+6, 0xffffff);
		}

		if (xVisible){
			textColor = ((inBoundsOfX(mx,my))) ? 16777120 : 0xffffff;
			renderVanillaButton(leftBoundOfX(),topBoundOfX(),0,66,X_SIZE,X_SIZE);
			drawCenteredString(mc.fontRenderer, "x", x-GAP-X_SIZE/2, y + 6, textColor);
		}
	}
	
	private void renderVanillaButton(int x, int y, int u, int v, int width, int height){
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(TEXTURE);
		
		//Top Left
		this.drawTexturedModalRect(x, y, u, v, width/2, height/2);
		//Top Right 
		this.drawTexturedModalRect(x+width/2, y, u +200 - width /2, v, width/2, height/2);
		//Bottom Left
		this.drawTexturedModalRect(x, y+height/2, u, v+20-height/2, width/2, height/2);
		//Bottom Right
		this.drawTexturedModalRect(x+width/2, y+height/2, u + 200-width/2, v+20-height/2, width/2, height/2);
	}

	private int leftBoundOfX(){
		return x - X_SIZE - GAP;
	}
	private int topBoundOfX(){
		return y + (HEIGHT-X_SIZE)/2;
	}

	public boolean inBoundsOfX(int mx, int my){
		int buttonX = leftBoundOfX();
		int buttonY = topBoundOfX();
		return xVisible && mx >= buttonX && my >= buttonY && mx < buttonX + X_SIZE && my < buttonY + X_SIZE;
	}
	public boolean inBounds(int mx, int my){
		return mx >= x && my >= y && mx < x + width && my < y + HEIGHT;
	}

	private void updatePosition(){
		width = mc.fontRenderer.getStringWidth(text)+24;
		if (width % 2 == 1)
			++width;
		width = MathHelper.clamp_int(width, MIN_WIDTH, MAX_WIDTH);
		x = rightX - width;
	}

	public void reset(){
		xVisible = false;
		save.tag = new NBTTagCompound();
		text =  "Save " + save.name;
		updatePosition();
	}


	public void saved() {
		xVisible = true;
		text =  "Load " + save.name;
		updatePosition();
	}


	public void keyTyped(char c, int key) {
		if (key == Keyboard.KEY_BACK){
			backSpace();
		}
		if (Character.isDigit(c) || Character.isLetter(c)){
			save.name += c;
			text = (save.tag.hasNoTags() ? "Save " : "Load ") + save.name;
			updatePosition();
		}
	}


	public void backSpace(){
		if (save.name.length() > 0){
			save.name = save.name.substring(0,save.name.length()-1);
			text = (save.tag.hasNoTags() ? "Save " : "Load ") + save.name;
			updatePosition();
		}
	}

	public void startEditing(){
		tickCount = 0;
	}

	public void stopEditing() {
		tickCount = -1;
	}


	public void update() {
		++tickCount;
	}

}
