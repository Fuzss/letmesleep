package com.fuzs.letmesleep.helper;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.event.entity.living.PotionEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;

public class ClearPotionsHelper {

    public static void clearActivePotions(EntityLivingBase entity, boolean beneficial) {

        if (!entity.world.isRemote) {

            Iterator<PotionEffect> iterator = entity.getActivePotionEffects().iterator();

            while (iterator.hasNext()) {

                PotionEffect effect = iterator.next();

                boolean flag1 = net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new PotionEvent.PotionRemoveEvent(entity, effect));
                boolean flag2 = !effect.getPotion().isBadEffect();

                if (flag1 || flag2 && !beneficial || !flag2 && beneficial) {
                    continue;
                }

                onFinishedPotionEffect(entity, effect);
                iterator.remove();

            }

        }

    }

    private static void onFinishedPotionEffect(EntityLivingBase entity, PotionEffect effect) {

        Method onFinishedPotionEffect = ReflectionHelper.getOnFinishedPotionEffect();
        if (onFinishedPotionEffect != null) {

            try {

                onFinishedPotionEffect.invoke(entity, effect);

            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }

        }

    }

}
