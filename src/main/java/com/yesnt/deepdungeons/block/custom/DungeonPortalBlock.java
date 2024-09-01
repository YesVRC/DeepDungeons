package com.yesnt.deepdungeons.block.custom;

import com.yesnt.deepdungeons.DeepDungeons;
import com.yesnt.deepdungeons.block.entity.DungeonPortalBlockEntity;
import com.yesnt.deepdungeons.block.entity.ModBlockEntities;
import com.yesnt.deepdungeons.dimension.ModDimensions;
import com.yesnt.deepdungeons.dimension.VoidChunkGenerator;
import com.yesnt.deepdungeons.dimension.tools.DynamicDimensionManager;
import com.yesnt.deepdungeons.item.ModItems;
import com.yesnt.deepdungeons.structure.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class DungeonPortalBlock extends BaseEntityBlock {

    public DungeonPortalBlock(Properties pProperties) {
        super(pProperties);
    }

    // BLOCK


    // called when the player right clicks this block
    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        ItemStack playerItem = player.getItemInHand(handIn);
        BlockEntity tileEntity = worldIn.getBlockEntity(pos);
        DungeonPortalBlockEntity myEntity = (DungeonPortalBlockEntity) tileEntity;
        if (handIn == InteractionHand.OFF_HAND || worldIn.isClientSide()) return InteractionResult.FAIL;
        // insert or remove an item from this block
        if (myEntity != null) {
            ItemStack insideItem = myEntity.getObjectInserted();

            // if the keyhole is currently empty
            if (insideItem.isEmpty()) {
                if (playerItem.is(ModItems.DUNGEON_KEY.get())) {
                    myEntity.setContents(playerItem.copy());
                    playerItem.shrink(1);
                }
                return InteractionResult.SUCCESS;
            }
            // if the keyhole is currently full
            else {
                // DimDungeons.LOGGER.info("Taking thing out of keyhole...");
                if (playerItem.isEmpty()) {
                    player.setItemInHand(handIn, insideItem); // hand it to the player
                } else if (!player.addItem(insideItem)) // okay put it in their inventory
                {
                    player.drop(insideItem, false); // whatever drop it on the ground
                }

                // actually remove the item from the keyhole here
                myEntity.removeContents();

                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public void stepOn(Level pLevel, BlockPos pPos, BlockState pState, Entity pEntity) {
        if (pEntity instanceof ServerPlayer && pLevel.getBlockEntity(pPos) instanceof DungeonPortalBlockEntity) {
            ItemStack stack = ((DungeonPortalBlockEntity) pLevel.getBlockEntity(pPos)).getObjectInserted();
            if (stack.hasTag()) {
                MinecraftServer server = pLevel.getServer();
                RegistryAccess registry = pLevel.registryAccess();
                Holder<DimensionType> type = registry.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY).getHolderOrThrow(ModDimensions.DUNGEON_TYPE);
                ResourceKey<Level> levelKey = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(DeepDungeons.MODID, ((DungeonPortalBlockEntity) Objects.requireNonNull(pLevel.getBlockEntity(pPos))).getDungeonNameFromItem()));
                ServerLevel level = DynamicDimensionManager.getOrCreateLevel(server, levelKey, ((minecraftServer, levelStemResourceKey) -> {
                    return new LevelStem(type, new VoidChunkGenerator(registry));
                }));
                if(level.structureManager().hasAnyStructureAt(new BlockPos(0, 300, 0))) {
                    ((ServerPlayer) pEntity).teleportTo(level, 0, 300, 0, pEntity.getXRot(), pEntity.getYRot());
                }
                else{

                }
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, RandomSource rand){
        double d0 = (double) pos.getX() + 0.5D;
        double d1 = (double) pos.getY() + rand.nextDouble() * 6.0D / 16.0D;
        double d2 = (double) pos.getZ() + 0.5D;
        double d4 = rand.nextDouble() * 0.6D - 0.3D;
        BlockEntity tileEntity = worldIn.getBlockEntity(pos);
        if(tileEntity instanceof DungeonPortalBlockEntity && ((DungeonPortalBlockEntity) tileEntity).getObjectInserted().hasTag()) {
            worldIn.addParticle(ParticleTypes.SMOKE, d0 - 0.52D, d1, d2 + d4, -1.0D, 1.0D, 0.0D);
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
    {
        return createTickerHelper(type, ModBlockEntities.DUNGEON_PORTAL_BLOCK_ENTITY.get(),
                DungeonPortalBlockEntity::tick);
    }

    // BLOCK ENTITY


    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new DungeonPortalBlockEntity(blockPos, blockState);
    }
}