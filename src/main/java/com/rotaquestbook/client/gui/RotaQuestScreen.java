package com.rotaquestbook.client.gui;

import com.rotaquestbook.config.QuestbookConfig;
import com.rotaquestbook.data.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

/**
 * Interface gráfica principal do Rota Questbook: Livro Aberto com duas páginas.
 * - Página Esquerda: Metadados, Capítulo, Título, Requisitos e Recompensas.
 * - Página Direita: Narrativa completa, Lore e Diário da missão.
 */
public class RotaQuestScreen extends Screen {

    private final PaginationManager paginationManager = new PaginationManager();
    private int bookWidth = 340;
    private int bookHeight = 250;
    private int guiLeft;
    private int guiTop;

    private PageTurnButton prevButton;
    private PageTurnButton nextButton;
    private Button tocReturnButton;
    private Button claimButton;

    public RotaQuestScreen() {
        super(Component.translatable("gui.rota_questbook.title"));
    }

    @Override
    protected void init() {
        super.init();

        // Proporção de livro 4:3 adaptada à resolução da tela
        this.bookWidth = Math.min(this.width - 24, 380);
        this.bookHeight = (int) (this.bookWidth * 0.74);
        this.guiLeft = (this.width - this.bookWidth) / 2;
        this.guiTop = (this.height - this.bookHeight) / 2;

        this.paginationManager.rebuildPagination(QuestDataManager.get());

        // Botões táteis de avançar/voltar nas dobras inferiores da página
        int buttonSize = 28;
        this.prevButton = new PageTurnButton(
                this.guiLeft + 16,
                this.guiTop + this.bookHeight - buttonSize - 10,
                buttonSize, buttonSize, false,
                () -> {
                    this.paginationManager.prevPage();
                    playPageFlipSound();
                    updateButtonStates();
                }
        );

        this.nextButton = new PageTurnButton(
                this.guiLeft + this.bookWidth - buttonSize - 16,
                this.guiTop + this.bookHeight - buttonSize - 10,
                buttonSize, buttonSize, true,
                () -> {
                    this.paginationManager.nextPage();
                    playPageFlipSound();
                    updateButtonStates();
                }
        );

        // Botão para retornar ao Sumário a partir de qualquer página de missão
        this.tocReturnButton = Button.builder(Component.literal("Sumário"), b -> {
            this.paginationManager.jumpToToc();
            playPageFlipSound();
            updateButtonStates();
        }).bounds(this.guiLeft + (this.bookWidth / 2) - 30, this.guiTop + this.bookHeight - 22, 60, 16).build();

        // Botão de resgatar recompensa na página esquerda
        int leftPageWidth = (this.bookWidth / 2) - 32;
        this.claimButton = Button.builder(Component.literal("Resgatar Recompensa"), b -> {
            // Emite pacote ao servidor para resgatar
        }).bounds(this.guiLeft + 26, this.guiTop + this.bookHeight - 48, leftPageWidth, 18).build();

        this.addRenderableWidget(this.prevButton);
        this.addRenderableWidget(this.nextButton);
        this.addRenderableWidget(this.tocReturnButton);
        this.addRenderableWidget(this.claimButton);

        updateButtonStates();
    }

    private void updateButtonStates() {
        this.prevButton.visible = this.paginationManager.hasPrev();
        this.nextButton.visible = this.paginationManager.hasNext();
        this.tocReturnButton.visible = !this.paginationManager.isTableOfContents();
        this.claimButton.visible = !this.paginationManager.isTableOfContents();
    }

    private void playPageFlipSound() {
        if (QuestbookConfig.get().enablePageFlipSound && this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.level.playSound(
                    this.minecraft.player,
                    this.minecraft.player.blockPosition(),
                    SoundEvents.BOOK_PAGE_TURN,
                    SoundSource.PLAYERS,
                    1.0f, 1.0f
            );
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Fundo escurecido suave
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        // 1. Textura do Livro Aberto
        ResourceLocation bg = ThemeManager.getBackgroundTexture();
        guiGraphics.blit(bg, this.guiLeft, this.guiTop, 0, 0, this.bookWidth, this.bookHeight, this.bookWidth, this.bookHeight);

        // 2. Renderização do Conteúdo
        if (this.paginationManager.isTableOfContents()) {
            renderTableOfContents(guiGraphics, mouseX, mouseY);
        } else {
            ResourceLocation qId = this.paginationManager.getCurrentQuestId();
            Quest quest = qId != null ? QuestDataManager.get().getQuest(qId) : null;
            if (quest != null) {
                renderQuestSpread(guiGraphics, quest, mouseX, mouseY);
            }
        }

        // 3. Renderiza botões e widgets sobrepostos
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // Tooltips dos cantos de virar página
        if (this.prevButton.isHoveredOrFocused() && this.prevButton.visible) {
            guiGraphics.renderTooltip(this.font, Component.literal("Página Anterior"), mouseX, mouseY);
        }
        if (this.nextButton.isHoveredOrFocused() && this.nextButton.visible) {
            guiGraphics.renderTooltip(this.font, Component.literal("Próxima Página"), mouseX, mouseY);
        }
    }

    /**
     * Renderiza o Sumário (Table of Contents) distribuído entre as duas páginas.
     */
    private void renderTableOfContents(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int leftX = this.guiLeft + 26;
        int rightX = this.guiLeft + (this.bookWidth / 2) + 16;
        int startY = this.guiTop + 24;
        int colWidth = (this.bookWidth / 2) - 40;

        // Título do Sumário na página esquerda
        guiGraphics.drawString(this.font, "Sumário de Aventuras", leftX, startY, ThemeManager.COLOR_TITLE, false);
        guiGraphics.fill(leftX, startY + 11, leftX + colWidth, startY + 12, 0x443A220F);

        List<ResourceLocation> quests = this.paginationManager.getLinearQuestList();
        int spreadIdx = this.paginationManager.getTocSpreadIndex();
        int startOffset = spreadIdx * PaginationManager.ENTRIES_PER_TOC_SPREAD;

        int lineH = 18;
        for (int i = 0; i < PaginationManager.ENTRIES_PER_TOC_SPREAD; i++) {
            int qIdx = startOffset + i;
            if (qIdx >= quests.size()) break;

            ResourceLocation qId = quests.get(qIdx);
            Quest q = QuestDataManager.get().getQuest(qId);
            if (q == null) continue;

            boolean isRightPage = (i >= PaginationManager.ENTRIES_PER_TOC_PAGE);
            int curX = isRightPage ? rightX : leftX;
            int curY = startY + 18 + ((i % PaginationManager.ENTRIES_PER_TOC_PAGE) * lineH);

            // Efeito hover na linha
            boolean isHovered = mouseX >= curX && mouseX <= curX + colWidth && mouseY >= curY - 2 && mouseY <= curY + 14;
            if (isHovered) {
                guiGraphics.fill(curX - 2, curY - 2, curX + colWidth, curY + 14, 0x1A8A4500);
            }

            int textColor = isHovered ? ThemeManager.COLOR_LINK : ThemeManager.COLOR_BODY;
            String displayTitle = (qIdx + 1) + ". " + (q.secret() ? "???" : q.title());
            if (displayTitle.length() > 22) {
                displayTitle = displayTitle.substring(0, 20) + "...";
            }

            guiGraphics.drawString(this.font, displayTitle, curX, curY + 2, textColor, false);
        }
    }

    /**
     * Renderiza uma missão em página dupla:
     * - Página Esquerda: Metadados, Título, Requisitos e Recompensas.
     * - Página Direita: Narrativa completa / História / Lore da missão.
     */
    private void renderQuestSpread(GuiGraphics guiGraphics, Quest quest, int mouseX, int mouseY) {
        int leftX = this.guiLeft + 26;
        int rightX = this.guiLeft + (this.bookWidth / 2) + 16;
        int startY = this.guiTop + 24;
        int leftWidth = (this.bookWidth / 2) - 40;
        int rightWidth = (this.bookWidth / 2) - 40;

        // ==========================================
        // PÁGINA ESQUERDA: AÇÃO, OBJETIVOS & RECOMPENSAS
        // ==========================================
        Chapter chapter = QuestDataManager.get().getChapter(quest.chapterId());
        String chapterName = chapter != null ? chapter.title() : "Aventuras";
        guiGraphics.drawString(this.font, chapterName.toUpperCase(), leftX, startY, ThemeManager.COLOR_SUBTITLE, false);

        // Título da Missão
        guiGraphics.drawString(this.font, quest.title(), leftX, startY + 12, ThemeManager.COLOR_TITLE, false);
        guiGraphics.fill(leftX, startY + 24, leftX + leftWidth, startY + 25, 0x443A220F);

        // Dependência
        int curY = startY + 28;
        if (!quest.dependencies().isEmpty()) {
            ResourceLocation depId = quest.dependencies().get(0);
            Quest depQuest = QuestDataManager.get().getQuest(depId);
            String depName = depQuest != null ? depQuest.title() : depId.getPath();
            guiGraphics.drawString(this.font, "Requer: " + depName, leftX, curY, ThemeManager.COLOR_LINK, false);
            curY += 14;
        }

        // Seção de Objetivos
        guiGraphics.drawString(this.font, "OBJETIVOS:", leftX, curY, ThemeManager.COLOR_TITLE, false);
        curY += 12;

        if (quest.requirements().isEmpty()) {
            guiGraphics.drawString(this.font, "• Explorar e descobrir", leftX + 4, curY, ThemeManager.COLOR_BODY, false);
            curY += 12;
        } else {
            for (QuestRequirement req : quest.requirements()) {
                String reqText = "☐ " + req.getTitle() + " (" + req.getTargetAmount() + ")";
                guiGraphics.drawString(this.font, reqText, leftX + 4, curY, ThemeManager.COLOR_BODY, false);
                curY += 12;
            }
        }

        // Seção de Recompensas
        curY += 6;
        guiGraphics.drawString(this.font, "RECOMPENSAS:", leftX, curY, ThemeManager.COLOR_TITLE, false);
        curY += 12;

        if (quest.rewards().isEmpty()) {
            guiGraphics.drawString(this.font, "• O prestígio da descoberta", leftX + 4, curY, ThemeManager.COLOR_MUTED, false);
        } else {
            for (QuestReward rw : quest.rewards()) {
                guiGraphics.drawString(this.font, "• " + rw.getTitle(), leftX + 4, curY, ThemeManager.COLOR_SUCCESS, false);
                curY += 12;
            }
        }

        // ==========================================
        // PÁGINA DIREITA: NARRATIVA & LORE
        // ==========================================
        guiGraphics.drawString(this.font, "CRÔNICA & HISTÓRIA", rightX, startY, ThemeManager.COLOR_TITLE, false);
        guiGraphics.fill(rightX, startY + 11, rightX + rightWidth, startY + 12, 0x443A220F);

        String desc = quest.description();
        if (desc == null || desc.isBlank()) {
            desc = "Nenhum relato adicional foi inscrito para esta jornada nos anais do tempo.";
        }

        List<FormattedCharSequence> lines = this.font.split(Component.literal(desc), rightWidth);
        int descY = startY + 18;
        for (FormattedCharSequence line : lines) {
            if (descY > this.guiTop + this.bookHeight - 35) {
                guiGraphics.drawString(this.font, "...", rightX, descY, ThemeManager.COLOR_MUTED, false);
                break;
            }
            guiGraphics.drawString(this.font, line, rightX, descY, ThemeManager.COLOR_BODY, false);
            descY += 10;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.paginationManager.isTableOfContents() && button == 0) {
            int leftX = this.guiLeft + 26;
            int rightX = this.guiLeft + (this.bookWidth / 2) + 16;
            int startY = this.guiTop + 24;
            int colWidth = (this.bookWidth / 2) - 40;
            int lineH = 18;

            List<ResourceLocation> quests = this.paginationManager.getLinearQuestList();
            int spreadIdx = this.paginationManager.getTocSpreadIndex();
            int startOffset = spreadIdx * PaginationManager.ENTRIES_PER_TOC_SPREAD;

            for (int i = 0; i < PaginationManager.ENTRIES_PER_TOC_SPREAD; i++) {
                int qIdx = startOffset + i;
                if (qIdx >= quests.size()) break;

                boolean isRightPage = (i >= PaginationManager.ENTRIES_PER_TOC_PAGE);
                int curX = isRightPage ? rightX : leftX;
                int curY = startY + 18 + ((i % PaginationManager.ENTRIES_PER_TOC_PAGE) * lineH);

                if (mouseX >= curX && mouseX <= curX + colWidth && mouseY >= curY - 2 && mouseY <= curY + 14) {
                    this.paginationManager.jumpToQuest(quests.get(qIdx));
                    playPageFlipSound();
                    updateButtonStates();
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
