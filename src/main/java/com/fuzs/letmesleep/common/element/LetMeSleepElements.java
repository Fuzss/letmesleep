package com.fuzs.letmesleep.common.element;

import com.fuzs.puzzleslib.element.AbstractElement;
import com.fuzs.puzzleslib.element.ElementRegistry;

public class LetMeSleepElements extends ElementRegistry {

    public static final AbstractElement WAKE_UP_ACTIONS = register("wake_up_actions", new WakeUpElement());
    public static final AbstractElement BAD_DREAMS = register("bad_dreams", new BadDreamElement());

    /**
     * create overload so this class and its elements are loaded
     */
    public static void setup() {

        ElementRegistry.setup();
    }

}
