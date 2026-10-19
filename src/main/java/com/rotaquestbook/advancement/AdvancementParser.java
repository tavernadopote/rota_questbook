package com.rotaquestbook.advancement;

import com.rotaquestbook.config.QuestbookConfig;
import com.rotaquestbook.data.Chapter;
import com.rotaquestbook.data.Quest;
import com.rotaquestbook.data.QuestRequirement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerAdvancementManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Utilitário inteligente que lê os Advancements do Minecraft Vanilla e de outros mods instalados,
 * convertendo-os automaticamente em Capítulos e Missões ordenados topologicamente.
 */
public class AdvancementParser {
    private static final Logger LOGGER = LoggerFactory.getLogger("rota_questbook/advancements");

    public record ParseResult(List<Chapter> chapters, Map<ResourceLocation, Quest> quests) {}

    public static ParseResult parseAll(ServerAdvancementManager manager, QuestbookConfig config) {
        if (manager == null) {
            return new ParseResult(Collections.emptyList(), Collections.emptyMap());
        }
        return parseAll(manager.getAllAdvancements(), config);
    }

    public static ParseResult parseAll(Collection<AdvancementHolder> allAdvancements, QuestbookConfig config) {
        if (allAdvancements == null || allAdvancements.isEmpty()) {
            return new ParseResult(Collections.emptyList(), Collections.emptyMap());
        }
        Map<ResourceLocation, AdvancementHolder> advMap = new HashMap<>();
        List<AdvancementHolder> roots = new ArrayList<>();

        for (AdvancementHolder holder : allAdvancements) {
            advMap.put(holder.id(), holder);
            if (holder.value().isRoot() && holder.value().display().isPresent()) {
                roots.add(holder);
            }
        }

        List<Chapter> chapters = new ArrayList<>();
        Map<ResourceLocation, Quest> quests = new LinkedHashMap<>();

        // Ordena raízes alfabeticamente ou com minecraft no topo
        roots.sort((r1, r2) -> {
            boolean m1 = r1.id().getNamespace().equals("minecraft");
            boolean m2 = r2.id().getNamespace().equals("minecraft");
            if (m1 && !m2) return -1;
            if (!m1 && m2) return 1;
            return r1.id().compareTo(r2.id());
        });

        for (AdvancementHolder root : roots) {
            String ns = root.id().getNamespace();
            boolean isVanilla = ns.equals("minecraft");

            if (isVanilla && !config.importVanillaAdvancements) continue;
            if (!isVanilla && !config.importModAdvancements) continue;

            DisplayInfo rootDisplay = root.value().display().orElse(null);
            if (rootDisplay == null) continue;

            ResourceLocation chapterId = root.id();
            String chapterTitle = rootDisplay.getTitle().getString();
            ResourceLocation chapterIcon = BuiltInRegistries.ITEM.getKey(rootDisplay.getIcon().getItem());

            List<ResourceLocation> chapterQuestIds = new ArrayList<>();

            // Ordenação topológica em amplitude (BFS) a partir da raiz
            Queue<AdvancementHolder> queue = new ArrayDeque<>();
            Set<ResourceLocation> visited = new HashSet<>();

            queue.add(root);
            visited.add(root.id());

            while (!queue.isEmpty()) {
                AdvancementHolder current = queue.poll();

                Optional<DisplayInfo> displayOpt = current.value().display();
                if (displayOpt.isPresent()) {
                    DisplayInfo display = displayOpt.get();

                    List<ResourceLocation> dependencies = new ArrayList<>();
                    current.value().parent().ifPresent(dependencies::add);

                    ResourceLocation iconLoc = BuiltInRegistries.ITEM.getKey(display.getIcon().getItem());

                    // Converte o objetivo da conquista em requisito direto e legível
                    List<QuestRequirement> reqs = new ArrayList<>();
                    reqs.add(new QuestRequirement.AdvancementRequirement(current.id(), display.getTitle().getString()));

                    Quest quest = new Quest(
                            current.id(),
                            display.getTitle().getString(),
                            iconLoc,
                            display.getDescription().getString(),
                            chapterId,
                            dependencies,
                            reqs,
                            Collections.emptyList(),
                            display.isHidden()
                    );

                    quests.put(quest.id(), quest);
                    chapterQuestIds.add(quest.id());
                }

                // Encontra os filhos imediatos deste nó
                for (AdvancementHolder candidate : allAdvancements) {
                    if (candidate.value().parent().isPresent() && candidate.value().parent().get().equals(current.id())) {
                        if (visited.add(candidate.id())) {
                            queue.add(candidate);
                        }
                    }
                }
            }

            if (!chapterQuestIds.isEmpty()) {
                Chapter chapter = new Chapter(chapterId, chapterTitle, chapterIcon, chapterQuestIds);
                chapters.add(chapter);
            }
        }

        LOGGER.info("Advancements importados com sucesso: {} capítulos e {} missões ordenadas.",
                chapters.size(), quests.size());

        return new ParseResult(chapters, quests);
    }
}
