package com.yesnt.deepdungeons.block.entity;

import com.yesnt.deepdungeons.DeepDungeons;
import com.yesnt.deepdungeons.block.ModBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, DeepDungeons.MODID);

    public static final RegistryObject<BlockEntityType<DungeonPortalBlockEntity>> DUNGEON_PORTAL_BLOCK_ENTITY = BLOCK_ENTITIES.register("dungeon_portal_block_entity",
            () -> BlockEntityType.Builder.of(DungeonPortalBlockEntity::new, ModBlocks.DUNGEON_PORTAL_BLOCK.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
