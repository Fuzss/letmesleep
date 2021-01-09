package com.fuzs.letmesleep.common.element;

import com.fuzs.letmesleep.client.element.WakeUpClientElement;
import com.fuzs.letmesleep.mixin.accessor.ILivingEntityAccessor;
import com.fuzs.puzzleslib.config.ConfigManager;
import com.fuzs.puzzleslib.config.deserialize.EntryCollectionBuilder;
import com.fuzs.puzzleslib.element.AbstractElement;
import com.fuzs.puzzleslib.element.ISidedElement;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.potion.Effects;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class WakeUpElement extends AbstractElement implements ISidedElement.Common, ISidedElement.Client {

    // config options
    private boolean healPlayer;
    private int healAmount;
    private boolean starvePlayer;
    private int starveAmount;
    private ClearEffects clearEffects;
    private Set<Effect> effectsNotToClear;
    private boolean applyEffects;
    private Set<EffectInstance> effectsToApply;

    @Override
    public boolean getDefaultState() {

        return true;
    }

    @Override
    public String getDisplayName() {
        
        return "Wake Up Actions";
    }

    @Override
    public String getDescription() {

        return "A bunch of options that may happen when the player wakes up from sleeping in a bed.";
    }

    @Nullable
    @Override
    protected Function<AbstractElement, Abstract> createClientPerformer() {

        return WakeUpClientElement::new;
    }

    @Override
    public void setupCommon() {

        this.addListener(this::onPlayerWakeUp);
    }

    @Override
    public void setupClient() {

        this.performForClient(Client::setupClient);
    }

    @Override
    public void setupCommonConfig(ForgeConfigSpec.Builder builder) {

        addToConfig(builder.comment("Should the player be healed when waking up.").define("Heal Player", true), v -> this.healPlayer = v);
        addToConfig(builder.comment("Amount of health the player should regain. Set to 0 to fully heal.").defineInRange("Heal Amount", 0, 0, Integer.MAX_VALUE), v -> this.healAmount = v);
        addToConfig(builder.comment("Should the player loose some food after waking up.").define("Loose Food", false), v -> this.starvePlayer = v);
        addToConfig(builder.comment("Amount of food to loose when waking up. Set to 0 to completely starve the player.").defineInRange("Food Amount", 3, 0, Integer.MAX_VALUE), v -> this.starveAmount = v);
        addToConfig(builder.comment("Clear potion effects after the player wakes up.").defineEnum("Clear Effects", ClearEffects.ALL), v -> this.clearEffects = v);
        addToConfig(builder.comment("Blacklist to prevent potion effects from being removed after waking up.", EntryCollectionBuilder.CONFIG_STRING).define("Effects Not To Clear", ConfigManager.get().getKeyList(Effects.BAD_OMEN, Effects.CONDUIT_POWER)),v -> this.effectsNotToClear = deserializeToSet(v, ForgeRegistries.POTIONS));
        addToConfig(builder.comment("Should custom potion effects be applied to the player after waking up.").define("Apply Effects", true), v -> this.applyEffects = v);
        addToConfig(builder.comment("Potion effects to be given to the player after waking up. Values are based on the \"/effect\" command.", EntryCollectionBuilder.CONFIG_STRING_BUILDER.apply(",[<seconds>],[<amplifier>],[<hideParticles>]")).define("Effects To Apply", ConfigManager.get().getKeyList(Effects.SPEED)),v -> {

            // use a fallback in case not enough values have been supplied
            final Supplier<double[]> fallback = () -> new double[]{30.0, 0.0, 0.0};
            Map<Effect, double[]> effectMap = new EntryCollectionBuilder<>(ForgeRegistries.POTIONS).buildEntryMap(v, (entry, value) -> value.length < 4, "Wrong number of arguments");
            this.effectsToApply = effectMap.entrySet().stream().map(entry -> {

                double[] array = fallback.get();
                // copy to fallback, throws NullPointerException when source is empty
                if (entry.getValue().length != 0) {

                    System.arraycopy(entry.getValue(), 0, array, 0, array.length);
                }

                // multiply duration by 20 as it's handled in ticks
                return new EffectInstance(entry.getKey(), (int) array[0] * 20, (int) array[1], false, array[2] != 1.0);
            }).collect(Collectors.toSet());
        });
    }

    @Override
    public void setupClientConfig(ForgeConfigSpec.Builder builder) {

        this.performForClient(performer -> performer.setupClientConfig(builder));
    }

    private void onPlayerWakeUp(final PlayerWakeUpEvent evt) {

        if (!evt.getPlayer().world.isRemote && !evt.wakeImmediately() && !evt.updateWorld()) {

            this.healPlayer(evt.getPlayer());
            this.starvePlayer(evt.getPlayer());
            this.clearActivePotions(evt.getPlayer());
            this.applyEffects(evt.getPlayer());
        }
    }

    private void healPlayer(PlayerEntity player) {

        if (this.healPlayer) {

            player.heal(this.healAmount == 0 ? player.getMaxHealth() : this.healAmount);
        }
    }

    private void starvePlayer(PlayerEntity player) {

        if (this.starvePlayer) {

            int foodLevel = player.getFoodStats().getFoodLevel();
            player.getFoodStats().setFoodLevel(Math.max(this.starveAmount == 0 ? foodLevel : foodLevel - this.starveAmount, 0));
        }
    }

    private void clearActivePotions(PlayerEntity player) {

        if (this.clearEffects == ClearEffects.NONE) {

            return;
        }

        Iterator<EffectInstance> iterator = player.getActivePotionEffects().iterator();
        while (iterator.hasNext()) {

            EffectInstance effect = iterator.next();
            boolean isCancelled = MinecraftForge.EVENT_BUS.post(new PotionEvent.PotionRemoveEvent(player, effect));
            if (isCancelled || !this.clearEffects.matches(effect.getPotion().getEffectType()) || this.effectsNotToClear.contains(effect.getPotion())) {

                continue;
            }

            ((ILivingEntityAccessor) player).callOnFinishedPotionEffect(effect);
            iterator.remove();
        }
    }

    private void applyEffects(PlayerEntity player) {

        if (this.applyEffects) {

            // makes a copy of every effect instance and adds them to the player
            this.effectsToApply.stream().map(EffectInstance::new).forEach(player::addPotionEffect);
        }
    }

    @SuppressWarnings("unused")
    private enum ClearEffects {

        NONE(null), POSITIVE(EffectType.BENEFICIAL), NEGATIVE(EffectType.HARMFUL), ALL(null);

        private final EffectType type;

        ClearEffects(EffectType type) {

            this.type = type;
        }

        boolean matches(EffectType type) {

            return this == ALL || this.type == type;
        }

    }

}
