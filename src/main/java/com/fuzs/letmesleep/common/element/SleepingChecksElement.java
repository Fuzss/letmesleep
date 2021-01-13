package com.fuzs.letmesleep.common.element;

import com.fuzs.letmesleep.mixin.accessor.IPlayerEntityAccessor;
import com.fuzs.letmesleep.mixin.accessor.IServerPlayerEntityAccessor;
import com.fuzs.letmesleep.mixin.accessor.IServerWorldAccessor;
import com.fuzs.puzzleslib.config.deserialize.EntryCollectionBuilder;
import com.fuzs.puzzleslib.element.AbstractElement;
import com.fuzs.puzzleslib.element.ISidedElement;
import com.google.common.collect.Lists;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class SleepingChecksElement extends AbstractElement implements ISidedElement.Common {

    // config settings
    private boolean rangeCheck;
    private boolean obstructionCheck;
    private boolean monsterCheck;
    private Set<? extends EntityType<?>> monsterBlacklist;
    private boolean makeMonstersGlow;
    private int monsterGlowDuration;
    private boolean allowNamedMonsters;
    private boolean instantSleeping;

    @Override
    public boolean getDefaultState() {

        return true;
    }

    @Override
    public String getDescription() {

        return "Allow configuration for every option that could normally prevent the player from sleeping.";
    }

    @Override
    public void setupCommon() {

        // run after other mods as we check for that
        this.addListener(this::onPlayerSleepInBed, EventPriority.LOW);
    }

    @Override
    public void setupCommonConfig(ForgeConfigSpec.Builder builder) {

        addToConfig(builder.comment("Check if the player is close enough to the bed.").define("Perform Range Check", false), v -> this.rangeCheck = v);
        addToConfig(builder.comment("Check if the bed has enough open space above it.").define("Perform Obstruction Check", true), v -> this.obstructionCheck = v);
        addToConfig(builder.comment("Check if monsters are nearby.").define("Perform Monster Check", true), v -> this.monsterCheck = v);
        addToConfig(builder.comment("Monsters to be ignored when checking for monsters nearby.", EntryCollectionBuilder.CONFIG_STRING).define("Monster Check Blacklist", Lists.<String>newArrayList()),v -> this.monsterBlacklist = v, v -> new EntryCollectionBuilder<>(ForgeRegistries.ENTITIES).buildEntrySet(v, entry -> entry.getClassification() == EntityClassification.MONSTER, "Not a monster"));
        addToConfig(builder.comment("Should monsters preventing the player from sleeping glow.").define("Make Monsters Glow", true), v -> this.makeMonstersGlow = v);
        addToConfig(builder.comment("Duration in seconds for which the monsters nearby will glow.").defineInRange("Monster Glowing Duration", 3, 0, Integer.MAX_VALUE), v -> this.monsterGlowDuration = v);
        addToConfig(builder.comment("Should sleeping be allowed when named monsters are nearby.").define("Allow Named Monsters", true), v -> this.allowNamedMonsters = v);
        addToConfig(builder.comment("Removes the falling asleep animation, so the player wakes up instantly after going to bed. Only works when all other players on the server are already asleep.").define("Instant Sleeping", false), v -> this.instantSleeping = v);
    }

    private void onPlayerSleepInBed(final PlayerSleepInBedEvent evt) {

        BlockPos at = evt.getPos();
        PlayerEntity.SleepResult result = evt.getResultStatus();
        boolean isModInterfering = result != null || at == null || !evt.getPlayer().world.getBlockState(at).isIn(BlockTags.BEDS);
        if (isModInterfering || evt.getPlayer().isSleeping() || !evt.getPlayer().isAlive()) {

            return;
        }

        ServerPlayerEntity player = (ServerPlayerEntity) evt.getPlayer();
        ServerWorld world = (ServerWorld) player.world;
        Direction direction = world.getBlockState(at).get(HorizontalBlock.HORIZONTAL_FACING);
        result = this.isSpawnSettingAllowed(at, (IServerPlayerEntityAccessor) player, world, direction);
        if (result != null) {

            evt.setResult(result);
            return;
        }

        // set new spawn point and send feedback message
        player.func_242111_a(world.getDimensionKey(), at, player.rotationYaw, false, true);
        if (!ForgeEventFactory.fireSleepingTimeCheck(player, Optional.of(at))) {

            evt.setResult(PlayerEntity.SleepResult.NOT_POSSIBLE_NOW);
        } else {

            result = this.isMonsterNearby(player, world, at);
            if (result != null) {

                evt.setResult(result);
                return;
            }

            this.trySleep(player, at);
            world.updateAllPlayersSleepingFlag();
            this.setInstantSleeping(player, player.getServerWorld());
            evt.setResult(PlayerEntity.SleepResult.OTHER_PROBLEM);
        }
    }

    @Nullable
    private PlayerEntity.SleepResult isSpawnSettingAllowed(BlockPos at, IServerPlayerEntityAccessor player, ServerWorld world, Direction direction) {

        if (!world.getDimensionType().isNatural()) {

            return PlayerEntity.SleepResult.NOT_POSSIBLE_HERE;
        } else if (!player.callIsBedInRange(at, direction) && this.rangeCheck) {

            return PlayerEntity.SleepResult.TOO_FAR_AWAY;
        } else if (player.callIsBedObstructed(at, direction) && this.obstructionCheck) {

            return PlayerEntity.SleepResult.OBSTRUCTED;
        }

        return null;
    }

    @Nullable
    private PlayerEntity.SleepResult isMonsterNearby(ServerPlayerEntity player, ServerWorld world, BlockPos at) {

        if (this.monsterCheck && !player.isCreative()) {

            Vector3d vector3d = Vector3d.copyCenteredHorizontally(at);
            Predicate<MonsterEntity> sleepingPredicate = monster -> this.isMonsterNearby(monster, player);
            List<MonsterEntity> monsterList = world.getEntitiesWithinAABB(MonsterEntity.class, new AxisAlignedBB(vector3d.getX() - 8.0, vector3d.getY() - 5.0, vector3d.getZ() - 8.0, vector3d.getX() + 8.0, vector3d.getY() + 5.0, vector3d.getZ() + 8.0), sleepingPredicate);
            if (!monsterList.isEmpty()) {

                if (this.makeMonstersGlow) {

                    // make particles not show
                    Supplier<EffectInstance> effect = () -> new EffectInstance(Effects.GLOWING, this.monsterGlowDuration * 20, 0, false, false);
                    monsterList.forEach(monster -> monster.addPotionEffect(effect.get()));
                }

                return PlayerEntity.SleepResult.NOT_SAFE;
            }
        }

        return null;
    }

    private boolean isMonsterNearby(MonsterEntity monster, PlayerEntity player) {

        boolean isBlacklisted = this.monsterBlacklist.contains(monster.getType());
        boolean isNamed = !this.allowNamedMonsters || !monster.hasCustomName();

        // isPreventingPlayerRest
        return !isBlacklisted && monster.isAlive() && monster.func_230292_f_(player) && isNamed;
    }

    private void trySleep(ServerPlayerEntity player, BlockPos at) {

        player.startSleeping(at);
        ((IPlayerEntityAccessor) player).setSleepTimer(0);
        player.addStat(Stats.SLEEP_IN_BED);
        CriteriaTriggers.SLEPT_IN_BED.trigger(player);
    }

    private void setInstantSleeping(ServerPlayerEntity player, ServerWorld world) {

        // needs to run after ServerPlayerEntity#startSleeping is called
        if (this.instantSleeping && ((IServerWorldAccessor) world).getAllPlayersSleeping()) {

            int sleepTimer = world.getPlayers().stream()
                    .filter(entity -> !entity.isSpectator() && entity != player)
                    .mapToInt(entity -> ((IPlayerEntityAccessor) player).getSleepTimer())
                    .min().orElse(100);

            ((IPlayerEntityAccessor) player).setSleepTimer(sleepTimer);
        }
    }

}
