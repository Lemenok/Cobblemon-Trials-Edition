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

        for (int i = 0; i < roster.size(); i++) {
            this.addEntry(new PokemonRosterEntry(this, roster, i));
        }
    }

    @Override
    public int getRowWidth() {
        return 320;
    }

    @Override
    protected int getScrollbarPosition() {
        return this.getX() + this.width - 10;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(graphics, mouseX, mouseY, partialTick);

        // If the config list is completely empty, draw a warning message so the tab isn't blank!
        if (this.children().isEmpty()) {
            graphics.drawCenteredString(
                    Minecraft.getInstance().font,
                    "No Pokémon configured in this roster.",
                    this.getX() + this.width / 2,
                    this.getY() + 30,
                    0xFFFFFF // White text
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
