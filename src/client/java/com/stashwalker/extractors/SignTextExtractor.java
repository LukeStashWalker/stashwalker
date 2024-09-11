package com.stashwalker.extractors;

import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.text.Text;

public class SignTextExtractor {

    public static String getSignText (SignBlockEntity signEntity) {

        StringBuilder signText = new StringBuilder();

        // Get front text
        for (int i = 0; i < 4; i++) {

            Text frontLine = signEntity.getFrontText().getMessage(i, false);
            signText.append(frontLine.getString()).append("\n");
        }

        // Get back text (if it exists)
        for (int i = 0; i < 4; i++) {

            Text backLine = signEntity.getBackText().getMessage(i, false);
            signText.append(backLine.getString()).append("\n");
        }

        return signText.toString().trim();
    }
}