package com.fuzs.letmesleep.mixin.accessor;

import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface ILivingEntityAccessor {

    @Invoker
    void callOnFinishedPotionEffect(EffectInstance effect);

}
