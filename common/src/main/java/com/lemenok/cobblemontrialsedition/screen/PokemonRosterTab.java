package com.lemenok.cobblemontrialsedition.screen;

import com.lemenok.cobblemontrialsedition.config.SpawnablePokemonProperties;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Consumer;

public class PokemonRosterTab implements Tab {
    private final String title;
    private final TrialSpawnerConfigScreen screen;
    private final List<SpawnablePokemonProperties> roster;
    private PokemonRosterList listWidget;
    private final Button addButton;

    public PokemonRosterTab(String title, TrialSpawnerConfigScreen screen, List<SpawnablePokemonProperties> roster) {
        this.title = title;
        this.screen = screen;
        this.roster = roster;

        this.addButton = Button.builder(Component.literal("+ Add Pokemon"), btn -> {
            Minecraft.getInstance().setScreen(new PokemonEditScreen(screen, null, newPokemon -> {
                this.roster.add(newPokemon);
                this.listWidget.refreshEntries(this.roster);
            }));
        }).bounds(0, 0, 120, 20).build();

        // 1. Instantiate the widget immediately so TabManager can register it during screen init()
        this.listWidget = new PokemonRosterList(
                Minecraft.getInstance(),
                screen.width,  // Initial bounds
                screen.height,
                0,
                36,            // Height of each row item
                roster
        );
    }

    @Override
    public void doLayout(ScreenRectangle screenRectangle) {
        this.addButton.setX(screenRectangle.left() + 5);
        this.addButton.setY(screenRectangle.top() + 2);

        this.listWidget.setX(screenRectangle.left());
        this.listWidget.setY(screenRectangle.top() + 24);
        this.listWidget.setWidth(screenRectangle.width());
        this.listWidget.setHeight(screenRectangle.height() - 24);
    }

    @Override
    public Component getTabTitle() {
        return Component.literal(title);
    }

    @Override
    public void visitChildren(Consumer<AbstractWidget> consumer) {
        consumer.accept(this.addButton);
        consumer.accept(this.listWidget);
    }
}
