package com.yesnt.deepdungeons.item.custom;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class DungeonKey extends Item {

    public DungeonKey(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public boolean isFoil(ItemStack pStack) {
        return pStack.hasTag();
    }
}
