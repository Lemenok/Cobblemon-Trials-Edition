package com.lemenok.cobblemontrialsedition.block.entity.cobblemontrialspawner;

import com.lemenok.cobblemontrialsedition.config.SpawnerProperties;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.ArrayList;
import java.util.List;

public record CobblemonTrialSpawnerConfig(int spawnRange, float totalMobs, float simultaneousMobs,
                                          float totalMobsAddedPerPlayer, float simultaneousMobsAddedPerPlayer,
                                          int ticksBetweenSpawn, boolean enableOminousSpawnerAttacks,
                                          SimpleWeightedRandomList<SpawnData> spawnPotentialsDefinition,
                                          SimpleWeightedRandomList<ResourceKey<LootTable>> lootTablesToEject,
                                          ResourceKey<LootTable> itemsToDropWhenOminous,
                                          List<WaveConfig> waves) {

    public static final CobblemonTrialSpawnerConfig DEFAULT;
    public static final Codec<CobblemonTrialSpawnerConfig> CODEC;

    public record WaveConfig(int baseMobs, int mobsPerPlayer, SimpleWeightedRandomList<SpawnData> spawnPotentials) {
        public static final Codec<WaveConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.optionalFieldOf("baseMobs", 4).forGetter(WaveConfig::baseMobs),
                Codec.INT.optionalFieldOf("mobsPerPlayer", 1).forGetter(WaveConfig::mobsPerPlayer),
                SpawnData.LIST_CODEC.optionalFieldOf("spawnPotentials", SimpleWeightedRandomList.empty()).forGetter(WaveConfig::spawnPotentials)
        ).apply(instance, WaveConfig::new));

        public int calculateTotalMobs(int additionalPlayers) {
            return this.baseMobs + (this.mobsPerPlayer * additionalPlayers);
        }
    }

    public SimpleWeightedRandomList<SpawnData> getSpawnPotentials(int mobsSpawned, int additionalPlayers) {
        if (this.waves == null || this.waves.isEmpty()) {
            return this.spawnPotentialsDefinition(); // Fallback to classic functionality
        }

        int mobsSoFar = 0;
        for (WaveConfig wave : this.waves) {
            mobsSoFar += wave.calculateTotalMobs(additionalPlayers);
            if (mobsSpawned < mobsSoFar) {
                return wave.spawnPotentials();
            }
        }
        return this.waves.get(this.waves.size() - 1).spawnPotentials();
    }

    public int calculateTargetTotalMobs(int additionalPlayers) {
        if (this.waves != null && !this.waves.isEmpty()) {
            return this.waves.stream().mapToInt(w -> w.calculateTotalMobs(additionalPlayers)).sum();
        }
        return (int)Math.floor(this.totalMobs + this.totalMobsAddedPerPlayer * (float)additionalPlayers);
    }

    public int calculateTargetSimultaneousMobs(int i) {
        return (int)Math.floor(this.simultaneousMobs + this.simultaneousMobsAddedPerPlayer * (float)i);
    }

    public long ticksBetweenItemSpawners() {
        return 160L;
    }

    static {
        DEFAULT = new CobblemonTrialSpawnerConfig(4, 4,
                2, 1, 1,
                40, false, SimpleWeightedRandomList.empty(),
                SimpleWeightedRandomList.empty(), BuiltInLootTables.SPAWNER_TRIAL_ITEMS_TO_DROP_WHEN_OMINOUS, new ArrayList<>());
        CODEC = RecordCodecBuilder.create((instance) ->
                instance.group(Codec.intRange(1, 128).lenientOptionalFieldOf("spawn_range", DEFAULT.spawnRange)
                                .forGetter(CobblemonTrialSpawnerConfig::spawnRange),
                        Codec.floatRange(0.0F, Float.MAX_VALUE).lenientOptionalFieldOf("total_mobs", DEFAULT.totalMobs)
                                .forGetter(CobblemonTrialSpawnerConfig::totalMobs),
                        Codec.floatRange(0.0F, Float.MAX_VALUE).lenientOptionalFieldOf("simultaneous_mobs", DEFAULT.simultaneousMobs)
                                .forGetter(CobblemonTrialSpawnerConfig::simultaneousMobs),
                        Codec.floatRange(0.0F, Float.MAX_VALUE).lenientOptionalFieldOf("total_mobs_added_per_player", DEFAULT.totalMobsAddedPerPlayer)
                                .forGetter(CobblemonTrialSpawnerConfig::totalMobsAddedPerPlayer),
                        Codec.floatRange(0.0F, Float.MAX_VALUE).lenientOptionalFieldOf("simultaneous_mobs_added_per_player", DEFAULT.simultaneousMobsAddedPerPlayer)
                                .forGetter(CobblemonTrialSpawnerConfig::simultaneousMobsAddedPerPlayer),
                        Codec.intRange(0, Integer.MAX_VALUE).lenientOptionalFieldOf("ticks_between_spawn", DEFAULT.ticksBetweenSpawn)
                                .forGetter(CobblemonTrialSpawnerConfig::ticksBetweenSpawn),
                        Codec.BOOL.lenientOptionalFieldOf("enable_ominous_spawner_attacks", DEFAULT.enableOminousSpawnerAttacks)
                                .forGetter(CobblemonTrialSpawnerConfig::enableOminousSpawnerAttacks),
                        SpawnData.LIST_CODEC.lenientOptionalFieldOf("spawn_potentials", SimpleWeightedRandomList.empty())
                                .forGetter(CobblemonTrialSpawnerConfig::spawnPotentialsDefinition),
                        SimpleWeightedRandomList.wrappedCodecAllowingEmpty(ResourceKey.codec(Registries.LOOT_TABLE))
                                .lenientOptionalFieldOf("loot_tables_to_eject", DEFAULT.lootTablesToEject)
                                .forGetter(CobblemonTrialSpawnerConfig::lootTablesToEject),
                        ResourceKey.codec(Registries.LOOT_TABLE).lenientOptionalFieldOf("items_to_drop_when_ominous", DEFAULT.itemsToDropWhenOminous)
                                .forGetter(CobblemonTrialSpawnerConfig::itemsToDropWhenOminous),
                        Codec.list(WaveConfig.CODEC).optionalFieldOf("waves", new ArrayList<>())
                                .forGetter(CobblemonTrialSpawnerConfig::waves))
                        .apply(instance, CobblemonTrialSpawnerConfig::new));
    }
}
