package com.fuzs.letmesleep.common.element;

import com.fuzs.letmesleep.mixin.accessor.IServerWorldAccessor;
import com.fuzs.puzzleslib.config.deserialize.EntryCollectionBuilder;
import com.fuzs.puzzleslib.element.AbstractElement;
import com.fuzs.puzzleslib.element.ISidedElement;
import com.google.common.collect.Lists;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
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
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.registries.ForgeRegistries;

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

        addToConfig(builder.comment("Chance to spawn a monster. Higher values make it more likely to happen.").defineInRange("Spawn Monster Chance", 12, 0, 256), v -> this.spawnMonsterChance = v);
        addToConfig(builder.comment("Range to spawn a monster in from the bed.").defineInRange("Spawn Monster Range", 2, 0, 16), v -> this.spawnMonsterRange = v);
        addToConfig(builder.comment("Possible monsters to spawn. One entry is chosen at random.", EntryCollectionBuilder.CONFIG_STRING_BUILDER.apply(",<weight>")).define("Spawnable Monsters", Lists.newArrayList(getEntityAndWeight(EntityType.ZOMBIE, 2), getEntityAndWeight(EntityType.SKELETON, 1), getEntityAndWeight(EntityType.SPIDER, 1))),v -> {

            BiPredicate<EntityType<?>, double[]> predicate = (entry, value) -> entry.getClassification() == EntityClassification.MONSTER && value.length == 1;
            Map<EntityType<?>, double[]> entries = new EntryCollectionBuilder<>(ForgeRegistries.ENTITIES).buildEntryMap(v, predicate , "Not a monster or no weight");
            this.spawnableMonsters = entries.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, value -> (int) value.getValue()[0]));
        });
    }

    @SuppressWarnings("ConstantConditions")
    private static String getEntityAndWeight(EntityType<?> entityType, int weight) {

        return entityType.getRegistryName().toString() + "," + weight;
    }

    private void onWorldTick(final TickEvent.WorldTickEvent evt) {

        if (evt.phase != TickEvent.Phase.START || evt.world.isRemote) {

            return;
        }

        ServerWorld world = (ServerWorld) evt.world;
        if (((IServerWorldAccessor) world).getAllPlayersSleeping() && world.getPlayers().stream().noneMatch(player -> !player.isSpectator() && !player.isPlayerFullyAsleep())) {

            this.performSleepSpawning(world, world.getPlayers());
        }
    }

    private void performSleepSpawning(ServerWorld world, List<ServerPlayerEntity> allPlayers) {

        for (ServerPlayerEntity player : allPlayers) {

            if (!player.getBedPosition().isPresent() || !EntityPredicates.CAN_AI_TARGET.test(player)) {

                continue;
            }

            int run = 0;
            int attempts = 0;
            for (int j = 0; j < 100; j++) {


                AxisAlignedBB spawningBoundingBox = this.getSpawningBoundingBox(world, player);
                int maxAttempts = world.getDifficulty().getId() * this.spawnMonsterChance;
                for (int i = 0; i < maxAttempts; i++) {

                    BlockPos spawnPos = this.getRandomPosWithinAABB(spawningBoundingBox, world.getRandom());
                    Optional<EntityType<?>> entityType = this.getRandomEntity(world.getRandom());
                    if (entityType.isPresent() && this.isSpawningPermitted(world, entityType.get(), spawnPos)) {

                        Entity entity = entityType.get().create(world, null, null, null, spawnPos, SpawnReason.EVENT, false, false);
                        if (entity instanceof MobEntity && this.setPathToPlayer(world, player, (MobEntity) entity)) {

//                        // addEntityPassengers
//                        world.func_242417_l(entity);
//                        player.wakeUp();
//                        player.lookAt(EntityAnchorArgument.Type.EYES, entity, EntityAnchorArgument.Type.EYES);
//                        // spawn smoke particles
//                        ((MobEntity) entity).spawnExplosionParticle();

                            run++;
                            attempts += i;
                            entity.remove();
                            break;
                        } else if (entity != null) {

                            entity.remove();
                        }
                    }
                }
            }

            System.out.println("Total: " + run + ", Average: " + (attempts / (double) run));
        }
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private AxisAlignedBB getSpawningBoundingBox(ServerWorld world, PlayerEntity player) {

        BlockPos bedPos = player.getBedPosition().get();
        BlockState state = world.getBlockState(bedPos);
        Direction bedPosDirection = state.get(HorizontalBlock.HORIZONTAL_FACING);
        // check for part, who knows what might happen
        Direction otherPartDirection = state.get(BedBlock.PART) == BedPart.FOOT ? bedPosDirection : bedPosDirection.getOpposite();

        // expand by one in all positive directions as this side will be excluded when picking a random spot
        return new AxisAlignedBB(bedPos, bedPos.offset(otherPartDirection)).grow(this.spawnMonsterRange).expand(1, 1, 1);
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
            if (weight <= 0) {

                return Optional.of(entry.getKey());
            }
        }

        return Optional.empty();
    }

    private boolean isSpawningPermitted(ServerWorld world, EntityType<?> entityType, BlockPos spawnPos) {

        boolean collisionCheck = world.hasNoCollisions(entityType.getBoundingBoxWithSizeApplied(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ()));
        return collisionCheck && EntitySpawnPlacementRegistry.canSpawnEntity(entityType, world, SpawnReason.EVENT, spawnPos, world.getRandom());
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

}
