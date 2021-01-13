package com.fuzs.letmesleep.mixin;

import com.fuzs.letmesleep.common.LetMeSleepElements;
import net.minecraft.block.Block;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@SuppressWarnings("unused")
@Mixin(RespawnAnchorBlock.class)
public abstract class RespawnAnchorBlockMixin extends Block {

    public RespawnAnchorBlockMixin(Properties properties) {

        super(properties);
    }

    @Inject(method = "doesRespawnAnchorWork", at = @At("HEAD"), cancellable = true)
    private static void doesRespawnAnchorWork(World world, CallbackInfoReturnable<Boolean> callbackInfo) {

        Optional<Object> optional = LetMeSleepElements.getConfigValue(LetMeSleepElements.SPAWN_POINT_SETTINGS, "Allow Respawn Anchor Everywhere");
        if (optional.isPresent() && (Boolean) optional.get()) {

            callbackInfo.setReturnValue(true);
        }
    }

}
