package com.rotaquestbook.client;

import com.rotaquestbook.client.gui.RotaQuestScreen;
import com.rotaquestbook.data.QuestDataManager;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

/**
 * Utilitário isolado do lado cliente para evitar vazamento de dependências no servidor dedicado.
 */
public class ClientHelper {

    /**
     * Sincroniza a árvore de avanços do cliente com o repositório de missões e abre a tela.
     */
    public static void openQuestScreen() {
        syncFromClientTree();
        Minecraft.getInstance().setScreen(new RotaQuestScreen());
    }

    /**
     * Lê os avanços já recebidos do servidor pelo cliente e os sincroniza com o QuestDataManager.
     */
    public static void syncFromClientTree() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() != null) {
            var clientAdv = mc.getConnection().getAdvancements();
            if (clientAdv != null && clientAdv.getTree() != null) {
                List<AdvancementHolder> holders = new ArrayList<>();
                for (var node : clientAdv.getTree().nodes()) {
                    holders.add(node.holder());
                }
                QuestDataManager.get().syncAdvancements(holders);
            }
        }
    }
}
