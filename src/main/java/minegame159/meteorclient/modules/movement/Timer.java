/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.modules.movement;

import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Module;
import minegame159.meteorclient.settings.DoubleSetting;
import minegame159.meteorclient.settings.Setting;
import minegame159.meteorclient.settings.SettingGroup;
import net.minecraft.client.resource.language.I18n;

public class Timer extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> speed = sgGeneral.add(new DoubleSetting.Builder()
            .name("speed")
            .displayName(I18n.translate("Module.Timer.setting.speed.displayName"))
            .description(I18n.translate("Module.Timer.setting.speed.description"))
            .defaultValue(1)
            .min(0.1)
            .sliderMin(0.1)
            .sliderMax(10)
            .build()
    );

    public Timer() {
        super(Category.Movement, "timer", I18n.translate("Module.Timer.description"));
    }
    // If you put your timer to 0.1 you're a dumbass.
    public double getMultiplier() {
        return isActive() ? speed.get() : 1;
    }
}
