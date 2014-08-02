package com.mcf.davidee.nbtedit.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiControls;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.mcf.davidee.nbtedit.NBTEdit;
import com.mcf.davidee.nbtedit.NBTHelper;
import com.mcf.davidee.nbtedit.NBTStringHelper;
import com.mcf.davidee.nbtedit.nbt.NBTTree;
import com.mcf.davidee.nbtedit.nbt.NamedNBT;
import com.mcf.davidee.nbtedit.nbt.Node;
import com.mcf.davidee.nbtedit.nbt.SaveStates;


/*
 * The main Gui class for NBTEdit. This implementation is messy, naive, and unoptimized, but it works.
 * This is from long before GuiLib (and is actually my motivation for GuiLib), but sadly I do not 
 * have time to rewrite it.
 * 
 * Issues:
 *    - Not extensible - a separate tree GUI class for GuiLib would be nice.
 *    - Naive/unoptimized - layout changes force an entire reload of the tree
 *    - Messy, good luck. Some of the button IDs are hardcoded.
 */
public class GuiNBTTree extends Gui{

	private Minecraft mc = Minecraft.getMinecraft();

	private NBTTree tree;
	private List<GuiNBTNode> nodes;
	private GuiSaveSlotButton[] saves;
	private GuiNBTButton[] buttons;

	private final int X_GAP = 10, START_X = 10, START_Y = 30;
	private final int Y_GAP = Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT+2;

	private int y, yClick, bottom, width, height, heightDiff, offset;

	private Node<NamedNBT> focused;
	private int focusedSlotIndex;

	private GuiEditNBT window;

	public Node<NamedNBT> getFocused(){
		return focused;
	}

	public GuiSaveSlotButton getFocusedSaveSlot(){
		return (focusedSlotIndex != -1) ? saves[focusedSlotIndex] : null;
	}

	public NBTTree getNBTTree(){
		return tree;
	}

	public GuiNBTTree(NBTTree tree){
		this.tree = tree;
		yClick = -1;
		focusedSlotIndex = -1;
		nodes = new ArrayList<GuiNBTNode>();
		buttons = new GuiNBTButton[16];
		saves = new GuiSaveSlotButton[7];
	}

	private int getHeightDifference(){
		return getContentHeight() - (bottom - START_Y + 2);
	}

	private int getContentHeight(){
		return Y_GAP * nodes.size();
	}

	public GuiEditNBT getWindow(){
		return window;
	}

	public void initGUI(int width, int height, int bottom){
		this.width = width;
		this.height = height;
		this.bottom = bottom;
		yClick = -1;
		initGUI(false);
		if (window != null)
			window.initGUI((width-GuiEditNBT.WIDTH)/2, (height-GuiEditNBT.HEIGHT)/2);
	}

	public void updateScreen(){
		if (window != null)
			window.update();
		if (focusedSlotIndex != -1)
			saves[focusedSlotIndex].update();
	}

	private void setFocused(Node<NamedNBT> toFocus){
		if (toFocus == null){
			for (GuiNBTButton b: buttons)
				b.setEnabled(false);
		}
		else if (toFocus.getObject().getNBT() instanceof NBTTagCompound){
			for (GuiNBTButton b: buttons)
				b.setEnabled(true);
			buttons[12].setEnabled(toFocus != tree.getRoot());
			buttons[11].setEnabled(toFocus.hasParent() && !(toFocus.getParent().getObject().getNBT() instanceof NBTTagList));
			buttons[13].setEnabled(true);
			buttons[14].setEnabled(toFocus != tree.getRoot());
			buttons[15].setEnabled(NBTEdit.clipboard != null);
		}
		else if (toFocus.getObject().getNBT() instanceof NBTTagList){
			if (toFocus.hasChildren()){
				byte type = toFocus.getChildren().get(0).getObject().getNBT().getId();
				for (GuiNBTButton b: buttons)
					b.setEnabled(false);
				buttons[type-1].setEnabled(true);
				buttons[12].setEnabled(true);
				buttons[11].setEnabled(! (toFocus.getParent().getObject().getNBT() instanceof NBTTagList));
				buttons[13].setEnabled(true);
				buttons[14].setEnabled(true);
				buttons[15].setEnabled(NBTEdit.clipboard != null && NBTEdit.clipboard.getNBT().getId() == type);
			}
			else
				for (GuiNBTButton b: buttons)
					b.setEnabled(true);
			buttons[11].setEnabled(! (toFocus.getParent().getObject().getNBT() instanceof NBTTagList));
			buttons[13].setEnabled(true);
			buttons[14].setEnabled(true);
			buttons[15].setEnabled(NBTEdit.clipboard != null);
		}
		else{
			for (GuiNBTButton b: buttons)
				b.setEnabled(false);
			buttons[12].setEnabled(true);
			buttons[11].setEnabled(true);
			buttons[13].setEnabled(true);
			buttons[14].setEnabled(true);
			buttons[15].setEnabled(false);
		}

		focused = toFocus;
		if (focused != null && focusedSlotIndex != -1){
			stopEditingSlot();
		}
	}

	public void initGUI(){
		initGUI(false);
	}

	public void initGUI(boolean shiftToFocused) {
		y = START_Y;
		nodes.clear();
		addNodes(tree.getRoot(),  START_X);
		addButtons();
		addSaveSlotButtons();
		if (focused != null){
			if (!checkValidFocus(focused)){
				setFocused(null);
			}
		}
		if (focusedSlotIndex != -1){
			saves[focusedSlotIndex].startEditing();
		}
		heightDiff = getHeightDifference();
		if (heightDiff <= 0)
			offset = 0;
		else{
			if (offset < -heightDiff)
				offset = -heightDiff;
			if (offset > 0)
				offset = 0;
			for (GuiNBTNode node : nodes)
				node.shift(offset);
			if (shiftToFocused && focused != null){
				shiftTo(focused);
			}
		}
	}
	private void addSaveSlotButtons(){
		SaveStates saveStates = NBTEdit.getSaveStates();
		for (int i =0; i < 7; ++i)
			saves[i] = new GuiSaveSlotButton(saveStates.getSaveState(i),width-24,31+i*25);
	}

	private void addButtons(){
		int x = 18, y = 4;

		for (byte i = 14; i < 17; ++i){
			buttons[i-1] = new GuiNBTButton(i,x,y);
			x+=15;
		}

		x += 30;
		for (byte i = 12; i < 14; ++i){
			buttons[i-1] = new GuiNBTButton(i,x,y);
			x +=15;
		}

		x = 18;
		y = 17;
		for (byte i = 1; i < 12; ++i){
			buttons[i-1] = new GuiNBTButton(i,x,y);
			x += 9;
		}
	}


	private boolean checkValidFocus(Node<NamedNBT> fc) {
		for (GuiNBTNode node : nodes){ //Check all nodes
			if (node.getNode() == fc){
				setFocused(fc);
				return true;
			}
		}
		return fc.hasParent() ? checkValidFocus(fc.getParent()) : false;
	}

	private void addNodes(Node<NamedNBT> node, int x) {
		nodes.add(new GuiNBTNode(this, node, x, y)); 
		x += X_GAP;
		y += Y_GAP;
		
		if (node.shouldDrawChildren())
			for (Node<NamedNBT> child : node.getChildren())
				addNodes(child,x);
	}

	public void draw(int mx, int my){
		int cmx = mx, cmy = my;
		if (window != null){
			cmx = -1;
			cmy = -1;
		}
		for (GuiNBTNode node : nodes){
			if (node.shouldDraw(START_Y-1,bottom))
				node.draw(cmx, cmy);
		}
		overlayBackground(0, START_Y-1, 255, 255);
		overlayBackground(bottom, height, 255, 255);
		for (GuiNBTButton but : buttons)
			but.draw(cmx,cmy);
		for (GuiSaveSlotButton but : saves)
			but.draw(cmx, cmy);
		drawScrollBar(cmx, cmy);
		if (window != null)
			window.draw(mx, my);
	}

	private void drawScrollBar(int mx, int my){
		if (heightDiff > 0){
			if (Mouse.isButtonDown(0)){
				if (yClick == -1){
					if (mx >= width-20 && mx < width && my >= START_Y -1 && my < bottom){
						yClick = my;
					}
				}
				else {
					float scrollMultiplier = 1.0F;
					int height = getHeightDifference();

					if (height < 1)
					{
						height = 1;
					}
					int length = (bottom - (START_Y-1)) * (bottom - (START_Y-1)) / getContentHeight();
					if (length < 32)
						length = 32;
					if (length > bottom - (START_Y-1) - 8)
						length = bottom - (START_Y-1) - 8;

					scrollMultiplier /= (float)(this.bottom - (START_Y - 1) - length) / (float) height;



					shift( (int)((yClick-my)  * scrollMultiplier));
					yClick = my;
				}
			}
			else
				yClick = -1;


			drawRect(width-20,START_Y - 1, width, bottom, Integer.MIN_VALUE);


			int length = (bottom - (START_Y-1)) * (bottom - (START_Y-1)) / getContentHeight();
			if (length < 32)
				length = 32;
			if (length > bottom - (START_Y-1) - 8)
				length = bottom - (START_Y-1) - 8;
			int y = -offset * (this.bottom - (START_Y-1) - length) / heightDiff + (START_Y-1);

			if (y < START_Y-1)
				y = START_Y-1;





			//	this.drawGradientRect(width-20,y,width,y+length,8421504, 12632256);
			//drawRect(width-20,y,width,y+length,0x80ffffff);
			drawGradientRect(width-20,y,width,y+length,0x80ffffff,0x80333333);
		}
	}

	protected void overlayBackground(int par1, int par2, int par3, int par4)
	{
		Tessellator var5 = Tessellator.instance;
		mc.renderEngine.bindTexture(optionsBackground);
		//GL11.glBindTexture(GL11.GL_TEXTURE_2D, mc.renderEngine.getTexture( "/gui/background.png"));
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		float var6 = 32.0F;
		var5.startDrawingQuads();
		var5.setColorRGBA_I(4210752, par4);
		var5.addVertexWithUV(0.0D, (double)par2, 0.0D, 0.0D, (double)((float)par2 / var6));
		var5.addVertexWithUV((double)this.width, (double)par2, 0.0D, (double)((float)this.width / var6), (double)((float)par2 / var6));
		var5.setColorRGBA_I(4210752, par3);
		var5.addVertexWithUV((double)this.width, (double)par1, 0.0D, (double)((float)this.width / var6), (double)((float)par1 / var6));
		var5.addVertexWithUV(0.0D, (double)par1, 0.0D, 0.0D, (double)((float)par1 / var6));
		var5.draw();
	}

	public void mouseClicked(int mx, int my) {
		if (window == null){
			boolean reInit = false;

			for (GuiNBTNode node : nodes){
				if (node.hideShowClicked(mx, my)){ // Check hide/show children buttons
					reInit = true;
					if (node.shouldDrawChildren())
						offset =  (START_Y+1) - (node.y) + offset;
					break;
				}
			}
			if (!reInit){
				for (GuiNBTButton button : buttons){ //Check top buttons
					if (button.inBounds(mx, my)){
						buttonClicked(button);
						return;
					}
				}
				for (GuiSaveSlotButton button : saves){
					if (button.inBoundsOfX(mx, my)){
						button.reset();
						NBTEdit.getSaveStates().save();
						mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));
						return;
					}
					if (button.inBounds(mx, my)){
						saveButtonClicked(button);
						return;
					}
				}
				if (my >= START_Y && mx <= width-175){ //Check actual nodes, remove focus if nothing clicked
					Node<NamedNBT> newFocus = null;
					for (GuiNBTNode node : nodes){
						if (node.clicked(mx, my)){
							newFocus = node.getNode();
							break;
						}
					}
					if (focusedSlotIndex != -1)
						stopEditingSlot();
					setFocused(newFocus);
				}
			}
			else
				initGUI();
		}
		else 
			window.click(mx, my);
	}

	private void saveButtonClicked(GuiSaveSlotButton button){
		if (button.save.tag.hasNoTags()){ //Copy into save slot
			Node<NamedNBT> obj = (focused == null) ? tree.getRoot() : focused;
			NBTBase base = obj.getObject().getNBT();
			String name = obj.getObject().getName();
			if (base instanceof NBTTagList){
				NBTTagList list = new NBTTagList();
				tree.addChildrenToList(obj, list);
				button.save.tag.setTag(name, list);
			}
			else if (base instanceof NBTTagCompound){
				NBTTagCompound compound = new NBTTagCompound();
				tree.addChildrenToTag(obj, compound);
				button.save.tag.setTag(name, compound);
			}

			else
				button.save.tag.setTag(name, base.copy());
			button.saved();
			NBTEdit.getSaveStates().save();
			mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));
		}
		else{ //Paste into
			Map<String, NBTBase> nbtMap = NBTHelper.getMap(button.save.tag);
			if (nbtMap.isEmpty()){
				NBTEdit.log(Level.WARNING, "Unable to copy from save \"" + button.save.name +"\".");
				NBTEdit.log(Level.WARNING, "The save is invalid - a valid save must only contain 1 core NBTBase");
			}
			else{
				if (focused == null)
					setFocused(tree.getRoot());
				
				Entry<String, NBTBase> firstEntry = null;
				for(Entry<String, NBTBase> entry : nbtMap.entrySet()) {
					firstEntry = entry;
					break;
				}

				String name = firstEntry.getKey();
				NBTBase nbt = firstEntry.getValue().copy();
				if (focused == tree.getRoot() && nbt instanceof NBTTagCompound && name.equals("ROOT")){
					setFocused(null);
					tree = new NBTTree((NBTTagCompound)nbt);
					initGUI();
					mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));
				}
				else if (canAddToParent(focused.getObject().getNBT(), nbt)){
					focused.setDrawChildren(true);
					for (Iterator<Node<NamedNBT>> it = focused.getChildren().iterator(); it.hasNext();){ //Replace object with same name
						if (it.next().getObject().getName().equals(name)){
							it.remove();
							break;
						}
					}
					Node<NamedNBT> node = insert(new NamedNBT(name, nbt));
					tree.addChildrenToTree(node);
					tree.sort(node);
					setFocused(node);
					initGUI(true);
					mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));
				}
			}
		}
	}

	private void buttonClicked(GuiNBTButton button){
		if (button.getId() == 16)
			paste();
		else if (button.getId() == 15)
			cut();
		else if (button.getId() == 14)
			copy();
		else if (button.getId() == 13)
			deleteSelected();
		else if (button.getId() == 12)
			edit();
		else if (focused != null){
			focused.setDrawChildren(true);
			List<Node<NamedNBT>> children = focused.getChildren();
			String type = NBTStringHelper.getButtonName(button.getId());

			if (focused.getObject().getNBT() instanceof NBTTagList){
				NBTBase nbt = NBTStringHelper.newTag(button.getId());
				if (nbt != null){
					Node<NamedNBT> newNode = new Node<NamedNBT>(focused, new NamedNBT("",nbt));
					children.add(newNode);
					setFocused(newNode);
				}
			}
			else if (children.size() == 0){
				setFocused(insert(type + "1", button.getId()));
			}
			else{
				for (int i =1; i <= children.size()+1; ++i){
					String name = type + i;
					if (validName(name,children)){
						setFocused(insert(name,button.getId()));
						break;
					}
				}
			}
			initGUI(true);
		}
	}

	private boolean validName(String name, List<Node<NamedNBT>> list){
		for (Node<NamedNBT> node : list)
			if (node.getObject().getName().equals(name))
				return false;
		return true;
	}

	private Node<NamedNBT> insert(NamedNBT nbt){
		Node<NamedNBT> newNode = new Node<NamedNBT>(focused, nbt);

		if (focused.hasChildren()){
			List<Node<NamedNBT>> children = focused.getChildren();

			boolean added = false;
			for (int i = 0; i < children.size(); ++i) {
				if (NBTEdit.SORTER.compare(newNode, children.get(i)) < 0) {
					children.add(i, newNode);
					added = true;
					break;
				}
			}
			if (!added)
				children.add(newNode);
		}
		else
			focused.addChild(newNode);
		return newNode;
	}

	private Node<NamedNBT> insert(String name, byte type){
		NBTBase nbt = NBTStringHelper.newTag(type);
		if (nbt != null)
			return insert(new NamedNBT(name, nbt));
		return null;
	}

	public void deleteSelected() {
		if (focused != null){
			if (tree.delete(focused)){
				Node<NamedNBT> oldFocused = focused;
				shiftFocus(true);
				if (focused == oldFocused)
					setFocused(null);
				initGUI();
			}
		}

	}

	public void editSelected() {
		if (focused != null){
			NBTBase base = focused.getObject().getNBT();
			if (focused.hasChildren() &&  (base instanceof NBTTagCompound || base instanceof NBTTagList)){
				focused.setDrawChildren(!focused.shouldDrawChildren());
				int index = -1;
				
				if(focused.shouldDrawChildren() && (index = indexOf(focused)) != -1)
					offset =  (START_Y+1) - nodes.get(index).y + offset;
				
				initGUI();
			}
			else if (buttons[11].isEnabled()){
				edit();
			}
		}
		else if (focusedSlotIndex != -1){
			stopEditingSlot();
		}
	}

	private boolean canAddToParent(NBTBase parent, NBTBase child) {
		if (parent instanceof NBTTagCompound)
			return true;
		if (parent instanceof NBTTagList){
			NBTTagList list = (NBTTagList) parent;
			return list.tagCount() == 0 || list.func_150303_d() == child.getId();
		}
		return false;
	}

	private boolean canPaste() {
		if (NBTEdit.clipboard != null && focused != null)
			return canAddToParent(focused.getObject().getNBT(), NBTEdit.clipboard.getNBT());
		return false;
	}
	
	private void paste() {
		if (NBTEdit.clipboard != null){
			focused.setDrawChildren(true);
			
			NamedNBT namedNBT = NBTEdit.clipboard.copy();
			if (focused.getObject().getNBT() instanceof NBTTagList){
				namedNBT.setName("");
				Node<NamedNBT> node = new Node<NamedNBT>(focused, namedNBT);
				focused.addChild(node);
				tree.addChildrenToTree(node);
				tree.sort(node);
				setFocused(node);
			}
			else{
				String name = namedNBT.getName();
				List<Node<NamedNBT>> children = focused.getChildren();
				if (!validName(name, children)){
					for (int i = 1; i <= children.size() + 1; ++i){
						String n = name + "(" + i + ")";
						if (validName(n,children)){
							namedNBT.setName(n);
							break;
						}
					}
				}
				Node<NamedNBT> node = insert(namedNBT);
				tree.addChildrenToTree(node);
				tree.sort(node);
				setFocused(node);
			}

			initGUI(true);
		}
	}
	
	private void copy(){
		if (focused != null){
			NamedNBT namedNBT = focused.getObject();
			if (namedNBT.getNBT() instanceof NBTTagList){
				NBTTagList list = new NBTTagList();
				tree.addChildrenToList(focused, list);
				NBTEdit.clipboard = new NamedNBT(namedNBT.getName(), list);
			}
			else if (namedNBT.getNBT() instanceof NBTTagCompound){
				NBTTagCompound compound = new NBTTagCompound();
				tree.addChildrenToTag(focused, compound);
				NBTEdit.clipboard = new NamedNBT(namedNBT.getName(), compound);
			}
			else
				NBTEdit.clipboard = focused.getObject().copy();
			setFocused(focused);
		}
	}

	private void cut(){
		copy();
		deleteSelected();
	}

	private void edit(){
		NBTBase base = focused.getObject().getNBT();
		NBTBase parent = focused.getParent().getObject().getNBT();
		window = new GuiEditNBT(this, focused, !(parent instanceof NBTTagList), !(base instanceof NBTTagCompound || base instanceof NBTTagList));
		window.initGUI((width-GuiEditNBT.WIDTH)/2, (height-GuiEditNBT.HEIGHT)/2);
	}

	public void nodeEdited(Node<NamedNBT> node){
		Node<NamedNBT> parent = node.getParent();
		Collections.sort(parent.getChildren(), NBTEdit.SORTER);
		initGUI(true);
	}

	public void arrowKeyPressed(boolean up){
		if (focused == null)
			shift((up) ? Y_GAP : -Y_GAP);
		else
			shiftFocus(up);
	}

	private int indexOf(Node<NamedNBT> node){
		for (int i =0; i < nodes.size(); ++i){
			if (nodes.get(i).getNode() == node){
				return i;
			}
		}
		return -1;
	}
	
	private void shiftFocus(boolean up){
		int index = indexOf(focused);
		if (index != -1){
			index += (up) ? -1 : 1;
			if (index >= 0 && index < nodes.size()){
				setFocused(nodes.get(index).getNode());
				shift((up) ? Y_GAP : -Y_GAP);
			}
		}
	}
	private void shiftTo(Node<NamedNBT> node){
		int index = indexOf(node);
		if (index != -1){
			GuiNBTNode gui = nodes.get(index);
			shift((bottom+START_Y+1)/2 - (gui.y+gui.height));
		}
	}

	public void shift(int i) {
		if (heightDiff <= 0 || window != null)
			return;
		int dif = offset + i;
		if (dif > 0)
			dif = 0;
		if (dif < -heightDiff)
			dif = -heightDiff;
		for (GuiNBTNode node : nodes)
			node.shift(dif-offset);
		offset = dif; 
	}

	public void closeWindow() {
		window = null;
	}

	public boolean isEditingSlot(){
		return focusedSlotIndex != -1;
	}

	public void stopEditingSlot(){
		saves[focusedSlotIndex].stopEditing();
		NBTEdit.getSaveStates().save();
		focusedSlotIndex = -1;
	}

	public void keyTyped(char ch, int key) {
		if (focusedSlotIndex != -1){
			saves[focusedSlotIndex].keyTyped(ch, key);
		}
		else{
			if (key == Keyboard.KEY_C && GuiControls.isCtrlKeyDown())
				copy();
			if (key == Keyboard.KEY_V && GuiControls.isCtrlKeyDown() && canPaste())
				paste();
			if (key == Keyboard.KEY_X && GuiControls.isCtrlKeyDown())
				cut();
		}
	}

	public void rightClick(int mx, int my) {
		for (int i = 0; i < 7; ++i){
			if (saves[i].inBounds(mx, my)){
				setFocused(null);
				if (focusedSlotIndex != -1){
					if (focusedSlotIndex != i){
						saves[focusedSlotIndex].stopEditing();
						NBTEdit.getSaveStates().save();
					}
					else //Already editing the correct one!
						return; 
				}
				saves[i].startEditing();
				focusedSlotIndex = i;
				break;
			}
		}
	}

}
