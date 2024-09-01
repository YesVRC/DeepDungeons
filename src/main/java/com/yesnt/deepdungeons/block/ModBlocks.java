package com.yesnt.deepdungeons.block;

import com.yesnt.deepdungeons.DeepDungeons;
import com.yesnt.deepdungeons.block.custom.DungeonPortalBlock;
import com.yesnt.deepdungeons.item.ModCreativeTabs;
import com.yesnt.deepdungeons.item.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, DeepDungeons.MODID);

    public static final RegistryObject<Block> TEST_BLOCK = registerBlock("test_block",
            () -> new Block(BlockBehaviour.Properties.of(Material.STONE)),
            ModCreativeTabs.DEEPDUNGEONS_TAB);

    public static final RegistryObject<Block> DUNGEON_PORTAL_BLOCK = registerBlock("dungeon_portal_block",
            () -> new DungeonPortalBlock(BlockBehaviour.Properties.of(Material.STONE)),
            ModCreativeTabs.DEEPDUNGEONS_TAB);


    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block, CreativeModeTab tab) {
        RegistryObject<T> blockObj = BLOCKS.register(name, block);
        registerBlockItem(name, blockObj, tab);
        return blockObj;
    }

    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, Supplier<T> block, CreativeModeTab tab) {

        return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(tab)));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
