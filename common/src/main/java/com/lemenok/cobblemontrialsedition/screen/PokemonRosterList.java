package com.lemenok.cobblemontrialsedition.screen;

import com.lemenok.cobblemontrialsedition.config.SpawnablePokemonProperties;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;

import java.util.List;

public class PokemonRosterList extends ContainerObjectSelectionList<PokemonRosterEntry> {

    public PokemonRosterList(Minecraft minecraft, int width, int height, int y, int itemHeight, List<SpawnablePokemonProperties> roster) {
        super(minecraft, width, height, y, itemHeight);
        this.centerListVertically = false;

        // Enable Header
        this.setRenderHeader(true, 16);

        for (int i = 0; i < roster.size(); i++) {
            this.addEntry(new PokemonRosterEntry(this, roster, i));
        }
    }

    @Override
    public int getRowWidth() {
        return this.width - 15;
    }

    @Override
    protected int getScrollbarPosition() {
        return this.getX() + this.width - 6;
    }

    @Override
    protected void renderHeader(GuiGraphics graphics, int x, int y) {
        int padding = 4;
        int levelW = 35;
        int formsW = 60;
        int weightW = 35;
        int editW = 45;
        int deleteW = 50;
        int speciesW = this.getRowWidth() - levelW - formsW - weightW - editW - deleteW - (padding * 5);

        int currentX = x;
        graphics.drawString(Minecraft.getInstance().font, "Species", currentX, y + 4, 0xFFAAAAAA, false);
        currentX += speciesW + padding;

        graphics.drawString(Minecraft.getInstance().font, "Level", currentX, y + 4, 0xFFAAAAAA, false);
        currentX += levelW + padding;

        graphics.drawString(Minecraft.getInstance().font, "Forms", currentX, y + 4, 0xFFAAAAAA, false);
        currentX += formsW + padding;

        graphics.drawString(Minecraft.getInstance().font, "Weight", currentX, y + 4, 0xFFAAAAAA, false);
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(graphics, mouseX, mouseY, partialTick);

        if (this.children().isEmpty()) {
            graphics.drawCenteredString(
                    Minecraft.getInstance().font,
                    "No Pokémon configured in this roster.",
                    this.getX() + this.width / 2,
                    this.getY() + 35,
                    0xFFFFFF
            );
        }
    }

    public void refreshEntries(List<SpawnablePokemonProperties> roster) {
        this.clearEntries();
        for (int i = 0; i < roster.size(); i++) {
            this.addEntry(new PokemonRosterEntry(this, roster, i));
        }
    }
}
