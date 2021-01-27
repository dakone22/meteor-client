/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.modules.combat;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.events.world.TickEvent;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Module;
import minegame159.meteorclient.settings.BoolSetting;
import minegame159.meteorclient.settings.Setting;
import minegame159.meteorclient.settings.SettingGroup;
import minegame159.meteorclient.utils.player.RotationUtils;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class SelfWeb extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> doubles = sgGeneral.add(new BoolSetting.Builder()
            .name("doubles")
            .displayName(I18n.translate("Modules.SelfWeb.setting.doubles.displayName"))
            .description(I18n.translate("Modules.SelfWeb.setting.doubles.description"))
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> turnOff = sgGeneral.add(new BoolSetting.Builder()
            .name("turn-off")
            .displayName(I18n.translate("Modules.SelfWeb.setting.turnOff.displayName"))
            .description(I18n.translate("Modules.SelfWeb.setting.turnOff.description"))
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
            .name("rotate")
            .displayName(I18n.translate("Modules.SelfWeb.setting.rotate.displayName"))
            .description(I18n.translate("Modules.SelfWeb.setting.rotate.description"))
            .defaultValue(true)
            .build()
    );

    public SelfWeb() {
        super(Category.Combat, "self-web", I18n.translate("Modules.SelfWeb.description"));
    }

    @EventHandler
    private final Listener<TickEvent.Post> onTick = new Listener<>(event -> {
        int webSlot = -1;
        for (int i = 0; i < 9; i++) {
            Item item = mc.player.inventory.getStack(i).getItem();

            if (item == Items.COBWEB) {
                webSlot = i;
                break;
            }
        }
        if (webSlot == -1) return;

        int prevSlot = mc.player.inventory.selectedSlot;
        mc.player.inventory.selectedSlot = webSlot;
        BlockPos playerPos = mc.player.getBlockPos();

        if (rotate.get()) RotationUtils.packetRotate(mc.player.getBlockPos());
        mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND, new BlockHitResult(mc.player.getPos(), Direction.DOWN, playerPos, true));

        if (doubles.get()) mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND, new BlockHitResult(mc.player.getPos(), Direction.DOWN, playerPos.add(0 ,1,0), true));

        mc.player.inventory.selectedSlot = prevSlot;
        if (turnOff.get()) toggle();
    });
}
