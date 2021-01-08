package com.fuzs.letmesleep.handler;

import com.fuzs.letmesleep.util.ClearPotions;
import com.fuzs.letmesleep.util.SetSpawnPoint;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Collections;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public class ConfigBuildHandler {

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final GeneralConfig GENERAL_CONFIG = new GeneralConfig("general");
    public static final SleepTimingsConfig SLEEP_TIMINGS_CONFIG = new SleepTimingsConfig("sleep_timings");
    public static final SleepConfig SLEEP_CONFIG = new SleepConfig("sleeping_checks");
    public static final WakeUpConfig WAKE_UP_CONFIG = new WakeUpConfig("wake_up_actions");

    public static class GeneralConfig {

        public final ForgeConfigSpec.EnumValue<SetSpawnPoint> setSpawn;
        public final ForgeConfigSpec.BooleanValue setSpawnAlways;
        public final ForgeConfigSpec.BooleanValue spawnMonster;
        public final ForgeConfigSpec.IntValue spawnMonsterChance;

        private GeneralConfig(String name) {

            BUILDER.push(name);

            this.setSpawn = ConfigBuildHandler.BUILDER.comment("How beds should be used for setting the respawn point.").defineEnum("Set Respawn Point", SetSpawnPoint.INTERACT);
            this.setSpawnAlways = ConfigBuildHandler.BUILDER.comment("Disable to prevent setting a new respawn point when there is already one present at another bed. The other bed will have to be removed to set a new respawn point.").define("Always Set Spawn", true);
            this.spawnMonster = ConfigBuildHandler.BUILDER.comment("Spawn a monster and wake player when sleeping in an insufficiently lit area.").define("Spawn Monster", true);
            this.spawnMonsterChance = ConfigBuildHandler.BUILDER.comment("Chance to spawn a monster, higher numbers make it more likely to happen.").defineInRange("Spawn Monster Chance", 10, 0, Integer.MAX_VALUE);

            BUILDER.pop();

        }

    }

    public static class SleepTimingsConfig {

        public final ForgeConfigSpec.IntValue bedtimeStart;
        public final ForgeConfigSpec.IntValue bedtimeEnd;
        public final ForgeConfigSpec.BooleanValue bedtimeThunder;
        public final ForgeConfigSpec.BooleanValue bedtimeRain;
        public final ForgeConfigSpec.BooleanValue timeTwelve;
        public final ForgeConfigSpec.BooleanValue timeClock;
        public final ForgeConfigSpec.BooleanValue instantSleeping;
        public final ForgeConfigSpec.IntValue wakeUpTime;

        private SleepTimingsConfig(String name) {

            BUILDER.push(name);

            this.bedtimeStart = ConfigBuildHandler.BUILDER.comment("Time from when onwards sleeping in a bed is possible.").defineInRange("Bedtime Start", 12541, 0, 24000);
            this.bedtimeEnd = ConfigBuildHandler.BUILDER.comment("Time until when sleeping is possible.").defineInRange("Bedtime End", 23458, 0, 24000);
            this.bedtimeThunder = ConfigBuildHandler.BUILDER.comment("Is going to bed during a thunderstorm permitted.").define("Sleep During Thunder", true);
            this.bedtimeRain = ConfigBuildHandler.BUILDER.comment("Is going to bed when it's raining permitted.").define("Sleep During Rain", false);
            this.timeTwelve = ConfigBuildHandler.BUILDER.comment("Use 12h format for status messages.").define("Time 12h Format", false);
            this.timeClock = ConfigBuildHandler.BUILDER.comment("Add current time to the clock item tooltip.").define("Clock Time Tooltip", true);
            this.instantSleeping = ConfigBuildHandler.BUILDER.comment("Removes the falling asleep animation, so you wake up instantly after going to bed. Some options from \"Set Respawn Point\" will no longer be accessible then.").define("Instant Sleeping", false);
            this.wakeUpTime = ConfigBuildHandler.BUILDER.comment("Time set after sleeping successfully.").defineInRange("Wake Up Time", 0, 0, 24000);

            BUILDER.pop();

        }

    }

    public static class SleepConfig {

        public final ForgeConfigSpec.BooleanValue rangeCheck;
        public final ForgeConfigSpec.BooleanValue obstructionCheck;
        public final ForgeConfigSpec.BooleanValue monsterCheck;
        public final ForgeConfigSpec.BooleanValue glow;
        public final ForgeConfigSpec.IntValue glowDuration;
        public final ForgeConfigSpec.BooleanValue namedMonsters;
        public final ForgeConfigSpec.BooleanValue persistentMonsters;

        private SleepConfig(String name) {

            BUILDER.push(name);

            this.rangeCheck = ConfigBuildHandler.BUILDER.comment("Check if the player is close enough to the bed.").define("Range Check", false);
            this.obstructionCheck = ConfigBuildHandler.BUILDER.comment("Check if the bed has enough open space above it.").define("Obstruction Check", true);
            this.monsterCheck = ConfigBuildHandler.BUILDER.comment("Check if monsters are nearby.").define("Monster Check", true);
            this.glow = ConfigBuildHandler.BUILDER.comment("Should monsters preventing the player from sleeping glow.").define("Glowing Monsters", true);
            this.glowDuration = ConfigBuildHandler.BUILDER.comment("Duration in ticks for which the monsters nearby will glow.").defineInRange("Glow Duration", 60, 0, Integer.MAX_VALUE);
            this.namedMonsters = ConfigBuildHandler.BUILDER.comment("Should named monsters prevent the player from sleeping.").define("Named Monsters", false);
            this.persistentMonsters = ConfigBuildHandler.BUILDER.comment("Should persistent monsters (unable to despawn via a vanilla tag) prevent the player from sleeping.").define("Persistent Monsters", false);

            BUILDER.pop();

        }

    }

    public static class WakeUpConfig {

        public final ForgeConfigSpec.BooleanValue heal;
        public final ForgeConfigSpec.IntValue healAmount;
        public final ForgeConfigSpec.BooleanValue starve;
        public final ForgeConfigSpec.IntValue starveAmount;
        public final ForgeConfigSpec.EnumValue<ClearPotions> clearPotions;
        public final ForgeConfigSpec.BooleanValue effects;
        public final ForgeConfigSpec.ConfigValue<List<String>> potionEffects;
        public final ForgeConfigSpec.BooleanValue persistentChat;

        private WakeUpConfig(String name) {

            BUILDER.push(name);

            this.heal = ConfigBuildHandler.BUILDER.comment("Should the player be healed when waking up.").define("Heal Player", true);
            this.healAmount = ConfigBuildHandler.BUILDER.comment("Amount of health the player should be healed by. Set to 0 to fully heal.").defineInRange("Heal Amount", 0, 0, Integer.MAX_VALUE);
            this.starve = ConfigBuildHandler.BUILDER.comment("Should the player loose some food after waking up.").define("Loose Food", false);
            this.starveAmount = ConfigBuildHandler.BUILDER.comment("Amount of food to loose when waking up. Set to 0 to completely starve the player. Negative values will feed the player instead.").defineInRange("Food Amount", 3, Integer.MIN_VALUE, Integer.MAX_VALUE);
            this.clearPotions = ConfigBuildHandler.BUILDER.comment("Clear potion effects after the player wakes up.").defineEnum("Clear Potions", ClearPotions.BOTH);
            this.effects = ConfigBuildHandler.BUILDER.comment("Should custom potion effects be applied to the player after waking up.").define("Apply Effects", false);
            this.potionEffects = ConfigBuildHandler.BUILDER.comment("Potion effects to be given to the player after waking up. Enter as \"modid:effect,duration,amplifier,hideParticles\", values are based on /effect command. Amplifier and hideParticles are optional.").define("Effects To Apply", Collections.singletonList("minecraft:regeneration,30,0,false"));
            this.persistentChat = ConfigBuildHandler.BUILDER.comment("Keep chat open after waking up if it contains any text.").define("Persistent Chat", true);

            BUILDER.pop();

        }

    }

    public static final ForgeConfigSpec SPEC = BUILDER.build();

}
