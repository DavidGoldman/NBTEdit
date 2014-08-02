package com.mcf.davidee.nbtedit.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.mcf.davidee.nbtedit.NBTStringHelper;
import com.mcf.davidee.nbtedit.nbt.NamedNBT;
import com.mcf.davidee.nbtedit.nbt.Node;

public class GuiNBTNode extends Gui{
	
	public static final ResourceLocation WIDGET_TEXTURE = new ResourceLocation("nbtedit", "textures/gui/widgets.png");

	private Minecraft mc = Minecraft.getMinecraft();
	
	private Node<NamedNBT> node;
	private GuiNBTTree tree;

	protected int width, height;
	protected int x, y;

	private String displayString;
	

	public GuiNBTNode(GuiNBTTree tree, Node<NamedNBT> node, int x, int y){
		this.tree = tree;
		this.node = node;
		this.x = x;
		this.y = y;
		height = mc.fontRenderer.FONT_HEIGHT;
		updateDisplay();
	}
	
	private boolean inBounds(int mx, int my){
		return mx >= x && my >= y && mx < width+x && my < height+y;
	}
	
	private boolean inHideShowBounds(int mx, int my){
		return mx >= x-9 && my >= y && mx < x && my < y+height;
	}
	
	public boolean shouldDrawChildren(){
		return node.shouldDrawChildren();
	}
	
	public boolean clicked(int mx, int my){
		return inBounds(mx,my);
	}
	
	public boolean hideShowClicked(int mx, int my){
		if(node.hasChildren() && inHideShowBounds(mx,my)){
			node.setDrawChildren(!node.shouldDrawChildren());
			return true;
		}
		return false;
	}
	
	public Node<NamedNBT> getNode(){
		return node;
	}
	
	public void shift(int dy){
		y += dy;
	}
	
	public void updateDisplay(){
		displayString = NBTStringHelper.getNBTNameSpecial(node.getObject());
		width = mc.fontRenderer.getStringWidth(displayString)+12;
	}
	
	public void draw(int mx, int my)
	{
		boolean selected = tree.getFocused() == node;
		boolean hover = inBounds(mx,my);
		boolean chHover = inHideShowBounds(mx,my);
		int color = selected ? 0xff : hover ? 16777120 : (node.hasParent()) ? 14737632 : -6250336;
	
		mc.renderEngine.bindTexture(WIDGET_TEXTURE);
		
		if (selected){
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			Gui.drawRect(x+11, y, x+width, y+height, Integer.MIN_VALUE);
		}
		if (node.hasChildren()){
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			this.drawTexturedModalRect(x-9, y, (node.shouldDrawChildren()) ? 9 : 0 ,(chHover)?height : 0,9,height);
		}
		
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		drawTexturedModalRect(x+1, y, (node.getObject().getNBT().getId() -1)*9, 18, 9, 9);
		drawString(mc.fontRenderer, displayString, x+11, y + (this.height - 8) / 2, color);
	}
	
	public boolean shouldDraw(int top, int bottom) {
		return y+height >= top && y <= bottom;
	}
	
	
}
