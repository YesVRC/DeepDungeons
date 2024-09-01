package com.yesnt.deepdungeons.world.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Vec3i;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;

public class DungeonPlacement extends RandomSpreadStructurePlacement {
    public static final Codec<DungeonPlacement> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            Codec.intRange(0, Integer.MAX_VALUE).fieldOf("spacing").forGetter(DungeonPlacement::spacing),
            Codec.intRange(0, Integer.MAX_VALUE).fieldOf("separation").forGetter(DungeonPlacement::separation),
            RandomSpreadType.CODEC.optionalFieldOf("type", RandomSpreadType.LINEAR).forGetter(DungeonPlacement::spreadType),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("salt").forGetter(DungeonPlacement::salt)
            ).apply(instance, instance.stable(DungeonPlacement::new)));

    public DungeonPlacement(int pSpacing, int pSeparation, RandomSpreadType pSpreadType, int pSalt) {
        super(pSpacing, pSeparation, pSpreadType, pSalt);
    }

    @Override
    protected boolean isPlacementChunk(ChunkGenerator pGenerator, RandomState pRandomState, long pSeed, int pX, int pY) {
        return pX == 0 && pY == 0;
    }

    @Override
    public boolean isStructureChunk(ChunkGenerator pGenerator, RandomState pRandomState, long pSeed, int pX, int pZ) {
        return pX == 0 && pZ == 0;
    }
}