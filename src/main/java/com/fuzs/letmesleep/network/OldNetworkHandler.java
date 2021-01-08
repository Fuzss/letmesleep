package com.fuzs.letmesleep.network;

import com.fuzs.letmesleep.LetMeSleep;
import com.fuzs.letmesleep.network.message.RequestSpawnMessage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

@SuppressWarnings("unused")
public class OldNetworkHandler {

    private static final String PROTOCOL_VERSION = Integer.toString(1);
    private static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(LetMeSleep.MODID, "main_channel"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void init() {

        int discriminator = 0;
        INSTANCE.registerMessage(discriminator, RequestSpawnMessage.class, RequestSpawnMessage::writePacketData, RequestSpawnMessage::readPacketData, RequestSpawnMessage::processPacket);

    }

    public static void sendToServer(Object message) {

        INSTANCE.sendToServer(message);

    }

    public static void sendTo(Object message, ServerPlayerEntity player) {

        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);

    }

    public static void sendToAll(Object message) {

        INSTANCE.send(PacketDistributor.ALL.noArg(), message);

    }

}