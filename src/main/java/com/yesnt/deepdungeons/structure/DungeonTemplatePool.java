package com.yesnt.deepdungeons.structure;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

import java.util.List;
import java.util.function.Function;

public class DungeonTemplatePool extends StructureTemplatePool {
    public DungeonTemplatePool(ResourceLocation pName, ResourceLocation pFallback, List<Pair<Function<Projection, ? extends StructurePoolElement>, Integer>> p_210571_, Projection pProjection) {
        super(pName, pFallback, p_210571_, pProjection);
    }
}
