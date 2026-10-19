package com.rotaquestbook.client;

import com.rotacore.api.RotaModule;
import com.rotaquestbook.client.gui.RotaQuestbookConfigScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Módulo de integração do Rota Questbook com o hub universal Rota Core.
 */
public class RotaQuestbookModule implements RotaModule {

    @Override
    public String getId() {
        return "rota_questbook";
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Rota Questbook");
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public Component getDescription() {
        return Component.literal("Livro diegético de missões, lore e progressão com interface imersiva de página dupla.");
    }

    @Override
    public Screen createConfigScreen(Screen parent) {
        return new RotaQuestbookConfigScreen(parent);
    }

    @Override
    public int getOrder() {
        return 20;
    }
}
