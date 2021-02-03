/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.modules.render;

import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Module;
import minegame159.meteorclient.settings.DoubleSetting;
import minegame159.meteorclient.settings.Setting;
import minegame159.meteorclient.settings.SettingGroup;
import net.minecraft.client.resource.language.I18n;

public class HandView extends Module {

    private final SettingGroup sgDefault = settings.getDefaultGroup();

    private final Setting<Double> rotationX = sgDefault.add(new DoubleSetting.Builder()
            .name("rotation-x")
            .displayName(I18n.translate("Module.HandView.setting.rotationX.displayName"))
            .description(I18n.translate("Module.HandView.setting.rotationX.description"))
            .defaultValue(0.00)
            .sliderMin(-0.2)
            .sliderMax(0.2)
            .build()
    );

    private final Setting<Double> rotationY = sgDefault.add(new DoubleSetting.Builder()
            .name("rotation-y")
            .displayName(I18n.translate("Module.HandView.setting.rotationY.displayName"))
            .description(I18n.translate("Module.HandView.setting.rotationY.description"))
            .defaultValue(0.00)
            .sliderMin(-0.2)
            .sliderMax(0.2)
            .build()
    );

    private final Setting<Double> rotationZ = sgDefault.add(new DoubleSetting.Builder()
            .name("rotation-z")
            .displayName(I18n.translate("Module.HandView.setting.rotationZ.displayName"))
            .description(I18n.translate("Module.HandView.setting.rotationZ.description"))
            .defaultValue(0.00)
            .sliderMin(-0.25)
            .sliderMax(0.25)
            .build()
    );

    private final Setting<Double> ScaleX = sgDefault.add(new DoubleSetting.Builder()
            .name("scale-x")
            .displayName(I18n.translate("Module.HandView.setting.ScaleX.displayName"))
            .description(I18n.translate("Module.HandView.setting.ScaleX.description"))
            .defaultValue(0.75)
            .sliderMin(0)
            .sliderMax(1.5)
            .build()
    );

    private final Setting<Double> ScaleY = sgDefault.add(new DoubleSetting.Builder()
            .name("scale-y")
            .displayName(I18n.translate("Module.HandView.setting.ScaleY.displayName"))
            .description(I18n.translate("Module.HandView.setting.ScaleY.description"))
            .defaultValue(0.60)
            .sliderMin(0)
            .sliderMax(2)
            .build()
    );

    private final Setting<Double> ScaleZ = sgDefault.add(new DoubleSetting.Builder()
            .name("scale-z")
            .displayName(I18n.translate("Module.HandView.setting.ScaleZ.displayName"))
            .description(I18n.translate("Module.HandView.setting.ScaleZ.description"))
            .defaultValue(1.00)
            .sliderMin(0)
            .sliderMax(5)
            .build()
    );

    private final Setting<Double> PosX = sgDefault.add(new DoubleSetting.Builder()
            .name("pos-x")
            .displayName(I18n.translate("Module.HandView.setting.PosX.displayName"))
            .description(I18n.translate("Module.HandView.setting.PosX.description"))
            .defaultValue(0.00)
            .sliderMin(-3)
            .sliderMax(3)
            .build()
    );

    private final Setting<Double> PosY = sgDefault.add(new DoubleSetting.Builder()
            .name("pos-y")
            .displayName(I18n.translate("Module.HandView.setting.PosY.displayName"))
            .description(I18n.translate("Module.HandView.setting.PosY.description"))
            .defaultValue(0.00)
            .sliderMin(-3)
            .sliderMax(3)
            .build()
    );

    private final Setting<Double> PosZ = sgDefault.add(new DoubleSetting.Builder()
            .name("pos-z")
            .displayName(I18n.translate("Module.HandView.setting.PosZ.displayName"))
            .description(I18n.translate("Module.HandView.setting.PosZ.description"))
            .defaultValue(-0.10)
            .sliderMin(-3)
            .sliderMax(3)
            .build()
    );


    public HandView() {
        super(Category.Render, "hand-view", I18n.translate("Module.HandView.description"));
    }

    public float rotationX() {
        return rotationX.get().floatValue();
    }
    public float rotationY() {
        return rotationY.get().floatValue();
    }
    public float rotationZ() {
        return rotationZ.get().floatValue();
    }

    public float scaleX() {
        return ScaleX.get().floatValue();
    }
    public float scaleY() {
        return ScaleY.get().floatValue();
    }
    public float scaleZ() {
        return ScaleZ.get().floatValue();
    }

    public float posX() {
        return PosX.get().floatValue();
    }
    public float posY() {
        return PosY.get().floatValue();
    }
    public float posZ() {
        return PosZ.get().floatValue();
    }
}