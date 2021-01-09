package com.fuzs.letmesleep.handler;

import com.fuzs.letmesleep.helper.ReflectionHelper;
import com.fuzs.letmesleep.helper.SetSpawnHelper;
import com.fuzs.letmesleep.helper.TimeFormatHelper;
import com.fuzs.letmesleep.util.SetSpawnPoint;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

public class SleepAttemptHandler {

    @SuppressWarnings("unused")
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onPlayerSleep(PlayerSleepInBedEvent evt) {

        EntityPlayer player = evt.getEntityPlayer();
        World world = player.world;
        BlockPos at = evt.getPos();
        BlockPos spawn = player.getBedLocation(player.dimension);
        IBlockState state = world.isBlockLoaded(at) ? world.getBlockState(at) : null;
        boolean isBed = state != null && state.getBlock().isBed(state, world, at, player);
        EnumFacing facing = isBed && state.getBlock() instanceof BlockHorizontal ? state.getValue(BlockHorizontal.FACING) : null;

        if (!world.isRemote) {

            // another mod changed the result
            if (evt.getResultStatus() != null) {
                return;
            }

            if (player.isPlayerSleeping() || !player.isEntityAlive()) {
                evt.setResult(EntityPlayer.SleepResult.OTHER_PROBLEM);
                return;
            }

            if (!world.provider.isSurfaceWorld()) {
                evt.setResult(EntityPlayer.SleepResult.NOT_POSSIBLE_HERE);
                return;
            }

            if (ConfigBuildHandler.sleepConfig.rangeCheck && !this.bedInRange(player, at, facing)) {
                evt.setResult(EntityPlayer.SleepResult.TOO_FAR_AWAY);
                return;
            }

            if (ConfigBuildHandler.sleepConfig.obstructionCheck && facing != null && this.bedObstructed(player, at, facing)) {
                player.sendStatusMessage(new TextComponentTranslation("block.minecraft.bed.obstructed"), true);
                evt.setResult(EntityPlayer.SleepResult.OTHER_PROBLEM);
                return;
            }

            if (SetSpawnHelper.isNewSpawnAllowed(world, player, at, SetSpawnPoint.INTERACT) && !player.isSneaking()) {
                player.setSpawnPoint(at, false);
            }

            if (!net.minecraftforge.event.ForgeEventFactory.fireSleepingTimeCheck(player, at)) {
                this.sendNotPossibleNowMessage(player);
                evt.setResult(EntityPlayer.SleepResult.OTHER_PROBLEM);
                return;
            }

            if (ConfigBuildHandler.sleepConfig.monsterCheck && !player.isCreative()) {

                List<EntityMob> list = player.world.getEntitiesWithinAABB(EntityMob.class, new AxisAlignedBB((double) at.getX() - 8.0D,
                        (double) at.getY() - 5.0D, (double) at.getZ() - 8.0D, (double) at.getX() + 8.0D,
                        (double) at.getY() + 5.0D, (double) at.getZ() + 8.0D), it -> {
                    if (it != null) {
                        boolean name = ConfigBuildHandler.sleepConfig.namedMonsters || !it.hasCustomName();
                        boolean persistent = ConfigBuildHandler.sleepConfig.persistentMonsters || !it.isNoDespawnRequired();
                        return it.isPreventingPlayerRest(player) && name && persistent && it.isEntityAlive();
                    }
                    return false;
                });

                if (!list.isEmpty()) {

                    if (ConfigBuildHandler.sleepConfig.glow) {
                        list.forEach(it -> it.addPotionEffect(new PotionEffect(MobEffects.GLOWING, ConfigBuildHandler.sleepConfig.glowDuration)));
                    }

                    evt.setResult(EntityPlayer.SleepResult.NOT_SAFE);
                    return;

                }

            }

            if (!ConfigBuildHandler.sleepTimingsConfig.instantSleeping && SetSpawnHelper.isNewSpawnAllowed(world, player, at, SetSpawnPoint.CHAT)) {
                player.sendStatusMessage(SetSpawnHelper.createRespawnMessage(), false);
            }

        }

        if (player.isRiding()) {
            player.dismountRidingEntity();
        }

        this.spawnShoulderEntities(player);
        this.setSize(player, 0.2F, 0.2F);

        if (facing != null) {

            float f1 = 0.5F + (float)facing.getFrontOffsetX() * 0.4F;
            float f = 0.5F + (float)facing.getFrontOffsetZ() * 0.4F;
            player.renderOffsetX = -1.8F * (float) facing.getFrontOffsetX();
            player.renderOffsetZ = -1.8F * (float) facing.getFrontOffsetZ();
            player.setPosition(((float) at.getX() + f1), ((float) at.getY() + 0.6875F), ((float) at.getZ() + f));

        } else {

            player.setPosition(((float) at.getX() + 0.5F), ((float) at.getY() + 0.6875F), ((float) at.getZ() + 0.5F));

        }

        ReflectionHelper.setSleeping(player, true);
        ReflectionHelper.setSleepTimer(player, ConfigBuildHandler.sleepTimingsConfig.instantSleeping ? 100 : 0);
        player.bedLocation = at;
        player.motionX = 0.0D;
        player.motionY = 0.0D;
        player.motionZ = 0.0D;

        if (!player.world.isRemote) {
            player.world.updateAllPlayersSleepingFlag();
        }

        // stop vanilla from executing
        evt.setResult(EntityPlayer.SleepResult.OK);

    }

    private boolean bedInRange(EntityPlayer player, BlockPos pos, EnumFacing facing) {

        Method bedInRange = ReflectionHelper.getBedInRange();

        if (bedInRange != null) {

            try {

                return (boolean) bedInRange.invoke(player, pos, facing);

            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }

        }

        return true;

    }

    private void spawnShoulderEntities(EntityPlayer player) {

        Method spawnShoulderEntities = ReflectionHelper.getSpawnShoulderEntities();

        if (spawnShoulderEntities != null) {

            try {

                spawnShoulderEntities.invoke(player);

            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }

        }

    }

    private boolean bedObstructed(EntityPlayer player, BlockPos pos, EnumFacing facing) {

        BlockPos blockpos1 = pos.up();
        BlockPos blockpos2 = blockpos1.offset(facing.getOpposite());
        boolean flag1 = player.world.getBlockState(blockpos1).causesSuffocation();
        boolean flag2 = player.world.getBlockState(blockpos2).causesSuffocation();
        return flag1 || flag2;

    }

    @SuppressWarnings("SameParameterValue")
    private void setSize(EntityPlayer player, float width, float height) {

        Method setSize = ReflectionHelper.getSetSize();

        if (setSize != null) {

            try {

                setSize.invoke(player, width, height);

            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }

        }

    }

    private void sendNotPossibleNowMessage(EntityPlayer player) {

        int min = ConfigBuildHandler.sleepTimingsConfig.bedtimeStart;
        int max = ConfigBuildHandler.sleepTimingsConfig.bedtimeEnd;
        String minTime = TimeFormatHelper.formatTime(min);
        String maxTime = TimeFormatHelper.formatTime(max);
        boolean thunder = ConfigBuildHandler.sleepTimingsConfig.bedtimeThunder;
        boolean rain = ConfigBuildHandler.sleepTimingsConfig.bedtimeRain;

        TextComponentTranslation time;
        Optional<TextComponentTranslation> weather = Optional.empty();
        TextComponentTranslation message;

        // use adapted vanilla message for default timings
        if (min == 12541 && max == 23458) {
            time = new TextComponentTranslation("block.minecraft.bed.no_sleep.night");
        } else {
            time = new TextComponentTranslation("block.minecraft.bed.no_sleep.time", minTime, maxTime);
        }

        if (thunder && rain) {
            weather = Optional.of(new TextComponentTranslation("block.minecraft.bed.no_sleep.bad_weather"));
        } else if (thunder) {
            weather = Optional.of(new TextComponentTranslation("block.minecraft.bed.no_sleep.thunder"));
        } else if (rain) {
            weather = Optional.of(new TextComponentTranslation("block.minecraft.bed.no_sleep.rain"));
        }

        message = weather.map(it -> new TextComponentTranslation("block.minecraft.bed.no_sleep.long_message", time, it))
                .orElseGet(() -> new TextComponentTranslation("block.minecraft.bed.no_sleep.short_message", time));

        player.sendStatusMessage(message, true);

    }

}
