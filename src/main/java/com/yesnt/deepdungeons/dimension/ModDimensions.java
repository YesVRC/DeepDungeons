package com.yesnt.deepdungeons.dimension;

import com.yesnt.deepdungeons.DeepDungeons;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.dimension.DimensionType;

public class ModDimensions {
    public static final ResourceKey<DimensionType> DUNGEON_TYPE = ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY,
            new ResourceLocation(DeepDungeons.MODID, "dungeon_type"));

    public static void register(){
        System.out.println("Registering Mod Dimensions for " + DeepDungeons.MODID);
    }
}
