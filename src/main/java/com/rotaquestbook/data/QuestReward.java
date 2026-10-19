package com.rotaquestbook.data;

import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Interface base para recompensas entregues ao completar uma missão.
 */
public interface QuestReward {

    String getType();

    String getTitle();

    void grant(ServerPlayer player);

    JsonObject toJson();

    /**
     * Recompensa de Itens.
     */
    record ItemReward(ResourceLocation itemId, int count) implements QuestReward {
        @Override
        public String getType() {
            return "item";
        }

        @Override
        public String getTitle() {
            var item = BuiltInRegistries.ITEM.get(itemId);
            return count + "x " + item.getDescription().getString();
        }

        @Override
        public void grant(ServerPlayer player) {
            if (player == null) return;
            var item = BuiltInRegistries.ITEM.get(itemId);
            ItemStack stack = new ItemStack(item, count);
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
        }

        @Override
        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("type", "item");
            json.addProperty("item", itemId.toString());
            json.addProperty("count", count);
            return json;
        }
    }

    /**
     * Recompensa de Experiência (XP).
     */
    record XPReward(int points, boolean levels) implements QuestReward {
        @Override
        public String getType() {
            return "xp";
        }

        @Override
        public String getTitle() {
            return levels ? (points + " Níveis de XP") : (points + " Pontos de XP");
        }

        @Override
        public void grant(ServerPlayer player) {
            if (player == null) return;
            if (levels) {
                player.giveExperienceLevels(points);
            } else {
                player.giveExperiencePoints(points);
            }
        }

        @Override
        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("type", "xp");
            json.addProperty("points", points);
            json.addProperty("levels", levels);
            return json;
        }
    }

    /**
     * Recompensa executando comando de console.
     */
    record CommandReward(String command, String displayName) implements QuestReward {
        @Override
        public String getType() {
            return "command";
        }

        @Override
        public String getTitle() {
            return displayName != null && !displayName.isBlank() ? displayName : "Recompensa Especial";
        }

        @Override
        public void grant(ServerPlayer player) {
            if (player == null || command == null || player.getServer() == null) return;
            String cmd = command.replace("@p", player.getScoreboardName())
                                .replace("@s", player.getScoreboardName());
            player.getServer().getCommands().performPrefixedCommand(
                    player.getServer().createCommandSourceStack(),
                    cmd
            );
        }

        @Override
        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("type", "command");
            json.addProperty("command", command);
            json.addProperty("display_name", displayName);
            return json;
        }
    }
}
