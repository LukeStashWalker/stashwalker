package com.stashwalker.utils;

import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class FeatureUtil {
    
    public static Text createStyledTextForFeature (String featureName, boolean featureToggle) {

                return Text.empty()
                        .append(Text.literal("[")
                                .setStyle(Style.EMPTY.withColor(Formatting.GRAY)))
                        .append(Text.literal("Stashwalker, ")
                                .setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY)))
                        .append(Text.literal(featureName)
                                .setStyle(Style.EMPTY.withColor(Formatting.BLUE)))
                        .append(Text.literal("]:")
                                .setStyle(Style.EMPTY.withColor(Formatting.GRAY)))
                        .append(Text.literal(featureToggle ? " enabled" : " disabled")
                                .setStyle(Style.EMPTY.withColor(featureToggle ? Formatting.GREEN : Formatting.RED)));
    }
}
