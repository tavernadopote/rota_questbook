package com.rotaquestbook.data;

import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Interface base para exigências/requisitos de uma missão.
 */
public interface QuestRequirement {

    String getType();

    String getTitle();

    int getTargetAmount();

    int getCurrentAmount(ServerPlayer player);

    boolean isSatisfied(ServerPlayer player);

    JsonObject toJson();

    /**
     * Requisito de Coleta ou Posse de Itens.
     */
    record ItemRequirement(ResourceLocation itemId, int count) implements QuestRequirement {
        @Override
        public String getType() {
            return "item";
        }

        @Override
        public String getTitle() {
            var item = BuiltInRegistries.ITEM.get(itemId);
            return item.getDescription().getString();
        }

        @Override
        public int getTargetAmount() {
            return count;
        }

        @Override
        public int getCurrentAmount(ServerPlayer player) {
            if (player == null) return 0;
            int found = 0;
            for (ItemStack stack : player.getInventory().items) {
                if (!stack.isEmpty() && BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(itemId)) {
                    found += stack.getCount();
                }
            }
            return found;
        }

        @Override
        public boolean isSatisfied(ServerPlayer player) {
            return getCurrentAmount(player) >= count;
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
     * Requisito de Abate de Entidades / Mobs.
     */
    record KillRequirement(ResourceLocation entityTypeId, int count) implements QuestRequirement {
        @Override
        public String getType() {
            return "kill";
        }

        @Override
        public String getTitle() {
            var entity = BuiltInRegistries.ENTITY_TYPE.get(entityTypeId);
            return entity.getDescription().getString();
        }

        @Override
        public int getTargetAmount() {
            return count;
        }

        @Override
        public int getCurrentAmount(ServerPlayer player) {
            // Rastreia pelo progresso registrado do jogador
            return 0;
        }

        @Override
        public boolean isSatisfied(ServerPlayer player) {
            return false;
        }

        @Override
        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("type", "kill");
            json.addProperty("entity", entityTypeId.toString());
            json.addProperty("count", count);
            return json;
        }
    }

    /**
     * Requisito de Exploração de Dimensão, Bioma ou Estrutura.
     */
    record ExploreRequirement(String exploreType, ResourceLocation targetId) implements QuestRequirement {
        @Override
        public String getType() {
            return "explore";
        }

        @Override
        public String getTitle() {
            return targetId.getPath().replace('_', ' ');
        }

        @Override
        public int getTargetAmount() {
            return 1;
        }

        @Override
        public int getCurrentAmount(ServerPlayer player) {
            return isSatisfied(player) ? 1 : 0;
        }

        @Override
        public boolean isSatisfied(ServerPlayer player) {
            if (player == null) return false;
            if ("dimension".equalsIgnoreCase(exploreType)) {
                return player.level().dimension().location().equals(targetId);
            }
            return false;
        }

        @Override
        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("type", "explore");
            json.addProperty("explore_type", exploreType);
            json.addProperty("target", targetId.toString());
            return json;
        }
    }

    /**
     * Requisito baseado em Conquistas / Avanços do Minecraft.
     */
    record AdvancementRequirement(ResourceLocation advancementId, String description) implements QuestRequirement {
        @Override
        public String getType() {
            return "advancement";
        }

        @Override
        public String getTitle() {
            return description != null && !description.isBlank() ? description : "Completar desafio";
        }

        @Override
        public int getTargetAmount() {
            return 1;
        }

        @Override
        public int getCurrentAmount(ServerPlayer player) {
            if (player == null || player.server == null) return 0;
            var adv = player.server.getAdvancements().get(advancementId);
            if (adv != null && player.getAdvancements().getOrStartProgress(adv).isDone()) {
                return 1;
            }
            return 0;
        }

        @Override
        public boolean isSatisfied(ServerPlayer player) {
            return getCurrentAmount(player) >= 1;
        }

        @Override
        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("type", "advancement");
            json.addProperty("advancement", advancementId.toString());
            json.addProperty("description", description);
            return json;
        }
    }
}
