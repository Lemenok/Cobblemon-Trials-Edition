package com.lemenok.cobblemontrialsedition.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class LootTableList extends ContainerObjectSelectionList<LootTableEntry> {
    private final List<ResourceLocation> availableLootTables;

    public LootTableList(Minecraft minecraft, int width, int height, int y, int itemHeight, List<WeightedLootEntry> entries, List<ResourceLocation> availableLootTables) {
        super(minecraft, width, height, y, itemHeight);
        this.centerListVertically = false;
        this.availableLootTables = availableLootTables;

        // 1. Enable the native list header and set its height
        this.setRenderHeader(true, 16);

        this.refreshEntries(entries);
    }

    @Override
    public int getRowWidth() {
        return this.width - 15;
    }

    @Override
    protected int getScrollbarPosition() {
        return this.getX() + this.width - 6;
    }

    public void refreshEntries(List<WeightedLootEntry> entries) {
        this.clearEntries();
        for (int i = 0; i < entries.size(); i++) {
            this.addEntry(new LootTableEntry(this, entries, i, availableLootTables));
        }
    }

    // 2. Render the headers using the exact width math from LootTableEntry
    @Override
    protected void renderHeader(GuiGraphics graphics, int x, int y) {
        int padding = 4;
        int deleteWidth = 50;
        int weightWidth = 35;
        int dropdownWidth = this.getRowWidth() - weightWidth - deleteWidth - (padding * 2);

        graphics.drawString(Minecraft.getInstance().font, "Location", x, y + 4, 0xFFAAAAAA, false);
        graphics.drawString(Minecraft.getInstance().font, "Weight", x + dropdownWidth + padding, y + 4, 0xFFAAAAAA, false);
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(graphics, mouseX, mouseY, partialTick);
        if (this.children().isEmpty()) {
            graphics.drawCenteredString(
                    Minecraft.getInstance().font,
                    "No loot tables added.",
                    this.getX() + this.width / 2,
                    this.getY() + 35,
                    0x888888
            );
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (LootTableEntry entry : this.children()) {
            for (net.minecraft.client.gui.components.events.GuiEventListener child : entry.children()) {
                if (child instanceof SearchableDropdownWidget dropdown) {
                    if (dropdown.checkDropdownClick(mouseX, mouseY)) {
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}