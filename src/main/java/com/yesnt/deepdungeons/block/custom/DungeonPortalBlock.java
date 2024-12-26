package com.yesnt.deepdungeons.block.custom;

import com.yesnt.deepdungeons.block.entity.DungeonPortalBlockEntity;
import com.yesnt.deepdungeons.block.entity.ModBlockEntities;
import com.yesnt.deepdungeons.dimension.tools.DynamicDimensionManager;
import com.yesnt.deepdungeons.item.custom.DungeonKey;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class DungeonPortalBlock extends BaseEntityBlock {

    public static BooleanProperty activated = BooleanProperty.create("activated");

    public DungeonPortalBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any().setValue(activated, false));
    }

    // BLOCK


    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(activated);
    }

    // called when the player right-clicks this block
    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        ItemStack playerItem = player.getItemInHand(handIn);
        BlockEntity tileEntity = worldIn.getBlockEntity(pos);
        DungeonPortalBlockEntity myEntity = (DungeonPortalBlockEntity) tileEntity;
        if (handIn == InteractionHand.OFF_HAND || worldIn.isClientSide() || !(playerItem.getItem() instanceof DungeonKey)) return InteractionResult.FAIL;
        // Block Entity Exists
        if (myEntity != null) {
            // If dimension already registered
            if(myEntity.getTargetLevel() != null) return InteractionResult.FAIL;
            playerItem.shrink(1);
            ServerLevel level = DynamicDimensionManager.createAndRegisterWorldAndDimension(
                    // server
                    worldIn.getServer(),

                    // Level Key
                    DynamicDimensionManager.generateLevelKey(),

                    // Key tier
                    Optional.ofNullable(((DungeonKey) playerItem.getItem()).tier));
            // Set Level
            myEntity.setTargetLevel(level.dimension().location().getPath());

        }

        return InteractionResult.PASS;
    }

    @Override
    public void stepOn(Level pLevel, BlockPos pPos, BlockState pState, Entity pEntity) {
        // Is player and right block entity
        if (pEntity instanceof ServerPlayer && pLevel.getBlockEntity(pPos) instanceof DungeonPortalBlockEntity) {
            // Get level ? from block entity
            ServerLevel level = DynamicDimensionManager.getLevel(pLevel.getServer(), ((DungeonPortalBlockEntity) pLevel.getBlockEntity(pPos)).getTargetLevel());
            if (level != null) {
                // Get spawn location and spawn
                DynamicDimensionManager.SpawnLocation spawnLocation = DynamicDimensionManager.getSpawnLocation(level, new Vec3(0, 250, 0), 10);
                ((ServerPlayer) pEntity).teleportTo(level, 0, 300, 0, pEntity.getXRot(), pEntity.getYRot());
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
        DungeonPortalBlockEntity tileEntity = (DungeonPortalBlockEntity) worldIn.getBlockEntity(pos);
        if(tileEntity instanceof DungeonPortalBlockEntity && tileEntity.getTargetLevel() != null) {
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