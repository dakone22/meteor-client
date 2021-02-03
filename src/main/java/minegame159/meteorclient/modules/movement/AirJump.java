/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.modules.movement;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.events.meteor.KeyEvent;
import minegame159.meteorclient.events.world.TickEvent;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Module;
import minegame159.meteorclient.modules.ModuleManager;
import minegame159.meteorclient.modules.render.Freecam;
import minegame159.meteorclient.settings.BoolSetting;
import minegame159.meteorclient.settings.Setting;
import minegame159.meteorclient.settings.SettingGroup;
import minegame159.meteorclient.utils.misc.input.KeyAction;
import net.minecraft.client.resource.language.I18n;

public class AirJump extends Module {
    public AirJump() {
        super(Category.Movement, "air-jump", I18n.translate("Module.AirJump.description"));
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> maintainY = sgGeneral.add(new BoolSetting.Builder()
            .name("maintain-level")
            .displayName(I18n.translate("Module.AirJump.setting.maintainY.displayName"))
            .description(I18n.translate("Module.AirJump.setting.maintainY.description"))
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> onHold = sgGeneral.add(new BoolSetting.Builder()
            .name("on-hold")
            .displayName(I18n.translate("Module.AirJump.setting.onHold.displayName"))
            .description(I18n.translate("Module.AirJump.setting.onHold.description"))
            .defaultValue(true)
            .build()
    );

    private int level = 0;

    @EventHandler
    private final Listener<KeyEvent> onKey = new Listener<>(event -> {
        if (ModuleManager.INSTANCE.isActive(Freecam.class) || mc.currentScreen != null) return;
        if ((event.action == KeyAction.Press || (event.action == KeyAction.Repeat && onHold.get())) && mc.options.keyJump.matchesKey(event.key, 0)) {
            mc.player.jump();
            level = mc.player.getBlockPos().getY();
        }
        if ((event.action == KeyAction.Press || (event.action == KeyAction.Repeat && onHold.get())) && mc.options.keySneak.matchesKey(event.key, 0)){
            level -= 1;
        }
    });

    @EventHandler
    private final Listener<TickEvent.Post> onTick = new Listener<>(event -> {
        if (ModuleManager.INSTANCE.isActive(Freecam.class)) return;
        if (maintainY.get() && mc.player.getBlockPos().getY() == level){
            mc.player.jump();
        }
    });
}
