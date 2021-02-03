/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.modules.player;

import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Module;
import minegame159.meteorclient.settings.DoubleSetting;
import minegame159.meteorclient.settings.Setting;
import minegame159.meteorclient.settings.SettingGroup;
import net.minecraft.client.resource.language.I18n;

public class Reach extends Module {
    private final SettingGroup sg = settings.getDefaultGroup();

    private final Setting<Double> reach = sg.add(new DoubleSetting.Builder()
            .name("reach")
            .displayName(I18n.translate("Module.Reach.setting.reach.displayName"))
            .description(I18n.translate("Module.Reach.setting.reach.description"))
            .defaultValue(5)
            .min(0)
            .sliderMax(6)
            .build()
    );

    public Reach() {
        super(Category.Player, "reach", I18n.translate("Module.Reach.description"));
    }

    public float getReach() {
        return reach.get().floatValue();
    }
}
