package com.yesnt.deepdungeons.events;

import com.yesnt.deepdungeons.DeepDungeons;
import com.yesnt.deepdungeons.commands.PingCommand;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DeepDungeons.MODID)
public class ModEvents {

    @SubscribeEvent
    public static void onCommandsRegistered(final RegisterCommandsEvent event) {
        new PingCommand(event.getDispatcher());
    }
}
