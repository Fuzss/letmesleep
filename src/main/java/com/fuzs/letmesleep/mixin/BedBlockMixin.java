package com.fuzs.letmesleep.mixin;

import com.fuzs.letmesleep.common.LetMeSleepElements;
import net.minecraft.block.BedBlock;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@SuppressWarnings("unused")
@Mixin(BedBlock.class)
public abstract class BedBlockMixin extends HorizontalBlock {

    protected BedBlockMixin(Properties builder) {

        super(builder);
    }

    @Inject(method = "doesBedWork", at = @At("HEAD"), cancellable = true)
    private static void doesBedWork(World world, CallbackInfoReturnable<Boolean> callbackInfo) {

        Optional<Object> optional = LetMeSleepElements.getConfigValue(LetMeSleepElements.SPAWN_POINT_SETTINGS, "Allow Bed Everywhere");
        if (optional.isPresent() && (Boolean) optional.get()) {

            callbackInfo.setReturnValue(true);
        }
    }

}
