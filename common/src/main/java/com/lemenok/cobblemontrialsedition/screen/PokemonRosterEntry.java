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
    private final List<AbstractWidget> children = new ArrayList<>();
    private final List<SpawnablePokemonProperties> roster;
    private final int index;

    public PokemonRosterEntry(PokemonRosterList parentList, List<SpawnablePokemonProperties> roster, int index) {
        this.roster = roster;
        this.index = index;
        SpawnablePokemonProperties poke = roster.get(index);
        SpawnablePokemonStats stats = poke.spawnablePokemonStats() != null ? poke.spawnablePokemonStats() : createDefaultStats();

        // 1. Species Edit Box
        EditBox speciesBox = new EditBox(Minecraft.getInstance().font, 100, 18, Component.literal("Species"));
        speciesBox.setValue(poke.species());
        speciesBox.setResponder(val -> updatePokemon(val, poke.weight(), poke.scaleModifier(), poke.isUncatchable(), poke.mustBeDefeatedInBattle(), poke.isAggressive(), poke.isAlwaysAlpha(), poke.aspects(), poke.spawnablePokemonStats()));
        children.add(speciesBox);

        // 2. Level Edit Box
        EditBox levelBox = new EditBox(Minecraft.getInstance().font, 40, 18, Component.literal("Level"));
        levelBox.setValue(String.valueOf(stats.level()));
        levelBox.setResponder(val -> {
            try {
                int lvl = Integer.parseInt(val);
                updateStats(poke, s -> new SpawnablePokemonStats(s.form(), lvl, s.gender(), s.nature(), s.defaultEVs(), s.defaultIVs(), s.ability(), s.moves(), s.heldItem(), s.dynaMaxLevel(), s.teraType(), s.isShiny()));
            } catch (NumberFormatException ignored) {}
        });
        children.add(levelBox);

        // 3. Edit Button
        Button editBtn = Button.builder(Component.literal("Edit"), btn -> {
            // Open the edit screen pre-filled with this entry's data
            Minecraft.getInstance().setScreen(new PokemonEditScreen(
                    Minecraft.getInstance().screen,
                    poke,
                    updatedPokemon -> {
                        roster.set(this.index, updatedPokemon); // Overwrite data at index
                        parentList.refreshEntries(roster);      // Refresh parent UI
                    }
            ));
        }).bounds(0, 0, 45, 18).build();
        children.add(editBtn);

        // 4 Delete Button
        Button deleteBtn = Button.builder(Component.literal("Delete"), btn -> {
            // Remove this specific pokemon from the data roster
            roster.remove(this.index);
            // Tell the parent UI to completely rebuild the list rows
            parentList.refreshEntries(roster);
        }).bounds(0, 0, 50, 18).build();
        children.add(deleteBtn);
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
        int xOffset = left + 5;
        int yPos = top + 2;

        graphics.drawString(Minecraft.getInstance().font, "#" + (this.index + 1), xOffset, yPos + 5, 0xFFAAAAAA, false);
        xOffset += 25;

        for (AbstractWidget widget : this.children) {
            widget.setX(xOffset);
            widget.setY(yPos);
            widget.render(graphics, mouseX, mouseY, partialTick);
            xOffset += widget.getWidth() + 10;
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