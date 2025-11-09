package com.lemenok.cobblemontrialsedition.config;

import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.mojang.serialization.DataResult;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.ArrayList;
import java.util.List;

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

    public void SetLootTable(LootTable lootTable, boolean isOminous){
        if(isOminous)
            SpawnerOminousLootTable = new SimpleWeightedRandomList.Builder<LootTable>().add(lootTable).build();
        else
            SpawnerLootTable = new SimpleWeightedRandomList.Builder<LootTable>().add(lootTable).build();
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
            if(DoPokemonSpawnedGlow) entityNbt.putByte("Glowing", (byte) 1);

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
}
