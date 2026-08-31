package com.lemenok.cobblemontrialsedition.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class LootTableEntry extends ContainerObjectSelectionList.Entry<LootTableEntry> {
    private final SearchableDropdownWidget dropdown;
    private final EditBox weightBox;
    private final Button deleteBtn;
    private final List<AbstractWidget> children;

    public LootTableEntry(LootTableList parentList, List<WeightedLootEntry> list, int index, List<ResourceLocation> availableLootTables) {
        WeightedLootEntry entry = list.get(index);

        // 1. Searchable Dropdown (Initial width doesn't matter, it's set in render)
        this.dropdown = new SearchableDropdownWidget(0, 18, Component.literal("Loot Table"), availableLootTables);
        if (entry.getLocation() != null) {
            this.dropdown.setValue(entry.getLocation().toString());
        }
        this.dropdown.setResponder(val -> {
            ResourceLocation res = ResourceLocation.tryParse(val.trim());
            if (res != null) {
                entry.setLocation(res);
            }
        });

        // 2. Weight Field
        this.weightBox = new EditBox(Minecraft.getInstance().font, 35, 18, Component.literal("Weight"));
        this.weightBox.setValue(String.valueOf(entry.getWeight()));
        this.weightBox.setResponder(val -> {
            try {
                entry.setWeight(Integer.parseInt(val));
            } catch (NumberFormatException ignored) {}
        });

        // 3. Delete Button
        this.deleteBtn = Button.builder(Component.literal("Delete"), btn -> {
            list.remove(index);
            parentList.refreshEntries(list);
        }).bounds(0, 0, 50, 18).build();

        this.children = List.of(this.dropdown, this.weightBox, this.deleteBtn);
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
        int deleteWidth = 50;
        int weightWidth = 35;

        // Calculate available space so the dropdown scales with screen size
        int dropdownWidth = width - weightWidth - deleteWidth - (padding * 2);

        this.dropdown.setX(left);
        this.dropdown.setY(top + 2);
        this.dropdown.setWidth(dropdownWidth);

        this.weightBox.setX(left + dropdownWidth + padding);
        this.weightBox.setY(top + 2);
        this.weightBox.setWidth(weightWidth);

        this.deleteBtn.setX(left + dropdownWidth + weightWidth + (padding * 2));
        this.deleteBtn.setY(top + 2);
        this.deleteBtn.setWidth(deleteWidth);

        for (AbstractWidget widget : this.children) {
            widget.render(graphics, mouseX, mouseY, partialTick);
        }
    }
}
