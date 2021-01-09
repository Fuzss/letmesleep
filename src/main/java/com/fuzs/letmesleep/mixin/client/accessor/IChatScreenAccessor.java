package com.fuzs.letmesleep.mixin.client.accessor;

import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.SleepInMultiplayerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChatScreen.class)
public interface IChatScreenAccessor {

    @Accessor
    TextFieldWidget getInputField();

}
