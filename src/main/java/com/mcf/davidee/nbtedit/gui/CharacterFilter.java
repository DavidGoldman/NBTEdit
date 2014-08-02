package com.mcf.davidee.nbtedit.gui;

import net.minecraft.util.ChatAllowedCharacters;

import com.mcf.davidee.nbtedit.NBTStringHelper;

public class CharacterFilter {
	public static String filerAllowedCharacters(String str, boolean section) {
        StringBuilder sb = new StringBuilder();
        char[] arr = str.toCharArray();
        int length = arr.length;

        for (int i = 0; i < length; ++i) {
            char c = arr[i];
            if (ChatAllowedCharacters.isAllowedCharacter(c) || (section && (c == NBTStringHelper.SECTION_SIGN || c == '\n')))
                sb.append(c);
        }

        return sb.toString();
    }
}
