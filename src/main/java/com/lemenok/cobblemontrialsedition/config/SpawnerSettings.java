package com.lemenok.cobblemontrialsedition.config;

import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.relocations.oracle.truffle.js.nodes.access.LocalVarIncNode;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lemenok.cobblemontrialsedition.block.entity.cobblemontrialspawner.CobblemonTrialSpawnerConfig;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SpawnerSettings {
    private final List<BlockEntityType> SpawnerTypesToReplace;
    private final List<EntityType> SpawnerEntityToReplace;
    private final int TicksBetweenSpawnAttempts;
    private final int SpawnerCooldown;
    private final int PlayerDetectionRange;
    private final int SpawnRange;
    private final int MaximumNumberOfSimultaneousPokemon;
    private final int MaximumNumberOfSimultaneousPokemonAddedPerPlayer;
    private final int TotalNumberOfPokemonPerTrial;
    private final int TotalNumberOfPokemonPerTrialAddedPerPlayer;
    private SimpleWeightedRandomList<LootTable> SpawnerLootTable;
    private SimpleWeightedRandomList<LootTable> SpawnerOminousLootTable;
    private final boolean OminousSpawnerAttacksEnabled;
    private final boolean DoPokemonSpawnedGlow;
    private List<SpawnablePokemonSettings> ListOfPokemonToSpawn;
    private List<SpawnablePokemonSettings> ListOfOminousPokemonToSpawn;

    private final ResourceLocation SpawnerType = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(BlockEntityType.MOB_SPAWNER);
    private final ResourceLocation TrialSpawnerType = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(BlockEntityType.TRIAL_SPAWNER);

    public SpawnerSettings(int ticksBetweenSpawnAttempts, int spawnerCooldown, int playerDetectionRange, int spawnRange,
                           int maximumNumberOfSimultaneousPokemon, int maximumNumberOfSimultaneousPokemonAddedPerPlayer,
                           int totalNumberOfPokemonPerTrial, int totalNumberOfPokemonPerTrialAddedPerPlayer,
                           boolean ominousSpawnerAttacksEnabled, boolean doPokemonSpawnedGlow){

        TicksBetweenSpawnAttempts = ticksBetweenSpawnAttempts;
        SpawnerCooldown = spawnerCooldown;
        PlayerDetectionRange = playerDetectionRange;
        SpawnRange = spawnRange;
        MaximumNumberOfSimultaneousPokemon = maximumNumberOfSimultaneousPokemon;
        MaximumNumberOfSimultaneousPokemonAddedPerPlayer = maximumNumberOfSimultaneousPokemonAddedPerPlayer;
        TotalNumberOfPokemonPerTrial = totalNumberOfPokemonPerTrial;
        TotalNumberOfPokemonPerTrialAddedPerPlayer = totalNumberOfPokemonPerTrialAddedPerPlayer;
        OminousSpawnerAttacksEnabled = ominousSpawnerAttacksEnabled;
        DoPokemonSpawnedGlow = doPokemonSpawnedGlow;

        SpawnerTypesToReplace = new ArrayList<>();
        SpawnerEntityToReplace = new ArrayList<>();
    }

    public void SetSpawnerTypesToReplace(String[] spawnerTypesToReplace){

        for(String spawnerTypeToReplace: spawnerTypesToReplace){
            String[] splitString = spawnerTypeToReplace.split(":");
            ResourceLocation spawnerTypeResourceLocation = ResourceLocation.fromNamespaceAndPath(splitString[0], splitString[1]);

            // At the moment we are only allowing Spawners and Trial Spawners to be replaced.
            // Other logic in here is future proofing for other types I may decide on adding.
            if(spawnerTypeResourceLocation.equals(SpawnerType) || spawnerTypeResourceLocation.equals(TrialSpawnerType) )
            {
                BlockEntityType<?> blockEntityType = BuiltInRegistries.BLOCK_ENTITY_TYPE.getOptional(spawnerTypeResourceLocation)
                        .orElseThrow(() -> new IllegalArgumentException("Unknown Block Entity: " + spawnerTypeResourceLocation));

                SpawnerTypesToReplace.add(blockEntityType);
            }
        }
    }

    public void SetSpawnerEntityToReplace(String[] entitiesToReplace){

        for(String entity: entitiesToReplace){
            String[] splitString = entity.split(":");

            ResourceLocation entityResourceLocation = ResourceLocation.fromNamespaceAndPath(splitString[0], splitString[1]);

            EntityType entityType = BuiltInRegistries.ENTITY_TYPE.getOptional(entityResourceLocation)
                    .orElseThrow(() -> new IllegalArgumentException("Unknown Entity: " + entity));

            SpawnerEntityToReplace.add(entityType);
        }
    }

    public void SetLootTable(@Nullable List<LootTable> lootTables, boolean isOminous){
        SimpleWeightedRandomList.Builder<LootTable> weightedLootTableListBuilder = new SimpleWeightedRandomList.Builder<>();

        if(lootTables.isEmpty()){
            // No loot table is detected, so we load the default loot table from the trial spawner at placement time.
            if(isOminous) SpawnerOminousLootTable = weightedLootTableListBuilder.build();
            else SpawnerLootTable = weightedLootTableListBuilder.build();
        } else {

            for(LootTable lootTable: lootTables){
                weightedLootTableListBuilder.add(lootTable);
            }

            if(isOminous) SpawnerOminousLootTable = weightedLootTableListBuilder.build();
            else SpawnerLootTable = weightedLootTableListBuilder.build();
        }
    }

    public void SetListOfPokemonToSpawn(List<SpawnablePokemonSettings> spawnablePokemonList, boolean isOminous){
        if(isOminous)
            ListOfOminousPokemonToSpawn = spawnablePokemonList;
        else
            ListOfPokemonToSpawn = spawnablePokemonList;
    }

    public boolean DoesSpawnerSettingsContainEntityToReplace(EntityType entityType){
        return SpawnerEntityToReplace.contains(entityType);
    }

    public boolean DoesSpawnerSettingsContainSpawnerTypesToReplace(BlockEntityType blockEntityType){
        return SpawnerTypesToReplace.contains(blockEntityType);
    }


    public int getTicksBetweenSpawnAttempts() {
        return TicksBetweenSpawnAttempts;
    }

    public int getSpawnerCooldown() {
        return SpawnerCooldown;
    }

    public int getPlayerDetectionRange() {
        return PlayerDetectionRange;
    }

    public int getSpawnRange() {
        return SpawnRange;
    }

    public int getMaximumNumberOfSimultaneousPokemon() {
        return MaximumNumberOfSimultaneousPokemon;
    }

    public int getMaximumNumberOfSimultaneousPokemonAddedPerPlayer() {
        return MaximumNumberOfSimultaneousPokemonAddedPerPlayer;
    }

    public int getTotalNumberOfPokemonPerTrial() {
        return TotalNumberOfPokemonPerTrial;
    }

    public int getTotalNumberOfPokemonPerTrialAddedPerPlayer() {
        return TotalNumberOfPokemonPerTrialAddedPerPlayer;
    }

    public SimpleWeightedRandomList<LootTable> getSpawnerLootTable() {
        return SpawnerLootTable;
    }

    public SimpleWeightedRandomList<LootTable> getSpawnerOminousLootTable() {
        return SpawnerOminousLootTable;
    }

    public boolean areOminousSpawnerAttacksEnabled() {
        return OminousSpawnerAttacksEnabled;
    }

    public SimpleWeightedRandomList<SpawnData> getListOfPokemonToSpawn(ServerLevel serverLevel, boolean isOminous) {
        var spawnDataList = new SimpleWeightedRandomList.Builder<SpawnData>();
        List<SpawnablePokemonSettings> listOfPokemonToSpawn = new ArrayList<>();

        if(isOminous)
            listOfPokemonToSpawn = this.ListOfOminousPokemonToSpawn;
        else
            listOfPokemonToSpawn = this.ListOfPokemonToSpawn;

        for(SpawnablePokemonSettings spawnablePokemonSettings: listOfPokemonToSpawn){

            PokemonProperties newPokemonProperties = spawnablePokemonSettings.getSpawnablePokemonProperties();
            Pokemon newPokemon = newPokemonProperties.create();
            newPokemon.setScaleModifier(spawnablePokemonSettings.getScaleModifier());

            CompoundTag pokemonNbt = newPokemon.saveToNBT(serverLevel.registryAccess(), new CompoundTag());

            if(spawnablePokemonSettings.isUncatchable()){
                // Make pokemon uncatchable
                String[] data = new String[] { "uncatchable", "uncatchable", "uncatchable" };
                ListTag listTag = new ListTag();
                for (String stringData : data) { listTag.add(StringTag.valueOf(stringData)); }
                pokemonNbt.put("PokemonData", listTag);
            }

            CompoundTag entityNbt = new CompoundTag();
            entityNbt.put("Pokemon", pokemonNbt);
            entityNbt.putString("id", "cobblemon:pokemon");
            entityNbt.putString("PoseType", "WALK");
            if(this.DoPokemonSpawnedGlow) entityNbt.putByte("Glowing", (byte) 1);

            if(spawnablePokemonSettings.isMustBeDefeatedInBattle()){
                entityNbt.putBoolean("Invulnerable", true);
            }

            CompoundTag spawnData = new CompoundTag();
            spawnData.put("entity", entityNbt);

            DataResult<SpawnData> result = SpawnData.CODEC.parse(NbtOps.INSTANCE, spawnData);
            SpawnData newPokemonSpawnData = result.getOrThrow();

            spawnDataList.add(newPokemonSpawnData, spawnablePokemonSettings.getSpawnWeight());
        }

        return spawnDataList.build();
    }

    private void getDefaultTrialSpawnerLootTables(SimpleWeightedRandomList.Builder<LootTable> weightedLootTableListBuilder, boolean isOminous) {
        ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();

        if(!isOminous){
            ResourceLocation chamberKey = BuiltInLootTables.SPAWNER_TRIAL_CHAMBER_KEY.location();
            ResourceLocation chamberConsumables = BuiltInLootTables.SPAWNER_TRIAL_CHAMBER_CONSUMABLES.location();

            ResourceLocation chamberKeyPath = ResourceLocation.fromNamespaceAndPath(chamberKey.getNamespace(), chamberKey.getPath());
            ResourceLocation chamberConsumablesPath = ResourceLocation.fromNamespaceAndPath(chamberConsumables.getNamespace(), chamberConsumables.getPath());

            LootTable chamberKeyLootTable = getLootTableFromResourcePath(weightedLootTableListBuilder, resourceManager, chamberKey);
            LootTable chamberConsumablesLootTable = getLootTableFromResourcePath(weightedLootTableListBuilder, resourceManager, chamberConsumablesPath);

            weightedLootTableListBuilder.add(chamberKeyLootTable).add(chamberConsumablesLootTable);
        } else {
            ResourceLocation chamberKey = BuiltInLootTables.SPAWNER_OMINOUS_TRIAL_CHAMBER_KEY.location();
            ResourceLocation chamberConsumables = BuiltInLootTables.SPAWNER_OMINOUS_TRIAL_CHAMBER_CONSUMABLES.location();

            ResourceLocation chamberKeyPath = ResourceLocation.fromNamespaceAndPath(chamberKey.getNamespace(), chamberKey.getPath());
            ResourceLocation chamberConsumablesPath = ResourceLocation.fromNamespaceAndPath(chamberConsumables.getNamespace(), chamberConsumables.getPath());

            LootTable chamberKeyLootTable = getLootTableFromResourcePath(weightedLootTableListBuilder, resourceManager, chamberKeyPath);
            LootTable chamberConsumablesLootTable = getLootTableFromResourcePath(weightedLootTableListBuilder, resourceManager, chamberConsumablesPath);

            weightedLootTableListBuilder.add(chamberKeyLootTable).add(chamberConsumablesLootTable);
        }
    }

    private static LootTable getLootTableFromResourcePath(SimpleWeightedRandomList.Builder<LootTable> weightedLootTableListBuilder, ResourceManager resourceManager, ResourceLocation keyPath) {
        try {
            Optional<Resource> resourceOptional = resourceManager.getResource(keyPath);
            if(resourceOptional.isEmpty()) return LootTable.EMPTY;

            Resource res = resourceOptional.get();
            try (InputStream inputStream = res.open(); InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                JsonElement jsonElement = JsonParser.parseReader(reader);
                DataResult<Holder<LootTable>> dataResult = LootTable.CODEC.parse(JsonOps.INSTANCE, jsonElement);

                return dataResult.getOrThrow(errorMessage ->
                        new IllegalStateException("Failed to parse loot table: " + errorMessage)).value();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
