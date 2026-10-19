package com.rotaquestbook.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Configuração serializada em JSON para o Rota Questbook.
 * Armazenada em config/rota_questbook.json.
 */
public class QuestbookConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("rota_questbook/config");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "rota_questbook.json";

    private static QuestbookConfig instance = new QuestbookConfig();

    /**
     * Se verdadeiro, substitui nativamente o atalho de teclado 'L' e o botão de Conquistas do menu de pausa
     * para abrir diretamente o Rota Questbook.
     */
    public boolean replaceVanillaAdvancementsScreen = true;

    /**
     * Toca efeito sonoro diegético de papel/pergaminho folheando ao virar as páginas.
     */
    public boolean enablePageFlipSound = true;

    /**
     * Exibe notificação estilizada de pergaminho (Toast) no canto da tela ao completar missões.
     */
    public boolean showToastOnQuestCompletion = true;

    /**
     * Entrega automaticamente um livro de missões físico ao jogador quando ele entra no mundo pela primeira vez.
     */
    public boolean giveBookOnFirstJoin = false;

    /**
     * Converte e organiza automaticamente as conquistas do Minecraft Vanilla como Capítulos do livro.
     */
    public boolean importVanillaAdvancements = true;

    /**
     * Converte e organiza automaticamente as conquistas registradas por outros mods como Capítulos do livro.
     */
    public boolean importModAdvancements = true;

    public static QuestbookConfig get() {
        if (instance == null) {
            load();
        }
        return instance;
    }

    public static void load() {
        Path configDir = FMLPaths.CONFIGDIR.get();
        Path configFile = configDir.resolve(FILE_NAME);

        if (!Files.exists(configFile)) {
            instance = new QuestbookConfig();
            save();
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(configFile)) {
            QuestbookConfig loaded = GSON.fromJson(reader, QuestbookConfig.class);
            if (loaded != null) {
                instance = loaded;
            }
        } catch (Exception e) {
            LOGGER.error("Falha ao carregar {}, criando novo com valores padrão.", FILE_NAME, e);
            instance = new QuestbookConfig();
            save();
        }
    }

    public static void save() {
        Path configDir = FMLPaths.CONFIGDIR.get();
        Path configFile = configDir.resolve(FILE_NAME);

        try {
            Files.createDirectories(configDir);
            try (BufferedWriter writer = Files.newBufferedWriter(configFile)) {
                GSON.toJson(instance, writer);
            }
        } catch (IOException e) {
            LOGGER.error("Falha ao salvar {}.", FILE_NAME, e);
        }
    }
}
