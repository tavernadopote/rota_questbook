package com.rotaquestbook.data;

import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.List;

/**
 * Agrupador lógico de missões (Capítulo do Livro).
 */
public record Chapter(
        ResourceLocation id,
        String title,
        ResourceLocation icon,
        List<ResourceLocation> quests
) {
    public Chapter {
        quests = quests == null ? Collections.emptyList() : List.copyOf(quests);
    }
}
