package com.fuzs.puzzleslib.element;

import com.fuzs.puzzleslib.config.ConfigManager;
import com.fuzs.puzzleslib.config.deserialize.EntryCollectionBuilder;
import com.google.common.collect.Lists;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * all features a mod adds are structured into elements which are then registered, this is an abstract version
 */
public abstract class AbstractElement implements IConfigurableElement {

    /**
     * all events registered by this element
     */
    private final List<EventStorage<? extends Event>> events = Lists.newArrayList();
    /**
     * is this element enabled (are events registered)
     */
    private boolean enabled;

    @Override
    public final void setupGeneralConfig(ForgeConfigSpec.Builder builder) {

        addToConfig(builder.comment(this.getDescription()).define(this.getDisplayName(), this.getDefaultState()), this::setEnabled);
    }

    /**
     * build element config and get event listeners
     */
    public final void setup() {

        if (this instanceof ISidedElement.Common) {

            ConfigManager.builder().create(this.getDisplayName(), ((ISidedElement.Common) this)::setupCommonConfig, ModConfig.Type.COMMON);
            ((ISidedElement.Common) this).setupCommon();
        }

        if (this instanceof ISidedElement.Client) {

            ConfigManager.builder().create(this.getDisplayName(), ((ISidedElement.Client) this)::setupClientConfig, ModConfig.Type.CLIENT);
            ((ISidedElement.Client) this).setupClient();
        }

        if (this instanceof ISidedElement.Server) {

            ConfigManager.builder().create(this.getDisplayName(), ((ISidedElement.Server) this)::setupServerConfig, ModConfig.Type.SERVER);
            ((ISidedElement.Server) this).setupServer();
        }
    }

    /**
     * register Forge events from internal storage
     */
    public final void load() {

        this.reload(true);
    }

    /**
     * update status of all stored events
     * @param isInit is this method called during initial setup
     */
    private void reload(boolean isInit) {

        if (this.isEnabled() || this.isAlwaysEnabled()) {

            this.events.forEach(EventStorage::register);
        } else if (!isInit) {

            // nothing to unregister during initial setup
            this.events.forEach(EventStorage::unregister);
        }
    }

    @Override
    public final boolean isEnabled() {

        return this.enabled;
    }

    /**
     * are the events from this mod always active
     * @return is always enabled
     */
    protected boolean isAlwaysEnabled() {

        return false;
    }

    /**
     * set {@link #enabled} state, reload when changed
     * @param enabled enabled
     */
    private void setEnabled(boolean enabled) {

        if (enabled != this.enabled) {

            this.enabled = enabled;
            this.reload(false);
        }
    }

    /**
     * @param entry config entry to add
     * @param action consumer for updating value when changed
     * @param <S> type of config value
     * @param <T> field type
     */
    protected static <S extends ForgeConfigSpec.ConfigValue<T>, T> void addToConfig(S entry, Consumer<T> action) {

        ConfigManager.get().registerEntry(entry, action);
    }

    /**
     * deserialize string <code>data</code> into entries of a <code>registry</code>
     * @param data data as string list as provided by Forge config
     * @param registry registry to get entries from
     * @param <T> type of registry
     * @return deserialized data as set
     */
    protected <T extends IForgeRegistryEntry<T>> Set<T> deserializeToSet(List<String> data, IForgeRegistry<T> registry) {

        return new EntryCollectionBuilder<>(registry).buildEntrySet(data);
    }

    /**
     * deserialize string <code>data</code> into entries of a <code>registry</code>
     * @param data data as string list as provided by Forge config
     * @param registry registry to get entries from
     * @param <T> type of registry
     * @return deserialized data as map
     */
    protected <T extends IForgeRegistryEntry<T>> Map<T, Double> deserializeToMap(List<String> data, IForgeRegistry<T> registry) {

        return new EntryCollectionBuilder<>(registry).buildEntryMap(data);
    }

    /**
     * Add a consumer listener with {@link EventPriority} set to {@link EventPriority#NORMAL}
     * @param consumer Callback to invoke when a matching event is received
     * @param <T> The {@link Event} subclass to listen for
     */
    protected final <T extends Event> void addListener(Consumer<T> consumer) {

        this.addListener(consumer, EventPriority.NORMAL);
    }

    /**
     * Add a consumer listener with {@link EventPriority} set to {@link EventPriority#NORMAL}
     * @param consumer Callback to invoke when a matching event is received
     * @param receiveCancelled Indicate if this listener should receive events that have been {@link Cancelable} cancelled
     * @param <T> The {@link Event} subclass to listen for
     */
    protected final <T extends Event> void addListener(Consumer<T> consumer, boolean receiveCancelled) {

        this.addListener(consumer, EventPriority.NORMAL, receiveCancelled);
    }

    /**
     * Add a consumer listener with the specified {@link EventPriority}
     * @param consumer Callback to invoke when a matching event is received
     * @param priority {@link EventPriority} for this listener
     * @param <T> The {@link Event} subclass to listen for
     */
    protected final <T extends Event> void addListener(Consumer<T> consumer, EventPriority priority) {

        this.addListener(consumer, priority, false);
    }

    /**
     * Add a consumer listener with the specified {@link EventPriority}
     * @param consumer Callback to invoke when a matching event is received
     * @param priority {@link EventPriority} for this listener
     * @param receiveCancelled Indicate if this listener should receive events that have been {@link Cancelable} cancelled
     * @param <T> The {@link Event} subclass to listen for
     */
    protected final <T extends Event> void addListener(Consumer<T> consumer, EventPriority priority, boolean receiveCancelled) {

        this.events.add(new EventStorage<>(consumer, priority, receiveCancelled));
    }

    /**
     * storage for {@link net.minecraftforge.eventbus.api.Event} so we can register and unregister them as needed
     * @param <T> type of event
     */
    private static class EventStorage<T extends Event> {

        /**
         * Callback to invoke when a matching event is received
         */
        private final Consumer<T> event;
        /**
         * {@link EventPriority} for this listener
         */
        private final EventPriority priority;
        /**
         * Indicate if this listener should receive events that have been {@link Cancelable} cancelled
         */
        private final boolean receiveCancelled;
        /**
         * has been registered or unregistered
         */
        private boolean active;

        /**
         * create new storage object with the same arguments as when calling {@link net.minecraftforge.eventbus.api.IEventBus#addListener}
         * @param priority {@link EventPriority} for this listener
         * @param receiveCancelled Indicate if this listener should receive events that have been {@link Cancelable} cancelled
         * @param consumer Callback to invoke when a matching event is received
         */
        EventStorage(Consumer<T> consumer, EventPriority priority, boolean receiveCancelled) {

            this.event = consumer;
            this.priority = priority;
            this.receiveCancelled = receiveCancelled;
        }

        /**
         * check if storage object can be registered and do so if possible
         */
        void register() {

            if (this.isActive(true)) {

                MinecraftForge.EVENT_BUS.addListener(this.priority, this.receiveCancelled, this.event);
            }
        }

        /**
         * check if storage object can be unregistered and do so if possible
         */
        void unregister() {

            if (this.isActive(false)) {

                MinecraftForge.EVENT_BUS.unregister(this.event);
            }
        }

        /**
         * verify with {@link #active} if registering action can be performed
         * @param newState new active state after registering or unregistering
         * @return is an action permitted
         */
        private boolean isActive(boolean newState) {

            if (this.active != newState) {

                this.active = newState;
                return true;
            }

            return false;
        }
    }

}
