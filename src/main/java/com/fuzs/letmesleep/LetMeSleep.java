package com.fuzs.letmesleep;

import com.fuzs.letmesleep.network.NetworkHandler;
import com.fuzs.letmesleep.proxy.CommonProxy;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(
        modid = LetMeSleep.MODID,
        name = LetMeSleep.NAME,
        version = LetMeSleep.VERSION,
        acceptedMinecraftVersions = LetMeSleep.RANGE,
        dependencies = LetMeSleep.DEPENDENCIES
)
@Mod.EventBusSubscriber(modid = LetMeSleep.MODID)
@SuppressWarnings({"WeakerAccess", "unused"})
public class LetMeSleep {

    public static final String MODID = "letmesleep";
    public static final String NAME = "Let Me Sleep";
    public static final String VERSION = "1.2.2";
    public static final String RANGE = "[1.12.2]";
    public static final String DEPENDENCIES = "required-after:forge@[14.23.5.2779,)";
    public static final String CLIENT_PROXY_CLASS = "com.fuzs.letmesleep.proxy.ClientProxy";
    public static final String SERVER_PROXY_CLASS = "com.fuzs.letmesleep.proxy.ServerProxy";

    public static final Logger LOGGER = LogManager.getLogger(LetMeSleep.NAME);

    @SidedProxy(clientSide = LetMeSleep.CLIENT_PROXY_CLASS, serverSide = LetMeSleep.SERVER_PROXY_CLASS)
    public static CommonProxy proxy;

    @EventHandler
    public void onPreInit(FMLPreInitializationEvent evt) {

        NetworkHandler.init();
        proxy.onCommonSetup();

    }

    @EventHandler
    public void onPostInit(FMLPostInitializationEvent evt) {

        proxy.onClientSetup();

    }

    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent evt) {

        if (evt.getModID().equals(LetMeSleep.MODID)) {
            ConfigManager.sync(LetMeSleep.MODID, Config.Type.INSTANCE);
        }

    }

}
