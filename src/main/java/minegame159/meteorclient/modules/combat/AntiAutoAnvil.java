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
import minegame159.meteorclient.utils.player.InvUtils;
import minegame159.meteorclient.utils.player.PlayerUtils;
import minegame159.meteorclient.utils.player.RotationUtils;
import net.minecraft.block.Blocks;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.Items;

public class AntiAutoAnvil extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
            .name("rotate")
            .displayName(I18n.translate("Modules.AntiAutoAnvil.setting.rotate.displayName"))
            .description(I18n.translate("Modules.AntiAutoAnvil.setting.rotate.description"))
            .defaultValue(true)
            .build()
    );

    public AntiAutoAnvil(){
        super(Category.Combat, "anti-auto-anvil", I18n.translate("Modules.AntiAutoAnvil.description"));
    }

    @EventHandler
    private final Listener<TickEvent.Pre> onTick = new Listener<>(event -> {
        assert mc.interactionManager != null;
        assert mc.world != null;
        assert mc.player != null;
        for(int i = 2; i <= mc.interactionManager.getReachDistance() + 2; i++){
            if (mc.world.getBlockState(mc.player.getBlockPos().add(0, i, 0)).getBlock() == Blocks.ANVIL
                    && mc.world.getBlockState(mc.player.getBlockPos().add(0, i - 1, 0)).isAir()){
                int slot = InvUtils.findItemWithCount(Items.OBSIDIAN).slot;
                if (rotate.get()) RotationUtils.packetRotate(mc.player.yaw, -90);
                if (slot != 1 && slot < 9) {
                    PlayerUtils.placeBlock(mc.player.getBlockPos().add(0, i - 2, 0), slot, InvUtils.getHand(Items.OBSIDIAN));
                } else if (mc.player.getOffHandStack().getItem() == Items.OBSIDIAN){
                    PlayerUtils.placeBlock(mc.player.getBlockPos().add(0, i - 2, 0),  InvUtils.getHand(Items.OBSIDIAN));
                }
            }
        }
    });
}
