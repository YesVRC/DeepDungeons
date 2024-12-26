package com.yesnt.deepdungeons.dimension;

import com.yesnt.deepdungeons.item.custom.DungeonKey;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.levelgen.FlatLevelSource;

import java.util.Optional;

public class VoidChunkGenerator extends FlatLevelSource {


    public VoidChunkGenerator(RegistryAccess access, Optional<DungeonKey.KeyTier> tier) {
        super(access.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY),
                new VoidGeneratorSettings(
                        Optional.empty()
                ,access.registryOrThrow(Registry.BIOME_REGISTRY),
                access));
    }
}