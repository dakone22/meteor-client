/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.modules.movement;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.events.entity.BoatMoveEvent;
import minegame159.meteorclient.events.packets.PacketEvent;
import minegame159.meteorclient.mixininterface.IVec3d;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Module;
import minegame159.meteorclient.settings.BoolSetting;
import minegame159.meteorclient.settings.DoubleSetting;
import minegame159.meteorclient.settings.Setting;
import minegame159.meteorclient.settings.SettingGroup;
import minegame159.meteorclient.utils.player.PlayerUtils;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.network.packet.s2c.play.VehicleMoveS2CPacket;
import net.minecraft.util.math.Vec3d;

public class BoatFly extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    
    private final Setting<Double> speed = sgGeneral.add(new DoubleSetting.Builder()
            .name("speed")
            .displayName(I18n.translate("Modules.BoatFly.setting.speed.displayName"))
            .description(I18n.translate("Modules.BoatFly.setting.speed.description"))
            .defaultValue(10)
            .min(0)
            .sliderMax(50)
            .build()
    );

    private final Setting<Double> verticalSpeed = sgGeneral.add(new DoubleSetting.Builder()
            .name("vertical-speed")
            .displayName(I18n.translate("Modules.BoatFly.setting.verticalSpeed.displayName"))
            .description(I18n.translate("Modules.BoatFly.setting.verticalSpeed.description"))
            .defaultValue(6)
            .min(0)
            .sliderMax(20)
            .build()
    );

    private final Setting<Double> fallSpeed = sgGeneral.add(new DoubleSetting.Builder()
            .name("fall-speed")
            .displayName(I18n.translate("Modules.BoatFly.setting.fallSpeed.displayName"))
            .description(I18n.translate("Modules.BoatFly.setting.fallSpeed.description"))
            .defaultValue(0.1)
            .min(0)
            .build()
    );

    private final Setting<Boolean> cancelServerPackets = sgGeneral.add(new BoolSetting.Builder()
            .name("cancel-server-packets")
            .displayName(I18n.translate("Modules.BoatFly.setting.cancelServerPackets.displayName"))
            .description(I18n.translate("Modules.BoatFly.setting.cancelServerPackets.description"))
            .defaultValue(false)
            .build()
    );

    public BoatFly() {
        super(Category.Movement, "boat-fly", I18n.translate("Modules.BoatFly.description"));
    }

    @EventHandler
    private final Listener<BoatMoveEvent> onBoatMove = new Listener<>(event -> {
        if (event.boat.getPrimaryPassenger() != mc.player) return;

        event.boat.yaw = mc.player.yaw;

        // Horizontal movement
        Vec3d vel = PlayerUtils.getHorizontalVelocity(speed.get());
        double velX = vel.getX();
        double velY = 0;
        double velZ = vel.getZ();

        // Vertical movement
        if (mc.options.keyJump.isPressed()) velY += verticalSpeed.get() / 20;
        if (mc.options.keySprint.isPressed()) velY -= verticalSpeed.get() / 20;
        else velY -= fallSpeed.get() / 20;

        // Apply velocity
        ((IVec3d) event.boat.getVelocity()).set(velX, velY, velZ);
    });

    @EventHandler
    private final Listener<PacketEvent.Receive> onReceivePacket = new Listener<>(event -> {
        if (event.packet instanceof VehicleMoveS2CPacket && cancelServerPackets.get()) {
            event.cancel();
        }
    });
}
