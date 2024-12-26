package com.yesnt.deepdungeons.dimension;

import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.worldgen.StructureSets;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraftforge.common.BiomeManager;

public class TestChunkGenerator extends NoiseBasedChunkGenerator {

    public TestChunkGenerator(Registry<StructureSet> structureSets, Registry<NormalNoise.NoiseParameters> noiseParameters, BiomeSource biomeSource, Holder<NoiseGeneratorSettings> noiseGeneratorSettingsHolder) {
        super(structureSets, noiseParameters, biomeSource, noiseGeneratorSettingsHolder);
    }
    public TestChunkGenerator(RegistryAccess access){
        super(
                access.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY).freeze(),
                access.registryOrThrow(Registry.NOISE_REGISTRY).freeze(),
                new FixedBiomeSource(access.registryOrThrow(Registry.BIOME_REGISTRY).getHolderOrThrow(Biomes.THE_VOID)),
                access.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY).getHolderOrThrow(NoiseGeneratorSettings.END)
        );

    }
}
