package com.rotaquestbook.client.gui;

import com.rotaquestbook.config.QuestbookConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Tela de configurações do Rota Questbook integrada ao Rota Mods Hub e ao menu nativo de Mods do NeoForge.
 */
public class RotaQuestbookConfigScreen extends Screen {

    private final Screen parentScreen;
    private String hoveredDescription = "";

    public RotaQuestbookConfigScreen(Screen parentScreen) {
        super(Component.literal("Rota Questbook - Configurações"));
        this.parentScreen = parentScreen;
    }

    @Override
    protected void init() {
        QuestbookConfig config = QuestbookConfig.get();

        int btnWidth = 200;
        int btnHeight = 24;
        int gapX = 14;
        int gapY = 12;

        int totalWidth = (btnWidth * 2) + gapX;
        int startX = (this.width - totalWidth) / 2;
        int startY = Math.max(50, this.height / 2 - 60);

        // Coluna 1: Experiência e Interface
        int col1X = startX;

        // 1. Substituir Conquistas
        this.addRenderableWidget(CycleButton.onOffBuilder(config.replaceVanillaAdvancementsScreen)
                .create(col1X, startY, btnWidth, btnHeight, Component.literal("Substituir Conquistas (L)"),
                        (btn, val) -> config.replaceVanillaAdvancementsScreen = val));

        // 2. Som de Folhear
        this.addRenderableWidget(CycleButton.onOffBuilder(config.enablePageFlipSound)
                .create(col1X, startY + (btnHeight + gapY), btnWidth, btnHeight, Component.literal("Efeito Sonoro de Folhear"),
                        (btn, val) -> config.enablePageFlipSound = val));

        // 3. Notificações Toast
        this.addRenderableWidget(CycleButton.onOffBuilder(config.showToastOnQuestCompletion)
                .create(col1X, startY + 2 * (btnHeight + gapY), btnWidth, btnHeight, Component.literal("Notificações na Tela"),
                        (btn, val) -> config.showToastOnQuestCompletion = val));

        // Coluna 2: Progressão e Importação
        int col2X = startX + btnWidth + gapX;

        // 4. Entregar Livro ao Entrar
        this.addRenderableWidget(CycleButton.onOffBuilder(config.giveBookOnFirstJoin)
                .create(col2X, startY, btnWidth, btnHeight, Component.literal("Entregar Livro ao Entrar"),
                        (btn, val) -> config.giveBookOnFirstJoin = val));

        // 5. Importar Conquistas Vanilla
        this.addRenderableWidget(CycleButton.onOffBuilder(config.importVanillaAdvancements)
                .create(col2X, startY + (btnHeight + gapY), btnWidth, btnHeight, Component.literal("Importar Conquistas Vanilla"),
                        (btn, val) -> config.importVanillaAdvancements = val));

        // 6. Importar Conquistas de Mods
        this.addRenderableWidget(CycleButton.onOffBuilder(config.importModAdvancements)
                .create(col2X, startY + 2 * (btnHeight + gapY), btnWidth, btnHeight, Component.literal("Importar Conquistas de Mods"),
                        (btn, val) -> config.importModAdvancements = val));

        // Botão Concluído no rodapé
        int doneWidth = 200;
        int doneX = (this.width - doneWidth) / 2;
        int doneY = this.height - 34;

        this.addRenderableWidget(Button.builder(Component.literal("Concluído"), b -> {
            QuestbookConfig.save();
            this.minecraft.setScreen(this.parentScreen);
        }).bounds(doneX, doneY, doneWidth, 22).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        // Título e subtítulo
        graphics.drawCenteredString(this.font, "Rota Questbook", this.width / 2, 16, 0xFFFFAA00);
        graphics.drawCenteredString(this.font, "Menu de Configurações - Personalize a experiência visual e de progressão", this.width / 2, 29, 0xFFAAAAAA);

        detectHoveredDescription(mouseX, mouseY);

        if (!hoveredDescription.isEmpty()) {
            int barY = this.height - 56;
            graphics.fill(this.width / 2 - 250, barY - 2, this.width / 2 + 250, barY + 12, 0x88000000);
            graphics.drawCenteredString(this.font, hoveredDescription, this.width / 2, barY, 0xFFFFFF);
        }
    }

    private void detectHoveredDescription(int mouseX, int mouseY) {
        int btnWidth = 200;
        int btnHeight = 24;
        int gapX = 14;
        int gapY = 12;
        int totalWidth = (btnWidth * 2) + gapX;
        int startX = (this.width - totalWidth) / 2;
        int startY = Math.max(50, this.height / 2 - 60);

        int col1X = startX;
        int col2X = startX + btnWidth + gapX;

        if (isHovering(mouseX, mouseY, col1X, startY, btnWidth, btnHeight)) {
            this.hoveredDescription = "Abre o Rota Questbook ao apertar a tecla 'L' ou clicar em Conquistas.";
        } else if (isHovering(mouseX, mouseY, col1X, startY + (btnHeight + gapY), btnWidth, btnHeight)) {
            this.hoveredDescription = "Toca som suave de papel ao avançar ou retroceder as páginas.";
        } else if (isHovering(mouseX, mouseY, col1X, startY + 2 * (btnHeight + gapY), btnWidth, btnHeight)) {
            this.hoveredDescription = "Exibe toast animado no canto superior da tela ao concluir objetivos.";
        } else if (isHovering(mouseX, mouseY, col2X, startY, btnWidth, btnHeight)) {
            this.hoveredDescription = "Coloca automaticamente um Questbook no inventário no primeiro login.";
        } else if (isHovering(mouseX, mouseY, col2X, startY + (btnHeight + gapY), btnWidth, btnHeight)) {
            this.hoveredDescription = "Converte a árvore de conquistas do Minecraft Vanilla em capítulos do livro.";
        } else if (isHovering(mouseX, mouseY, col2X, startY + 2 * (btnHeight + gapY), btnWidth, btnHeight)) {
            this.hoveredDescription = "Converte e indexa conquistas registradas por outros mods instalados.";
        } else {
            this.hoveredDescription = "";
        }
    }

    private boolean isHovering(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    @Override
    public void onClose() {
        QuestbookConfig.save();
        this.minecraft.setScreen(this.parentScreen);
    }
}
