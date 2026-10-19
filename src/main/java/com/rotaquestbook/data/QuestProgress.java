package com.rotaquestbook.data;

import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Estado individual de progresso de uma missão para um jogador.
 */
public class QuestProgress {
    private QuestStatus status;
    private final Map<Integer, Integer> requirementProgress;

    public QuestProgress(QuestStatus status) {
        this.status = status;
        this.requirementProgress = new HashMap<>();
    }

    public QuestStatus getStatus() {
        return status;
    }

    public void setStatus(QuestStatus status) {
        this.status = status;
    }

    public int getRequirementProgress(int index) {
        return requirementProgress.getOrDefault(index, 0);
    }

    public void setRequirementProgress(int index, int amount) {
        requirementProgress.put(index, amount);
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("status", status.name());
        JsonObject reqs = new JsonObject();
        for (var entry : requirementProgress.entrySet()) {
            reqs.addProperty(String.valueOf(entry.getKey()), entry.getValue());
        }
        json.add("requirements", reqs);
        return json;
    }

    public static QuestProgress fromJson(JsonObject json) {
        QuestStatus st = QuestStatus.LOCKED;
        if (json.has("status")) {
            try {
                st = QuestStatus.valueOf(json.get("status").getAsString());
            } catch (Exception ignored) {}
        }
        QuestProgress progress = new QuestProgress(st);
        if (json.has("requirements") && json.get("requirements").isJsonObject()) {
            JsonObject reqs = json.getAsJsonObject("requirements");
            for (String k : reqs.keySet()) {
                try {
                    int idx = Integer.parseInt(k);
                    progress.setRequirementProgress(idx, reqs.get(k).getAsInt());
                } catch (Exception ignored) {}
            }
        }
        return progress;
    }
}
