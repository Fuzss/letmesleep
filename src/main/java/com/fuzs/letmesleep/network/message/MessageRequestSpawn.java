package com.fuzs.letmesleep.network.message;

import com.fuzs.letmesleep.helper.SetSpawnHelper;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketSpawnPosition;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageRequestSpawn implements IMessage, IMessageHandler<MessageRequestSpawn, MessageRequestSpawn> {

    private BlockPos position;

    @SuppressWarnings("unused")
    public MessageRequestSpawn() {
    }

    public MessageRequestSpawn(BlockPos pos) {

        this.position = pos;

    }

    @Override
    public void fromBytes(ByteBuf buf) {

        this.position = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());

    }

    @Override
    public void toBytes(ByteBuf buf) {

        buf.writeInt(this.position.getX());
        buf.writeInt(this.position.getY());
        buf.writeInt(this.position.getZ());

    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public MessageRequestSpawn onMessage(MessageRequestSpawn message, MessageContext ctx) {

        EntityPlayerMP player = ctx.getServerHandler().player;

        player.getServerWorld().addScheduledTask(() -> {

            BlockPos pos = message.getPosition();

            // can actually be null, although the IDE says it won't be, therefore a null check later on
            BlockPos spawn = player.getBedLocation(player.dimension);

            if (pos.equals(player.world.getSpawnPoint()) && spawn != null) {

                player.connection.sendPacket(new SPacketSpawnPosition(spawn));

            } else if (pos.equals(player.bedLocation) && SetSpawnHelper.isNewSpawnAllowed(player.world, player, pos, null)) {

                player.setSpawnPoint(pos, false);
                player.connection.sendPacket(new SPacketSpawnPosition(pos));

            }

        });

        return null;

    }

    private BlockPos getPosition() {
        return this.position;
    }

}
