package com.yesnt.deepdungeons.world;

import com.yesnt.deepdungeons.DeepDungeons;
import com.yesnt.deepdungeons.world.placement.DungeonPlacement;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModStructurePlacement {
    public static final DeferredRegister<StructurePlacementType<?>> STRUCTURE_PLACEMENT_TYPE = DeferredRegister.create(Registry.STRUCTURE_PLACEMENT_TYPE_REGISTRY, DeepDungeons.MODID);

    public static final RegistryObject<StructurePlacementType<DungeonPlacement>> DUNGEON_PLACEMENT = STRUCTURE_PLACEMENT_TYPE.register("dungeon_placement", () -> () -> DungeonPlacement.CODEC);

    public static void register(IEventBus bus) {
        STRUCTURE_PLACEMENT_TYPE.register(bus);
    }
}