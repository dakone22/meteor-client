/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.modules.player;

import minegame159.meteorclient.Config;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Module;
import minegame159.meteorclient.settings.BoolSetting;
import minegame159.meteorclient.settings.EnumSetting;
import minegame159.meteorclient.settings.Setting;
import minegame159.meteorclient.settings.SettingGroup;
import minegame159.meteorclient.utils.player.ChatUtils;
import minegame159.meteorclient.utils.player.InvUtils;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.List;

@InvUtils.Priority(priority = 0)
public class ChestSwap extends Module {
    public enum Chestplate {
        Diamond,
        Netherite,
        PreferDiamond,
        PreferNetherite
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Chestplate> chestplate = sgGeneral.add(new EnumSetting.Builder<Chestplate>()
            .name("chestplate")
            .displayName(I18n.translate("Modules.ChestSwap.setting.chestplate.displayName"))
            .description(I18n.translate("Modules.ChestSwap.setting.chestplate.description"))
            .defaultValue(Chestplate.PreferNetherite)
            .build()
    );

    private final Setting<Boolean> stayOn = sgGeneral.add(new BoolSetting.Builder()
            .name("stay-on")
            .displayName(I18n.translate("Modules.ChestSwap.setting.stayOn.displayName"))
            .description(I18n.translate("Modules.ChestSwap.setting.stayOn.description"))
            .defaultValue(false)
            .build()
    );

    public ChestSwap() {
        super(Category.Player, "chest-swap", I18n.translate("Modules.ChestSwap.description"));
    }

    @Override
    public void onActivate() {
        swap();
        if (!stayOn.get()) toggle();
    }

    @Override
    public void onDeactivate() {
        if (stayOn.get()) swap();
    }

    public void swap() {
        Item currentItem = mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem();

        if (currentItem == Items.ELYTRA) {
            equipChestplate();
        } else if (currentItem instanceof ArmorItem && ((ArmorItem) currentItem).getSlotType() == EquipmentSlot.CHEST) {
            equipElytra();
        } else {
            if (!equipChestplate()) equipElytra();
        }
    }

    private boolean equipChestplate() {
        int bestSlot = -1;
        boolean breakLoop = false;

        for (int i = 0; i < mc.player.inventory.main.size(); i++) {
            Item item = mc.player.inventory.main.get(i).getItem();

            switch (chestplate.get()) {
                case Diamond:
                    if (item == Items.DIAMOND_CHESTPLATE) {
                        bestSlot = i;
                        breakLoop = true;
                    }
                    break;
                case Netherite:
                    if (item == Items.NETHERITE_CHESTPLATE) {
                        bestSlot = i;
                        breakLoop = true;
                    }
                    break;
                case PreferDiamond:
                    if (item == Items.DIAMOND_CHESTPLATE) {
                        bestSlot = i;
                        breakLoop = true;
                    } else if (item == Items.NETHERITE_CHESTPLATE) {
                        bestSlot = i;
                    }
                    break;
                case PreferNetherite:
                    if (item == Items.DIAMOND_CHESTPLATE) {
                        bestSlot = i;
                    } else if (item == Items.NETHERITE_CHESTPLATE) {
                        bestSlot = i;
                        breakLoop = true;
                    }
                    break;
            }

            if (breakLoop) break;
        }

        if (bestSlot != -1) equip(bestSlot);
        return bestSlot != -1;
    }

    private void equipElytra() {
        for (int i = 0; i < mc.player.inventory.main.size(); i++) {
            Item item = mc.player.inventory.main.get(i).getItem();

            if (item == Items.ELYTRA) {
                equip(i);
                break;
            }
        }
    }

    private void equip(int slot) {
        int chestSlot = 8 - 2;
        slot = InvUtils.invIndexToSlotId(slot);

        List<Integer> slots = new ArrayList<>();
        slots.add(InvUtils.invIndexToSlotId(slot));
        slots.add(chestSlot);
        slots.add(InvUtils.invIndexToSlotId(slot));
        InvUtils.addSlots(slots, this.getClass());
    }

    @Override
    public void sendToggledMsg() {
        if (stayOn.get()) super.sendToggledMsg();
        else if (Config.INSTANCE.chatCommandsInfo) ChatUtils.moduleInfo(this, "Triggered (highlight)%s(default).", title);
    }
}
