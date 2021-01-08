package com.fuzs.letmesleep.handler;

import com.fuzs.letmesleep.helper.ReflectionHelper;
import com.fuzs.letmesleep.helper.SetSpawnHelper;
import com.fuzs.letmesleep.network.OldNetworkHandler;
import com.fuzs.letmesleep.network.message.RequestSpawnMessage;
import com.fuzs.letmesleep.util.SetSpawnPoint;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.NewChatGui;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.SleepInMultiplayerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.entity.player.PlayerSetSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.Optional;

public class SetSpawnHandler {

    private final Minecraft mc = Minecraft.getInstance();

    @SuppressWarnings("unused")
    @SubscribeEvent
    public void onSetSpawn(PlayerSetSpawnEvent evt) {

        boolean flag = ConfigBuildHandler.GENERAL_CONFIG.setSpawn.get() == SetSpawnPoint.BUTTON || ConfigBuildHandler.GENERAL_CONFIG.setSpawn.get() == SetSpawnPoint.CHAT;
        if (flag && evt.getPlayer().world.isRemote && evt.getNewSpawn() != null && evt.isForced()) {

            RequestSpawnMessage message = new RequestSpawnMessage(new RequestSpawnMessage.RequestSpawnMessageData(evt.getNewSpawn()));
            OldNetworkHandler.sendToServer(message);

        }

    }

    @SuppressWarnings("unused")
    @SubscribeEvent
    public void onInitGuiPost(GuiScreenEvent.InitGuiEvent.Post evt) {

        if (evt.getGui() instanceof SleepInMultiplayerScreen) {

            ClientPlayerEntity player = this.mc.player;
            BlockPos pos = player.getBedPosition().orElse(null);

            if (pos != null && SetSpawnHelper.isNewSpawnAllowed(player.world, player, pos, SetSpawnPoint.BUTTON)) {

                SleepInMultiplayerScreen screen = (SleepInMultiplayerScreen) evt.getGui();

                Button setSpawn = new Button(screen.width / 2 - 100, screen.height - 64, 200, 20,
                        new TranslationTextComponent("multiplayer.spawn.button").getFormattedText(), button -> {
                    button.visible = false;
                    RequestSpawnMessage message = new RequestSpawnMessage(new RequestSpawnMessage.RequestSpawnMessageData(pos));
                    OldNetworkHandler.sendToServer(message);
                });

                evt.addWidget(setSpawn);

            }

        }

    }

    @SuppressWarnings("unused")
    @SubscribeEvent
    public void onMouseClickedPre(GuiScreenEvent.MouseClickedEvent.Pre evt) {

        if (evt.getGui() instanceof ChatScreen) {

            if (evt.getButton() == 0) {

                ITextComponent itextcomponent = this.mc.ingameGUI.getChatGUI().getTextComponent(evt.getMouseX(), evt.getMouseY());

                if (itextcomponent != null) {

                    evt.setCanceled(this.handleComponentClicked(itextcomponent));

                }

            }

        }

    }

    private boolean handleComponentClicked(ITextComponent itextcomponent) {

        if (itextcomponent != null) {

            ClickEvent clickevent = itextcomponent.getStyle().getClickEvent();

            if (!Screen.hasShiftDown() && clickevent != null) {

                if (clickevent.getAction() == ClickEvent.Action.CHANGE_PAGE) {

                    ClientPlayerEntity player = this.mc.player;
                    player.getBedPosition().ifPresent(pos -> {
                        RequestSpawnMessage message = new RequestSpawnMessage(new RequestSpawnMessage.RequestSpawnMessageData(pos));
                        OldNetworkHandler.sendToServer(message);
                        this.removeSpawnMessage();
                    });

                    return true;

                }

            }

        }

        return false;

    }

    @SuppressWarnings("unused")
    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent evt) {

        if (this.mc.currentScreen instanceof SleepInMultiplayerScreen) {

            this.removeSpawnMessage();

        }

    }

    private void removeSpawnMessage() {

        NewChatGui chat = this.mc.ingameGUI.getChatGUI();
        List<ChatLine> chatLines = ReflectionHelper.getChatLines(chat);
        List<ChatLine> drawnChatLines = ReflectionHelper.getDrawnChatLines(chat);

        if (chatLines != null && drawnChatLines != null) {

            Optional.ofNullable(TextFormatting.getTextWithoutFormattingCodes(SetSpawnHelper.createRespawnMessage().getString())).ifPresent(template -> {

                chatLines.removeIf(chatline -> template.equals(TextFormatting.getTextWithoutFormattingCodes(chatline.getChatComponent().getString())));
                drawnChatLines.removeIf(chatline -> template.equals(TextFormatting.getTextWithoutFormattingCodes(chatline.getChatComponent().getString())));

            });

        }

    }

}
