package com.yesnt.deepdungeons.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

import java.util.Optional;

public class DungeonStructure extends Structure {

    public static final Codec<DungeonStructure> CODEC = RecordCodecBuilder.<DungeonStructure>mapCodec(instance ->
            instance.group(DungeonStructure.settingsCodec(instance),
                    StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(structure -> structure.startPool),
                    ResourceLocation.CODEC.optionalFieldOf("start_jigsaw_name").forGetter(structure -> structure.startJigsawName),
                    Codec.intRange(1, 9999).fieldOf("size").forGetter(structure -> structure.size),
                    HeightProvider.CODEC.fieldOf("start_height").forGetter(structure -> structure.startHeight),
                    Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap").forGetter(structure -> structure.projectStartToHeightmap),
                    Codec.intRange(1, 128).fieldOf("max_distance_from_center").forGetter(structure -> structure.maxDistanceFromCenter)
            ).apply(instance, DungeonStructure::new)).codec();

    private final Holder<StructureTemplatePool> startPool;
    private final Optional<ResourceLocation> startJigsawName;
    private final int size;
    private final HeightProvider startHeight;
    private final Optional<Heightmap.Types> projectStartToHeightmap;
    private final int maxDistanceFromCenter;


    protected DungeonStructure(StructureSettings pSettings, Holder<StructureTemplatePool> startPool, Optional<ResourceLocation> startJigsawName, int size, HeightProvider startHeight, Optional<Heightmap.Types> projectStartToHeightmap, int maxDistanceFromCenter) {
        super(pSettings);
        this.startPool = startPool;
        this.startJigsawName = startJigsawName;
        this.size = size;
        this.startHeight = startHeight;
        this.projectStartToHeightmap = projectStartToHeightmap;
        this.maxDistanceFromCenter = maxDistanceFromCenter;
    }

    private static boolean extraSpawningChecks(Structure.GenerationContext context) {
        // Grabs the chunk position we are at
        ChunkPos chunkpos = context.chunkPos();

        // Checks to make sure our structure does not spawn above land that's higher than y = 150
        // to demonstrate how this method is good for checking extra conditions for spawning
        /*return context.chunkGenerator().getFirstOccupiedHeight(
                chunkpos.getMinBlockX(),
                chunkpos.getMinBlockZ(),
                Heightmap.Types.WORLD_SURFACE,
                context.heightAccessor(),
                context.randomState()) < 150;*/

        return chunkpos.x == 0 && chunkpos.z == 0;
    }

    @Override
    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext context) {

        // Check if the spot is valid for our structure. This is just as another method for cleanness.
        // Returning an empty optional tells the game to skip this spot as it will not generate the structure.
        if (!DungeonStructure.extraSpawningChecks(context)) {
            return Optional.empty();
        }

        // Set's our spawning blockpos's y offset to be 60 blocks up.
        // Since we are going to have heightmap/terrain height spawning set to true further down, this will make it so we spawn 60 blocks above terrain.
        // If we wanted to spawn on ocean floor, we would set heightmap/terrain height spawning to false and the grab the y value of the terrain with OCEAN_FLOOR_WG heightmap.
        int startY = this.startHeight.sample(context.random(), new WorldGenerationContext(context.chunkGenerator(), context.heightAccessor()));
        // Turns the chunk coordinates into actual coordinates we can use. (Gets corner of that chunk)
        ChunkPos chunkPos = context.chunkPos();
        BlockPos blockPos = new BlockPos(chunkPos.getMinBlockX(), startY, chunkPos.getMinBlockZ());

        Optional<Structure.GenerationStub> structurePiecesGenerator =
                JigsawPlacement.addPieces(
                        context, // Used for JigsawPlacement to get all the proper behaviors done.
                        this.startPool, // The starting pool to use to create the structure layout from
                        this.startJigsawName, // Can be used to only spawn from one Jigsaw block. But we don't need to worry about this.
                        this.size, // How deep a branch of pieces can go away from center piece. (5 means branches cannot be longer than 5 pieces from center piece)
                        blockPos, // Where to spawn the structure.
                        false, // "useExpansionHack" This is for legacy villages to generate properly. You should keep this false always.
                        this.projectStartToHeightmap, // Adds the terrain height's y value to the passed in blockpos's y value. (This uses WORLD_SURFACE_WG heightmap which stops at top water too)
                        // Here, blockpos's y value is 60 which means the structure spawn 60 blocks above terrain height.
                        // Set this to false for structure to be place only at the passed in blockpos's Y value instead.
                        // Definitely keep this false when placing structures in the nether as otherwise, heightmap placing will put the structure on the Bedrock roof.
                        this.maxDistanceFromCenter); // Maximum limit for how far pieces can spawn from center. You cannot set this bigger than 128 or else pieces gets cutoff.

        /*
         * Note, you are always free to make your own JigsawPlacement class and implementation of how the structure
         * should generate. It is tricky but extremely powerful if you are doing something that vanilla's jigsaw system cannot do.
         * Such as for example, forcing 3 pieces to always spawn every time, limiting how often a piece spawns, or remove the intersection limitation of pieces.
         */

        // Return the pieces generator that is now set up so that the game runs it when it needs to create the layout of structure pieces.
        return structurePiecesGenerator;
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.DUNGEON_STRUCTURE_TYPE.get(); // Helps the game know how to turn this structure back to json to save to chunks
    }
}
