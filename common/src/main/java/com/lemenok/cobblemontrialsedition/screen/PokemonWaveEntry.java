package com.lemenok.cobblemontrialsedition.screen;

import com.lemenok.cobblemontrialsedition.config.SpawnablePokemonProperties;
import com.lemenok.cobblemontrialsedition.config.SpawnablePokemonStats;
import com.lemenok.cobblemontrialsedition.config.SpawnerProperties;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class PokemonWaveEntry extends PokemonWaveList.WaveListEntry {
    private final EditBox speciesBox;
    private final EditBox levelBox;
    private final EditBox formsBox;
    private final EditBox weightBox;
    private final Button editBtn;
    private final Button deleteBtn;
    private final List<AbstractWidget> children;

    public PokemonWaveEntry(PokemonWaveList parentList, List<SpawnerProperties.WaveDefinition> waves, int waveIndex, int pokemonIndex, TrialSpawnerConfigScreen screen) {
        SpawnablePokemonProperties poke = waves.get(waveIndex).pokemonToSpawn().get(pokemonIndex);
        SpawnablePokemonStats stats = poke.spawnablePokemonStats() != null ? poke.spawnablePokemonStats() : createDefaultStats();

        this.speciesBox = new EditBox(Minecraft.getInstance().font, 100, 18, Component.literal("Species"));
        this.speciesBox.setValue(poke.species());
        this.speciesBox.setEditable(false);

        this.levelBox = new EditBox(Minecraft.getInstance().font, 100, 18, Component.literal("Level"));
        this.levelBox.setValue(String.valueOf(stats.level()));
        this.levelBox.setEditable(false);

        this.formsBox = new EditBox(Minecraft.getInstance().font, 100, 18, Component.literal("Forms"));
        this.formsBox.setValue(String.join(", ", stats.form()));
        this.formsBox.setMaxLength(256);
        this.formsBox.setEditable(false);

        this.weightBox = new EditBox(Minecraft.getInstance().font, 100, 18, Component.literal("Weight"));
        this.weightBox.setValue(String.valueOf(poke.weight()));
        this.weightBox.setEditable(false);

        this.editBtn = Button.builder(Component.literal("Edit"), btn -> {
            Minecraft.getInstance().setScreen(new PokemonEditScreen(screen, poke, updatedPokemon -> {
                waves.get(waveIndex).pokemonToSpawn().set(pokemonIndex, updatedPokemon);
                parentList.refreshEntries(waves);
            }));
        }).bounds(0, 0, 45, 18).build();

        this.deleteBtn = Button.builder(Component.literal("Delete"), btn -> {
            waves.get(waveIndex).pokemonToSpawn().remove(pokemonIndex);
            parentList.refreshEntries(waves);
        }).bounds(0, 0, 50, 18).build();

        this.children = List.of(this.speciesBox, this.levelBox, this.formsBox, this.weightBox, this.editBtn, this.deleteBtn);
    }

    @Override
    public List<? extends GuiEventListener> children() { return this.children; }

    @Override
    public List<? extends NarratableEntry> narratables() { return this.children; }

    @Override
    public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isSelected, float partialTick) {
        int padding = 4;
        int levelW = 35;
        int formsW = 60;
        int weightW = 35;
        int editW = 45;
        int deleteW = 50;

        // Push the pokemon list visually to the right to imply nesting
        int indent = 15;

        int speciesW = width - levelW - formsW - weightW - editW - deleteW - (padding * 5) - indent;
        int currentX = left + indent;

        this.speciesBox.setX(currentX);
        this.speciesBox.setY(top + 2);
        this.speciesBox.setWidth(speciesW);
        currentX += speciesW + padding;

        this.levelBox.setX(currentX);
        this.levelBox.setY(top + 2);
        this.levelBox.setWidth(levelW);
        currentX += levelW + padding;

        this.formsBox.setX(currentX);
        this.formsBox.setY(top + 2);
        this.formsBox.setWidth(formsW);
        currentX += formsW + padding;

        this.weightBox.setX(currentX);
        this.weightBox.setY(top + 2);
        this.weightBox.setWidth(weightW);
        currentX += weightW + padding;

        this.editBtn.setX(currentX);
        this.editBtn.setY(top + 2);
        this.editBtn.setWidth(editW);
        currentX += editW + padding;

        this.deleteBtn.setX(currentX);
        this.deleteBtn.setY(top + 2);
        this.deleteBtn.setWidth(deleteW);

        for (AbstractWidget widget : this.children) {
            widget.render(graphics, mouseX, mouseY, partialTick);
        }
    }

    private SpawnablePokemonStats createDefaultStats() {
        return new SpawnablePokemonStats(new ArrayList<>(), 25, "", "", new ArrayList<>(), new ArrayList<>(), "", new ArrayList<>(), "", 0, "", false);
    }
}