package com.rotaquestbook.client.event;

import com.rotaquestbook.client.ClientHelper;
import com.rotaquestbook.client.gui.RotaQuestScreen;
import com.rotaquestbook.config.QuestbookConfig;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

/**
 * Interceptador cliente que substitui transparentemente a tela vanilla de Conquistas (AdvancementsScreen)
 * pela tela do Rota Questbook quando a opção estiver ativada na configuração.
 */
@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class ClientScreenInterceptor {

    @SubscribeEvent
    public static void onScreenOpening(ScreenEvent.Opening event) {
        if (QuestbookConfig.get().replaceVanillaAdvancementsScreen) {
            if (event.getNewScreen() instanceof AdvancementsScreen) {
                // Sincroniza nós de avanço recebidos e substitui a tela
                ClientHelper.syncFromClientTree();
                event.setNewScreen(new RotaQuestScreen());
            }
        }
    }
}
