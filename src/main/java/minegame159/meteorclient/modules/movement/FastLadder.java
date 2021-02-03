/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.modules.movement;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.events.world.TickEvent;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Module;
import minegame159.meteorclient.settings.DoubleSetting;
import minegame159.meteorclient.settings.Setting;
import minegame159.meteorclient.settings.SettingGroup;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.math.Vec3d;

public class FastLadder extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    
    private final Setting<Double> speed = sgGeneral.add(new DoubleSetting.Builder()
            .name("climb-speed")
            .displayName(I18n.translate("Module.FastLadder.setting.speed.displayName"))
            .description(I18n.translate("Module.FastLadder.setting.speed.description"))
            .defaultValue(0.2872)
            .min(0.0)
            .build()
    );

    public FastLadder() {
        super(Category.Movement, "fast-ladder", I18n.translate("Module.FastLadder.description"));
    }

    @EventHandler
    private final Listener<TickEvent.Post> onTick = new Listener<>(event -> {
        if (!mc.player.isClimbing() || !mc.player.horizontalCollision) return;
        if (mc.player.input.movementForward == 0 && mc.player.input.movementSideways == 0) return;

        Vec3d velocity = mc.player.getVelocity();
        mc.player.setVelocity(velocity.x, speed.get(), velocity.z);
    });
}
