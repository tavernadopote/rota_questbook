package com.rotaquestbook.item;

import com.rotaquestbook.client.ClientHelper;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

/**
 * Item físico do Rota Questbook.
 * Ao clicar com botão direito, abre a interface imersiva em estilo de livro de duas páginas.
 */
public class QuestbookItem extends Item {

    public QuestbookItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            if (FMLEnvironment.dist == Dist.CLIENT) {
                ClientHelper.openQuestScreen();
            }
        }

        level.playSound(
                player,
                player.getX(), player.getY(), player.getZ(),
                SoundEvents.BOOK_PAGE_TURN,
                SoundSource.PLAYERS,
                1.0F, 1.0F
        );

        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }
}
