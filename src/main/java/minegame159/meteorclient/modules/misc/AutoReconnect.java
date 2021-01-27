/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.modules.misc;

import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.MeteorClient;
import minegame159.meteorclient.events.world.ConnectToServerEvent;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Module;
import minegame159.meteorclient.settings.DoubleSetting;
import minegame159.meteorclient.settings.Setting;
import minegame159.meteorclient.settings.SettingGroup;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.resource.language.I18n;

public class AutoReconnect extends Module {
    
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    
    public final Setting<Double> time = sgGeneral.add(new DoubleSetting.Builder()
            .name("delay")
            .displayName(I18n.translate("Modules.AutoReconnect.setting.time.displayName"))
            .description(I18n.translate("Modules.AutoReconnect.setting.time.description"))
            .defaultValue(5.0)
            .min(0.0)
            .build()
    );

    public ServerInfo lastServerInfo;

    public AutoReconnect() {
        super(Category.Misc, "auto-reconnect", I18n.translate("Modules.AutoReconnect.description"));
        MeteorClient.EVENT_BUS.subscribe(new Listener<ConnectToServerEvent>(event -> {
            lastServerInfo = mc.isInSingleplayer() ? null : mc.getCurrentServerEntry();
        }));
    }
}
