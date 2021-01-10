package com.fuzs.letmesleep.client.element;

import com.fuzs.puzzleslib.element.AbstractElement;
import com.fuzs.puzzleslib.element.ISidedElement;
import net.minecraftforge.common.ForgeConfigSpec;

public class SpawnPointClientElement extends ISidedElement.Abstract implements ISidedElement.Client {

    public SpawnPointClientElement(AbstractElement parent) {

        super(parent);
    }

    @Override
    public void setupClient() {

    }

    @Override
    public void setupClientConfig(ForgeConfigSpec.Builder builder) {

    }

}
