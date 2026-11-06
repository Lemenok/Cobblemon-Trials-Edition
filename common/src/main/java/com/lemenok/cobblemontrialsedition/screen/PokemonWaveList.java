package com.lemenok.cobblemontrialsedition.screen;

import com.lemenok.cobblemontrialsedition.config.SpawnerProperties;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;

import java.util.List;

public class PokemonWaveList extends ContainerObjectSelectionList<PokemonWaveList.WaveListEntry> {

    // Base entry class allowing us to mix Header rows and Pokemon rows
    public static abstract class WaveListEntry extends ContainerObjectSelectionList.Entry<WaveListEntry> {}

    private final TrialSpawnerConfigScreen screen;

    public PokemonWaveList(Minecraft minecraft, int width, int height, int y, int itemHeight, List<SpawnerProperties.PokemonWaveDefinition> waves, TrialSpawnerConfigScreen screen) {
        super(minecraft, width, height, y, itemHeight);
        this.screen = screen;
        this.centerListVertically = false;
        this.refreshEntries(waves);
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
    public void renderWidget(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(graphics, mouseX, mouseY, partialTick);

        if (this.children().isEmpty()) {
            graphics.drawCenteredString(
                    Minecraft.getInstance().font,
                    "No Waves configured in this roster.",
                    this.getX() + this.width / 2,
                    this.getY() + 35,
                    0xFFFFFF
            );
        }
    }

    public void refreshEntries(List<SpawnerProperties.PokemonWaveDefinition> pokemonWaveDefinitions) {
        this.clearEntries();
        for (int i = 0; i < pokemonWaveDefinitions.size(); i++) {
            // Render the Wave Header
            this.addEntry(new PokemonWaveHeaderEntry(this, pokemonWaveDefinitions, i, screen));

            var wave = pokemonWaveDefinitions.get(i);

            // Render Column Headers only if the wave contains Pokémon
            if (!wave.pokemonToSpawn().isEmpty()) {
                this.addEntry(new PokemonWaveColumnHeaderEntry());
            }

            // Render each Pokemon inside that Wave
            for (int j = 0; j < wave.pokemonToSpawn().size(); j++) {
                this.addEntry(new PokemonWaveEntry(this, pokemonWaveDefinitions, i, j, screen));
            }
        }
    }
}