package com.rotaquestbook.client;

import com.rotacore.api.RotaCoreRegistry;
import com.rotaquestbook.client.gui.RotaQuestbookConfigScreen;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * Ponto de entrada das funcionalidades exclusivas do lado cliente do Rota Questbook.
 */
public class RotaQuestbookClient {

    public static void registerClient(ModContainer modContainer) {
        // Registra o módulo no ecossistema central da suíte Rota Core
        RotaCoreRegistry.register(new RotaQuestbookModule());

        // Registra a tela de configuração no botão nativo "Config" da lista de mods do NeoForge
        modContainer.registerExtensionPoint(
                IConfigScreenFactory.class,
                (container, screen) -> new RotaQuestbookConfigScreen(screen)
        );
    }
}
