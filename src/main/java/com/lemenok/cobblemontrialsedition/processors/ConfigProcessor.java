package com.lemenok.cobblemontrialsedition.processors;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.lemenok.cobblemontrialsedition.CobblemonTrialsEdition;
import com.lemenok.cobblemontrialsedition.config.GlobalSettings;
import com.lemenok.cobblemontrialsedition.config.SpawnablePokemonSettings;
import com.lemenok.cobblemontrialsedition.config.SpawnerSettings;
import com.lemenok.cobblemontrialsedition.config.StructureSettings;
import com.lemenok.cobblemontrialsedition.config.mappers.GlobalSettingsMapper;
import com.lemenok.cobblemontrialsedition.config.mappers.SpawnablePokemonMapper;
import com.lemenok.cobblemontrialsedition.config.mappers.SpawnerMapper;
import com.lemenok.cobblemontrialsedition.config.mappers.StructureMapper;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

public class ConfigProcessor {
    private static final Logger LOGGER = LogManager.getLogger(CobblemonTrialsEdition.MODID);
    private static final Gson GSON = new Gson();

    private static final String DefaultGlobalSettings = "globalSettings.json";
    private static final String DefaultFortressSettings = "minecraft-fortress.json";
    private static final String DefaultSpawnerSettings = "spawner-fortress.json";
    private static final String DefaultLootTableSettings = "loot_table-fortress.json";
    private static final String DefaultOminousLootTableSettings = "loot_table-ominous-fortress.json";

    private static final Path GlobalDefaultConfigPath = FMLPaths.CONFIGDIR.get().resolve(CobblemonTrialsEdition.MODID);
    private static final Path StructuresConfigPath = GlobalDefaultConfigPath.resolve("structures");
    private static final Path SpawnersConfigPath = StructuresConfigPath.resolve("spawners");
    private static final Path LootTablesConfigPath = SpawnersConfigPath.resolve("loot_tables");

    public static GlobalSettings GLOBAL_SETTINGS = null;
    private static List<StructureMapper> STRUCTURE_SETTINGS = null;
    private static HashMap<String, SpawnerMapper> SPAWNER_MAPPING = null;
    private static HashMap<String, LootTable> LOOT_TABLES = null;

    public static void SetupDefaultConfigs() {
        Path modConfigDirectory = FMLPaths.CONFIGDIR.get().resolve(CobblemonTrialsEdition.MODID);

        try {
            // Check if the Global Settings exists. If it doesn't we want to copy
            // all other default config files into the appropriate directories.
            Path targetPath = modConfigDirectory.resolve(DefaultGlobalSettings);

            if (!Files.exists(targetPath)) {
                LOGGER.info("Default config not found. Copying all default config files.");
                copyDefaultConfig();
            }

        } catch (Exception e) {
            LOGGER.error("Failed to setup default configs", e);
            throw new RuntimeException(e);
        }
    }

    public static void LoadConfigs() {

        if(!Files.isDirectory(GlobalDefaultConfigPath)) LOGGER.error("GlobalSettings Path does not exist.");
        if(!Files.isDirectory(StructuresConfigPath)) LOGGER.error("StructuresConfig Path does not exist.");
        if(!Files.isDirectory(SpawnersConfigPath)) LOGGER.error("SpawnersConfig Path does not exist.");
        if(!Files.isDirectory(LootTablesConfigPath)) LOGGER.error("LootTablesConfig Path does not exist.");

        // Clear for old reloads
        GLOBAL_SETTINGS = null;
        STRUCTURE_SETTINGS = new ArrayList<>();
        SPAWNER_MAPPING = new HashMap<>();
        LOOT_TABLES = new HashMap<>();
        LOGGER.info("Loading config files.");


        try (Stream<Path> stream = Files.walk(GlobalDefaultConfigPath, 1)){
            stream.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".json"))
                    .forEach(ConfigProcessor::ProcessGlobalFile);
        } catch (Exception e) {
            LOGGER.error("Failed to read files from directory", e);
        }

        try (Stream<Path> stream = Files.walk(StructuresConfigPath, 1)){
            stream.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".json"))
                    .forEach(ConfigProcessor::ProcessStructuresFiles);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (Stream<Path> stream = Files.walk(SpawnersConfigPath, 1)){
            stream.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".json"))
                    .forEach(ConfigProcessor::ProcessSpawnerFiles);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (Stream<Path> stream = Files.walk(LootTablesConfigPath, 1)){
            stream.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".json"))
                    .forEach(ConfigProcessor::ProcessLootTableFiles);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        BuildGlobalConfig();

        LOGGER.info("Successfully loaded config files.");
    }

    private static void copyDefaultConfig() throws IOException {
        String globalDefaultResourcePath = "/assets/" + CobblemonTrialsEdition.MODID + "/config/";
        String structuresDefaultResourcePath = globalDefaultResourcePath + "structures/";
        String spawnersDefaultResourcePath = structuresDefaultResourcePath + "spawners/";
        String lootTablesDefaultResourcePath = spawnersDefaultResourcePath + "loot_tables/";
        String ominousLootTablesDefaultResourcePath = spawnersDefaultResourcePath + "loot_tables/";

        Files.createDirectories(GlobalDefaultConfigPath);
        Files.createDirectories(StructuresConfigPath);
        Files.createDirectories(SpawnersConfigPath);
        Files.createDirectories(LootTablesConfigPath);

        CopyFileFromPath(ConfigProcessor.class.getResourceAsStream(globalDefaultResourcePath + DefaultGlobalSettings), GlobalDefaultConfigPath, DefaultGlobalSettings);
        CopyFileFromPath(ConfigProcessor.class.getResourceAsStream(structuresDefaultResourcePath + DefaultFortressSettings), StructuresConfigPath, DefaultFortressSettings);
        CopyFileFromPath(ConfigProcessor.class.getResourceAsStream(spawnersDefaultResourcePath + DefaultSpawnerSettings), SpawnersConfigPath, DefaultSpawnerSettings);
        CopyFileFromPath(ConfigProcessor.class.getResourceAsStream(lootTablesDefaultResourcePath + DefaultLootTableSettings), LootTablesConfigPath, DefaultLootTableSettings);
        CopyFileFromPath(ConfigProcessor.class.getResourceAsStream(ominousLootTablesDefaultResourcePath + DefaultOminousLootTableSettings), LootTablesConfigPath, DefaultOminousLootTableSettings);
    }

    private static void CopyFileFromPath(InputStream ResourceAsStream, Path targetPath, String fileName) {
        try (InputStream stream = ResourceAsStream) {
            if (stream == null) {
                throw new RuntimeException("Default config file is not found in the JAR: " + fileName);
            }

            Path fileLocationPath = targetPath.resolve(fileName);
            Files.copy(stream, fileLocationPath, StandardCopyOption.REPLACE_EXISTING);

        } catch (Exception e) {
            LOGGER.error("Failed to copy default global config", e);
            throw new RuntimeException(e);
        }
    }

    private static void BuildGlobalConfig() {
        List<StructureSettings> structureSettingsList =  new ArrayList<>();

        for(StructureMapper structureMapper: STRUCTURE_SETTINGS){
            StructureSettings structureSettings = new StructureSettings();

            String[] structureNamespacePath = structureMapper.structureId.split(":");
            ResourceLocation newStructureResource = ResourceLocation.fromNamespaceAndPath(structureNamespacePath[0], structureNamespacePath[1]);

            structureSettings.AddToStructureSpawnerMapping(newStructureResource, BuildSpawnerConfigList(structureMapper.spawners));

            structureSettingsList.add(structureSettings);
        }

        GLOBAL_SETTINGS.setStructureSettingsList(structureSettingsList);
    }

    private static List<SpawnerSettings> BuildSpawnerConfigList(String[] arrayOfSpawnerFileNames) {
        List<SpawnerSettings> newSpawnerSettingsList = new ArrayList<>();

        for(String spawnerFileName : arrayOfSpawnerFileNames){
            SpawnerMapper spawnerMapper = SPAWNER_MAPPING.get(spawnerFileName);
            SpawnerSettings spawnerSettings = new SpawnerSettings(
                    spawnerMapper.TicksBetweenSpawnAttempts,
                    spawnerMapper.SpawnerCooldown,
                    spawnerMapper.PlayerDetectionRange,
                    spawnerMapper.SpawnRange,
                    spawnerMapper.MaximumNumberOfSimultaneousPokemon,
                    spawnerMapper.MaximumNumberOfSimultaneousPokemonAddedPerPlayer,
                    spawnerMapper.TotalNumberOfPokemonPerTrial,
                    spawnerMapper.TotalNumberOfPokemonPerTrialAddedPerPlayer,
                    spawnerMapper.OminousSpawnerAttacksEnabled,
                    spawnerMapper.DoPokemonSpawnedGlow
            );

            spawnerSettings.SetSpawnerTypesToReplace(spawnerMapper.SpawnerTypeToReplace);
            spawnerSettings.SetSpawnerEntityToReplace(spawnerMapper.SpawnerEntityToReplace);

            List<LootTable> lootTableList = new ArrayList<>();
            if(!spawnerMapper.LootTable.isEmpty()) lootTableList.add(LOOT_TABLES.get(spawnerMapper.LootTable));
            spawnerSettings.SetLootTable(lootTableList, false);

            List<LootTable> ominousLootTableList = new ArrayList<>();
            if(!spawnerMapper.OminousLootTable.isEmpty()) ominousLootTableList.add(LOOT_TABLES.get(spawnerMapper.OminousLootTable));
            spawnerSettings.SetLootTable(ominousLootTableList, true);

            spawnerSettings.SetListOfPokemonToSpawn(BuildSpawnablePokemonList(spawnerMapper.ListOfPokemonToSpawn), false);
            spawnerSettings.SetListOfPokemonToSpawn(BuildSpawnablePokemonList(spawnerMapper.ListOfOminousPokemonToSpawn), true);

            newSpawnerSettingsList.add(spawnerSettings);
        }

        return newSpawnerSettingsList;
    }

    private static List<SpawnablePokemonSettings> BuildSpawnablePokemonList(SpawnablePokemonMapper[] listOfPokemonToSpawn) {
        List<SpawnablePokemonSettings> newSpawnablePokemonSettings = new ArrayList<>();

        for(SpawnablePokemonMapper spawnablePokemonMapper: listOfPokemonToSpawn){
            SpawnablePokemonSettings spawnablePokemonSettings = new SpawnablePokemonSettings(
                    spawnablePokemonMapper.Species,
                    spawnablePokemonMapper.Weight,
                    spawnablePokemonMapper.Form,
                    spawnablePokemonMapper.Level,
                    spawnablePokemonMapper.Gender,
                    spawnablePokemonMapper.Nature,
                    spawnablePokemonMapper.DefaultEVs,
                    spawnablePokemonMapper.DefaultIVs,
                    spawnablePokemonMapper.Ability,
                    spawnablePokemonMapper.DynaMaxLevel,
                    spawnablePokemonMapper.TeraType,
                    spawnablePokemonMapper.IsShiny,
                    spawnablePokemonMapper.ScaleModifier,
                    spawnablePokemonMapper.IsUncatchable,
                    spawnablePokemonMapper.MustBeDefeatedInBattle
            );

            newSpawnablePokemonSettings.add(spawnablePokemonSettings);
        }

        return newSpawnablePokemonSettings;
    }

    private static void ProcessGlobalFile(Path path) {

        try (Reader reader = Files.newBufferedReader(path)) {
            GlobalSettingsMapper globalSettings = GSON.fromJson(reader, GlobalSettingsMapper.class);

            if(globalSettings != null){
                GLOBAL_SETTINGS = new GlobalSettings(
                        globalSettings.EnableDebugLogs,
                        globalSettings.ReplaceGeneratedSpawnersWithCobblemonSpawners,
                        globalSettings.ReplaceSpawnersNotInStructuresWithCobblemonSpawners,
                        globalSettings.ReplaceSpawnersInStructuresWithCobblemonSpawners);

                LOGGER.info("Loaded global settings.");
            } else {
                LOGGER.info("Failed to parse globalSettings file or file was empty: {}", path);
            }

        } catch (Exception e) {
            LOGGER.error("Invalid JSON syntax in file: {}", path, e);
        }
    }

    private static void ProcessStructuresFiles(Path path) {
        try (Reader reader = Files.newBufferedReader(path)) {
            String structureFileName = path.getFileName().toString();
            StructureMapper structureMapper = GSON.fromJson(reader, StructureMapper.class);

            if(structureMapper != null){
                STRUCTURE_SETTINGS.add(structureMapper);
                LOGGER.info("Loaded Structure file: {}", structureFileName);
            } else {
                LOGGER.info("Failed to parse structure file or file was empty: {}", path);
            }

        } catch (Exception e) {
            LOGGER.error("Invalid JSON syntax in file: {}", path, e);
        }
    }

    private static void ProcessSpawnerFiles(Path path) {
        try (Reader reader = Files.newBufferedReader(path)) {
            String spawnerFileName = path.getFileName().toString();
            SpawnerMapper newSpawnerMapper = GSON.fromJson(reader, SpawnerMapper.class);

            if(newSpawnerMapper != null){
                SPAWNER_MAPPING.put(spawnerFileName, newSpawnerMapper);
                LOGGER.info("Loaded Spawner file: {}", spawnerFileName);
            } else {
                LOGGER.info("Failed to parse spawner file or file was empty: {}", path);
            }

        } catch (Exception e) {
            LOGGER.error("Invalid JSON syntax in file: {}", path, e);
        }
    }

    private static void ProcessLootTableFiles(Path path) {
        try (Reader reader = Files.newBufferedReader(path)) {
            String lootTableFileName = path.getFileName().toString();

            JsonElement jsonElement = JsonParser.parseReader(reader);
            DataResult<Holder<LootTable>> dataResult = LootTable.CODEC.parse(JsonOps.INSTANCE, jsonElement);


            LootTable newLootTable = dataResult.getOrThrow(errorMessage ->
                    new IllegalStateException("Failed to parse loot table: " + errorMessage)).value();

            LOOT_TABLES.put(lootTableFileName, newLootTable);
            LOGGER.info("Loaded loot table file: {}", lootTableFileName);

        } catch (Exception e) {
            LOGGER.error("Invalid JSON syntax in file: {}", path, e);
        }
    }



}
