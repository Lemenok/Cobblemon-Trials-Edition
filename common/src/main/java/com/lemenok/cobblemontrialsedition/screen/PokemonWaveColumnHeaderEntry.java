package com.lemenok.cobblemontrialsedition.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;

import java.util.Collections;
import java.util.List;

public class PokemonWaveColumnHeaderEntry extends PokemonWaveList.WaveListEntry {

    @Override
    public List<? extends GuiEventListener> children() {
        return Collections.emptyList();
    }

    @Override
    public List<? extends NarratableEntry> narratables() {
        return Collections.emptyList();
    }

    @Override
    public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isSelected, float partialTick) {
        int padding = 4;
        int levelW = 35;
        int formsW = 60;
        int weightW = 35;
        int editW = 45;
        int deleteW = 50;

        // Match the indent used in PokemonWaveEntry for nested alignment
        int indent = 15;

        int speciesW = width - levelW - formsW - weightW - editW - deleteW - (padding * 5) - indent;
        int currentX = left + indent;
        int textY = top + 6;

        // Render headers matching PokemonRosterList styling (color 0xFFAAAAAA)
        graphics.drawString(Minecraft.getInstance().font, "Species", currentX, textY, 0xFFAAAAAA, false);
        currentX += speciesW + padding;

        graphics.drawString(Minecraft.getInstance().font, "Level", currentX, textY, 0xFFAAAAAA, false);
        currentX += levelW + padding;

        graphics.drawString(Minecraft.getInstance().font, "Forms", currentX, textY, 0xFFAAAAAA, false);
        currentX += formsW + padding;

        graphics.drawString(Minecraft.getInstance().font, "Weight", currentX, textY, 0xFFAAAAAA, false);
    }
}