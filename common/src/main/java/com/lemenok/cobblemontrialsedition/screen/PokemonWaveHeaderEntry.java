package com.lemenok.cobblemontrialsedition.screen;

import com.lemenok.cobblemontrialsedition.config.SpawnerProperties;
import com.lemenok.cobblemontrialsedition.config.SpawnablePokemonProperties;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;

import java.util.List;

public class PokemonWaveHeaderEntry extends PokemonWaveList.WaveListEntry {
    private final StringWidget waveLabel;
    private final StringWidget totalLabel;
    private final EditBox mobsInWaveBox;
    private final StringWidget addedLabel;
    private final EditBox addedPerPlayerBox;
    private final Button addPokemonBtn;
    private final Button deleteWaveBtn;
    private final List<AbstractWidget> children;

    private final List<SpawnerProperties.WaveDefinition> waves;
    private final int waveIndex;

    public PokemonWaveHeaderEntry(PokemonWaveList parentList, List<SpawnerProperties.WaveDefinition> waves, int waveIndex, TrialSpawnerConfigScreen screen) {
        this.waves = waves;
        this.waveIndex = waveIndex;
        SpawnerProperties.WaveDefinition wave = waves.get(waveIndex);

        this.waveLabel = new StringWidget(Component.literal("Wave " + (waveIndex + 1)), Minecraft.getInstance().font);
        //this.waveLabel.setColor(0xFFAA00); // Orange tint to stand out

        this.totalLabel = new StringWidget(Component.literal("Total Pokemon in Wave:"), Minecraft.getInstance().font);
        this.mobsInWaveBox = new EditBox(Minecraft.getInstance().font, 30, 18, Component.empty());
        this.mobsInWaveBox.setValue(String.valueOf(wave.mobsInWave()));
        this.mobsInWaveBox.setResponder(val -> {
            try {
                updateWave(Integer.parseInt(val), waves.get(waveIndex).mobsInWaveAddedPerPlayer());
            } catch (NumberFormatException ignored) {}
        });

        this.addedLabel = new StringWidget(Component.literal("Pokemon in Wave added per player:"), Minecraft.getInstance().font);
        this.addedPerPlayerBox = new EditBox(Minecraft.getInstance().font, 30, 18, Component.empty());
        this.addedPerPlayerBox.setValue(String.valueOf(wave.mobsInWaveAddedPerPlayer()));
        this.addedPerPlayerBox.setResponder(val -> {
            try {
                updateWave(waves.get(waveIndex).mobsInWave(), Integer.parseInt(val));
            } catch (NumberFormatException ignored) {}
        });

        this.addPokemonBtn = Button.builder(Component.literal("+ Add Pokemon"), btn -> {
            com.lemenok.cobblemontrialsedition.config.SpawnablePokemonStats defaultStats =
                    new com.lemenok.cobblemontrialsedition.config.SpawnablePokemonStats(new java.util.ArrayList<>(), 25, "", "", new java.util.ArrayList<>(), new java.util.ArrayList<>(), "", new java.util.ArrayList<>(), "", 0, "", false);
            SpawnablePokemonProperties defaultPokemon =
                    new SpawnablePokemonProperties("pikachu", 10, 1.0f, false, false, false, false, new java.util.ArrayList<>(), defaultStats);

            waves.get(this.waveIndex).pokemonToSpawn().add(defaultPokemon);
            parentList.refreshEntries(waves);
        }).bounds(0, 0, 90, 18).build();

        this.deleteWaveBtn = Button.builder(Component.literal("Delete"), btn -> {
            waves.remove(this.waveIndex);
            parentList.refreshEntries(waves);
        }).bounds(0, 0, 50, 18).build();

        this.children = List.of(this.waveLabel, this.totalLabel, this.mobsInWaveBox, this.addedLabel, this.addedPerPlayerBox, this.addPokemonBtn, this.deleteWaveBtn);
    }

    private void updateWave(int mobs, int added) {
        var old = waves.get(waveIndex);
        waves.set(waveIndex, new SpawnerProperties.WaveDefinition(mobs, added, old.pokemonToSpawn()));
    }

    @Override
    public List<? extends GuiEventListener> children() { return this.children; }

    @Override
    public List<? extends NarratableEntry> narratables() { return this.children; }

    @Override
    public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isSelected, float partialTick) {
        int padding = 4;
        int currentX = left + 5;

        // Force header row background to distinct it visually
        graphics.fill(left, top, left + width, top + height, 0x44000000);

        this.waveLabel.setPosition(currentX, top + 6);
        currentX += 45 + padding;

        this.totalLabel.setPosition(currentX, top + 6);
        currentX += 110 + padding;

        this.mobsInWaveBox.setPosition(currentX, top + 2);
        currentX += 30 + padding;

        this.addedLabel.setPosition(currentX, top + 6);
        currentX += 175 + padding;

        this.addedPerPlayerBox.setPosition(currentX, top + 2);
        currentX += 30 + padding;

        this.addPokemonBtn.setPosition(currentX, top + 2);
        currentX += 90 + padding;

        this.deleteWaveBtn.setPosition(currentX, top + 2);

        for (AbstractWidget widget : this.children) {
            widget.render(graphics, mouseX, mouseY, partialTick);
        }
    }
}