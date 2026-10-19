package com.rotaquestbook.client.gui;

import com.rotaquestbook.data.Chapter;
import com.rotaquestbook.data.Quest;
import com.rotaquestbook.data.QuestDataManager;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * Gerenciador de paginação do livro aberto (páginas duplas/spreads).
 * - As primeiras páginas formam o Sumário (Índice).
 * - Cada missão subsequente ocupa uma página dupla completa:
 *   - Página Esquerda: Metadados, Título, Objetivos, Requisitos e Recompensas.
 *   - Página Direita: Narrativa, Lore e História detalhada da missão.
 */
public class PaginationManager {

    public static final int ENTRIES_PER_TOC_PAGE = 8;
    public static final int ENTRIES_PER_TOC_SPREAD = ENTRIES_PER_TOC_PAGE * 2; // 16 por página dupla de sumário

    private int currentSpread = 0;
    private final List<ResourceLocation> linearQuestList = new ArrayList<>();
    private int tocSpreadCount = 1;

    public void rebuildPagination(QuestDataManager dataManager) {
        linearQuestList.clear();

        for (Chapter chapter : dataManager.getOrderedChapters()) {
            for (ResourceLocation qId : chapter.quests()) {
                Quest q = dataManager.getQuest(qId);
                if (q != null) {
                    linearQuestList.add(qId);
                }
            }
        }

        int totalQuests = linearQuestList.size();
        tocSpreadCount = Math.max(1, (int) Math.ceil((double) totalQuests / ENTRIES_PER_TOC_SPREAD));

        if (currentSpread >= getTotalSpreads()) {
            currentSpread = Math.max(0, getTotalSpreads() - 1);
        }
    }

    public int getTotalSpreads() {
        return tocSpreadCount + linearQuestList.size();
    }

    public int getCurrentSpread() {
        return currentSpread;
    }

    public boolean isTableOfContents() {
        return currentSpread < tocSpreadCount;
    }

    public int getTocSpreadIndex() {
        return currentSpread;
    }

    public ResourceLocation getCurrentQuestId() {
        if (isTableOfContents()) return null;
        int questIndex = currentSpread - tocSpreadCount;
        if (questIndex >= 0 && questIndex < linearQuestList.size()) {
            return linearQuestList.get(questIndex);
        }
        return null;
    }

    public boolean hasNext() {
        return currentSpread < getTotalSpreads() - 1;
    }

    public boolean hasPrev() {
        return currentSpread > 0;
    }

    public void nextPage() {
        if (hasNext()) {
            currentSpread++;
        }
    }

    public void prevPage() {
        if (hasPrev()) {
            currentSpread--;
        }
    }

    public void jumpToToc() {
        currentSpread = 0;
    }

    public void jumpToQuest(ResourceLocation questId) {
        int idx = linearQuestList.indexOf(questId);
        if (idx >= 0) {
            currentSpread = tocSpreadCount + idx;
        }
    }

    public List<ResourceLocation> getLinearQuestList() {
        return linearQuestList;
    }
}
