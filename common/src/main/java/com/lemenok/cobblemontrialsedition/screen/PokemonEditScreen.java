package com.lemenok.cobblemontrialsedition.screen;

import com.lemenok.cobblemontrialsedition.config.SpawnablePokemonProperties;
import com.lemenok.cobblemontrialsedition.config.SpawnablePokemonStats;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class PokemonEditScreen extends Screen {

    private final Screen parentScreen;
    private final Consumer<SpawnablePokemonProperties> onSave;
    private final GridLayout grid = new GridLayout();

    // Field States
    private String species = "pikachu";
    private int weight = 10;
    private float scaleModifier = 1.0f;
    private boolean isUncatchable = false;
    private boolean mustBeDefeatedInBattle = false;
    private boolean isAggressive = false;
    private boolean isAlwaysAlpha = false;
    private List<String> aspects = new ArrayList<>();

    // Stats States
    private List<String> forms = new ArrayList<>();
    private int level = 25;
    private String gender = "";
    private String nature = "";
    private String ability = "";
    private List<String> moves = new ArrayList<>();
    private String heldItem = "";
    private int dynaMaxLevel = 0;
    private String teraType = "";
    private boolean isShiny = false;

    public PokemonEditScreen(Screen parentScreen, SpawnablePokemonProperties existingData, Consumer<SpawnablePokemonProperties> onSave) {
        super(Component.literal("Add / Edit Pokemon"));
        this.parentScreen = parentScreen;
        this.onSave = onSave;

        if (existingData != null) {
            this.species = existingData.species();
            this.weight = existingData.weight();
            this.scaleModifier = existingData.scaleModifier();
            this.isUncatchable = existingData.isUncatchable();
            this.mustBeDefeatedInBattle = existingData.mustBeDefeatedInBattle();
            this.isAggressive = existingData.isAggressive();
            this.isAlwaysAlpha = existingData.isAlwaysAlpha();
            this.aspects = new ArrayList<>(existingData.aspects());

            if (existingData.spawnablePokemonStats() != null) {
                SpawnablePokemonStats stats = existingData.spawnablePokemonStats();
                this.forms = new ArrayList<>(stats.form());
                this.level = stats.level();
                this.gender = stats.gender();
                this.nature = stats.nature();
                this.ability = stats.ability();
                this.moves = new ArrayList<>(stats.moves());
                this.heldItem = stats.heldItem();
                this.dynaMaxLevel = stats.dynaMaxLevel();
                this.teraType = stats.teraType();
                this.isShiny = stats.isShiny();
            }
        }
    }

    @Override
    protected void init() {
        super.init();
        this.grid.defaultCellSetting().padding(3);
        GridLayout.RowHelper rowHelper = this.grid.createRowHelper(4);

        // Core Properties
        addTextField(rowHelper, "Species:", species, val -> species = val);
        addIntField(rowHelper, "Weight:", weight, val -> weight = val);
        addFloatField(rowHelper, "Scale:", scaleModifier, val -> scaleModifier = val);
        addBoolField(rowHelper, "Uncatchable:", isUncatchable, val -> isUncatchable = val);

        addBoolField(rowHelper, "Must Defeat:", mustBeDefeatedInBattle, val -> mustBeDefeatedInBattle = val);
        addBoolField(rowHelper, "Aggressive:", isAggressive, val -> isAggressive = val);
        addBoolField(rowHelper, "Always Alpha:", isAlwaysAlpha, val -> isAlwaysAlpha = val);
        addListField(rowHelper, "Aspects:", aspects, val -> aspects = val);

        // Stats
        addIntField(rowHelper, "Level:", level, val -> level = val);
        addBoolField(rowHelper, "Shiny:", isShiny, val -> isShiny = val);
        addTextField(rowHelper, "Gender:", gender, val -> gender = val);
        addTextField(rowHelper, "Nature:", nature, val -> nature = val);

        addTextField(rowHelper, "Ability:", ability, val -> ability = val);
        addTextField(rowHelper, "Held Item:", heldItem, val -> heldItem = val);
        addListField(rowHelper, "Forms:", forms, val -> forms = val);
        addTextField(rowHelper, "Tera Type:", teraType, val -> teraType = val);

        addIntField(rowHelper, "DynaMax Lvl:", dynaMaxLevel, val -> dynaMaxLevel = val);
        addListField(rowHelper, "Moves:", moves, val -> moves = val);

        // Spacer
        rowHelper.addChild(new StringWidget(Component.empty(), font), 4);

        // Actions
        Button saveBtn = Button.builder(Component.literal("Save Pokemon"), b -> {
            SpawnablePokemonStats stats = new SpawnablePokemonStats(
                    forms, level, gender, nature, new ArrayList<>(), new ArrayList<>(),
                    ability, moves, heldItem, dynaMaxLevel, teraType, isShiny
            );
            SpawnablePokemonProperties pokemon = new SpawnablePokemonProperties(
                    species, weight, scaleModifier, isUncatchable, mustBeDefeatedInBattle,
                    isAggressive, isAlwaysAlpha, aspects, stats
            );
            onSave.accept(pokemon);
            Minecraft.getInstance().setScreen(parentScreen);
        }).width(120).build();

        Button cancelBtn = Button.builder(Component.literal("Cancel"), b ->
                Minecraft.getInstance().setScreen(parentScreen)
        ).width(120).build();

        rowHelper.addChild(saveBtn, 2);
        rowHelper.addChild(cancelBtn, 2);

        this.grid.arrangeElements();
        FrameLayout.alignInRectangle(this.grid, 0, 0, this.width, this.height, 0.5f, 0.3f);
        this.grid.visitWidgets(this::addRenderableWidget);
    }

    private void addTextField(GridLayout.RowHelper row, String label, String init, Consumer<String> onChange) {
        EditBox box = new EditBox(font, 90, 18, Component.literal(label));
        box.setValue(init);
        box.setResponder(onChange);
        row.addChild(new StringWidget(Component.literal(label), font));
        row.addChild(box);
    }

    private void addIntField(GridLayout.RowHelper row, String label, int init, Consumer<Integer> onChange) {
        EditBox box = new EditBox(font, 90, 18, Component.literal(label));
        box.setValue(String.valueOf(init));
        box.setResponder(v -> { try { onChange.accept(Integer.parseInt(v)); } catch (Exception ignored){} });
        row.addChild(new StringWidget(Component.literal(label), font));
        row.addChild(box);
    }

    private void addFloatField(GridLayout.RowHelper row, String label, float init, Consumer<Float> onChange) {
        EditBox box = new EditBox(font, 90, 18, Component.literal(label));
        box.setValue(String.valueOf(init));
        box.setResponder(v -> { try { onChange.accept(Float.parseFloat(v)); } catch (Exception ignored){} });
        row.addChild(new StringWidget(Component.literal(label), font));
        row.addChild(box);
    }

    private void addBoolField(GridLayout.RowHelper row, String label, boolean init, Consumer<Boolean> onChange) {
        CycleButton<Boolean> btn = CycleButton.onOffBuilder(init).displayOnlyValue()
                .create(0, 0, 90, 18, Component.empty(), (c, val) -> onChange.accept(val));
        row.addChild(new StringWidget(Component.literal(label), font));
        row.addChild(btn);
    }

    private void addListField(GridLayout.RowHelper row, String label, List<String> init, Consumer<List<String>> onChange) {
        EditBox box = new EditBox(font, 90, 18, Component.literal(label));
        box.setValue(String.join(", ", init));
        box.setResponder(v -> onChange.accept(Arrays.stream(v.split(",")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList())));
        row.addChild(new StringWidget(Component.literal(label), font));
        row.addChild(box);
    }

    @Override
    public void render(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
    }
}
