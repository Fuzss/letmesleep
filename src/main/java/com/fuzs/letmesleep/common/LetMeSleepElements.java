package com.fuzs.letmesleep.common;

import com.fuzs.letmesleep.common.element.BadDreamElement;
import com.fuzs.letmesleep.common.element.SleepingChecksElement;
import com.fuzs.letmesleep.common.element.SpawnPointElement;
import com.fuzs.letmesleep.common.element.WakeUpElement;
import com.fuzs.puzzleslib.element.AbstractElement;
import com.fuzs.puzzleslib.element.ElementRegistry;

@SuppressWarnings("unused")
public class LetMeSleepElements extends ElementRegistry {

    public static final AbstractElement WAKE_UP_ACTIONS = register("wake_up_actions", new WakeUpElement());
    public static final AbstractElement BAD_DREAMS = register("bad_dreams", new BadDreamElement());
    public static final AbstractElement SLEEPING_CHECKS = register("sleeping_checks", new SleepingChecksElement());
    public static final AbstractElement SPAWN_POINT_SETTINGS = register("spawn_point_settings", new SpawnPointElement());

    /**
     * create overload so this class and its elements are loaded
     */
    public static void setup() {

        ElementRegistry.setup();
    }

}
