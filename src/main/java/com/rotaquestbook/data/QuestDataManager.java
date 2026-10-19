package com.rotaquestbook.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rotaquestbook.advancement.AdvancementParser;
import com.rotaquestbook.config.QuestbookConfig;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Gerenciador central de dados de missões e capítulos.
 * Carrega dados JSON de Datapacks (data/<namespace>/rota_quests/) e mescla com Advancements importados.
 */
public class QuestDataManager extends SimpleJsonResourceReloadListener {
    private static final Logger LOGGER = LoggerFactory.getLogger("rota_questbook/data_manager");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final QuestDataManager INSTANCE = new QuestDataManager();

    private final Map<ResourceLocation, Quest> quests = new LinkedHashMap<>();
    private final Map<ResourceLocation, Chapter> chapters = new LinkedHashMap<>();
    private final List<Chapter> orderedChapters = new ArrayList<>();

    public QuestDataManager() {
        super(GSON, "rota_quests");
    }

    public static QuestDataManager get() {
        return INSTANCE;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsonMap, ResourceManager resourceManager, ProfilerFiller profiler) {
        LOGGER.info("Carregando missões customizadas via Datapacks ({} arquivos encontrados)...", jsonMap.size());
        quests.clear();
        chapters.clear();
        orderedChapters.clear();

        for (Map.Entry<ResourceLocation, JsonElement> entry : jsonMap.entrySet()) {
            ResourceLocation fileId = entry.getKey();
            JsonElement el = entry.getValue();
            if (!el.isJsonObject()) continue;
            JsonObject obj = el.getAsJsonObject();

            try {
                // Checa se o arquivo é um Capítulo ou uma Quest
                if (obj.has("chapter_title") || obj.has("is_chapter")) {
                    parseChapter(fileId, obj);
                } else {
                    parseQuest(fileId, obj);
                }
            } catch (Exception e) {
                LOGGER.error("Erro ao analisar arquivo de missão {}", fileId, e);
            }
        }
    }

    private void parseChapter(ResourceLocation id, JsonObject obj) {
        String title = obj.has("chapter_title") ? obj.get("chapter_title").getAsString() : id.getPath();
        ResourceLocation icon = obj.has("icon") ? ResourceLocation.parse(obj.get("icon").getAsString()) : ResourceLocation.withDefaultNamespace("book");
        List<ResourceLocation> questIds = new ArrayList<>();
        if (obj.has("quests") && obj.get("quests").isJsonArray()) {
            for (JsonElement qe : obj.getAsJsonArray("quests")) {
                questIds.add(ResourceLocation.parse(qe.getAsString()));
            }
        }
        Chapter chapter = new Chapter(id, title, icon, questIds);
        chapters.put(id, chapter);
        orderedChapters.add(chapter);
    }

    private void parseQuest(ResourceLocation id, JsonObject obj) {
        String title = obj.has("title") ? obj.get("title").getAsString() : id.getPath();
        ResourceLocation icon = obj.has("icon") ? ResourceLocation.parse(obj.get("icon").getAsString()) : ResourceLocation.withDefaultNamespace("book");
        String description = obj.has("description") ? obj.get("description").getAsString() : "";
        ResourceLocation chapterId = obj.has("chapter") ? ResourceLocation.parse(obj.get("chapter").getAsString()) : ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "main");

        List<ResourceLocation> deps = new ArrayList<>();
        if (obj.has("dependencies") && obj.get("dependencies").isJsonArray()) {
            for (JsonElement de : obj.getAsJsonArray("dependencies")) {
                deps.add(ResourceLocation.parse(de.getAsString()));
            }
        }

        List<QuestRequirement> reqs = new ArrayList<>();
        if (obj.has("requirements") && obj.get("requirements").isJsonArray()) {
            for (JsonElement re : obj.getAsJsonArray("requirements")) {
                if (re.isJsonObject()) {
                    JsonObject rObj = re.getAsJsonObject();
                    String type = rObj.has("type") ? rObj.get("type").getAsString() : "item";
                    if ("item".equalsIgnoreCase(type)) {
                        ResourceLocation itemLoc = ResourceLocation.parse(rObj.get("item").getAsString());
                        int count = rObj.has("count") ? rObj.get("count").getAsInt() : 1;
                        reqs.add(new QuestRequirement.ItemRequirement(itemLoc, count));
                    } else if ("kill".equalsIgnoreCase(type)) {
                        ResourceLocation entityLoc = ResourceLocation.parse(rObj.get("entity").getAsString());
                        int count = rObj.has("count") ? rObj.get("count").getAsInt() : 1;
                        reqs.add(new QuestRequirement.KillRequirement(entityLoc, count));
                    }
                }
            }
        }

        List<QuestReward> rewards = new ArrayList<>();
        if (obj.has("rewards") && obj.get("rewards").isJsonArray()) {
            for (JsonElement rwe : obj.getAsJsonArray("rewards")) {
                if (rwe.isJsonObject()) {
                    JsonObject rwObj = rwe.getAsJsonObject();
                    String type = rwObj.has("type") ? rwObj.get("type").getAsString() : "item";
                    if ("item".equalsIgnoreCase(type)) {
                        ResourceLocation itemLoc = ResourceLocation.parse(rwObj.get("item").getAsString());
                        int count = rwObj.has("count") ? rwObj.get("count").getAsInt() : 1;
                        rewards.add(new QuestReward.ItemReward(itemLoc, count));
                    } else if ("xp".equalsIgnoreCase(type)) {
                        int points = rwObj.has("points") ? rwObj.get("points").getAsInt() : 50;
                        boolean levels = rwObj.has("levels") && rwObj.get("levels").getAsBoolean();
                        rewards.add(new QuestReward.XPReward(points, levels));
                    }
                }
            }
        }

        boolean secret = obj.has("secret") && obj.get("secret").getAsBoolean();

        Quest quest = new Quest(id, title, icon, description, chapterId, deps, reqs, rewards, secret);
        quests.put(id, quest);
    }

    /**
     * Mescla as conquistas do servidor com os dados carregados de datapacks.
     */
    public void syncAdvancements(ServerAdvancementManager manager) {
        if (manager != null) {
            syncAdvancements(manager.getAllAdvancements());
        }
    }

    /**
     * Mescla as conquistas (servidor ou cliente) com os dados carregados de datapacks.
     */
    public void syncAdvancements(Collection<AdvancementHolder> advancements) {
        if (advancements == null) return;
        QuestbookConfig config = QuestbookConfig.get();
        AdvancementParser.ParseResult parsed = AdvancementParser.parseAll(advancements, config);

        // Adiciona capítulos importados que ainda não existam
        for (Chapter c : parsed.chapters()) {
            if (!chapters.containsKey(c.id())) {
                chapters.put(c.id(), c);
                orderedChapters.add(c);
            }
        }

        // Adiciona missões importadas que ainda não existam
        for (Map.Entry<ResourceLocation, Quest> entry : parsed.quests().entrySet()) {
            if (!quests.containsKey(entry.getKey())) {
                quests.put(entry.getKey(), entry.getValue());
            }
        }

        LOGGER.info("Sincronização de missões concluída: {} capítulos no total e {} missões registradas.",
                orderedChapters.size(), quests.size());
    }

    public List<Chapter> getOrderedChapters() {
        return Collections.unmodifiableList(orderedChapters);
    }

    public Chapter getChapter(ResourceLocation id) {
        return chapters.get(id);
    }

    public Quest getQuest(ResourceLocation id) {
        return quests.get(id);
    }

    public Collection<Quest> getAllQuests() {
        return Collections.unmodifiableCollection(quests.values());
    }
}
