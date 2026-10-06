package com.lemenok.cobblemontrialsedition.screen;

import com.lemenok.cobblemontrialsedition.config.SpawnerProperties;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class PokemonWaveTab implements Tab {
    private final String title;
    private final TrialSpawnerConfigScreen screen;
    private final List<SpawnerProperties.WaveDefinition> waves;
    private PokemonWaveList listWidget = null;
    private final Button addWaveButton;

    public PokemonWaveTab(String title, TrialSpawnerConfigScreen screen, List<SpawnerProperties.WaveDefinition> waves) {
        this.title = title;
        this.screen = screen;
        this.waves = waves;

        this.addWaveButton = Button.builder(Component.literal("+ Add Wave"), btn -> {
            // Adds a new blank wave.
            this.waves.add(new SpawnerProperties.WaveDefinition(4, 1, new ArrayList<>()));
            this.listWidget.refreshEntries(this.waves);
        }).bounds(0, 0, 100, 20).build();

        this.listWidget = new PokemonWaveList(
                Minecraft.getInstance(),
                screen.width,
                screen.height,
                0,
                36, // Height of each row item
                this.waves,
                screen
        );
    }

    @Override
    public void doLayout(ScreenRectangle screenRectangle) {
        this.addWaveButton.setX(screenRectangle.left() + 5);
        this.addWaveButton.setY(screenRectangle.top() + 2);

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
        consumer.accept(this.addWaveButton);
        consumer.accept(this.listWidget);
    }
}