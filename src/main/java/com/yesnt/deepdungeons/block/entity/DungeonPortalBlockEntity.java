package com.yesnt.deepdungeons.block.entity;

import com.yesnt.deepdungeons.block.custom.DungeonPortalBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class DungeonPortalBlockEntity extends BlockEntity {

    private static final String TARGET_DIMENSION_KEY = "targetDimension";
    private static final String LIFETIME_KEY = "lifetime";

    private String targetLevel = null;
    private int lifetime = 0;

    public DungeonPortalBlockEntity(BlockPos blockPos, BlockState pBlockState) {
        super(ModBlockEntities.DUNGEON_PORTAL_BLOCK_ENTITY.get(), blockPos, pBlockState);
    }

    @Override
    public void load(@NotNull CompoundTag compound)
    {
        super.load(compound);
        if (compound.contains(TARGET_DIMENSION_KEY))
        {
            this.targetLevel = compound.getString(TARGET_DIMENSION_KEY);
        }
        if(compound.contains(LIFETIME_KEY)){
            this.lifetime = compound.getInt(LIFETIME_KEY);
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag compound)
    {
        super.saveAdditional(compound);

        // always send this, even if it is empty or air
        compound.putString(TARGET_DIMENSION_KEY, this.targetLevel);
        compound.putInt(LIFETIME_KEY, this.lifetime);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
    }

    @Nullable
    public String getTargetLevel() {return this.targetLevel;}

    public void setTargetLevel(String targetLevel){
        this.targetLevel = targetLevel;
        this.getBlockState().setValue(DungeonPortalBlock.activated, targetLevel != null);
        this.lifetime = 0;
        this.setChanged();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, DungeonPortalBlockEntity pEntity) {
        if (pEntity.targetLevel != null){
            pEntity.lifetime++;
        }
    }
}
