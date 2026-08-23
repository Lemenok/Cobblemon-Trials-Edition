package com.lemenok.cobblemontrialsedition.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.stream.Collectors;

public class SearchableDropdownWidget extends EditBox {
    private final List<String> allOptions;
    private List<String> filteredOptions;
    private boolean isDropdownOpen = false;

    public SearchableDropdownWidget(int width, int height, Component title, List<ResourceLocation> availableLootTables) {
        super(Minecraft.getInstance().font, width, height, title);
        this.allOptions = availableLootTables.stream().map(ResourceLocation::toString).collect(Collectors.toList());
        this.filteredOptions = this.allOptions;

        // Update suggestions as the user types
        this.setResponder(query -> {
            this.isDropdownOpen = !query.isEmpty();
            this.filteredOptions = allOptions.stream()
                    .filter(opt -> opt.toLowerCase().contains(query.toLowerCase()))
                    .limit(5) // Limit to top 5 results to prevent massive dropdowns
                    .collect(Collectors.toList());
        });
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(graphics, mouseX, mouseY, partialTick);

        if (this.isFocused() && this.isDropdownOpen && !this.filteredOptions.isEmpty()) {
            int dropDownY = this.getY() + this.getHeight();

            // Draw dropdown background
            graphics.fill(this.getX(), dropDownY, this.getX() + this.getWidth(), dropDownY + (this.filteredOptions.size() * 12), 0xFF000000);

            // Draw suggestions
            for (int i = 0; i < this.filteredOptions.size(); i++) {
                int textY = dropDownY + (i * 12) + 2;
                boolean isHovered = mouseX >= this.getX() && mouseX <= this.getX() + this.getWidth() && mouseY >= textY && mouseY < textY + 12;

                int color = isHovered ? 0xFFFFFF00 : 0xFFFFFFFF; // Highlight yellow on hover
                graphics.drawString(Minecraft.getInstance().font, this.filteredOptions.get(i), this.getX() + 4, textY, color, false);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isFocused() && this.isDropdownOpen) {
            int dropDownY = this.getY() + this.getHeight();
            for (int i = 0; i < this.filteredOptions.size(); i++) {
                int textY = dropDownY + (i * 12);
                if (mouseX >= this.getX() && mouseX <= this.getX() + this.getWidth() && mouseY >= textY && mouseY < textY + 12) {
                    this.setValue(this.filteredOptions.get(i));
                    this.isDropdownOpen = false;
                    return true; // Consume the click
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
