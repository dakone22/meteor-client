/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.modules.movement;

import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Module;
import minegame159.meteorclient.settings.BoolSetting;
import minegame159.meteorclient.settings.Setting;
import minegame159.meteorclient.settings.SettingGroup;
import net.minecraft.client.resource.language.I18n;

public class AntiLevitation extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    
    private final Setting<Boolean> applyGravity = sgGeneral.add(new BoolSetting.Builder()
            .name("apply-gravity")
            .displayName(I18n.translate("Modules.AntiLevitation.setting.applyGravity.displayName"))
            .description(I18n.translate("Modules.AntiLevitation.setting.applyGravity.description"))
            .defaultValue(false)
            .build()
    );

    public AntiLevitation() {
        super(Category.Movement, "anti-levitation", I18n.translate("Modules.AntiLevitation.description"));
    }

    public boolean isApplyGravity() {
        return applyGravity.get();
    }
}
