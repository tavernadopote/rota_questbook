package com.rotaquestbook.data;

/**
 * Estados do ciclo de vida de uma missão para um determinado jogador.
 */
public enum QuestStatus {
    LOCKED("Trancada", 0x888888),
    UNLOCKED("Disponível", 0x3F2817),
    COMPLETED("Concluída", 0x2A7E19),
    CLAIMED("Resgatada", 0x7E5E19);

    private final String displayName;
    private final int colorRgb;

    QuestStatus(String displayName, int colorRgb) {
        this.displayName = displayName;
        this.colorRgb = colorRgb;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getColorRgb() {
        return colorRgb;
    }
}
