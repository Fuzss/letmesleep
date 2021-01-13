package com.fuzs.letmesleep.mixin.accessor;

import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerEntity.class)
public interface IPlayerEntityAccessor {

    @Accessor
    void setSleepTimer(int sleepTimer);

    @Accessor
    int getSleepTimer();

}
