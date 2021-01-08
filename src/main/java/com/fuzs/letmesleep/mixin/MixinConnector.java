package com.fuzs.letmesleep.mixin;

import com.fuzs.letmesleep.LetMeSleep;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.connect.IMixinConnector;

@SuppressWarnings("unused")
public class MixinConnector implements IMixinConnector {

    @Override
    public void connect() {

        Mixins.addConfiguration("META-INF/" + LetMeSleep.MODID + ".mixins.json");
    }

}
