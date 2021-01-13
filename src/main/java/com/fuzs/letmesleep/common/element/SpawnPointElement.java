package com.fuzs.letmesleep.common.element;

import com.fuzs.puzzleslib.element.AbstractElement;
import com.fuzs.puzzleslib.element.ISidedElement;
import net.minecraftforge.common.ForgeConfigSpec;

public class SpawnPointElement extends AbstractElement implements ISidedElement.Common, ISidedElement.Client {

    // config settings
//    private boolean bedEverywhere;
//    private boolean respawnAnchorEverywhere;
//    private SetSpawnPoint setSpawn;
//    private boolean setSpawnAlways;

    @Override
    public boolean getDefaultState() {

        return true;
    }

    @Override
    public String getDescription() {

        return "Various settings for how and where the player should be able to set their respawn point.";
    }

    @Override
    public void setupCommon() {

    }

    @Override
    public void setupCommonConfig(ForgeConfigSpec.Builder builder) {

        // TODO make this actually work as the bed itself also checks the dimension
        addToConfig(builder.comment("Make sleeping possible in every dimension.").define("Allow Bed Everywhere", false), v -> {});
        addToConfig(builder.comment("Enable respawn anchors to work outside of the Nether.").define("Allow Respawn Anchor Everywhere", false), v -> {});
//        addToConfig(builder.comment("How beds should be used for setting the respawn point.").defineEnum("Set Respawn Point", SetSpawnPoint.BUTTON), v -> this.setSpawn = v);
//        addToConfig(builder.comment("Disable to prevent setting a new respawn point when there is already one present at another bed. The other bed will have to be removed to set a new respawn point.").define("Always Set Spawn", true), v -> this.setSpawnAlways = v);
    }

    @Override
    public void setupClient() {

    }

    @Override
    public void setupClientConfig(ForgeConfigSpec.Builder builder) {

    }

    @SuppressWarnings("unused")
    private enum SetSpawnPoint {

        NEVER, VANILLA, AFTER_SLEEPING, BUTTON

    }

}
