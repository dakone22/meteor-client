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

public class Velocity extends Module {
    private final SettingGroup sg = settings.getDefaultGroup();

    private final Setting<Double> horizontal = sg.add(new DoubleSetting.Builder()
            .name("horizontal-multiplier")
            .displayName(I18n.translate("Modules.Velocity.setting.horizontal.displayName"))
            .description(I18n.translate("Modules.Velocity.setting.horizontal.description"))
            .defaultValue(0)
            .sliderMin(0)
            .sliderMax(1)
            .build()
    );

    private final Setting<Double> vertical = sg.add(new DoubleSetting.Builder()
            .name("vertical-multiplier")
            .displayName(I18n.translate("Modules.Velocity.setting.vertical.displayName"))
            .description(I18n.translate("Modules.Velocity.setting.vertical.description"))
            .defaultValue(0)
            .sliderMin(0)
            .sliderMax(1)
            .build()
    );

    public Velocity() {
        super(Category.Movement, "velocity", I18n.translate("Modules.Velocity.description"));
    }

    public double getHorizontal() {
        return isActive() ? horizontal.get() : 1;
    }

    public double getVertical() {
        return isActive() ? vertical.get() : 1;
    }
}
