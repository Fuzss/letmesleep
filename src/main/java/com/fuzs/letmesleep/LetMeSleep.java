package com.fuzs.letmesleep;

import com.fuzs.letmesleep.common.element.LetMeSleepElements;
import com.fuzs.puzzleslib.PuzzlesLib;
import com.fuzs.puzzleslib.config.ConfigManager;
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
        LetMeSleepElements.setup();
        ConfigManager.get().load();
    }

    protected void onCommonSetup(final FMLCommonSetupEvent evt) {

        super.onCommonSetup(evt);
    }

    protected void onClientSetup(final FMLClientSetupEvent evt) {

        super.onClientSetup(evt);
    }

}
