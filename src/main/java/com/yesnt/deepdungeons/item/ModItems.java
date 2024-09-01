package com.yesnt.deepdungeons.item;

import com.yesnt.deepdungeons.DeepDungeons;
import com.yesnt.deepdungeons.item.custom.DungeonKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, DeepDungeons.MODID);

    public static final RegistryObject<Item> TEST_ITEM = ITEMS.register("test_item",
            () -> new Item(new Item.Properties().tab(ModCreativeTabs.DEEPDUNGEONS_TAB))
    );

    public static final RegistryObject<Item> DUNGEON_KEY = ITEMS.register("dungeon_key", () -> new DungeonKey(new Item.Properties().tab(ModCreativeTabs.DEEPDUNGEONS_TAB)));
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

}
