/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.modules.misc;

import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import me.zero.alpine.event.EventPriority;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.events.packets.PacketEvent;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Module;
import minegame159.meteorclient.settings.PacketBoolSetting;
import minegame159.meteorclient.settings.Setting;
import minegame159.meteorclient.settings.SettingGroup;
import minegame159.meteorclient.utils.network.PacketUtils;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.network.Packet;

public class PacketCanceller extends Module {
    public static Object2BooleanMap<Class<? extends Packet<?>>> S2C_PACKETS = new Object2BooleanArrayMap<>();
    public static Object2BooleanMap<Class<? extends Packet<?>>> C2S_PACKETS = new Object2BooleanArrayMap<>();
    
    static {
        for (Class<? extends Packet<?>> packet : PacketUtils.getS2CPackets()) S2C_PACKETS.put(packet, false);
        for (Class<? extends Packet<?>> packet : PacketUtils.getC2SPackets()) C2S_PACKETS.put(packet, false);
    }
    
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    
    private final Setting<Object2BooleanMap<Class<? extends Packet<?>>>> s2cPackets = sgGeneral.add(new PacketBoolSetting.Builder()
            .name("S2C-packets")
            .description(I18n.translate("Modules.PacketCanceller.setting.s2cPackets.description"))
            .defaultValue(S2C_PACKETS)
            .build()
    );

    private final Setting<Object2BooleanMap<Class<? extends Packet<?>>>> c2sPackets = sgGeneral.add(new PacketBoolSetting.Builder()
            .name("C2S-packets")
            .description(I18n.translate("Modules.PacketCanceller.setting.c2sPackets.description"))
            .defaultValue(C2S_PACKETS)
            .build()
    );

    public PacketCanceller() {
        super(Category.Misc, "packet-canceller", I18n.translate("Modules.PacketCanceller.description"));
    }

    @EventHandler
    private final Listener<PacketEvent.Receive> onReceivePacket = new Listener<>(event -> {
        if (s2cPackets.get().getBoolean(event.packet.getClass())) event.cancel();
    }, EventPriority.HIGHEST + 1);

    @EventHandler
    private final Listener<PacketEvent.Send> onSendPacket = new Listener<>(event -> {
        if (c2sPackets.get().getBoolean(event.packet.getClass())) event.cancel();
    }, EventPriority.HIGHEST + 1);
}
