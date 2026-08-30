package com.lemenok.cobblemontrialsedition.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SearchableDropdownWidget extends EditBox {
    private final List<String> allOptions;
    private List<String> filteredOptions;
    private boolean isDropdownOpen = false;
    private Consumer<String> externalResponder = s -> {};

    public SearchableDropdownWidget(int width, int height, Component title, List<ResourceLocation> availableOptions) {
        super(Minecraft.getInstance().font, width, height, title);
        this.allOptions = availableOptions.stream().map(ResourceLocation::toString).collect(Collectors.toList());
        this.filteredOptions = this.allOptions;

        super.setResponder(query -> {
            this.isDropdownOpen = true;
            this.updateFilter(query);
            this.externalResponder.accept(query);
        });
    }

    private void updateFilter(String query) {
        String currentSearch = query;
        int lastComma = query.lastIndexOf(',');
        if (lastComma != -1) {
            currentSearch = query.substring(lastComma + 1).trim();
        }
        String finalSearch = currentSearch.toLowerCase();
        this.filteredOptions = this.allOptions.stream()
                .filter(opt -> opt.toLowerCase().contains(finalSearch))
                .limit(5)
                .collect(Collectors.toList());
    }

    @Override
    public void setResponder(Consumer<String> responder) {
        this.externalResponder = responder;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        super.onClick(mouseX, mouseY);
        this.isDropdownOpen = true;
        this.updateFilter(this.getValue());
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if (focused) {
            this.isDropdownOpen = true;
            this.updateFilter(this.getValue());
        } else {
            this.isDropdownOpen = false;
        }
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(graphics, mouseX, mouseY, partialTick);

        if (this.isFocused() && this.isDropdownOpen && !this.filteredOptions.isEmpty()) {
            // Push pose and translate Z-index so the dropdown draws OVER widgets below it
            graphics.pose().pushPose();
            graphics.pose().translate(0, 0, 1000);

            int dropDownY = this.getY() + this.getHeight();

            graphics.fill(this.getX(), dropDownY, this.getX() + this.getWidth(), dropDownY + (this.filteredOptions.size() * 12), 0xFF000000);

            for (int i = 0; i < this.filteredOptions.size(); i++) {
                int textY = dropDownY + (i * 12) + 2;
                boolean isHovered = mouseX >= this.getX() && mouseX <= this.getX() + this.getWidth() && mouseY >= textY && mouseY < textY + 12;

                int color = isHovered ? 0xFFFFFF00 : 0xFFFFFFFF;
                graphics.drawString(Minecraft.getInstance().font, this.filteredOptions.get(i), this.getX() + 4, textY, color, false);
            }

            graphics.pose().popPose();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isFocused() && this.isDropdownOpen && !this.filteredOptions.isEmpty()) {
            int dropDownY = this.getY() + this.getHeight();
            for (int i = 0; i < this.filteredOptions.size(); i++) {
                int textY = dropDownY + (i * 12);
                if (mouseX >= this.getX() && mouseX <= this.getX() + this.getWidth() && mouseY >= textY && mouseY < textY + 12) {

                    String currentText = this.getValue();
                    int lastComma = currentText.lastIndexOf(',');
                    if (lastComma != -1) {
                        this.setValue(currentText.substring(0, lastComma + 1) + " " + this.filteredOptions.get(i) + ", ");
                    } else {
                        this.setValue(this.filteredOptions.get(i) + ", ");
                    }

                    this.isDropdownOpen = false;
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
