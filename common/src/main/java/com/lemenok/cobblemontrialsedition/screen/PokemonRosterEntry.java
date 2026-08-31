package com.lemenok.cobblemontrialsedition.screen;

import com.lemenok.cobblemontrialsedition.config.SpawnablePokemonProperties;
import com.lemenok.cobblemontrialsedition.config.SpawnablePokemonStats;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class PokemonRosterEntry extends ContainerObjectSelectionList.Entry<PokemonRosterEntry> {
    private final EditBox speciesBox;
    private final EditBox levelBox;
    private final EditBox formsBox;
    private final EditBox weightBox;
    private final Button editBtn;
    private final Button deleteBtn;
    private final List<AbstractWidget> children;

    private final List<SpawnablePokemonProperties> roster;
    private final int index;

    public PokemonRosterEntry(PokemonRosterList parentList, List<SpawnablePokemonProperties> roster, int index) {
        this.roster = roster;
        this.index = index;
        SpawnablePokemonProperties poke = roster.get(index);
        SpawnablePokemonStats stats = poke.spawnablePokemonStats() != null ? poke.spawnablePokemonStats() : createDefaultStats();

        // 1. Species (Read-only)
        this.speciesBox = new EditBox(Minecraft.getInstance().font, 100, 18, Component.literal("Species"));
        this.speciesBox.setValue(poke.species());
        this.speciesBox.setEditable(false);

        // 2. Level (Read-only)
        this.levelBox = new EditBox(Minecraft.getInstance().font, 100, 18, Component.literal("Level"));
        this.levelBox.setValue(String.valueOf(stats.level()));
        this.levelBox.setEditable(false);

        // 3. Forms (Read-only)
        this.formsBox = new EditBox(Minecraft.getInstance().font, 100, 18, Component.literal("Forms"));
        this.formsBox.setValue(String.join(", ", stats.form()));
        this.formsBox.setMaxLength(256);
        this.formsBox.setEditable(false);

        // 4. Weight (Read-only)
        this.weightBox = new EditBox(Minecraft.getInstance().font, 100, 18, Component.literal("Weight"));
        this.weightBox.setValue(String.valueOf(poke.weight()));
        this.weightBox.setEditable(false);

        // 5. Edit Button
        this.editBtn = Button.builder(Component.literal("Edit"), btn -> {
            Minecraft.getInstance().setScreen(new PokemonEditScreen(
                    Minecraft.getInstance().screen,
                    poke,
                    updatedPokemon -> {
                        roster.set(this.index, updatedPokemon);
                        parentList.refreshEntries(roster);
                    }
            ));
        }).bounds(0, 0, 45, 18).build();

        // 6. Delete Button
        this.deleteBtn = Button.builder(Component.literal("Delete"), btn -> {
            roster.remove(this.index);
            parentList.refreshEntries(roster);
        }).bounds(0, 0, 50, 18).build();

        this.children = List.of(this.speciesBox, this.levelBox, this.formsBox, this.weightBox, this.editBtn, this.deleteBtn);
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return this.children;
    }

    @Override
    public List<? extends NarratableEntry> narratables() {
        return this.children;
    }

    @Override
    public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isSelected, float partialTick) {
        int padding = 4;
        int levelW = 35;
        int formsW = 60;
        int weightW = 35;
        int editW = 45;
        int deleteW = 50;

        // Species box scales to fill the remaining horizontal space
        int speciesW = width - levelW - formsW - weightW - editW - deleteW - (padding * 5);
        int currentX = left;

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

    private void updatePokemon(String species, int weight, float scale, boolean uncatchable, boolean defeated, boolean aggressive, boolean alpha, List<String> aspects, SpawnablePokemonStats stats) {
        if (index >= 0 && index < roster.size()) {
            roster.set(index, new SpawnablePokemonProperties(species, weight, scale, uncatchable, defeated, aggressive, alpha, aspects, stats));
        }
    }

    private void updateStats(SpawnablePokemonProperties poke, java.util.function.Function<SpawnablePokemonStats, SpawnablePokemonStats> statUpdater) {
        SpawnablePokemonStats currentStats = poke.spawnablePokemonStats() != null ? poke.spawnablePokemonStats() : createDefaultStats();
        SpawnablePokemonStats newStats = statUpdater.apply(currentStats);
        updatePokemon(poke.species(), poke.weight(), poke.scaleModifier(), poke.isUncatchable(), poke.mustBeDefeatedInBattle(), poke.isAggressive(), poke.isAlwaysAlpha(), poke.aspects(), newStats);
    }

    private SpawnablePokemonStats createDefaultStats() {
        return new SpawnablePokemonStats(new ArrayList<>(), 25, "", "", new ArrayList<>(), new ArrayList<>(), "", new ArrayList<>(), "", 0, "", false);
    }
}