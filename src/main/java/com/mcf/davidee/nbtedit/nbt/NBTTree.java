package com.mcf.davidee.nbtedit.nbt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import com.mcf.davidee.nbtedit.NBTEdit;
import com.mcf.davidee.nbtedit.NBTHelper;
import com.mcf.davidee.nbtedit.NBTStringHelper;

public class NBTTree {
	
	private NBTTagCompound baseTag;
	
	private Node<NamedNBT> root;
	
	public NBTTree (NBTTagCompound tag){
		baseTag = tag;
		construct();
	}
	public Node<NamedNBT> getRoot(){
		return root;
	}
	
	public boolean canDelete(Node<NamedNBT> node){
		return node != root;
	}
	
	public boolean delete(Node<NamedNBT> node){
		if (node == null || node == root)
			return false;
		return deleteNode(node,root);
	}
	
	private boolean deleteNode(Node<NamedNBT> toDelete, Node<NamedNBT> cur){
		for (Iterator<Node<NamedNBT>> it = cur.getChildren().iterator(); it.hasNext();){
			Node<NamedNBT> child = it.next();
			if (child == toDelete){
				it.remove();
				return true;
			}
			boolean flag = deleteNode(toDelete,child);
			if (flag)
				return true;
		}
		return false;
	}
	
	
	private void construct() {
		root = new Node<NamedNBT>(new NamedNBT("ROOT", (NBTTagCompound)baseTag.copy()));
		addChildrenToTree(root);
		sort(root);
	}
	
	public void sort(Node<NamedNBT> node) {
		Collections.sort(node.getChildren(), NBTEdit.SORTER);
		for (Node<NamedNBT> c : node.getChildren())
			sort(c);
	}
	
	public void addChildrenToTree(Node<NamedNBT> parent){
		NBTBase tag = parent.getObject().getNBT();
		if (tag instanceof NBTTagCompound){
			Map<String,NBTBase> map =  NBTHelper.getMap((NBTTagCompound)tag);
			for (Entry<String,NBTBase> entry : map.entrySet()){
				NBTBase base = entry.getValue();
				Node<NamedNBT> child = new Node<NamedNBT>(parent, new NamedNBT(entry.getKey(), base));
				parent.addChild(child);
				addChildrenToTree(child);
			}
			
		}
		else if (tag instanceof NBTTagList){
			NBTTagList list = (NBTTagList)tag;
			for (int i =0; i < list.tagCount(); ++ i){
				NBTBase base = NBTHelper.getTagAt(list, i);
				Node<NamedNBT> child = new Node<NamedNBT>(parent, new NamedNBT(base));
				parent.addChild(child);
				addChildrenToTree(child);
			}
		}
	}
	
	public NBTTagCompound toNBTTagCompound(){
		NBTTagCompound tag = new NBTTagCompound();
		addChildrenToTag(root, tag);
		return tag;
	}
	
	public void addChildrenToTag (Node<NamedNBT> parent, NBTTagCompound tag){
		for (Node<NamedNBT> child : parent.getChildren()){
			NBTBase base = child.getObject().getNBT();
			String name = child.getObject().getName();
			if (base instanceof NBTTagCompound){
				NBTTagCompound newTag = new NBTTagCompound();
				addChildrenToTag(child, newTag);
				tag.setTag(name, newTag);
			}
			else if (base instanceof NBTTagList){
				NBTTagList list = new NBTTagList();
				addChildrenToList(child, list);
				tag.setTag(name, list);
			}
			else
				tag.setTag(name, base.copy());
		}
	}
	
	public void addChildrenToList(Node<NamedNBT> parent, NBTTagList list){
		for (Node<NamedNBT> child: parent.getChildren()){
			NBTBase base = child.getObject().getNBT();
			if (base instanceof NBTTagCompound){
				NBTTagCompound newTag = new NBTTagCompound();
				addChildrenToTag(child, newTag);
				list.appendTag(newTag);
			}
			else if (base instanceof NBTTagList){
				NBTTagList newList = new NBTTagList();
				addChildrenToList(child, newList);
				list.appendTag(newList);
			}
			else
				list.appendTag(base.copy());
		}
	}
	
	public void print(){
		print(root,0);
	}
	
	private void print(Node<NamedNBT> n, int i){
		System.out.println(repeat("\t",i) + NBTStringHelper.getNBTName(n.getObject()));
		for (Node<NamedNBT> child : n.getChildren())
			print(child,i+1);
	}
	
	public List<String> toStrings(){
		List<String> s = new ArrayList<String>();
		toStrings(s,root,0);
		return s;
	}
	
	private void toStrings(List<String> s, Node<NamedNBT> n, int i){
		s.add(repeat("   ",i) + NBTStringHelper.getNBTName(n.getObject()));
		for (Node<NamedNBT> child : n.getChildren())
			toStrings(s,child,i+1);
	}
	
	public static String repeat(String c, int i){
		StringBuilder b = new StringBuilder(i+1);
		for (int j =0; j < i; ++ j)
			b.append(c);
		return b.toString();
	}
}
