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
import minegame159.meteorclient.settings.BoolSetting;
import minegame159.meteorclient.settings.Setting;
import minegame159.meteorclient.settings.SettingGroup;
import net.minecraft.client.resource.language.I18n;

public class AutoSprint extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> permanent = sgGeneral.add(new BoolSetting.Builder()
            .name("permanent")
            .displayName(I18n.translate("Module.AutoSprint.setting.permanent.displayName"))
            .description(I18n.translate("Module.AutoSprint.setting.permanent.description"))
            .defaultValue(true)
            .build()
    );

    public AutoSprint() {
        super(Category.Movement, "auto-sprint", I18n.translate("Module.AutoSprint.description"));
    }
    
    @Override
    public void onDeactivate() {
        mc.player.setSprinting(false);
    }

    @EventHandler
    private final Listener<TickEvent.Post> onTick = new Listener<>(event -> {
        if(mc.player.forwardSpeed > 0 && !permanent.get()) {
            mc.player.setSprinting(true);
        } else if (permanent.get()) {
            mc.player.setSprinting(true);
        }
    });
}
