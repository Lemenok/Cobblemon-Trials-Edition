package com.lemenok.cobblemontrialsedition.screen;

import com.lemenok.cobblemontrialsedition.config.SpawnablePokemonProperties;
import com.lemenok.cobblemontrialsedition.config.SpawnerProperties;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class TrialSpawnerConfigScreen extends Screen {

    private final BlockPos pos;
    private final SpawnerProperties originalProperties;
    private final TabManager tabManager = new TabManager(this::addRenderableWidget, this::removeWidget);
    private TabNavigationBar tabNavigationBar;

    private Button saveAndCloseButton;

    public int ticksBetweenSpawnAttempts;
    public int spawnerCooldown;
    public int playerDetectionRange;
    public int spawnRange;
    public int maximumNumberOfSimultaneousPokemon;
    public int maximumNumberOfSimultaneousPokemonAddedPerPlayer;
    public int totalNumberOfPokemonPerTrial;
    public int totalNumberOfPokemonPerTrialAddedPerPlayer;

    public boolean ominousSpawnerAttacksEnabled;
    public boolean doPokemonSpawnedGlow;

    public List<ResourceLocation> blockTypesToReplace;
    public List<ResourceLocation> mobEntitiesInSpawnerToReplace;
    public List<ResourceLocation> lootTables;
    public List<ResourceLocation> ominousLootTables;

    public List<SpawnablePokemonProperties> editableNormalRoster;
    public List<SpawnablePokemonProperties> editableOminousRoster;

    public TrialSpawnerConfigScreen(BlockPos pos, SpawnerProperties spawnerProperties) {
        super(Component.literal("Cobblemon Trial Spawner Config"));
        this.pos = pos;
        originalProperties = spawnerProperties;

        this.ticksBetweenSpawnAttempts = spawnerProperties.ticksBetweenSpawnAttempts();
        this.spawnerCooldown = spawnerProperties.spawnerCooldown();
        this.playerDetectionRange = spawnerProperties.playerDetectionRange();
        this.spawnRange = spawnerProperties.spawnRange();
        this.maximumNumberOfSimultaneousPokemon = spawnerProperties.maximumNumberOfSimultaneousPokemon();
        this.maximumNumberOfSimultaneousPokemonAddedPerPlayer = spawnerProperties.maximumNumberOfSimultaneousPokemonAddedPerPlayer();
        this.totalNumberOfPokemonPerTrial = spawnerProperties.totalNumberOfPokemonPerTrial();
        this.totalNumberOfPokemonPerTrialAddedPerPlayer = spawnerProperties.totalNumberOfPokemonPerTrialAddedPerPlayer();

        this.ominousSpawnerAttacksEnabled = spawnerProperties.ominousSpawnerAttacksEnabled();
        this.doPokemonSpawnedGlow = spawnerProperties.doPokemonSpawnedGlow();

        this.lootTables = new ArrayList<>(spawnerProperties.lootTables());
        this.ominousLootTables = new ArrayList<>(spawnerProperties.ominousLootTables());

        this.editableNormalRoster = new ArrayList<>(spawnerProperties.listOfPokemonToSpawn());
        this.editableOminousRoster = new ArrayList<>(spawnerProperties.listOfOminousPokemonToSpawn());
    }

    @Override
    protected void init() {
        super.init();

        this.tabNavigationBar = TabNavigationBar.builder(this.tabManager, this.width)
                .addTabs(new SpawnerSettingsTab(this))
                .addTabs(new PokemonRosterTab("Normal Roster", this, editableNormalRoster))
                .addTabs(new PokemonRosterTab("Ominous Roster", this, editableOminousRoster))
                .build();

        this.addRenderableWidget(this.tabNavigationBar);

        this.saveAndCloseButton = Button.builder(Component.literal("Save & Close"), btn -> {
            // Reconstruct updated properties record from GUI fields
            SpawnerProperties updatedProperties = new SpawnerProperties(
                    this.blockTypesToReplace,
                    this.mobEntitiesInSpawnerToReplace,
                    this.ticksBetweenSpawnAttempts,
                    this.spawnerCooldown,
                    this.playerDetectionRange,
                    this.spawnRange,
                    this.maximumNumberOfSimultaneousPokemon,
                    this.maximumNumberOfSimultaneousPokemonAddedPerPlayer,
                    this.totalNumberOfPokemonPerTrial,
                    this.totalNumberOfPokemonPerTrialAddedPerPlayer,
                    this.lootTables,
                    this.ominousLootTables,
                    this.ominousSpawnerAttacksEnabled,
                    this.doPokemonSpawnedGlow,
                    this.editableNormalRoster,
                    this.editableOminousRoster
            );

            // Send packet to server
            SaveSpawnerC2SPacket.send(this.pos, updatedProperties);
            this.minecraft.setScreen(null);
        }).width(200).build();

        this.addRenderableWidget(this.saveAndCloseButton);

        this.repositionElements();
        this.tabNavigationBar.selectTab(0, false);
    }

    @Override
    protected void repositionElements() {
        if (this.tabNavigationBar != null) {
            this.tabNavigationBar.setWidth(this.width);
            this.tabNavigationBar.arrangeElements();

            int footerHeight = 40;

            if (this.saveAndCloseButton != null) {
                // Position the button centered horizontally, near the bottom edge
                this.saveAndCloseButton.setPosition((this.width - this.saveAndCloseButton.getWidth()) / 2, this.height - 30);
            }

            // Calculate the screen area directly below the navigation tabs
            int topOffset = this.tabNavigationBar.getRectangle().bottom();
            ScreenRectangle screenRectangle = new ScreenRectangle(0, topOffset, this.width, this.height - topOffset - footerHeight);

            // This is the missing piece that tells the tab where to render and calls doLayout()
            this.tabManager.setTabArea(screenRectangle);
        }
    }

    @Override
    public void render(net.minecraft.client.gui.GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
}
