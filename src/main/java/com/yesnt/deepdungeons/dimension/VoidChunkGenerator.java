package com.yesnt.deepdungeons.dimension;

import com.yesnt.deepdungeons.DeepDungeons;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.StructureSet;

import javax.swing.text.html.Option;
import java.util.Optional;

public class VoidChunkGenerator extends FlatLevelSource {


    public VoidChunkGenerator(RegistryAccess access) {
        super(access.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY),
                new VoidGeneratorSettings(
                        Optional.empty()
                ,access.registryOrThrow(Registry.BIOME_REGISTRY),
                access));
    }
}