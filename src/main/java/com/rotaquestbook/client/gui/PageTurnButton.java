package com.rotaquestbook.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

/**
 * Botão tátil invisível/interativo posicionado nos cantos inferiores dobrados da página para folhear.
 */
public class PageTurnButton extends AbstractButton {
    private final boolean isNext;
    private final Runnable onPressAction;

    public PageTurnButton(int x, int y, int width, int height, boolean isNext, Runnable onPressAction) {
        super(x, y, width, height, Component.translatable(isNext ? "gui.rota_questbook.next_page" : "gui.rota_questbook.prev_page"));
        this.isNext = isNext;
        this.onPressAction = onPressAction;
    }

    @Override
    public void onPress() {
        if (onPressAction != null) {
            onPressAction.run();
        }
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Renderização suave: quando com o mouse em cima, desenha um brilho sutil dourado no canto dobrado
        if (this.isHoveredOrFocused()) {
            guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0x22FFE082);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }
}
