package com.fuzs.letmesleep.common.element;

import com.fuzs.letmesleep.mixin.accessor.IServerWorldAccessor;
import com.fuzs.puzzleslib.config.deserialize.EntryCollectionBuilder;
import com.fuzs.puzzleslib.element.AbstractElement;
import com.fuzs.puzzleslib.element.ISidedElement;
import com.google.common.collect.Lists;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.command.arguments.EntityAnchorArgument;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.state.properties.BedPart;
import net.minecraft.util.Direction;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Difficulty;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.spawner.WorldEntitySpawner;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

public class BadDreamElement extends AbstractElement implements ISidedElement.Common {

    private int spawnMonsterChance;
    private int spawnMonsterRange;
    private Map<EntityType<?>, Integer> spawnableMonsters;

    @Override
    public boolean getDefaultState() {

        return true;
    }

    @Override
    public String getDisplayName() {

        return "Bad Dreams";
    }

    @Override
    public String getDescription() {

        return "Try to spawn a random monster and wake the player when sleeping in a dark area.";
    }

    @Override
    public void setupCommon() {

        this.addListener(this::onWorldTick);
    }

    @Override
    public void setupCommonConfig(ForgeConfigSpec.Builder builder) {

        addToConfig(builder.comment("Chance to spawn a monster. Higher values make it more likely to happen.").defineInRange("Spawn Monster Chance", 12, 0, 128), v -> this.spawnMonsterChance = v);
        addToConfig(builder.comment("Range to spawn a monster in from the bed. Increasing the range makes it a lot less likely for a monster to spawn.").defineInRange("Spawn Monster Range", 2, 0, 16), v -> this.spawnMonsterRange = v);
        addToConfig(builder.comment("Possible monsters to spawn. One entry is chosen at random.", EntryCollectionBuilder.CONFIG_STRING_BUILDER.apply(",<weight>")).define("Spawnable Monsters", Lists.newArrayList(getEntityAndWeight(EntityType.ZOMBIE, 2), getEntityAndWeight(EntityType.SKELETON, 1), getEntityAndWeight(EntityType.SPIDER, 1))),v -> {

            BiPredicate<EntityType<?>, double[]> predicate = (entry, value) -> entry.getClassification() == EntityClassification.MONSTER && value.length == 1 && value[0] > 0;
            Map<EntityType<?>, double[]> entries = new EntryCollectionBuilder<>(ForgeRegistries.ENTITIES).buildEntryMap(v, predicate , "Not a monster or incorrect weight");
            this.spawnableMonsters = entries.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, value -> (int) value.getValue()[0]));
        });
    }

    @SuppressWarnings("ConstantConditions")
    private static String getEntityAndWeight(EntityType<?> entityType, int weight) {

        return entityType.getRegistryName().toString() + "," + weight;
    }

    private void onWorldTick(final TickEvent.WorldTickEvent evt) {

        if (evt.phase != TickEvent.Phase.START || evt.world.isRemote || evt.world.getDifficulty() == Difficulty.PEACEFUL) {

            return;
        }

        ServerWorld world = (ServerWorld) evt.world;
        List<ServerPlayerEntity> allPlayers = world.getPlayers();
        if (((IServerWorldAccessor) world).getAllPlayersSleeping() && allPlayers.stream().noneMatch(player -> !player.isSpectator() && !player.isPlayerFullyAsleep())) {

            this.performSleepSpawning(world, allPlayers);
        }
    }

    private void performSleepSpawning(ServerWorld world, List<ServerPlayerEntity> allPlayers) {

        for (ServerPlayerEntity player : allPlayers) {

            if (!player.getBedPosition().isPresent() || !EntityPredicates.CAN_AI_TARGET.test(player)) {

                continue;
            }

            AxisAlignedBB spawningBoundingBox = this.getSpawningBoundingBox(world, player);
            if (spawningBoundingBox == null) {

                continue;
            }

            int maxAttempts = this.spawnMonsterChance * (int) Math.pow(2, world.getDifficulty().getId() - 1);
            for (int index = 0; index < maxAttempts; index++) {

                if (this.trySpawnMonster(world, player, spawningBoundingBox)) {

                    break;
                }
            }
        }
    }

    private boolean trySpawnMonster(ServerWorld world, ServerPlayerEntity player, AxisAlignedBB spawningBoundingBox) {

        BlockPos pos = this.getRandomPosWithinAABB(spawningBoundingBox, world.getRandom());
        Optional<EntityType<?>> entityType = this.getRandomEntity(world.getRandom());
        if (entityType.isPresent() && this.isSpawningPermitted(world, entityType.get(), pos)) {

            Entity entity = entityType.get().create(world);
            if (entity instanceof MobEntity) {

                entity.setLocationAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, MathHelper.wrapDegrees(world.getRandom().nextFloat() * 360.0F), 0.0F);
                if (world.checkNoEntityCollision(entity) && world.hasNoCollisions(entity) && !world.containsAnyLiquid(entity.getBoundingBox()) && this.setPathToPlayer(world, player, (MobEntity) entity)) {

                    this.setupSpawnedMonster((MobEntity) entity, world);
                    player.wakeUp();
                    player.lookAt(EntityAnchorArgument.Type.EYES, entity, EntityAnchorArgument.Type.EYES);

                    return true;
                } else {

                    entity.remove();
                }
            }
        }

        return false;
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Nullable
    private AxisAlignedBB getSpawningBoundingBox(ServerWorld world, PlayerEntity player) {

        BlockPos bedPos = player.getBedPosition().get();
        BlockState state = world.getBlockState(bedPos);
        // mods could add bed position which is not a bed
        if (state.isBed(world, bedPos, player)) {

            Direction bedPosDirection = state.get(HorizontalBlock.HORIZONTAL_FACING);
            // check for part, who knows what might happen
            Direction otherPartDirection = state.get(BedBlock.PART) == BedPart.FOOT ? bedPosDirection : bedPosDirection.getOpposite();

            // expand by one in all positive directions as this side will be excluded when picking a random spot
            return new AxisAlignedBB(bedPos, bedPos.offset(otherPartDirection)).grow(this.spawnMonsterRange).expand(1, 1, 1);
        }

        return null;
    }

    private BlockPos getRandomPosWithinAABB(AxisAlignedBB axisAlignedBB, Random random) {

        int x = (int) axisAlignedBB.minX + (int) (axisAlignedBB.getXSize() * random.nextDouble());
        int y = (int) axisAlignedBB.minY + (int) (axisAlignedBB.getYSize() * random.nextDouble());
        int z = (int) axisAlignedBB.minZ + (int) (axisAlignedBB.getZSize() * random.nextDouble());

        return new BlockPos(x, y, z);
    }

    private Optional<EntityType<?>> getRandomEntity(Random random) {

        int weight = (int) (this.spawnableMonsters.values().stream().mapToInt(Integer::intValue).sum() * random.nextDouble());
        for (Map.Entry<EntityType<?>, Integer> entry : this.spawnableMonsters.entrySet()) {

            weight -= entry.getValue();
            if (weight < 0) {

                return Optional.of(entry.getKey());
            }
        }

        return Optional.empty();
    }

    private boolean isSpawningPermitted(ServerWorld world, EntityType<?> entityType, BlockPos pos) {

        EntitySpawnPlacementRegistry.PlacementType placementType = EntitySpawnPlacementRegistry.getPlacementType(entityType);
        boolean canSpawnAtLocation = WorldEntitySpawner.canCreatureTypeSpawnAtLocation(placementType, world, pos, entityType);
        return canSpawnAtLocation && EntitySpawnPlacementRegistry.canSpawnEntity(entityType, world, SpawnReason.EVENT, pos, world.getRandom());
    }

    private boolean setPathToPlayer(ServerWorld world, ServerPlayerEntity player, MobEntity monster) {

        // required for navigator to be able to find a path
        monster.setOnGround(true);
        Path path = monster.getNavigator().getPathToEntity(player, 0);
        if (path != null && path.getCurrentPathLength() > 1) {

            PathPoint pathpoint = path.getFinalPathPoint();
            if (pathpoint != null && world.isPlayerWithin(pathpoint.x, pathpoint.y, pathpoint.z, this.spawnMonsterRange)) {

                monster.setAttackTarget(player);
                return true;
            }
        }

        return false;
    }

    private void setupSpawnedMonster(MobEntity mobentity, ServerWorld world) {

        mobentity.rotationYawHead = mobentity.rotationYaw;
        mobentity.renderYawOffset = mobentity.rotationYaw;
        mobentity.onInitialSpawn(world, world.getDifficultyForLocation(mobentity.getPosition()), SpawnReason.EVENT, null, null);
        // add entity and passengers to world
        world.func_242417_l(mobentity);
        mobentity.playAmbientSound();
        // spawn smoke particles
        mobentity.spawnExplosionParticle();
    }

}
