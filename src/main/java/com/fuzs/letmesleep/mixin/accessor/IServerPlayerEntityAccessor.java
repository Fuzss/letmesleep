package com.fuzs.letmesleep.mixin.accessor;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ServerPlayerEntity.class)
public interface IServerPlayerEntityAccessor {

    @Invoker("func_241147_a_")
    boolean callIsBedInRange(BlockPos p_241147_1_, Direction p_241147_2_);

    @Invoker("func_241156_b_")
    boolean callIsBedObstructed(BlockPos p_241156_1_, Direction p_241156_2_);

}
