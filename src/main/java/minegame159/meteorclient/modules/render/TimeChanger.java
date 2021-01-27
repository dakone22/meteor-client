/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.modules.render;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.events.packets.PacketEvent;
import minegame159.meteorclient.events.world.TickEvent;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Module;
import minegame159.meteorclient.settings.DoubleSetting;
import minegame159.meteorclient.settings.Setting;
import minegame159.meteorclient.settings.SettingGroup;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;

public class TimeChanger extends Module {

    private final SettingGroup sgDefault = settings.getDefaultGroup();

    private final Setting<Double> time = sgDefault.add(new DoubleSetting.Builder()
            .name("time")
            .displayName(I18n.translate("Modules.TimeChanger.setting.time.displayName"))
            .description(I18n.translate("Modules.TimeChanger.setting.time.description"))
            .defaultValue(0)
            .sliderMin(-20000)
            .sliderMax(20000)
            .build()
    );

    public TimeChanger() {
        super(Category.Render, "time-changer", I18n.translate("Modules.TimeChanger.description"));
    }

    long oldTime;

    @Override
    public void onActivate() {
        oldTime = mc.world.getTime();
    }

    @Override
    public void onDeactivate() {
        mc.world.setTimeOfDay(oldTime);
    }

    @EventHandler
    private final Listener<PacketEvent.Receive> onTime = new Listener<>(event -> {
        if (event.packet instanceof WorldTimeUpdateS2CPacket) {
            oldTime = ((WorldTimeUpdateS2CPacket) event.packet).getTime();
            event.setCancelled(true);
        }
    });

    @EventHandler
    private final Listener<TickEvent.Post> onTick = new Listener<>(event -> {
        mc.world.setTimeOfDay(time.get().longValue());
    });
}
