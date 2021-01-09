package com.fuzs.letmesleep.handler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.event.entity.player.PlayerSetSpawnEvent;
import net.minecraftforge.event.entity.player.SleepingTimeCheckEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CommonEventHandler {

    @SuppressWarnings("unused")
    @SubscribeEvent
    public void onSleepingTimeCheck(SleepingTimeCheckEvent evt) {

        EntityPlayer player = evt.getEntityPlayer();

        if (!player.world.isRemote) {

            long time = player.world.getWorldTime() % 24000L;
            long start = ConfigBuildHandler.sleepTimingsConfig.bedtimeStart;
            long end = ConfigBuildHandler.sleepTimingsConfig.bedtimeEnd;
            boolean flag = end < start ? start <= time || time <= end : start <= time && time <= end;

            boolean thunder = ConfigBuildHandler.sleepTimingsConfig.bedtimeThunder && player.world.getWorldInfo().isThundering();
            boolean rain = ConfigBuildHandler.sleepTimingsConfig.bedtimeRain && player.world.getWorldInfo().isRaining();

            evt.setResult(flag || thunder || rain ? Event.Result.ALLOW : Event.Result.DENY);

        }

    }

    @SuppressWarnings("unused")
    @SubscribeEvent
    public void onSetSpawn(PlayerSetSpawnEvent evt) {

        EntityPlayer player = evt.getEntityPlayer();
        boolean flag = !player.world.isRemote && !evt.isForced();

        if (flag && evt.getNewSpawn() != null && !evt.getNewSpawn().equals(player.getBedLocation(player.dimension))) {

            player.sendStatusMessage(new TextComponentTranslation("block.minecraft.bed.spawn_set"), false);

        }

    }

}
