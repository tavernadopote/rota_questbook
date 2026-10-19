package com.rotaquestbook.client.gui;

import net.minecraft.resources.ResourceLocation;

/**
 * Gerenciador de tema, paleta de cores e textura de fundo do livro.
 * Permite customização completa via Resource Packs.
 */
public class ThemeManager {

    private static final ResourceLocation DEFAULT_BACKGROUND = ResourceLocation.fromNamespaceAndPath("rota_questbook", "textures/gui/questbook.png");

    public static final int COLOR_TITLE = 0x3A220F;       // Tinta marrom escura nobre
    public static final int COLOR_SUBTITLE = 0x5C3818;    // Marrom quente
    public static final int COLOR_BODY = 0x423122;        // Texto corrido legível
    public static final int COLOR_MUTED = 0x8C7B6D;       // Texto esmaecido / desativado
    public static final int COLOR_SUCCESS = 0x2A7E19;     // Verde de conquista
    public static final int COLOR_LINK = 0x8A4500;        // Cor de link / dependência

    public static ResourceLocation getBackgroundTexture() {
        return DEFAULT_BACKGROUND;
    }
}
