package com.yesnt.deepdungeons.item.custom;

import com.yesnt.deepdungeons.dimension.tools.DynamicDimensionManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class DungeonKey extends Item {

    public KeyTier tier;
    public DungeonKey(Properties pProperties, KeyTier pTier) {
        super(pProperties);
        this.tier = pTier;
    }

    @Override
    public boolean isFoil(ItemStack pStack) {
        return true;
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return 1;
    }

    KeyTier getTier() {
        return tier;
    }

    public enum KeyTier {
        DEBUG,
        ONE,
        TWO,
        THREE,
        FOUR
    }
}
