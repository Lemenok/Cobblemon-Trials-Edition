package com.lemenok.cobblemontrialsedition.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SpawnerSettingsTab implements Tab {
    private final TrialSpawnerConfigScreen screen;
    private final FrameLayout layout = new FrameLayout();
    private final GridLayout grid = new GridLayout();

    public SpawnerSettingsTab(TrialSpawnerConfigScreen screen) {
        this.screen = screen;

        this.grid.defaultCellSetting().padding(5);

        // 4 columns allows us to put two settings side-by-side per row
        GridLayout.RowHelper rowHelper = this.grid.createRowHelper(4);

        // --- INTEGERS ---
        addIntRow(rowHelper, "Ticks Between Spawns:", screen.ticksBetweenSpawnAttempts, val -> screen.ticksBetweenSpawnAttempts = val);
        addIntRow(rowHelper, "Spawner Cooldown:", screen.spawnerCooldown, val -> screen.spawnerCooldown = val);
        addIntRow(rowHelper, "Player Detect Range:", screen.playerDetectionRange, val -> screen.playerDetectionRange = val);
        addIntRow(rowHelper, "Spawn Range:", screen.spawnRange, val -> screen.spawnRange = val);
        addIntRow(rowHelper, "Max Simultaneous:", screen.maximumNumberOfSimultaneousPokemon, val -> screen.maximumNumberOfSimultaneousPokemon = val);
        addIntRow(rowHelper, "Max Sim. Added/Player:", screen.maximumNumberOfSimultaneousPokemonAddedPerPlayer, val -> screen.maximumNumberOfSimultaneousPokemonAddedPerPlayer = val);
        addIntRow(rowHelper, "Total Per Trial:", screen.totalNumberOfPokemonPerTrial, val -> screen.totalNumberOfPokemonPerTrial = val);
        addIntRow(rowHelper, "Total Added/Player:", screen.totalNumberOfPokemonPerTrialAddedPerPlayer, val -> screen.totalNumberOfPokemonPerTrialAddedPerPlayer = val);

        // --- BOOLEANS ---
        addBoolRow(rowHelper, "Ominous Attacks:", screen.ominousSpawnerAttacksEnabled, val -> screen.ominousSpawnerAttacksEnabled = val);
        addBoolRow(rowHelper, "Spawned Pokemon Glow:", screen.doPokemonSpawnedGlow, val -> screen.doPokemonSpawnedGlow = val);

        // --- SPACER ROW ---
        for (int i = 0; i < 4; i++) {
            rowHelper.addChild(new StringWidget(Component.empty(), Minecraft.getInstance().font));
        }

        // --- LISTS (Comma Separated Strings) ---
        addListRow(rowHelper, "Loot Tables:", screen.lootTables, val -> screen.lootTables = val);
        addListRow(rowHelper, "Ominous Loot Tables:", screen.ominousLootTables, val -> screen.ominousLootTables = val);

        this.layout.addChild(this.grid);
    }

    // Helper: Creates an Integer EditBox
    private void addIntRow(GridLayout.RowHelper rowHelper, String label, int initialValue, Consumer<Integer> onChange) {
        EditBox editBox = new EditBox(Minecraft.getInstance().font, 100, 20, Component.literal(label));
        editBox.setValue(String.valueOf(initialValue));
        editBox.setResponder(val -> {
            try {
                onChange.accept(Integer.parseInt(val));
            } catch (NumberFormatException ignored) {
                // Ignore incomplete inputs like a single "-"
            }
        });
        rowHelper.addChild(new StringWidget(Component.literal(label), Minecraft.getInstance().font));
        rowHelper.addChild(editBox);
    }

    // Helper: Creates a Boolean Toggle Button
    private void addBoolRow(GridLayout.RowHelper rowHelper, String label, boolean initialValue, Consumer<Boolean> onChange) {
        CycleButton<Boolean> button = CycleButton.onOffBuilder(initialValue)
                .displayOnlyValue()
                .create(0, 0, 100, 20, Component.empty(), (cycle, val) -> onChange.accept(val));
        rowHelper.addChild(new StringWidget(Component.literal(label), Minecraft.getInstance().font));
        rowHelper.addChild(button);
    }

    // Helper: Creates an EditBox that parses comma-separated ResourceLocations safely
    private void addListRow(GridLayout.RowHelper rowHelper, String label, List<ResourceLocation> initialValue, Consumer<List<ResourceLocation>> onChange) {
        SearchableDropdownWidget searchableDropdownWidget = new SearchableDropdownWidget(100, 20, Component.literal(label), initialValue);
        String initialStr = initialValue.stream().map(ResourceLocation::toString).collect(Collectors.joining(", "));
        searchableDropdownWidget.setValue(initialStr);
        searchableDropdownWidget.setResponder(val -> {
            List<ResourceLocation> parsed = Arrays.stream(val.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(ResourceLocation::tryParse)
                    .filter(Objects::nonNull) // Ignores invalid inputs while typing
                    .toList();
            onChange.accept(parsed);
        });
        rowHelper.addChild(new StringWidget(Component.literal(label), Minecraft.getInstance().font));
        rowHelper.addChild(searchableDropdownWidget);
    }

    @Override
    public Component getTabTitle() {
        return Component.literal("Spawner Settings");
    }

    @Override
    public void visitChildren(Consumer<AbstractWidget> consumer) {
        this.layout.visitWidgets(consumer);
    }

    @Override
    public void doLayout(ScreenRectangle screenRectangle) {
        grid.arrangeElements();
        this.layout.arrangeElements();
        FrameLayout.alignInRectangle(this.layout, screenRectangle, 0.5f, 0.1f);

    }
}
