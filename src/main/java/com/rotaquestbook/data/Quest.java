package com.rotaquestbook.data;

import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.List;

/**
 * Representação de uma missão carregada via Datapack ou gerada de Advancements.
 */
public record Quest(
        ResourceLocation id,
        String title,
        ResourceLocation icon,
        String description,
        ResourceLocation chapterId,
        List<ResourceLocation> dependencies,
        List<QuestRequirement> requirements,
        List<QuestReward> rewards,
        boolean secret
) {
    public Quest {
        dependencies = dependencies == null ? Collections.emptyList() : List.copyOf(dependencies);
        requirements = requirements == null ? Collections.emptyList() : List.copyOf(requirements);
        rewards = rewards == null ? Collections.emptyList() : List.copyOf(rewards);
    }
}
