/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.modules.movement;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.events.entity.player.JumpVelocityMultiplierEvent;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Module;
import minegame159.meteorclient.settings.DoubleSetting;
import minegame159.meteorclient.settings.Setting;
import minegame159.meteorclient.settings.SettingGroup;
import net.minecraft.client.resource.language.I18n;

public class HighJump extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    
    private final Setting<Double> multiplier = sgGeneral.add(new DoubleSetting.Builder()
            .name("multiplier")
            .displayName(I18n.translate("Module.HighJump.setting.multiplier.displayName"))
            .description(I18n.translate("Module.HighJump.setting.multiplier.description"))
            .defaultValue(1)
            .min(0)
            .build()
    );

    public HighJump() {
        super(Category.Movement, "high-jump", I18n.translate("Module.HighJump.description"));
    }

    @EventHandler
    private final Listener<JumpVelocityMultiplierEvent> onJumpVelocityMultiplier = new Listener<>(event -> {
        event.multiplier *= multiplier.get();
    });
}
