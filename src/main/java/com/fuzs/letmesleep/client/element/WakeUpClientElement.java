package com.fuzs.letmesleep.client.element;

import com.fuzs.letmesleep.mixin.client.accessor.IChatScreenAccessor;
import com.fuzs.puzzleslib.element.AbstractElement;
import com.fuzs.puzzleslib.element.ISidedElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.SleepInMultiplayerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.ForgeConfigSpec;

public class WakeUpClientElement extends ISidedElement.Abstract implements ISidedElement.Client {

    // config options
    private boolean persistentChat;

    public WakeUpClientElement(AbstractElement parent) {

        super(parent);
    }

    @Override
    public void setupClient() {

        this.getParent().addListener(this::onGuiOpen);
    }

    @Override
    public void setupClientConfig(ForgeConfigSpec.Builder builder) {

        AbstractElement.addToConfig(builder.comment("Keep chat open after waking up if it contains any text.").define("Persistent Chat", true), v -> this.persistentChat = v);
    }

    private void onGuiOpen(final GuiOpenEvent evt) {

        if (!this.persistentChat) {

            return;
        }

        Screen currentScreen = Minecraft.getInstance().currentScreen;
        if (currentScreen instanceof SleepInMultiplayerScreen && evt.getGui() == null) {

            TextFieldWidget textWidget = ((IChatScreenAccessor) currentScreen).getInputField();
            if (textWidget != null) {

                String typedText = textWidget.getText().trim();
                if (!typedText.isEmpty()) {

                    evt.setGui(new ChatScreen(typedText));
                }
            }
        }
    }

}
