package com.fuzs.letmesleep.mixin.accessor;

import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerWorld.class)
public interface IServerWorldAccessor {

    @Accessor
    boolean getAllPlayersSleeping();

}
