package com.mcf.davidee.nbtedit;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagEnd;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;

import com.google.common.base.Strings;
import com.mcf.davidee.nbtedit.nbt.NamedNBT;

public class NBTStringHelper {

	public static final char SECTION_SIGN = '\u00A7';

	public static String getNBTName(NamedNBT namedNBT){
		String name = namedNBT.getName();
		NBTBase obj = namedNBT.getNBT();

		String s = toString(obj);
		return Strings.isNullOrEmpty(name) ? "" + s : name + ": " + s;
	}

	public static String getNBTNameSpecial(NamedNBT namedNBT){
		String name = namedNBT.getName();
		NBTBase obj = namedNBT.getNBT();

		String s = toString(obj);
		return Strings.isNullOrEmpty(name) ? "" + s : name + ": " + s + SECTION_SIGN + 'r';
	}

	public static NBTBase newTag(byte type){
		switch (type)
		{
		case 0:
			return new NBTTagEnd();
		case 1:
			return new NBTTagByte((byte) 0);
		case 2:
			return new NBTTagShort();
		case 3:
			return new NBTTagInt(0);
		case 4:
			return new NBTTagLong(0);
		case 5:
			return new NBTTagFloat(0);
		case 6:
			return new NBTTagDouble(0);
		case 7:
			return new NBTTagByteArray(new byte[0]);
		case 8:
			return new NBTTagString("");
		case 9:
			return new NBTTagList();
		case 10:
			return new NBTTagCompound();
		case 11:
			return new NBTTagIntArray(new int[0]);
		default:
			return null;
		}
	}

	public static String toString(NBTBase base) {
		switch(base.getId()) {
		case 1:
			return "" + ((NBTTagByte)base).func_150290_f();
		case 2:
			return "" + ((NBTTagShort)base).func_150289_e();
		case 3:
			return "" + ((NBTTagInt)base).func_150287_d();
		case 4:
			return "" + ((NBTTagLong)base).func_150291_c();
		case 5:
			return "" + ((NBTTagFloat)base).func_150288_h();
		case 6:
			return "" + ((NBTTagDouble)base).func_150286_g();
		case 7:
			return base.toString();
		case 8:
			return ((NBTTagString)base).func_150285_a_();
		case 9:
			return "(TagList)";
		case 10:
			return "(TagCompound)";
		case 11:
			return base.toString();
		default:
			return "?";
		}
	}

	public static String getButtonName(byte id){
		switch(id){
		case 1 :
			return "Byte";
		case 2: 
			return "Short";
		case 3:
			return "Int";
		case 4:
			return "Long";
		case 5:
			return "Float";
		case 6:
			return "Double";
		case 7: 
			return "Byte[]";
		case 8:
			return "String";
		case 9:
			return "List";
		case 10:
			return "Compound";
		case 11:
			return "Int[]";
		case 12:
			return "Edit";
		case 13:
			return "Delete";
		case 14:
			return "Copy";
		case 15:
			return "Cut";
		case 16:
			return "Paste";
		default:
			return "Unknown";
		}
	}
}
