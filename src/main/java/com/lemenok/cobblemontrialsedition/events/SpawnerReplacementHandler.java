package com.lemenok.cobblemontrialsedition.events;

import com.cobblemon.mod.common.api.abilities.Ability;
import com.cobblemon.mod.common.api.abilities.AbilityTemplate;
import com.cobblemon.mod.common.api.pokemon.Natures;
import com.cobblemon.mod.common.pokemon.*;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.lemenok.cobblemontrialsedition.CobblemonTrialsEdition;
import com.lemenok.cobblemontrialsedition.Config;
import com.lemenok.cobblemontrialsedition.block.ModBlocks;
import com.lemenok.cobblemontrialsedition.block.entity.CobblemonTrialSpawnerEntity;
import com.lemenok.cobblemontrialsedition.block.entity.cobblemontrialspawner.CobblemonTrialSpawnerConfig;
import com.lemenok.cobblemontrialsedition.config.SpawnerProperties;
import com.lemenok.cobblemontrialsedition.config.SpawnerSettings;
import com.lemenok.cobblemontrialsedition.config.StructureProperties;
import com.lemenok.cobblemontrialsedition.config.StructureSettings;
import com.lemenok.cobblemontrialsedition.processors.ConfigProcessor;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawner;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraft.util.random.WeightedEntry.Wrapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class SpawnerReplacementHandler {
    // TODO: Create Centralized Logger
    // TODO: Pull data from config
    private static final Logger LOGGER = LogManager.getLogger(CobblemonTrialsEdition.MODID);

    @SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load event) {

        if(!Config.REPLACE_GENERATED_SPAWNERS_WITH_COBBLEMON_SPAWNERS.get())
            return;

        Level level = (Level) event.getLevel();

        // Verify that the events are chunk events.
        if(!(level instanceof ServerLevel serverLevel)) return;
        if(!(event.getChunk() instanceof LevelChunk chunk)) return;

        if (!event.isNewChunk()) return;

        List<BlockEntity> listOfBlockEntities = new ArrayList<>();
        for (BlockEntity blockEntity: chunk.getBlockEntities().values()) {
            if (blockEntity instanceof SpawnerBlockEntity || blockEntity instanceof TrialSpawnerBlockEntity){
                listOfBlockEntities.add(blockEntity);
            }
        }

        if(listOfBlockEntities.isEmpty()) return;

        StructureManager structureManager = serverLevel.structureManager();
        RegistryAccess registryAccess = level.registryAccess();
        Registry<Structure> structureRegistry = registryAccess.registryOrThrow(Registries.STRUCTURE);

        List<StructureProperties> listOfStructuresToModify = getListOfStructuresToModify(level);

        for (BlockEntity blockEntity : listOfBlockEntities) {
            try {
                BlockPos blockEntityPosition = blockEntity.getBlockPos();

                if (!serverLevel.isLoaded(blockEntityPosition)) continue;

                var allStructuresAtPosition = structureManager.getAllStructuresAt(blockEntityPosition);

                // If the there are no structures but still a spawner, this is likely from a Feature.
                // Check if the user has Default Spawners turned on and no structures, if both are true replace the spawner.
                if (allStructuresAtPosition.isEmpty() && Config.REPLACE_SPAWNERS_OUTSIDE_OF_STRUCTURES_WITH_COBBLEMON_SPAWNERS.get()) {

                    /*CobblemonTrialSpawnerEntity cobblemonTrialSpawnerEntity = new CobblemonTrialSpawnerEntity(blockEntityPosition, ModBlocks.COBBLEMON_TRIAL_SPAWNER.get().defaultBlockState());
                    cobblemonTrialSpawnerEntity.loadWithComponents(BuildPokemonForSpawn(serverLevel, blockEntityPosition), serverLevel.registryAccess());
                    cobblemonTrialSpawnerEntity.setChanged();

                    chunk.getSection(serverLevel.getSectionIndex(blockEntityPosition.getY())).setBlockState(blockEntityPosition.getX() & 15, blockEntityPosition.getY() & 15, blockEntityPosition.getZ() & 15, cobblemonTrialSpawnerEntity.getBlockState());
                    // ServerLevel set required to properly allow the Trial Spawner to work.
                    serverLevel.setBlockEntity(cobblemonTrialSpawnerEntity);*/
                    LOGGER.info("Replaced Spawner at Location '{}'", blockEntityPosition);
                    return;
                }
                // Check to see if the Structure is on the CustomSpawner List
                else if(Config.REPLACE_SPAWNERS_IN_STRUCTURES_WITH_COBBLEMON_SPAWNERS.get()) {
                    for (Structure structure: allStructuresAtPosition.keySet()){
                        ResourceLocation resourceAtPosition = structureRegistry.getKey(structure);

                        // Check if Structure Exists to have its spawners swapped.
                                //StructureSettings structureSettingsConfig = ConfigProcessor.GLOBAL_SETTINGS.getStructureSettingsByResourceLocation(resourceAtPosition);
                        List<SpawnerProperties> spawnerPropertiesForStructure = new ArrayList<>();

                        for(StructureProperties properties: listOfStructuresToModify){
                            spawnerPropertiesForStructure = properties.getSpawnerPropertiesIfResourceLocationMatches(resourceAtPosition);
                            if(spawnerPropertiesForStructure != null)
                                break;
                        }

                        if(spawnerPropertiesForStructure != null) {
                            EntityType spawnerEntityType = null;

                            // Grab Entity in spawner to specify which spawner to replace.
                            if(blockEntity instanceof SpawnerBlockEntity){
                                spawnerEntityType = Objects.requireNonNull(((SpawnerBlockEntity) blockEntity).getSpawner().getOrCreateDisplayEntity(level, blockEntity.getBlockPos())).getType();
                            }
                            if (blockEntity instanceof TrialSpawnerBlockEntity){
                                spawnerEntityType = getEntityTypeFromTrialSpawner(Objects.requireNonNull(((TrialSpawnerBlockEntity) blockEntity).getTrialSpawner()));
                            }

                            SpawnerProperties newSpawner = null;
                            // Grab the spawner settings that match the blockEntity and Entity in the spawner to be replaced.
                            for(SpawnerProperties spawner: spawnerPropertiesForStructure) {
                                if(spawner.doesSpawnerSettingsContainEntityType(level, spawnerEntityType, blockEntity)) {
                                    newSpawner = spawner;
                                    break;
                                }
                            }

                            //SpawnerSettings spawnerSettings = structureSettingsConfig.GetSpawnerSettingsByStructureIdAndSpawnerEntityToReplace(resourceAtPosition, spawnerEntityType);
                            if(newSpawner != null) {
                                // Setup all configuration for the spawner.
                                CobblemonTrialSpawnerConfig cobblemonTrialSpawnerConfig;
                                CobblemonTrialSpawnerConfig cobblemonTrialSpawnerOminousConfig;

                                cobblemonTrialSpawnerConfig = new CobblemonTrialSpawnerConfig(
                                        newSpawner.spawnRange(),
                                        newSpawner.totalNumberOfPokemonPerTrial(),
                                        newSpawner.maximumNumberOfSimultaneousPokemon(),
                                        newSpawner.totalNumberOfPokemonPerTrial(),
                                        newSpawner.maximumNumberOfSimultaneousPokemonAddedPerPlayer(),
                                        newSpawner.ticksBetweenSpawnAttempts(),
                                        newSpawner.ominousSpawnerAttacksEnabled(),
                                        newSpawner.getListOfPokemonToSpawn(serverLevel, false),
                                        newSpawner.getLootTables(blockEntity, false),
                                        BuiltInLootTables.SPAWNER_TRIAL_ITEMS_TO_DROP_WHEN_OMINOUS
                                );
                                cobblemonTrialSpawnerOminousConfig = new CobblemonTrialSpawnerConfig(
                                        newSpawner.spawnRange(),
                                        newSpawner.totalNumberOfPokemonPerTrial(),
                                        newSpawner.maximumNumberOfSimultaneousPokemon(),
                                        newSpawner.totalNumberOfPokemonPerTrial(),
                                        newSpawner.maximumNumberOfSimultaneousPokemonAddedPerPlayer(),
                                        newSpawner.ticksBetweenSpawnAttempts(),
                                        newSpawner.ominousSpawnerAttacksEnabled(),
                                        newSpawner.getListOfPokemonToSpawn(serverLevel, true),
                                        newSpawner.getLootTables(blockEntity, true),
                                        BuiltInLootTables.SPAWNER_TRIAL_ITEMS_TO_DROP_WHEN_OMINOUS
                                );

                                CobblemonTrialSpawnerEntity cobblemonTrialSpawnerEntity = new CobblemonTrialSpawnerEntity(
                                        blockEntityPosition, ModBlocks.COBBLEMON_TRIAL_SPAWNER.get().defaultBlockState());

                                cobblemonTrialSpawnerEntity.getCobblemonTrialSpawner().setConfig(cobblemonTrialSpawnerConfig, false);
                                cobblemonTrialSpawnerEntity.getCobblemonTrialSpawner().setConfig(cobblemonTrialSpawnerOminousConfig, true);
                                cobblemonTrialSpawnerEntity.getCobblemonTrialSpawner().setTargetCooldownLength(newSpawner.spawnerCooldown());
                                cobblemonTrialSpawnerEntity.getCobblemonTrialSpawner().setRequiredPlayerRange(newSpawner.playerDetectionRange());
                                cobblemonTrialSpawnerEntity.getCobblemonTrialSpawner().getData().getOrCreateNextSpawnData(cobblemonTrialSpawnerEntity.getCobblemonTrialSpawner(),RandomSource.create());
                                cobblemonTrialSpawnerEntity.getCobblemonTrialSpawner().markUpdated();
                                cobblemonTrialSpawnerEntity.markUpdated();

                                chunk.getSection(serverLevel.getSectionIndex(blockEntityPosition.getY())).setBlockState(blockEntityPosition.getX() & 15, blockEntityPosition.getY() & 15, blockEntityPosition.getZ() & 15, cobblemonTrialSpawnerEntity.getBlockState());
                                serverLevel.setBlockEntity(cobblemonTrialSpawnerEntity);
                                LOGGER.info("Replaced Structure Spawner at Location '{}'", blockEntityPosition);
                                return;
                            }
                        }
                    }
                }
                // If there are still structures around the spawner this means that the spawner is in a structure
                // that the user has defined they WANT to leave the default spawner. So we do nothing.

            } catch (Exception ex) {
                LOGGER.error(ex);
                throw ex;
            }
        }
    }

    private static List<StructureProperties> getListOfStructuresToModify(Level level) {
        var ctsstructureRegistry = level.registryAccess().registryOrThrow(CobblemonTrialsEdition.ClientModEvents.COBBLEMON_TRIALS_STRUCTURE_REGISTRY);
        Set<String> pathsInNamespace = ctsstructureRegistry.keySet().stream()
                .filter(resourceLocation -> resourceLocation.getNamespace().equals(CobblemonTrialsEdition.MODID))
                .map(ResourceLocation::getPath)
                .collect(Collectors.toSet());

        List<StructureProperties> listOfStructuresToModify = new ArrayList<>();
        for(String path: pathsInNamespace){
            listOfStructuresToModify.add(ctsstructureRegistry.get(ResourceLocation.fromNamespaceAndPath(CobblemonTrialsEdition.MODID, path)));
        }

        return listOfStructuresToModify;
    }

    private EntityType getEntityTypeFromTrialSpawner(TrialSpawner trialSpawner) {
        String entityId = trialSpawner.getConfig().spawnPotentialsDefinition().getRandomValue(RandomSource.create()).get().entityToSpawn().getString("id");
        ResourceLocation resourceLocation = ResourceLocation.tryParse(entityId);

        return BuiltInRegistries.ENTITY_TYPE.get(resourceLocation);
    }

    private List<LootTable> getTrialSpawnerLootTable(SimpleWeightedRandomList<ResourceKey<LootTable>> resourceKeySimpleWeightedRandomList, ServerLevel serverLevel) {

        List<LootTable> lootTableList = new ArrayList<>();

        StreamSupport.stream(resourceKeySimpleWeightedRandomList.unwrap().spliterator(), false)
                .map(Wrapper::data)
                .forEach(lootTableResourceKey -> {
                    lootTableList.add(serverLevel.getServer().reloadableRegistries().getLootTable(lootTableResourceKey));
                });

        return lootTableList;
    }

    private static LootTable getLootTableFromResourcePath(ResourceLocation keyPath) {
        ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();

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

    /*
    private void getDefaultTrialSpawnerLootTables(SimpleWeightedRandomList.Builder<LootTable> weightedLootTableListBuilder, boolean isOminous) {


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
    }*/




    private boolean isStructureConfiguredToHaveSpawnersReplaced(Map<Structure, LongSet> allStructuresAtPosition) {

        for (Structure structure : allStructuresAtPosition.keySet()){

            // Get the ResourceKey for the given Structure instance.
            Optional<ResourceKey<StructureType<?>>> optionalStructureKey = BuiltInRegistries.STRUCTURE_TYPE.getResourceKey(structure.type());
            // If the Structure is in the registry, proceed with the comparison.
            if (optionalStructureKey.isPresent()) {
                ResourceKey<StructureType<?>> structureKey = optionalStructureKey.get();
                ResourceLocation structureLocation = structureKey.location(); // Get the ResourceLocation from the key.

                if(ConfigProcessor.GLOBAL_SETTINGS.getStructureSettingsByResourceLocation(structureLocation) != null){
                    return true;
                }
            }
        }

        return false;

        /*for (ResourceLocation resourceLocation : ConfigProcessor.WHITELISTED_STRUCTURES) {
            try {
                for (Structure structure : allStructuresAtPosition.keySet()){
                    // Get the ResourceKey for the given Structure instance.
                    Optional<ResourceKey<StructureType<?>>> optionalStructureKey = BuiltInRegistries.STRUCTURE_TYPE.getResourceKey(structure.type());

                    // If the Structure is in the registry, proceed with the comparison.
                    if (optionalStructureKey.isPresent()) {
                        ResourceKey<StructureType<?>> structureKey = optionalStructureKey.get();
                        ResourceLocation structureLocation = structureKey.location(); // Get the ResourceLocation from the key.

                        if(resourceLocation.equals(structureLocation)) return true;
                    }
                }
            }
            catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }*/
        //return false;
    }

    public static CompoundTag BuildPokemonForSpawn(ServerLevel serverLevel, BlockPos blockEntityPosition) {
        Species species = new Species();
        species.setName("charcadet");
        species.setResourceIdentifier(ResourceLocation.fromNamespaceAndPath("cobblemon","charcadet"));
        species.initialize();

        Pokemon pokemon = new Pokemon();

        pokemon.setShiny(true);

        pokemon.setGender(Gender.GENDERLESS);

        pokemon.setLevel(15);

        // Setting Scale of Pokemon
        pokemon.setScaleModifier(2);

        Nature nature = Natures.INSTANCE.getADAMANT();
        pokemon.setNature(Natures.INSTANCE.getADAMANT());

        EVs evs = new EVs();
        pokemon.setEvs$common(evs);

        IVs ivs = new IVs();
        pokemon.setIvs$common(ivs);

        // Setup Abilities
        AbilityTemplate abilityTemplate = new AbilityTemplate();
        CompoundTag abilityNbt = new CompoundTag();
        abilityNbt.putString("AbilityName", "flashfire");
        Ability ability = abilityTemplate.create(abilityNbt);

        // Ability must be before species.
        pokemon.updateAbility(ability);
        pokemon.setSpecies(species);
        pokemon.initialize();

        CompoundTag pokemonNbt = pokemon.saveToNBT(serverLevel.registryAccess(), new CompoundTag());

        // Make pokemon uncatchable
        String[] data = new String[] { "uncatchable", "uncatchable", "uncatchable" };
        ListTag listTag = new ListTag();
        for (String stringData : data) { listTag.add(StringTag.valueOf(stringData)); }
        pokemonNbt.put("PokemonData", listTag);

        CompoundTag entityNbt = new CompoundTag();
        entityNbt.put("Pokemon", pokemonNbt);
        entityNbt.putString("id", "cobblemon:pokemon");
        entityNbt.putString("PoseType", "WALK");

        // Check if Invulnerable
        entityNbt.putBoolean("Invulnerable", true);

        // Build spawn_data compound that contains entity
        CompoundTag spawnData = new CompoundTag();
        spawnData.put("entity", entityNbt);

        CompoundTag trialNBT = new CompoundTag();
        trialNBT.put("spawn_data",spawnData);
        trialNBT.putInt("target_cooldown_length", 1200);
        trialNBT.putInt("x", blockEntityPosition.getX());
        trialNBT.putInt("y", blockEntityPosition.getY());
        trialNBT.putInt("z", blockEntityPosition.getZ());

        LOGGER.info("Trial Data: '{}'", trialNBT);

        return trialNBT;

        /*PokemonEntity pokemonEntity = new PokemonEntity(serverLevel, pokemon, CobblemonEntities.POKEMON);

        CompoundTag poseType = new CompoundTag();
        poseType.putString("POKEMON_POSE_TYPE", "WALK");

        pokemonEntity.load(poseType);

        return pokemonEntity;*/
    }

    public static CompoundTag BuildTrialSpawnerNBT(BlockPos blockEntityPosition) {

        // Build the innermost Pokemon compound
        CompoundTag pokemon = new CompoundTag();

        // Simple string/number/byte/float fields
        pokemon.putByte("Tradeable", (byte)1);
        pokemon.put("PersistentData", new CompoundTag()); // empty compound
        pokemon.putString("Gender", "GENDERLESS");
        pokemon.putInt("HealingTimer", 60);
        pokemon.putInt("Friendship", 140);
        pokemon.putByte("GmaxFactor", (byte)0);
        pokemon.putString("CaughtBall", "cobblemon:poke_ball");
        pokemon.putFloat("ScaleModifier", 1.0f);
        pokemon.putString("FormId", "");
        pokemon.putIntArray("UUID", new int[] { -2017762761, -223001211, -1359438121, 803759701 });
        pokemon.putByte("Shiny", (byte)0);
        pokemon.putString("Species", "cobblemon:beedrill");
        pokemon.putInt("cobblemon:data_version", 1);

        // Ability as a compound
        CompoundTag ability = new CompoundTag();
        ability.putString("AbilityName", "earlybird");
        ability.putInt("AbilityIndex", 1);
        pokemon.put("Ability", ability);

        // Nature
        pokemon.putString("Nature", "cobblemon:calm");

        // BenchedMoves and MoveSet as empty lists
        pokemon.put("BenchedMoves", new ListTag());
        pokemon.put("MoveSet", new ListTag());

        // EVs compound (all zero)
        CompoundTag evs = new CompoundTag();
        evs.putInt("cobblemon:defence", 0);
        evs.putInt("cobblemon:hp", 0);
        evs.putInt("cobblemon:special_defence", 0);
        evs.putInt("cobblemon:special_attack", 0);
        evs.putInt("cobblemon:attack", 0);
        evs.putInt("cobblemon:speed", 0);
        pokemon.put("EVs", evs);

        // Original trainer type
        pokemon.putString("PokemonOriginalTrainerType", "NONE");

        // Health, TeraType, IVs, timers, levels, evolutions, experience
        pokemon.putInt("Health", 13);
        pokemon.putString("TeraType", "cobblemon:bug");

        // IVs compound
        CompoundTag ivs = new CompoundTag();
        ivs.putInt("cobblemon:defence", 8);
        ivs.putInt("cobblemon:hp", 0);
        ivs.putInt("cobblemon:special_defence", 25);
        ivs.putInt("cobblemon:special_attack", 16);
        ivs.putInt("cobblemon:attack", 26);
        ivs.putInt("cobblemon:speed", 27);
        pokemon.put("IVs", ivs);

        pokemon.putInt("FaintedTimer", -1);
        pokemon.putInt("DmaxLevel", 0);

        // Evolutions compound with two empty lists: pending and progress
        CompoundTag evolutions = new CompoundTag();
        evolutions.put("pending", new ListTag());
        evolutions.put("progress", new ListTag());
        pokemon.put("Evolutions", evolutions);

        pokemon.putInt("Experience", 6);
        pokemon.putInt("Level", 2);

        // Build entity compound that wraps id and Pokemon
        CompoundTag entity = new CompoundTag();
        entity.putString("id", "cobblemon:pokemon");
        entity.put("Pokemon", pokemon);
        entity.putString("PoseType", "WALK");

        // Build spawn_data compound that contains entity
        CompoundTag spawnData = new CompoundTag();
        spawnData.put("entity", entity);

        CompoundTag trialNBT = new CompoundTag();
        trialNBT.put("spawn_data",spawnData);
        trialNBT.putInt("target_cooldown_length", 1200);
        trialNBT.putInt("x", blockEntityPosition.getX());
        trialNBT.putInt("y", blockEntityPosition.getY());
        trialNBT.putInt("z", blockEntityPosition.getZ());

        LOGGER.info("Trial Data: '{}'", trialNBT);

        return entity;
    }
}
