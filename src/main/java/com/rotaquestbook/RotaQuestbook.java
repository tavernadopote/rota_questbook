package com.rotaquestbook;

import com.rotaquestbook.config.QuestbookConfig;
import com.rotaquestbook.data.QuestDataManager;
import com.rotaquestbook.item.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mod Rota Questbook: Livro diegético de missões com páginas duplas,
 * integração nativa de avanços e compatibilidade universal com mods.
 */
@Mod(RotaQuestbook.MOD_ID)
public class RotaQuestbook {
    public static final String MOD_ID = "rota_questbook";
    public static final Logger LOGGER = LoggerFactory.getLogger("RotaQuestbook");

    public RotaQuestbook(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Inicializando Rota Questbook (Minecraft 1.21.1)...");

        // Carrega configurações a partir de config/rota_questbook.json
        QuestbookConfig.load();

        // Registra itens e eventos de abas criativas no bus do mod
        ModItems.ITEMS.register(modEventBus);
        modEventBus.addListener(ModItems::onBuildCreativeTab);

        // Registra este ouvinte no bus global do NeoForge
        NeoForge.EVENT_BUS.register(this);

        // Registra integrações de cliente e Rota Core Hub
        if (FMLEnvironment.dist == Dist.CLIENT) {
            com.rotaquestbook.client.RotaQuestbookClient.registerClient(modContainer);
        }
    }

    @SubscribeEvent
    public void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(QuestDataManager.get());
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        QuestDataManager.get().syncAdvancements(event.getServer().getAdvancements());
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (QuestbookConfig.get().giveBookOnFirstJoin && event.getEntity() instanceof ServerPlayer player) {
            String tag = "rota_questbook_received_book";
            if (!player.getTags().contains(tag)) {
                player.addTag(tag);
                player.addItem(new ItemStack(ModItems.QUESTBOOK.get()));
            }
        }
    }
}
