/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.modules.player;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.events.world.TickEvent;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Module;
import minegame159.meteorclient.settings.DoubleSetting;
import minegame159.meteorclient.settings.EnumSetting;
import minegame159.meteorclient.settings.Setting;
import minegame159.meteorclient.settings.SettingGroup;
import net.minecraft.client.resource.language.I18n;

public class Rotation extends Module {

    public enum LockMode {
        Smart,
        Simple,
        None
    }


    private final SettingGroup sgYaw = settings.createGroup(I18n.translate("Modules.Rotation.group.sgYaw"));
    private final SettingGroup sgPitch = settings.createGroup(I18n.translate("Modules.Rotation.group.sgPitch"));

    // Yaw

    private final Setting<LockMode> yawLockMode = sgYaw.add(new EnumSetting.Builder<LockMode>()
            .name("yaw-lock-mode")
            .displayName(I18n.translate("Modules.Rotation.setting.yawLockMode.displayName"))
            .description(I18n.translate("Modules.Rotation.setting.yawLockMode.description"))
            .displayValues(new String[]{
                    I18n.translate("Modules.Rotation.LockMode.Smart"),
                    I18n.translate("Modules.Rotation.LockMode.Simple"),
                    I18n.translate("Modules.Rotation.LockMode.None"),
            })
            .defaultValue(LockMode.Simple)
            .build()
    );

    private final Setting<Double> yawAngle = sgYaw.add(new DoubleSetting.Builder()
            .name("yaw-angle")
            .displayName(I18n.translate("Modules.Rotation.setting.yawAngle.displayName"))
            .description(I18n.translate("Modules.Rotation.setting.yawAngle.description"))
            .defaultValue(0)
            .sliderMax(360)
            .max(360)
            .build()
    );

    // Pitch

    private final Setting<LockMode> pitchLockMode = sgPitch.add(new EnumSetting.Builder<LockMode>()
            .name("pitch-lock-mode")
            .displayName(I18n.translate("Modules.Rotation.setting.pitchLockMode.displayName"))
            .description(I18n.translate("Modules.Rotation.setting.pitchLockMode.description"))
            .defaultValue(LockMode.Simple)
            .build()
    );

    private final Setting<Double> pitchAngle = sgPitch.add(new DoubleSetting.Builder()
            .name("pitch-angle")
            .displayName(I18n.translate("Modules.Rotation.setting.pitchAngle.displayName"))
            .description(I18n.translate("Modules.Rotation.setting.pitchAngle.description"))
            .defaultValue(0)
            .min(-90)
            .max(90)
            .sliderMin(-90)
            .sliderMax(90)
            .build()
    );

    public Rotation() {
        super(Category.Player, "rotation", I18n.translate("Modules.Rotation.description"));
    }

    @Override
    public void onActivate() {
        onTick.invoke(null);
    }

    @EventHandler
    private final Listener<TickEvent.Post> onTick = new Listener<>(event -> {
        switch (yawLockMode.get()) {
            case Simple:
                setYawAngle(yawAngle.get().floatValue());
                break;
            case Smart:
                setYawAngle(getSmartYawDirection());
                break;
        }

        switch (pitchLockMode.get()) {
            case Simple:
                mc.player.pitch = pitchAngle.get().floatValue();
                break;
            case Smart:
                mc.player.pitch = getSmartPitchDirection();
                break;
        }
    });

    private float getSmartYawDirection() {
        return Math.round((mc.player.yaw + 1f) / 45f) * 45f;
    }

    private float getSmartPitchDirection() {
        return Math.round((mc.player.pitch + 1f) / 30f) * 30f;
    }

    private void setYawAngle(float yawAngle) {
        mc.player.yaw = yawAngle;
        mc.player.headYaw = yawAngle;
        mc.player.bodyYaw = yawAngle;
    }
}
