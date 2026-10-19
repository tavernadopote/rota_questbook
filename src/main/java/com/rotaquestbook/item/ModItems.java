package com.rotaquestbook.item;

import com.rotaquestbook.RotaQuestbook;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registro de itens do Rota Questbook para NeoForge 1.21.1.
 */
public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(
            Registries.ITEM,
            RotaQuestbook.MOD_ID
    );

    public static final DeferredHolder<Item, QuestbookItem> QUESTBOOK = ITEMS.register(
            "questbook",
            () -> new QuestbookItem(new Item.Properties().stacksTo(1))
    );

    @SubscribeEvent
    public static void onBuildCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(QUESTBOOK.get());
        }
    }
}
