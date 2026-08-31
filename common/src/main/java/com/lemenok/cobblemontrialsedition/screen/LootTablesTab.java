package com.lemenok.cobblemontrialsedition.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.function.Consumer;

public class LootTablesTab implements Tab {
    private final List<WeightedLootEntry> normalLootTables;
    private final List<WeightedLootEntry> ominousLootTables;

    private final Button addNormalBtn;
    private LootTableList normalListWidget = null;

    private final Button addOminousBtn;
    private LootTableList ominousListWidget = null;

    public LootTablesTab(TrialSpawnerConfigScreen screen, List<WeightedLootEntry> normalLootTables, List<WeightedLootEntry> ominousLootTables, List<ResourceLocation> availableLootTables) {
        this.normalLootTables = normalLootTables;
        this.ominousLootTables = ominousLootTables;

        // --- Left Side (Normal) ---
        this.addNormalBtn = Button.builder(Component.literal("+ Add Normal Loot Table"), btn -> {
            this.normalLootTables.add(new WeightedLootEntry(ResourceLocation.parse("minecraft:chests/trial_chambers/reward"), 1));
            this.normalListWidget.refreshEntries(this.normalLootTables);
        }).bounds(0, 0, 135, 18).build();

        this.normalListWidget = new LootTableList(
                Minecraft.getInstance(),
                screen.width / 2 - 10,
                screen.height,
                0,
                24,
                normalLootTables,
                availableLootTables
        );

        // --- Right Side (Ominous) ---
        this.addOminousBtn = Button.builder(Component.literal("+ Add Ominous Loot Table"), btn -> {
            this.ominousLootTables.add(new WeightedLootEntry(ResourceLocation.parse("minecraft:chests/trial_chambers/reward_ominous"), 1));
            this.ominousListWidget.refreshEntries(this.ominousLootTables);
        }).bounds(0, 0, 135, 18).build();

        this.ominousListWidget = new LootTableList(
                Minecraft.getInstance(),
                screen.width / 2 - 10,
                screen.height,
                0,
                24,
                ominousLootTables,
                availableLootTables
        );
    }

    @Override
    public Component getTabTitle() {
        return Component.literal("Loot Tables");
    }

    @Override
    public void visitChildren(Consumer<AbstractWidget> consumer) {
        consumer.accept(this.addNormalBtn);
        consumer.accept(this.normalListWidget);

        consumer.accept(this.addOminousBtn);
        consumer.accept(this.ominousListWidget);
    }

    @Override
    public void doLayout(ScreenRectangle screenRectangle) {
        int halfWidth = (screenRectangle.width() - 15) / 2;
        int topY = screenRectangle.top();
        int headerHeight = 25;

        // Position Normal Side (Left)
        int leftX = screenRectangle.left() + 5;
        this.addNormalBtn.setX(leftX);
        this.addNormalBtn.setY(topY + 3);

        this.normalListWidget.setX(leftX);
        this.normalListWidget.setY(topY + headerHeight);
        this.normalListWidget.setWidth(halfWidth);
        this.normalListWidget.setHeight(screenRectangle.height() - headerHeight);

        // Position Ominous Side (Right)
        int rightX = leftX + halfWidth + 10;

        this.addOminousBtn.setX(rightX);
        this.addOminousBtn.setY(topY + 3);

        this.ominousListWidget.setX(rightX);
        this.ominousListWidget.setY(topY + headerHeight);
        this.ominousListWidget.setWidth(halfWidth);
        this.ominousListWidget.setHeight(screenRectangle.height() - headerHeight);
    }
}
