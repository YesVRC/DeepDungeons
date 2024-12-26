package com.yesnt.deepdungeons.dimension.tools;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.mojang.serialization.Lifecycle;
import com.yesnt.deepdungeons.DeepDungeons;
import com.yesnt.deepdungeons.dimension.ModDimensions;
import com.yesnt.deepdungeons.dimension.VoidChunkGenerator;
import com.yesnt.deepdungeons.item.custom.DungeonKey;
import com.yesnt.deepdungeons.network.PacketSyncDimensionListChanges;
import com.yesnt.utils.NanoID;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.LevelEvent;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Thanks to Commoble for providing this code.
 * <p>
 * API for creating and uncreating dynamic dimensions during game runtime. This
 * should only be used for creating dynamic dimensions, whose quantity and
 * properties aren't known until created by some game mechanic. Static
 * dimensions (whose quantities and properties are fixed ahead of time) should
 * be created using the vanilla json dimension system.
 */
public class DynamicDimensionManager {

    private static final Set<ResourceKey<Level>> VANILLA_WORLDS = ImmutableSet.of(Level.OVERWORLD, Level.NETHER, Level.END);
    private static Set<ResourceKey<Level>> pendingLevelsToUnregister = new HashSet<>();

    public static ServerLevel getLevel(MinecraftServer server, ResourceKey<Level> levelKey) {
        @SuppressWarnings("deprecation") // forgeGetWorldMap is deprecated because it's a forge-internal-use-only method
        final Map<ResourceKey<Level>, ServerLevel> map = server.forgeGetWorldMap();

        // if the level already exists, return it
        return map.get(levelKey);
    }

    public static ServerLevel getLevel(MinecraftServer server, String id){
        return getLevel(server, ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(DeepDungeons.MODID, id)));
    }

    /**
     * Marks a level and its levelstem for unregistration. Unregistered levels will stop ticking,
     * unregistered levelstems will not be loaded on server startup unless and until they are reregistered again.
     * <p>
     * Unregistration is delayed until the end of the server tick (just after the post-server-tick-event fires).
     * <p>
     * Players who are still in the given level at that time will be ejected to their respawn points.
     * Players who have respawn points in levels being unloaded will have their spawn points reset to the overworld and respawned there.
     * <p>
     * Unregistering a level does not delete the region files or other data associated with the level's level folder.
     * If a level is reregistered after unregistering it, the level will retain all prior data (unless manually deleted via server admin)
     *
     * @param levelToRemove The key for the level to schedule for unregistration. Vanilla dimensions are not removable as they are
     *                      generally assumed to exist (especially the overworld)
     * @apiNote Not intended for use with vanilla or json dimensions, doing so may cause strange problems.
     * <p>
     * However, if a vanilla or json dimension *is* removed, restarting the server will reconstitute it as
     * vanilla automatically detects and registers these.
     * <p>
     * Mods whose dynamic dimensions require the ejection of players to somewhere other than their respawn point
     * should teleport these worlds' players to appropriate locations before unregistering their dimensions.
     */
    public static void markDimensionForUnregistration(final MinecraftServer server, final ResourceKey<Level> levelToRemove) {
        if (!VANILLA_WORLDS.contains(levelToRemove)) {
            DynamicDimensionManager.pendingLevelsToUnregister.add(levelToRemove);
        }
    }

    /**
     * @return an immutable view of the levels pending to be unregistered and unloaded at the end of the current server tick
     */
    public static Set<ResourceKey<Level>> getWorldsPendingUnregistration() {
        return Collections.unmodifiableSet(DynamicDimensionManager.pendingLevelsToUnregister);
    }

    /**
     * called at the end of the server tick just before the post-server-tick-event
     *
     * @deprecated Internal forge method
     */
    @Deprecated
    public static void unregisterScheduledDimensions(final MinecraftServer server) {
        // flush the buffer
        final Set<ResourceKey<Level>> keysToRemove = DynamicDimensionManager.pendingLevelsToUnregister;
        DynamicDimensionManager.pendingLevelsToUnregister = new HashSet<>();

        // we need to remove the dimension/world form three places
        // the server's dimension registry, the server's world registry, and the overworld's world border listener
        // the world registry is just a simple map and the world border listener has a remove() method
        // the dimension registry has five sub-collections that need to be cleaned up
        // we should also eject players from the removed worlds or they could get stuck there

        final WorldGenSettings worldGenSettings = server.getWorldData().worldGenSettings();
        final Set<ResourceKey<Level>> removedLevelKeys = new HashSet<>();
        final ServerLevel overworld = server.getLevel(Level.OVERWORLD);

        for (final ResourceKey<Level> levelKeyToRemove : keysToRemove) {
            ServerLevel removedLevel = server.forgeGetWorldMap().remove(levelKeyToRemove); // null if the specified key was not present
            if (removedLevel != null) {
                // if we removed the key from the map
                // eject players from dead world
                // iterate over a copy as the world will remove players from the original list
                for (final ServerPlayer player : Lists.newArrayList(removedLevel.players())) {
                    // send players to their respawn point
                    ResourceKey<Level> respawnKey = player.getRespawnDimension();
                    // if we're removing their respawn world then just send them to the overworld
                    if (keysToRemove.contains(respawnKey)) {
                        respawnKey = Level.OVERWORLD;
                        player.setRespawnPosition(Level.OVERWORLD, null, 0, false, false);
                    }
                    if (respawnKey == null) {
                        respawnKey = Level.OVERWORLD;
                    }
                    final ServerLevel destinationLevel = server.getLevel(respawnKey);
                    BlockPos destinationPos = player.getRespawnPosition();
                    if (destinationPos == null) {
                        destinationPos = destinationLevel.getSharedSpawnPos();
                    }
                    final float respawnAngle = player.getRespawnAngle();
                    // "respawning" the player via the player list schedules a task in the server to run after the post-server tick
                    // that causes some minor logspam due to the player's world no longer being loaded
                    // teleporting the player this way instead avoids this
                    player.teleportTo(destinationLevel, destinationPos.getX(), destinationPos.getY(), destinationPos.getZ(), respawnAngle, 0F);
                }
                // save the world now or it won't be saved later and data that may be wanted to be kept may be lost
                removedLevel.save(null, false, removedLevel.noSave());

                // fire world unload event -- when the server stops, this would fire after worlds get saved, so we'll do that here too
                MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.level.LevelEvent.Unload(removedLevel));

                // remove the world border listener if possible
                final WorldBorder overworldBorder = overworld.getWorldBorder();
                final WorldBorder removedWorldBorder = removedLevel.getWorldBorder();
                final List<BorderChangeListener> listeners = overworldBorder.listeners;
                BorderChangeListener targetListener = null;
                for (BorderChangeListener listener : listeners) {
                    if (listener instanceof BorderChangeListener.DelegateBorderChangeListener && removedWorldBorder == ((BorderChangeListener.DelegateBorderChangeListener) listener).worldBorder) {
                        targetListener = listener;
                        break;
                    }
                }
                if (targetListener != null) {
                    overworldBorder.removeListener(targetListener);
                }

                // track the removed world
                removedLevelKeys.add(levelKeyToRemove);
            }
        }

        if (!removedLevelKeys.isEmpty()) {
            // replace the old dimension registry with a new one containing the dimensions that weren't removed, in the same order
            final Registry<LevelStem> oldRegistry = worldGenSettings.dimensions();
            final Registry<LevelStem> newRegistry = new MappedRegistry<>(Registry.LEVEL_STEM_REGISTRY, oldRegistry.lifecycle(), null);

            for (var entry : oldRegistry.entrySet()) {
                final ResourceKey<LevelStem> oldKey = entry.getKey();
                final ResourceKey<Level> oldLevelKey = ResourceKey.create(Registry.DIMENSION_REGISTRY, oldKey.location());
                final LevelStem dimension = entry.getValue();
                if (oldKey != null && dimension != null && !removedLevelKeys.contains(oldLevelKey)) {
//                    newRegistry.register(oldKey, dimension, oldRegistry.lifecycle(dimension));
                    Registry.register(newRegistry, oldKey, dimension);   // @todo 1.18.2 is this right?
                }
            }

            // then replace the old registry with the new registry
            worldGenSettings.dimensions = newRegistry;

            // update the server's levels so dead levels don't get ticked
            server.markWorldsDirty();
            // client will need to be notified of the removed level for the dimension command suggester
            PacketSyncDimensionListChanges.updateClientDimensionLists(ImmutableSet.of(), removedLevelKeys);
        }
    }

    @SuppressWarnings("deprecation") // because we call the forge internal method server#markWorldsDirty
    public static ServerLevel createAndRegisterWorldAndDimension(final MinecraftServer server, final ResourceKey<Level> worldKey, Optional<DungeonKey.KeyTier> tier) {
        // get everything we need to create the dimension and the level
        final ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        final RegistryAccess registry = server.registryAccess();
        Holder<DimensionType> type = registry.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY).getHolderOrThrow(ModDimensions.DUNGEON_TYPE);

        @SuppressWarnings("deprecation") // forgeGetWorldMap is deprecated because it's a forge-internal-use-only method
        final Map<ResourceKey<Level>, ServerLevel> map = server.forgeGetWorldMap();

        // dimension keys have a 1:1 relationship with level keys, they have the same IDs as well
        final ResourceKey<LevelStem> dimensionKey = ResourceKey.create(Registry.LEVEL_STEM_REGISTRY, worldKey.location());
        final LevelStem dimension = new LevelStem(type, new VoidChunkGenerator(registry, tier));;

        // the int in create() here is radius of chunks to watch, 11 is what the server uses when it initializes worlds
        final ChunkProgressListener chunkProgressListener = server.progressListenerFactory.create(11);
        final Executor executor = server.executor;
        final LevelStorageSource.LevelStorageAccess anvilConverter = server.storageSource;
        final WorldData worldData = server.getWorldData();
        final WorldGenSettings worldGenSettings = worldData.worldGenSettings();
        final DerivedLevelData derivedLevelData = new DerivedLevelData(worldData, worldData.overworldData());
        final Random rand = new Random();

        // now we have everything we need to create the dimension and the level
        // this is the same order server init creates levels:
        // the dimensions are already registered when levels are created, we'll do that first
        // then instantiate level, add border listener, add to map, fire world load event

        // register the actual dimension
//        Registry.register(worldGenSettings.dimensions(), dimensionKey, dimension);
        Registry<LevelStem> dimensionRegistry = worldGenSettings.dimensions();
        if (dimensionRegistry instanceof WritableRegistry<LevelStem> writableRegistry) {
            if (dimensionRegistry instanceof MappedRegistry<LevelStem> mr) {
                mr.unfreeze();
            }
            writableRegistry.register(dimensionKey, dimension, Lifecycle.stable());
        } else {
            throw new IllegalStateException("Unable to register dimension '" + dimensionKey.location() + "'! Registry not writable!");
        }

        // create the world instance
        final ServerLevel newWorld = new ServerLevel(
                server,
                executor,
                anvilConverter,
                derivedLevelData,
                worldKey,
                dimension,
                chunkProgressListener,
                worldGenSettings.isDebug(),
                net.minecraft.world.level.biome.BiomeManager.obfuscateSeed(worldGenSettings.seed()),
                ImmutableList.of(), // "special spawn list"
                // phantoms, travelling traders, patrolling/sieging raiders, and cats are overworld special spawns
                // this is always empty for non-overworld dimensions (including json dimensions)
                // these spawners are ticked when the world ticks to do their spawning logic,
                // mods that need "special spawns" for their own dimensions should implement them via tick events or other systems
                false // "tick time", true for overworld, always false for nether, end, and json dimensions
        );

        // add world border listener, for parity with json dimensions
        // the vanilla behaviour is that world borders exist in every dimension simultaneously with the same size and position
        // these border listeners are automatically added to the overworld as worlds are loaded, so we should do that here too
        // TODO if world-specific world borders are ever added, change it here too
        overworld.getWorldBorder().addListener(new BorderChangeListener.DelegateBorderChangeListener(newWorld.getWorldBorder()));

        // register level
        map.put(worldKey, newWorld);

        // update forge's world cache so the new level can be ticked
        server.markWorldsDirty();

        // fire world load event
        MinecraftForge.EVENT_BUS.post(new LevelEvent.Load(newWorld));

        Holder<Structure> structure = getRandomDungeon(registry.registryOrThrow(Registry.STRUCTURE_REGISTRY), tier.orElse(DungeonKey.KeyTier.DEBUG), rand);

        PlaceStructure(structure, newWorld, new BlockPos(0, 250, 0));

        // update clients' dimension lists
        PacketSyncDimensionListChanges.updateClientDimensionLists(ImmutableSet.of(worldKey), ImmutableSet.of());

        return newWorld;
    }

    public static void PlaceStructure(Holder<Structure> structure, ServerLevel level, BlockPos pos) {
        ServerLevel $$3 = level;
        Structure $$4 = (Structure) structure.value();
        ChunkGenerator $$5 = $$3.getChunkSource().getGenerator();
        StructureStart $$6 = $$4.generate(level.registryAccess(), $$5, $$5.getBiomeSource(), $$3.getChunkSource().randomState(), $$3.getStructureManager(), $$3.getSeed(), new ChunkPos(pos), 0, $$3, (p_214580_) -> {
            return true;
        });
        if ($$6.isValid()) {
            BoundingBox $$7 = $$6.getBoundingBox();
            ChunkPos $$8 = new ChunkPos(SectionPos.blockToSectionCoord($$7.minX()), SectionPos.blockToSectionCoord($$7.minZ()));
            ChunkPos $$9 = new ChunkPos(SectionPos.blockToSectionCoord($$7.maxX()), SectionPos.blockToSectionCoord($$7.maxZ()));
            $$3.setChunkForced(0, 0, true);
            ChunkPos.rangeClosed($$8, $$9).forEach((p_214558_) -> {
                $$6.placeInChunk($$3, $$3.structureManager(), $$5, $$3.getRandom(), new BoundingBox(p_214558_.getMinBlockX(), $$3.getMinBuildHeight(), p_214558_.getMinBlockZ(), p_214558_.getMaxBlockX(), $$3.getMaxBuildHeight(), p_214558_.getMaxBlockZ()), p_214558_);
            });
            String $$10 = (String) structure.unwrapKey().map((p_214539_) -> {
                return p_214539_.location().toString();
            }).orElse("[unregistered]");
            level.getServer().sendSystemMessage(Component.translatable("commands.place.structure.success", new Object[]{$$10, pos.getX(), pos.getY(), pos.getZ()}));
        }
    }

    public static ResourceKey<Level> generateLevelKey(String id){
        return ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(DeepDungeons.MODID, "dungeon_" + id));
    }

    public static ResourceKey<Level> generateLevelKey(){
        return generateLevelKey(NanoID.randomNanoId());
    }

    public static String getTypeFromKey(DungeonKey.KeyTier tier){

        switch (tier){
            case ONE -> {
                return "tier_one_dungeon";
            }
            case TWO -> {
                return "tier_two_dungeon";
            }
            case THREE -> {
                return "tier_three_dungeon";
            }
            case FOUR -> {
                return "tier_four_dungeon";
            }
            case DEBUG -> {
                return "debug_dungeon";
            }
            default -> {
                return "debug_dungeon";
            }
        }
    }

    public static SpawnLocation getSpawnLocation(ServerLevel level, Vec3 start, float size){
        List<ArmorStand> armorStand = level.getEntitiesOfClass(ArmorStand.class, AABB.ofSize(start, size, size, size));
        final ArmorStand[] spawn = {null};
        armorStand.forEach((stand) -> {
            if(stand.serializeNBT().getString("custom_name").equals("Spawn Point")){
                spawn[0] = stand;
            }
        });
        return spawn[0] != null ? new SpawnLocation(spawn[0].blockPosition(), spawn[0].getXRot(), spawn[0].getYRot()) : null;
    }

    public static List<ResourceKey<Structure>> getStructuresFromKey(Registry<Structure> access, DungeonKey.KeyTier tier){
        String prefix = getTypeFromKey(tier);
        List<ResourceKey<Structure>> locations = new ArrayList<>();
        for (Map.Entry<ResourceKey<Structure>, Structure> entry : access.entrySet()) {
            ResourceLocation location = entry.getKey().location();

            if (location.getPath().startsWith(prefix) && location.getNamespace().equals(DeepDungeons.MODID)) {
                locations.add(ResourceKey.create(Registry.STRUCTURE_REGISTRY, location));
                System.out.println(location.getPath());

            }
        }
        System.out.println(locations.size());
        return locations;
    }

    public static Holder<Structure> getRandomDungeon(Registry<Structure> access, DungeonKey.KeyTier tier, Random random){
        List<ResourceKey<Structure>> locations = getStructuresFromKey(access, tier);
        int index = random.nextInt(locations.size());
        return access.getHolderOrThrow(locations.get(index));
    }

    public record SpawnLocation (
        BlockPos pos,
        float pitch,
        float yaw
    ) {}
}