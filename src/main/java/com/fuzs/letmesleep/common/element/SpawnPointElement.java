package com.fuzs.letmesleep.common.element;

import com.fuzs.puzzleslib.element.AbstractElement;
import com.fuzs.puzzleslib.element.ISidedElement;
import net.minecraftforge.common.ForgeConfigSpec;

public class SpawnPointElement extends AbstractElement implements ISidedElement.Common, ISidedElement.Client {

    @Override
    public boolean getDefaultState() {

        return true;
    }

    @Override
    public String getDisplayName() {

        return "Spawn Point Setting";
    }

    @Override
    public String getDescription() {

        return null;
    }

    @Override
    public void setupCommon() {

    }

    @Override
    public void setupCommonConfig(ForgeConfigSpec.Builder builder) {

    }

    @Override
    public void setupClient() {

    }

    @Override
    public void setupClientConfig(ForgeConfigSpec.Builder builder) {

    }

    @SuppressWarnings("unused")
    private enum SetSpawnPoint {

        NEVER, VANILLA, INTERACT, BUTTON, CHAT

    }

}
