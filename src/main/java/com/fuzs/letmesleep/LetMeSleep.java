package com.fuzs.letmesleep;

import com.fuzs.letmesleep.common.element.LetMeSleepElements;
import com.fuzs.puzzleslib.PuzzlesLib;
import com.fuzs.puzzleslib.config.ConfigManager;
import com.fuzs.puzzleslib.element.ElementRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("unused")
@Mod(LetMeSleep.MODID)
public class LetMeSleep extends PuzzlesLib {

    public static final String MODID = "letmesleep";
    public static final String NAME = "Let Me Sleep";
    public static final Logger LOGGER = LogManager.getLogger(LetMeSleep.NAME);

    public LetMeSleep() {

        super();
        // start new
        LetMeSleepElements.setup();
        ConfigManager.get().load();
        // end new
//        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConfigBuildHandler.SPEC);
    }

    protected void onCommonSetup(final FMLCommonSetupEvent evt) {

        super.onCommonSetup(evt);
//        OldNetworkHandler.init();
//        MinecraftForge.EVENT_BUS.register(new CommonEventHandler());
//        MinecraftForge.EVENT_BUS.register(new SleepAttemptHandler());
//        MinecraftForge.EVENT_BUS.register(new WakeUpHandler());
//        MinecraftForge.EVENT_BUS.register(new BadDreamHandler());
    }

    protected void onClientSetup(final FMLClientSetupEvent evt) {

        super.onClientSetup(evt);
//        MinecraftForge.EVENT_BUS.register(new SetSpawnHandler());
    }

}
