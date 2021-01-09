package com.fuzs.letmesleep.handler;

import com.fuzs.letmesleep.helper.ReflectionHelper;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DungeonHooks;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

public class BadDreamHandler {

    @SuppressWarnings("unused")
    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent evt) {

        if (evt.phase != TickEvent.Phase.START || evt.world.isRemote) {
            return;
        }

        WorldServer world = (WorldServer) evt.world;

        if (world.areAllPlayersAsleep()) {

            if (!ConfigBuildHandler.generalConfig.spawnMonster || !this.performSleepSpawning(world, world.playerEntities, world.getDifficulty().getDifficultyId())) {

                if (world.getGameRules().getBoolean("doDaylightCycle")) {

                    long dayTime = world.getWorldTime() + 24000L;
                    long wakeUpTime = ConfigBuildHandler.sleepTimingsConfig.wakeUpTime;
                    world.setWorldTime(dayTime - (24000L - wakeUpTime + dayTime) % 24000L);

                }

                this.wakeAllPlayers(world);

            }

        }

    }

    private boolean performSleepSpawning(World world, List<? extends EntityPlayer> playerlist, int difficulty) {

        boolean flag = false;

        for (EntityPlayer player : playerlist) {

            if (player.isCreative() || player.isSpectator() || player.bedLocation == null) {
                continue;
            }

            int i = 0;
            boolean flag1 = false;
            BlockPos bedPos = player.bedLocation;
            if (!(world.getBlockState(bedPos).getBlock() instanceof BlockBed)) {
                continue;
            }

            while (i < difficulty * ConfigBuildHandler.generalConfig.spawnMonsterChance && !flag1) {

                EnumFacing direction = world.getBlockState(bedPos).getValue(BlockHorizontal.FACING).getOpposite();
                double d1 = world.rand.nextDouble() - world.rand.nextDouble();
                double d2 = world.rand.nextDouble() - world.rand.nextDouble();

                double xCoord = (double) bedPos.getX() + d1 * (2.0 + (Math.signum(d1) == Math.signum(direction.getFrontOffsetX()) ? Math.abs(direction.getFrontOffsetX()) : 0)) + 0.5;
                double yCoord = bedPos.getY() + world.rand.nextInt(3) - 1;
                double zCoord = (double) bedPos.getZ() + d2 * (2.0 + (Math.signum(d2) == Math.signum(direction.getFrontOffsetZ()) ? Math.abs(direction.getFrontOffsetZ()) : 0)) + 0.5;

                BlockPos pos = new BlockPos(xCoord, yCoord, zCoord);
                boolean solidCheck = world.getBlockState(pos.down()).isSideSolid(world, pos.down(), EnumFacing.UP);
                boolean lightCheck = world.getLightFromNeighbors(pos) < 8;

                if (solidCheck && lightCheck) {

                    Entity entity = Optional.ofNullable(ForgeRegistries.ENTITIES.getValue(DungeonHooks.getRandomDungeonMob(world.rand))).map(entityEntry ->
                            entityEntry.newInstance(world)).orElse(null);

                    if (entity instanceof EntityLiving) {

                        EntityLiving mob = (EntityLiving) entity;
                        mob.setLocationAndAngles(xCoord, yCoord, zCoord, world.rand.nextFloat() * 360.0F, 0.0F);

                        if (entity.world.getCollisionBoxes(entity, entity.getEntityBoundingBox()).isEmpty()
                                && !entity.world.containsAnyLiquid(entity.getEntityBoundingBox())) {

                            mob.onGround = true; // required for navigator to be able to find a path
                            Path path = mob.getNavigator().getPathToEntityLiving(player);

                            if (path != null && path.getCurrentPathLength() > 1) {

                                PathPoint pathpoint = path.getFinalPathPoint();

                                if (pathpoint != null && world.isAnyPlayerWithinRangeAt(pathpoint.x, pathpoint.y, pathpoint.z, 1.5)) {

                                    mob.onInitialSpawn(world.getDifficultyForLocation(new BlockPos(entity)), null);
                                    mob.setAttackTarget(player);
                                    mob.playLivingSound();
                                    this.addEntityPassengers(world, entity);

                                    player.wakeUpPlayer(true, false, false);
                                    flag = flag1 = true;

                                }

                            }

                        }

                    }

                }

                i++;

            }

        }

        return flag;

    }

    private void addEntityPassengers(World world, Entity entity) {

        if (world.spawnEntity(entity)) {

            for(Entity e : entity.getPassengers()) {
                this.addEntityPassengers(world, e);
            }

        }

    }

    private void wakeAllPlayers(WorldServer world) {

        Method wakeAllPlayers = ReflectionHelper.getWakeAllPlayers();

        if (wakeAllPlayers != null) {

            try {

                wakeAllPlayers.invoke(world);

            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }

        }

    }

}
