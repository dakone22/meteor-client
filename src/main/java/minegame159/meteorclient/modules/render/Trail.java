/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.modules.render;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.events.world.TickEvent;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Module;
import minegame159.meteorclient.settings.BoolSetting;
import minegame159.meteorclient.settings.ParticleEffectListSetting;
import minegame159.meteorclient.settings.Setting;
import minegame159.meteorclient.settings.SettingGroup;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.particle.ParticleEffect;

import java.util.ArrayList;
import java.util.List;

public class Trail extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<ParticleEffect>> particles = sgGeneral.add(new ParticleEffectListSetting.Builder()
            .name("particles")
            .displayName(I18n.translate("Modules.Trail.setting.particles.displayName"))
            .description(I18n.translate("Modules.Trail.setting.particles.description"))
            .defaultValue(new ArrayList<>(0))
            .build()
    );

    private final Setting<Boolean> pause = sgGeneral.add(new BoolSetting.Builder()
            .name("pause-when-stationary")
            .displayName(I18n.translate("Modules.Trail.setting.pause.displayName"))
            .description(I18n.translate("Modules.Trail.setting.pause.description"))
            .defaultValue(true)
            .build()
    );


    public Trail() {
        super(Category.Render, "trail", I18n.translate("Modules.Trail.description"));
    }

    @EventHandler
    private final Listener<TickEvent.Post> onTick = new Listener<>(event -> {
        if (pause.get() && mc.player.input.movementForward == 0f && mc.player.input.movementSideways == 0f && !mc.options.keyJump.isPressed()) return;
        for (ParticleEffect particleEffect : particles.get()) {
            mc.world.addParticle(particleEffect, mc.player.getX(), mc.player.getY(), mc.player.getZ(), 0, 0, 0);
        }
    });
}
