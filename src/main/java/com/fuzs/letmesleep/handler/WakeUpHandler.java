package com.fuzs.letmesleep.handler;

import com.fuzs.letmesleep.util.SetSpawnPoint;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class WakeUpHandler {

    @SubscribeEvent
    public void onPlayerWake(PlayerWakeUpEvent evt) {

        if (!evt.getPlayer().world.isRemote) {

            ServerPlayerEntity player = (ServerPlayerEntity) evt.getPlayer();
            ServerWorld world = (ServerWorld) player.world;
            BlockPos spawn = player.getBedLocation(player.dimension);
            boolean flag = !ConfigBuildHandler.GENERAL_CONFIG.setSpawnAlways.get() && spawn != null && PlayerEntity.func_213822_a(world, spawn, false).isPresent();

            if (ConfigBuildHandler.GENERAL_CONFIG.setSpawn.get() != SetSpawnPoint.VANILLA || flag) {

                player.getBedPosition().filter(world::isBlockLoaded).ifPresent((p_213368_1_) -> {

                    BlockState blockstate = world.getBlockState(p_213368_1_);

                    if (blockstate.isBed(world, p_213368_1_, player)) {

                        blockstate.setBedOccupied(world, p_213368_1_, player, false);
                        Vec3d vec3d = blockstate.getBedSpawnPosition(player.getType(), world, p_213368_1_, player).orElseGet(()-> {
                            BlockPos blockpos = p_213368_1_.up();
                            return new Vec3d((double)blockpos.getX() + 0.5D, (double)blockpos.getY() + 0.1D, (double)blockpos.getZ() + 0.5D);
                        });

                        player.setPosition(vec3d.x, vec3d.y, vec3d.z);

                    }

                });

                player.clearBedPosition();

            }

//            if (evt.shouldSetSpawn() && !evt.wakeImmediately() && !evt.updateWorld()) {
//
//                if (ConfigBuildHandler.WAKE_UP_CONFIG.heal.get()) {
//
//                    int i = ConfigBuildHandler.WAKE_UP_CONFIG.healAmount.get();
//                    player.heal(i == 0 ? player.getMaxHealth() : i);
//
//                }
//
//                if (ConfigBuildHandler.WAKE_UP_CONFIG.starve.get()) {
//
//                    int j = ConfigBuildHandler.WAKE_UP_CONFIG.starveAmount.get();
//                    int k = player.getFoodStats().getFoodLevel();
//                    player.getFoodStats().setFoodLevel(MathHelper.clamp(k - (j == 0 ? k : j), 0, 20));
//
//                }
//
//                ClearPotions clearPotions = ConfigBuildHandler.WAKE_UP_CONFIG.clearPotions.get();
//                if (clearPotions == ClearPotions.BOTH) {
//
//                    player.clearActivePotions();
//
//                } else if (clearPotions == ClearPotions.POSITIVE) {
//
//                    ClearPotionsHelper.clearActivePotions(player, true);
//
//                } else if (clearPotions == ClearPotions.NEGATIVE) {
//
//                    ClearPotionsHelper.clearActivePotions(player, false);
//
//                }
//
//                if (ConfigBuildHandler.WAKE_UP_CONFIG.effects.get()) {
//
//                    this.applyPotions(player);
//
//                }
//
//            }

        }

    }

    private void applyPotions(PlayerEntity player) {

        List<String> list = ConfigBuildHandler.WAKE_UP_CONFIG.potionEffects.get();

        for (String s : list) {

            String error = "Potion effect to be applied on waking up has been specified incorrectly!";
            String[] values = s.split(",");
            Optional<Effect> effectOptional = Optional.empty();
            int duration = 0;
            int amplifier = 0;
            boolean showParticles = false;

            if (values.length > 0) {

                String[] name = values[0].split(":");

                if (name.length > 1) {
                    ResourceLocation location = new ResourceLocation(name[0], name[1]);
                    effectOptional = Optional.ofNullable(ForgeRegistries.POTIONS.getValue(location));
                } else {
                    LetMeSleep.LOGGER.error(error);
                }

            } else {
                LetMeSleep.LOGGER.error(error);
            }

            try {

                if (values.length > 1) {
                    duration = Integer.parseInt(values[1]) * 20;
                } else {
                    LetMeSleep.LOGGER.error(error);
                }

                if (values.length > 2) {
                    amplifier = Integer.parseInt(values[2]);
                }

            } catch (NumberFormatException e) {
                LetMeSleep.LOGGER.error(error, e);
            }

            if (values.length > 3) {
                showParticles = !Boolean.parseBoolean(values[3]);
            }

            if (effectOptional.isPresent()) {

                EffectInstance effect = new EffectInstance(effectOptional.get(), duration, amplifier, false, showParticles);
                player.addPotionEffect(effect);

            }

        }

    }

}
