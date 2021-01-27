/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.modules.player;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.events.packets.PacketEvent;
import minegame159.meteorclient.events.world.TickEvent;
import minegame159.meteorclient.mixininterface.IPlayerMoveC2SPacket;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Module;
import minegame159.meteorclient.settings.BoolSetting;
import minegame159.meteorclient.settings.Setting;
import minegame159.meteorclient.settings.SettingGroup;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class AntiHunger extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> sprint = sgGeneral.add(new BoolSetting.Builder()
            .name("sprint")
            .displayName(I18n.translate("Modules.AntiHunger.setting.sprint.displayName"))
            .description(I18n.translate("Modules.AntiHunger.setting.sprint.description"))
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> onGround = sgGeneral.add(new BoolSetting.Builder()
            .name("on-ground")
            .displayName(I18n.translate("Modules.AntiHunger.setting.onGround.displayName"))
            .description(I18n.translate("Modules.AntiHunger.setting.onGround.description"))
            .defaultValue(true)
            .build()
    );

    private boolean lastOnGround;
    private boolean sendOnGroundTruePacket;
    private boolean ignorePacket;

    public AntiHunger() {
        super(Category.Player, "anti-hunger", I18n.translate("Modules.AntiHunger.description"));
    }

    @Override
    public void onActivate() {
        lastOnGround = mc.player.isOnGround();
        sendOnGroundTruePacket = true;
    }

    @EventHandler
    private final Listener<PacketEvent.Send> onSendPacket = new Listener<>(event -> {
        if (ignorePacket) return;

        if (event.packet instanceof ClientCommandC2SPacket && sprint.get()) {
            ClientCommandC2SPacket.Mode mode = ((ClientCommandC2SPacket) event.packet).getMode();

            if (mode == ClientCommandC2SPacket.Mode.START_SPRINTING || mode == ClientCommandC2SPacket.Mode.STOP_SPRINTING) {
                event.cancel();
            }
        }

        if (event.packet instanceof PlayerMoveC2SPacket && onGround.get() && mc.player.isOnGround() && mc.player.fallDistance <= 0.0 && !mc.interactionManager.isBreakingBlock()) {
            ((IPlayerMoveC2SPacket) event.packet).setOnGround(false);
        }
    });

    @EventHandler
    private final Listener<TickEvent.Post> onTick = new Listener<>(event -> {
        if (mc.player.isOnGround() && !lastOnGround && !sendOnGroundTruePacket) sendOnGroundTruePacket = true;

        if (mc.player.isOnGround() && sendOnGroundTruePacket && onGround.get()) {
            ignorePacket = true;
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket(true));
            ignorePacket = false;

            sendOnGroundTruePacket = false;
        }

        lastOnGround = mc.player.isOnGround();
    });
}
