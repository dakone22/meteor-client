/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2020 Meteor Development.
 */

package minegame159.meteorclient.gui.screens.topbar;

import minegame159.meteorclient.Config;
import minegame159.meteorclient.settings.BoolSetting;
import minegame159.meteorclient.settings.SettingGroup;
import minegame159.meteorclient.settings.Settings;
import minegame159.meteorclient.settings.StringSetting;
import net.minecraft.client.resource.language.I18n;

public class TopBarConfig extends TopBarWindowScreen {
    public TopBarConfig() {
        super(TopBarType.Config);
    }

    @Override
    protected void initWidgets() {
        Settings s = new Settings();

        SettingGroup sgGeneral = s.getDefaultGroup();

        sgGeneral.add(new StringSetting.Builder()
                .name("prefix")
                .description(I18n.translate("TopBar.TopBarConfig.prefix.description"))
                .defaultValue(".")
                .onChanged(Config.INSTANCE::setPrefix)
                .onModuleActivated(stringSetting -> stringSetting.set(Config.INSTANCE.getPrefix()))
                .build()
        );

        sgGeneral.add(new BoolSetting.Builder()
                .name("custom-font")
                .description(I18n.translate("TopBar.TopBarConfig.custom-font.description"))
                .defaultValue(true)
                .onChanged(aBoolean -> {
                    Config.INSTANCE.customFont = aBoolean;
                    root.invalidate();
                })
                .onModuleActivated(booleanSetting -> booleanSetting.set(Config.INSTANCE.customFont))
                .build()
        );

        sgGeneral.add(new BoolSetting.Builder()
                .name("chat-commands-info")
                .description(I18n.translate("TopBar.TopBarConfig.chat-commands-info.description"))
                .defaultValue(true)
                .onChanged(aBoolean -> Config.INSTANCE.chatCommandsInfo = aBoolean)
                .onModuleActivated(booleanSetting -> booleanSetting.set(Config.INSTANCE.chatCommandsInfo))
                .build()
        );

        sgGeneral.add(new BoolSetting.Builder()
                .name("delete-chat-commands-info")
                .description(I18n.translate("TopBar.TopBarConfig.delete-chat-commands-info.description"))
                .defaultValue(true)
                .onChanged(aBoolean -> Config.INSTANCE.deleteChatCommandsInfo = aBoolean)
                .onModuleActivated(booleanSetting -> booleanSetting.set(Config.INSTANCE.deleteChatCommandsInfo))
                .build()
        );

        add(s.createTable()).fillX().expandX();
    }
}
