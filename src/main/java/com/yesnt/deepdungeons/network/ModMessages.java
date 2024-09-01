package com.yesnt.deepdungeons.network;

import com.yesnt.deepdungeons.DeepDungeons;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModMessages {

    public static SimpleChannel INSTANCE;

    private static int packetId = 0;
    private static int id() {
        return packetId++;
    }

    public static void register(){
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(DeepDungeons.MODID, "messages"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = net;

        net.messageBuilder(PacketSyncDimensionListChanges.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(PacketSyncDimensionListChanges::new)
                .encoder(PacketSyncDimensionListChanges::toBytes)
                .consumerMainThread(PacketSyncDimensionListChanges::handle)
                .add();
    }

    public static <MSG> void sendToServer(MSG packet){
        INSTANCE.sendToServer(packet);
    }

    public static <MSG> void sendToClient(MSG packet, ServerPlayer player){
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }
}
