package com.mcf.davidee.nbtedit;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import cpw.mods.fml.relauncher.ReflectionHelper;

public class NBTHelper {
	
	public static NBTTagCompound nbtRead(DataInputStream in) throws IOException {
		return CompressedStreamTools.read(in);
	}
	
	public static void nbtWrite(NBTTagCompound compound, DataOutput out) throws IOException {
		CompressedStreamTools.write(compound, out);
	}
	
	public static Map<String,NBTBase> getMap(NBTTagCompound tag){
		return ReflectionHelper.getPrivateValue(NBTTagCompound.class, tag, 1);
	}
	
	public static NBTBase getTagAt(NBTTagList tag, int index) {
		List<NBTBase> list = ReflectionHelper.getPrivateValue(NBTTagList.class, tag, 0);
		return list.get(index);
	}
}
