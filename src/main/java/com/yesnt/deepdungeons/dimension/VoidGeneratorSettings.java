package com.yesnt.deepdungeons.dimension;

import com.yesnt.deepdungeons.DeepDungeons;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.StructureSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VoidGeneratorSettings extends FlatLevelGeneratorSettings {
    public VoidGeneratorSettings(Optional<HolderSet<StructureSet>> pStructureOverrides, Registry<Biome> pBiomes, RegistryAccess access) {
        super(pStructureOverrides, pBiomes);
        List<FlatLayerInfo> layers = new ArrayList<FlatLayerInfo>();
        layers.add(new FlatLayerInfo(0, Blocks.BEDROCK));
        this.setBiome(pBiomes.getHolderOrThrow(ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(DeepDungeons.MODID, "dungeon_biome"))));
        this.withLayers(layers, Optional.empty());
    }
}
