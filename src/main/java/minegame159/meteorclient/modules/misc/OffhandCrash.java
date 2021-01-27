/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.modules.misc;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.events.world.PlaySoundEvent;
import minegame159.meteorclient.events.world.TickEvent;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Module;
import minegame159.meteorclient.settings.BoolSetting;
import minegame159.meteorclient.settings.IntSetting;
import minegame159.meteorclient.settings.Setting;
import minegame159.meteorclient.settings.SettingGroup;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class OffhandCrash extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> doCrash = sgGeneral.add(new BoolSetting.Builder()
            .name("do-crash")
            .displayName(I18n.translate("Modules.OffhandCrash.setting.doCrash.displayName"))
            .description(I18n.translate("Modules.OffhandCrash.setting.doCrash.description"))
            .defaultValue(true)
            .build()
    );

    private final Setting<Integer> speed = sgGeneral.add(new IntSetting.Builder()
            .name("speed")
            .displayName(I18n.translate("Modules.OffhandCrash.setting.speed.displayName"))
            .description(I18n.translate("Modules.OffhandCrash.setting.speed.description"))
            .defaultValue(2000)
            .min(1)
            .sliderMax(10000)
            .build()
    );

    private final Setting<Boolean> antiCrash = sgGeneral.add(new BoolSetting.Builder()
            .name("anti-crash")
            .displayName(I18n.translate("Modules.OffhandCrash.setting.antiCrash.displayName"))
            .description(I18n.translate("Modules.OffhandCrash.setting.antiCrash.description"))
            .defaultValue(true)
            .build()
    );

    public OffhandCrash() {
        super(Category.Misc, "offhand-crash", I18n.translate("Modules.OffhandCrash.description"));
    }


    private static final PlayerActionC2SPacket PACKET = new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, new BlockPos(0, 0, 0) , Direction.UP);

    @EventHandler
    private final Listener<TickEvent.Post> onTick = new Listener<>(event -> {
        if (doCrash.get()) {
            for(int i = 0; i < speed.get(); ++i) mc.player.networkHandler.sendPacket(PACKET);
        }
    });

    @EventHandler
    private final Listener<PlaySoundEvent> onPlaySound = new Listener<>(event -> {
        if (antiCrash.get() && event.sound.getId().toString().equals("minecraft:item.armor.equip_generic")){
            event.cancel();
        }
    });
}