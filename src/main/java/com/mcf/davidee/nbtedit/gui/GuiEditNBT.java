package com.mcf.davidee.nbtedit.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.mcf.davidee.nbtedit.NBTStringHelper;
import com.mcf.davidee.nbtedit.nbt.NamedNBT;
import com.mcf.davidee.nbtedit.nbt.Node;
import com.mcf.davidee.nbtedit.nbt.ParseHelper;

public class GuiEditNBT extends Gui{

	public static final ResourceLocation WINDOW_TEXTURE = new ResourceLocation("nbtedit", "textures/gui/window.png");
	
	public static final int WIDTH = 178, HEIGHT = 93;

	private Minecraft mc = Minecraft.getMinecraft();
	private Node<NamedNBT> node;
	private NBTBase nbt;
	private boolean canEditText, canEditValue;
	private GuiNBTTree parent;

	private int x, y;


	private GuiTextField key,  value;
	private GuiButton save, cancel;
	private String kError, vError;

	private GuiCharacterButton newLine, section;


	public GuiEditNBT(GuiNBTTree parent, Node<NamedNBT> node, boolean editText, boolean editValue){
		this.parent = parent;
		this.node = node;
		this.nbt = node.getObject().getNBT();
		canEditText = editText;
		canEditValue = editValue;
	}
	
	public void initGUI(int x, int y){
		this.x=x;
		this.y=y;
		
		section = new GuiCharacterButton((byte)0,x+WIDTH-1,y+34);
		newLine = new GuiCharacterButton((byte)1,x+WIDTH-1,y+50);
		String sKey = (key == null) ? node.getObject().getName() : key.getText();
		String sValue = (value == null) ? getValue(nbt) : value.getText();
		this.key = new GuiTextField(mc.fontRenderer,x+46,y+18,116,15,false);
		this.value = new GuiTextField(mc.fontRenderer,x+46,y+44,116,15,true);
		
		key.setText(sKey);
		key.setEnableBackgroundDrawing(false);
		key.func_82265_c(canEditText);
		value.setMaxStringLength(256);
		value.setText(sValue);
		value.setEnableBackgroundDrawing(false);
		value.func_82265_c(canEditValue);
		save = new GuiButton(1,x+9,y+62,75,20,"Save");
		if(!key.isFocused() && !value.isFocused()){
			if (canEditText)
				key.setFocused(true);
			else if (canEditValue)
				value.setFocused(true);
		}
		section.setEnabled(value.isFocused());
		newLine.setEnabled(value.isFocused());
		cancel = new GuiButton(0,x+93,y+62,75,20,"Cancel");
	}

	public void click(int mx, int my){
		if (newLine.inBounds(mx, my) && value.isFocused()){
			value.writeText("\n");
			checkValidInput();
		}
		else if (section.inBounds(mx,my) && value.isFocused()){
			value.writeText("" + NBTStringHelper.SECTION_SIGN);
			checkValidInput();
		}
		else{
			key.mouseClicked(mx, my, 0);
			value.mouseClicked(mx, my, 0);
			if(save.mousePressed(mc, mx, my))
				saveAndQuit();
			if(cancel.mousePressed(mc, mx, my))
				parent.closeWindow();
			section.setEnabled(value.isFocused());
			newLine.setEnabled(value.isFocused());
		}
	}
	
	private void saveAndQuit(){
		if (canEditText)
			node.getObject().setName(key.getText());
		setValidValue(node, value.getText());
		parent.nodeEdited(node);
		parent.closeWindow();
	}

	public void draw(int mx, int my){
		//GL11.glBindTexture(GL11.GL_TEXTURE_2D, mc.renderEngine.getTexture("/nbtedit_textures/nbteditwindow.png"));
		mc.renderEngine.bindTexture(WINDOW_TEXTURE);
		
		GL11.glColor4f(1, 1, 1, 1);
		drawTexturedModalRect(x,y,0,0,WIDTH,HEIGHT);
		if (!canEditText)
			drawRect(x+42, y+15, x+169, y+31, 0x80000000);
		if(!canEditValue)
			drawRect(x+42, y+41, x+169, y+57, 0x80000000);
		key.drawTextBox();
		value.drawTextBox();

		save.drawButton(mc, mx, my);
		cancel.drawButton(mc, mx, my);

		if (kError != null)
			drawCenteredString(mc.fontRenderer, kError, x+WIDTH/2, y+4, 0xFF0000);
		if (vError != null)
			drawCenteredString(mc.fontRenderer,vError,x+WIDTH/2,y+32,0xFF0000);

		newLine.draw(mx, my);
		section.draw(mx, my);
	}
	
	public void drawCenteredString(FontRenderer par1FontRenderer, String par2Str, int par3, int par4, int par5) {
		par1FontRenderer.drawString(par2Str, par3 - par1FontRenderer.getStringWidth(par2Str) / 2, par4, par5);
	}
	
	public void update() {
		value.updateCursorCounter();
		key.updateCursorCounter();
	}
	
	public void keyTyped(char c, int i) {
		if (i == Keyboard.KEY_ESCAPE){
			parent.closeWindow();
		}
		else if (i == Keyboard.KEY_TAB){
			if (key.isFocused() && canEditValue){
				key.setFocused(false);
				value.setFocused(true);
			}
			else if (value.isFocused() && canEditText){
				key.setFocused(true);
				value.setFocused(false);
			}
			section.setEnabled(value.isFocused());
			newLine.setEnabled(value.isFocused());
		}
		else if (i == Keyboard.KEY_RETURN) {
			checkValidInput();
			if (save.enabled)
				saveAndQuit();
		}
		else{
			key.textboxKeyTyped(c, i);
			value.textboxKeyTyped(c, i);
			checkValidInput();
		}
	}
	
	private void checkValidInput(){
		boolean valid = true;
		kError = null;
		vError = null;
		if (canEditText && !validName()){
			valid = false;
			kError = "Duplicate Tag Name";
		}
		try {
			validValue(value.getText(),nbt.getId());
			valid &= true;
		}
		catch(NumberFormatException e){
			vError = e.getMessage();
			valid = false;
		}
		save.enabled = valid;
	}
	
	private boolean validName(){
		for (Node<NamedNBT> node : this.node.getParent().getChildren()){
			NBTBase base = node.getObject().getNBT();
			if (base != nbt && node.getObject().getName().equals(key.getText()))
				return false;
		}
		return true;
	}
	
	private static void setValidValue(Node<NamedNBT> node, String value){
		NamedNBT named = node.getObject();
		NBTBase base = named.getNBT();
		
		if (base instanceof NBTTagByte)
			named.setNBT(new NBTTagByte(ParseHelper.parseByte(value)));
		if (base instanceof NBTTagShort)
			named.setNBT(new NBTTagShort(ParseHelper.parseShort(value)));
		if (base instanceof NBTTagInt)
			named.setNBT(new NBTTagInt(ParseHelper.parseInt(value)));
		if (base instanceof NBTTagLong)
			named.setNBT(new NBTTagLong(ParseHelper.parseLong(value)));
		if(base instanceof NBTTagFloat)
			named.setNBT(new NBTTagFloat(ParseHelper.parseFloat(value)));
		if(base instanceof NBTTagDouble)
			named.setNBT(new NBTTagDouble(ParseHelper.parseDouble(value)));
		if(base instanceof NBTTagByteArray)
			named.setNBT(new NBTTagByteArray(ParseHelper.parseByteArray(value)));
		if(base instanceof NBTTagIntArray)
			named.setNBT(new NBTTagIntArray(ParseHelper.parseIntArray(value)));
		if (base instanceof NBTTagString)
			named.setNBT(new NBTTagString(value));
	}

	private static void validValue(String value, byte type) throws NumberFormatException{
		switch(type){
		case 1:
			ParseHelper.parseByte(value);
			break;
		case 2: 
			ParseHelper.parseShort(value);
			break;
		case 3:
			ParseHelper.parseInt(value);
			break;
		case 4:
			ParseHelper.parseLong(value);
			break;
		case 5:
			ParseHelper.parseFloat(value);
			break;
		case 6:
			ParseHelper.parseDouble(value);
			break;
		case 7:
			ParseHelper.parseByteArray(value);
			break;
		case 11:
			ParseHelper.parseIntArray(value);
			break;
		}
	}
	
	private static String getValue(NBTBase base){
		switch(base.getId()){
		case 7:
			String s = "";
			for (byte b : ((NBTTagByteArray)base).func_150292_c() /*byteArray*/){
				s += b + " ";
			}
			return s;
		case 9:
			return "TagList";
		case 10:
			return "TagCompound";
		case 11:
			String i = "";
			for (int a : ((NBTTagIntArray)base).func_150302_c() /*intArray*/){
				i += a + " ";
			}
			return i;
		default: 
			return NBTStringHelper.toString(base);
		}
	}

}
