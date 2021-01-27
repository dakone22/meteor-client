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

public class NoSlow extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> items = sgGeneral.add(new BoolSetting.Builder()
            .name("items")
            .displayName(I18n.translate("Modules.NoSlow.setting.items.displayName"))
            .description(I18n.translate("Modules.NoSlow.setting.items.description"))
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> web = sgGeneral.add(new BoolSetting.Builder()
            .name("web")
            .displayName(I18n.translate("Modules.NoSlow.setting.web.displayName"))
            .description(I18n.translate("Modules.NoSlow.setting.web.description"))
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> soulSand = sgGeneral.add(new BoolSetting.Builder()
            .name("soul-sand")
            .displayName(I18n.translate("Modules.NoSlow.setting.soulSand.displayName"))
            .description(I18n.translate("Modules.NoSlow.setting.soulSand.description"))
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> slimeBlock = sgGeneral.add(new BoolSetting.Builder()
            .name("slime-block")
            .displayName(I18n.translate("Modules.NoSlow.setting.slimeBlock.displayName"))
            .description(I18n.translate("Modules.NoSlow.setting.slimeBlock.description"))
            .defaultValue(true)
            .build()
    );

    public NoSlow() {
        super(Category.Movement, "no-slow", I18n.translate("Modules.NoSlow.description"));
    }

    public boolean items() {
        return isActive() && items.get();
    }

    public boolean web() {
        return isActive() && web.get();
    }

    public boolean soulSand() {
        return isActive() && soulSand.get();
    }

    public boolean slimeBlock() {
        return isActive() && slimeBlock.get();
    }
}
