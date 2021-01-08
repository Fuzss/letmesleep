package com.fuzs.puzzleslib.element;

import com.fuzs.puzzleslib.config.ConfigManager;
import com.google.common.collect.Maps;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * default registry for elements
 */
public abstract class ElementRegistry {

    /**
     * storage for elements of all mods for performing actions on all of them
     */
    private static final Map<ResourceLocation, AbstractElement> ELEMENTS = Maps.newHashMap();

    /**
     * register an element to the namespace of the active mod container
     * @param key identifier for this element
     * @param element element to be registered
     * @return <code>element</code>
     */
    public static AbstractElement register(String key, AbstractElement element) {

        return register(getActiveNamespace(), key, element);
    }

    /**
     * register an element, overload this to set mod namespace
     * every element must be sided, meaning must somehow implement {@link ISidedElement}
     * @param namespace namespace of registering mod
     * @param key identifier for this element
     * @param element element to be registered
     * @return <code>element</code>
     */
    protected static AbstractElement register(String namespace, String key, AbstractElement element) {

        if (element instanceof ISidedElement) {

            ELEMENTS.put(new ResourceLocation(namespace, key), element);
            return element;
        }

        throw new RuntimeException("Unable to register element: " + "Invalid element, no instance of ISidedElement");
    }

    /**
     * get an element from another mod which uses this registry
     * @param namespace namespace of owning mod
     * @param key key for element to get
     * @return optional element
     */
    public static Optional<AbstractElement> get(String namespace, String key) {

        return Optional.ofNullable(ELEMENTS.get(new ResourceLocation(namespace, key)));
    }

    /**
     * cast an element to its class type to make unique methods accessible
     * @param element element to get
     * @param clazz type to cast to
     * @param <T> return type
     * @return <code>element</code> casted as <code>T</code>
     */
    @SuppressWarnings("unchecked")
    public static <T extends AbstractElement> T getAs(AbstractElement element, Class<T> clazz) {

        return (T) element;
    }

    /**
     * generate general config section for controlling elements, setup individual config sections and collect events to be registered in {@link #load}
     */
    public static void setup() {

        Set<AbstractElement> elements = getOwnElements();
        ConfigManager.builder().create("general", builder -> elements.forEach(element -> element.setupGeneralConfig(builder)), getSide(elements));
        elements.forEach(AbstractElement::setup);
    }

    /**
     * @return elements for active mod container as set
     */
    private static Set<AbstractElement> getOwnElements() {

        return ELEMENTS.entrySet().stream().filter(entry -> entry.getKey().getNamespace().equals(getActiveNamespace())).map(Map.Entry::getValue).collect(Collectors.toSet());
    }

    /**
     * execute on common load and register events
     * loads all elements, no matter which mod they're from
     */
    public static void load() {

        ELEMENTS.values().forEach(AbstractElement::load);
        ELEMENTS.values().stream()
                .filter(element -> element instanceof ISidedElement.Common)
                .map(element -> (ISidedElement.Common) element)
                .forEach(ISidedElement.Common::loadCommon);
    }

    /**
     * execute on client load
     * loads all elements, no matter which mod they're from
     */
    public static void loadClient() {

        ELEMENTS.values().stream()
                .filter(element -> element instanceof ISidedElement.Client)
                .map(element -> (ISidedElement.Client) element)
                .forEach(ISidedElement.Client::loadClient);
    }

    /**
     * execute on server load
     * loads all elements, no matter which mod they're from
     */
    public static void loadServer() {

        ELEMENTS.values().stream()
                .filter(element -> element instanceof ISidedElement.Server)
                .map(element -> (ISidedElement.Server) element)
                .forEach(ISidedElement.Server::loadServer);
    }

    /**
     * finds the main side this mod is running on, usually {@link net.minecraftforge.fml.config.ModConfig.Type#COMMON}
     * @return main side
     */
    private static ModConfig.Type getSide(Set<AbstractElement> elements) {

        if (elements.stream().allMatch(element -> element instanceof ISidedElement.Client)) {

            return ModConfig.Type.CLIENT;
        } else if (elements.stream().allMatch(element -> element instanceof ISidedElement.Server)) {

            return ModConfig.Type.SERVER;
        }

        return ModConfig.Type.COMMON;
    }

    /**
     * get active modid so entries can still be associated with the mod
     * @return active modid
     */
    private static String getActiveNamespace() {

        return ModLoadingContext.get().getActiveNamespace();
    }

}
