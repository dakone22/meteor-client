/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.modules.player;

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
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class EXPThrower extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> lookDown = sgGeneral.add(new BoolSetting.Builder()
            .name("look-down")
            .displayName(I18n.translate("Modules.EXPThrower.setting.lookDown.displayName"))
            .description(I18n.translate("Modules.EXPThrower.setting.lookDown.description"))
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> autoToggle = sgGeneral.add(new BoolSetting.Builder()
            .name("auto-toggle")
            .displayName(I18n.translate("Modules.EXPThrower.setting.autoToggle.displayName"))
            .description(I18n.translate("Modules.EXPThrower.setting.autoToggle.description"))
            .defaultValue(true)
            .build()
    );

    public EXPThrower() {
        super(Category.Player, "exp-thrower", I18n.translate("Modules.EXPThrower.description"));
    }

    @EventHandler
    private final Listener<TickEvent.Post> onTick = new Listener<>(event -> {

        if(autoToggle.get()) {
            int count = 0;
            int set = 0;

            for(int i = 0; i < 4; i++) {
                if(!mc.player.inventory.armor.get(i).isEmpty() && EnchantmentHelper.getLevel(Enchantments.MENDING, mc.player.inventory.getArmorStack(i)) == 1) set++;
                if(!mc.player.inventory.armor.get(i).isDamaged()) count++;
            }
            if(count == set && set != 0) {
                toggle();
                return;
            }
        }

        int slot = -1;

        for (int i = 0; i < 9; i++) {
            if (mc.player.inventory.getStack(i).getItem() == Items.EXPERIENCE_BOTTLE) {
                slot = i;
                break;
            }
        }

        if (slot != -1) {
            if (lookDown.get()) RotationUtils.packetRotate(mc.player.yaw, 90);
            int preSelectedSlot = mc.player.inventory.selectedSlot;
            mc.player.inventory.selectedSlot = slot;
            mc.interactionManager.interactItem(mc.player, mc.world, Hand.MAIN_HAND);
            mc.player.inventory.selectedSlot = preSelectedSlot;
        }
    });
}
