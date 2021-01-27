/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.modules.movement;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.events.world.TickEvent;
import minegame159.meteorclient.mixininterface.IVec3d;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Module;
import minegame159.meteorclient.settings.DoubleSetting;
import minegame159.meteorclient.settings.EnumSetting;
import minegame159.meteorclient.settings.Setting;
import minegame159.meteorclient.settings.SettingGroup;
import net.minecraft.client.resource.language.I18n;

public class AutoJump extends Module {
    public enum JumpWhen {
        Sprinting,
        Walking,
        Always
    }

    public enum Mode {
        Jump,
        Velocity
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
            .name("mode")
            .displayName(I18n.translate("Modules.AutoJump.setting.mode.displayName"))
            .description(I18n.translate("Modules.AutoJump.setting.mode.description"))
            .defaultValue(Mode.Jump)
            .build()
    );

    private final Setting<JumpWhen> jumpIf = sgGeneral.add(new EnumSetting.Builder<JumpWhen>()
            .name("jump-if")
            .displayName(I18n.translate("Modules.AutoJump.setting.jumpIf.displayName"))
            .description(I18n.translate("Modules.AutoJump.setting.jumpIf.description"))
            .defaultValue(JumpWhen.Always)
            .build()
    );

    private final Setting<Double> velocityHeight = sgGeneral.add(new DoubleSetting.Builder()
            .name("velocity-height")
            .displayName(I18n.translate("Modules.AutoJump.setting.velocityHeight.displayName"))
            .description(I18n.translate("Modules.AutoJump.setting.velocityHeight.description"))
            .defaultValue(0.25)
            .min(0)
            .sliderMax(2)
            .build()
    );

    public AutoJump() {
        super(Category.Movement, "auto-jump", I18n.translate("Modules.AutoJump.description"));
    }

    private boolean jump() {
        switch (jumpIf.get()) {
            case Sprinting: return mc.player.isSprinting() && (mc.player.forwardSpeed != 0 || mc.player.sidewaysSpeed != 0);
            case Walking:   return mc.player.forwardSpeed != 0 || mc.player.sidewaysSpeed != 0;
            case Always:    return true;
            default:        return false;
        }
    }

    @EventHandler
    private final Listener<TickEvent.Pre> onTick = new Listener<>(event -> {
        if (!mc.player.isOnGround() || mc.player.isSneaking() || !jump()) return;

        if (mode.get() == Mode.Jump) mc.player.jump();
        else ((IVec3d) mc.player.getVelocity()).setY(velocityHeight.get());
    });
}
