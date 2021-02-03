/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.modules.misc;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.events.world.TickEvent;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Module;
import minegame159.meteorclient.settings.*;
import minegame159.meteorclient.utils.Utils;
import minegame159.meteorclient.utils.player.RotationUtils;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class AutoBreed extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Object2BooleanMap<EntityType<?>>> entities = sgGeneral.add(new EntityTypeListSetting.Builder()
            .name("entities")
            .displayName(I18n.translate("Module.AutoBreed.setting.entities.displayName"))
            .description(I18n.translate("Module.AutoBreed.setting.entities.description"))
            .defaultValue(Utils.asObject2BooleanOpenHashMap(EntityType.HORSE, EntityType.DONKEY, EntityType.COW,
                    EntityType.MOOSHROOM, EntityType.SHEEP, EntityType.PIG, EntityType.CHICKEN, EntityType.WOLF,
                    EntityType.CAT, EntityType.OCELOT, EntityType.RABBIT, EntityType.LLAMA, EntityType.TURTLE,
                    EntityType.PANDA, EntityType.FOX, EntityType.BEE, EntityType.STRIDER, EntityType.HOGLIN))
            .onlyAttackable()
            .build()
    );

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
            .name("range")
            .displayName(I18n.translate("Module.AutoBreed.setting.range.displayName"))
            .description(I18n.translate("Module.AutoBreed.setting.range.description"))
            .min(0)
            .defaultValue(4.5)
            .build()
    );

    private final Setting<Hand> hand = sgGeneral.add(new EnumSetting.Builder<Hand>()
            .name("hand-for-breeding")
            .displayName(I18n.translate("Module.AutoBreed.setting.hand.displayName"))
            .description(I18n.translate("Module.AutoBreed.setting.hand.description"))
            .defaultValue(Hand.MAIN_HAND)
            .build()
    );

    private final Setting<Boolean> ignoreBabies = sgGeneral.add(new BoolSetting.Builder()
            .name("ignore-babies")
            .displayName(I18n.translate("Module.AutoBreed.setting.ignoreBabies.displayName"))
            .description(I18n.translate("Module.AutoBreed.setting.ignoreBabies.description"))
            .defaultValue(true)
            .build()
    );

    private final List<Entity> animalsFed = new ArrayList<>();

    public AutoBreed() {
        super(Category.Misc, "auto-breed", I18n.translate("Module.AutoBreed.description"));
    }

    @Override
    public void onActivate() {
        animalsFed.clear();
    }

    @EventHandler
    private final Listener<TickEvent.Post> onTick = new Listener<>(event -> {
        for (Entity entity : mc.world.getEntities()) {
            AnimalEntity animal;

            if (!(entity instanceof AnimalEntity)) continue;
            else animal = (AnimalEntity) entity;

            if (!entities.get().getBoolean(animal.getType())
                    || (animal.isBaby() && !ignoreBabies.get())
                    || animalsFed.contains(animal)
                    || mc.player.distanceTo(animal) > range.get()
                    || !animal.isBreedingItem(hand.get() == Hand.MAIN_HAND ? mc.player.getMainHandStack() : mc.player.getOffHandStack())) continue;

            RotationUtils.packetRotate(new Vec3d(animal.getX(), animal.getY() + animal.getHeight() / 2, animal.getZ()));
            mc.interactionManager.interactEntity(mc.player, animal, hand.get());
            mc.player.swingHand(hand.get());
            animalsFed.add(animal);
            return;
        }
    });
}
