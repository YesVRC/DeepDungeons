package com.yesnt.deepdungeons.block.entity;

import com.yesnt.deepdungeons.item.ModItems;
import com.yesnt.utils.NanoID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;


public class DungeonPortalBlockEntity extends BlockEntity {

    private static final String ITEM_PROPERTY_KEY = "itemInserted";

    private ItemStack objectInserted = ItemStack.EMPTY;

    public DungeonPortalBlockEntity(BlockPos blockPos, BlockState pBlockState) {
        super(ModBlockEntities.DUNGEON_PORTAL_BLOCK_ENTITY.get(), blockPos, pBlockState);
    }

    @Override
    public void load(@NotNull CompoundTag compound)
    {
        super.load(compound);
        if (compound.contains(ITEM_PROPERTY_KEY))
        {
            setContents(ItemStack.of(compound.getCompound(ITEM_PROPERTY_KEY)));
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag compound)
    {
        super.saveAdditional(compound);

        // always send this, even if it is empty or air
        compound.put(ITEM_PROPERTY_KEY, objectInserted.save(new CompoundTag()));
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
    }

    public ItemStack getObjectInserted()
    {
        return this.objectInserted;
    }

    // be sure to notify the world of a block update after calling this
    public void setContents(ItemStack item)
    {
        this.objectInserted = item;
        this.objectInserted.setCount(1);
        if(this.objectInserted.is(ModItems.DUNGEON_KEY.get()) && !this.objectInserted.hasTag()){
            CompoundTag tag = new CompoundTag();
            tag.putString("dungeon_id", "dungeon_" + NanoID.randomNanoId());
            tag.putInt("lifetime", 0 );
            this.objectInserted.setTag(tag);
        }
        this.setChanged();
    }

    // be sure to notify the world of a block update after calling this
    public void removeContents()
    {
        this.objectInserted = ItemStack.EMPTY;
        this.setChanged();
    }
    public String getDungeonNameFromItem(){
        assert this.objectInserted.getTag() != null;
        return this.objectInserted.getTag().getString("dungeon_id");
    }

    public static void tick(Level level, BlockPos pos, BlockState state, DungeonPortalBlockEntity pEntity) {
        if(pEntity.getObjectInserted().hasTag()){
            assert pEntity.getObjectInserted().getTag() != null;
            pEntity.getObjectInserted().getTag().putInt("lifetime", pEntity.getObjectInserted().getTag().getInt("lifetime") + 1);
        }
    }
}
