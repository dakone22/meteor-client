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
import minegame159.meteorclient.modules.ModuleManager;
import minegame159.meteorclient.modules.combat.AutoTotem;
import minegame159.meteorclient.settings.*;
import minegame159.meteorclient.utils.player.ChatUtils;
import minegame159.meteorclient.utils.player.InvUtils;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.List;

@InvUtils.Priority(priority = 1)
public class AutoReplenish extends Module {

    public AutoReplenish(){
        super(Category.Player, "auto-replenish", I18n.translate("Module.AutoReplenish.description"));
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> amount = sgGeneral.add(new IntSetting.Builder()
            .name("amount")
            .displayName(I18n.translate("Module.AutoReplenish.setting.amount.displayName"))
            .description(I18n.translate("Module.AutoReplenish.setting.amount.description"))
            .defaultValue(8)
            .min(1)
            .sliderMax(63)
            .build()
    );

    private final Setting<Boolean> offhand = sgGeneral.add(new BoolSetting.Builder()
            .name("offhand")
            .displayName(I18n.translate("Module.AutoReplenish.setting.offhand.displayName"))
            .description(I18n.translate("Module.AutoReplenish.setting.offhand.description"))
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> alert = sgGeneral.add(new BoolSetting.Builder()
            .name("alert")
            .displayName(I18n.translate("Module.AutoReplenish.setting.alert.displayName"))
            .description(I18n.translate("Module.AutoReplenish.setting.alert.description"))
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> unstackable = sgGeneral.add(new BoolSetting.Builder()
            .name("unstackable")
            .displayName(I18n.translate("Module.AutoReplenish.setting.unstackable.displayName"))
            .description(I18n.translate("Module.AutoReplenish.setting.unstackable.description"))
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> searchHotbar = sgGeneral.add(new BoolSetting.Builder()
            .name("search-hotbar")
            .displayName(I18n.translate("Module.AutoReplenish.setting.searchHotbar.displayName"))
            .description(I18n.translate("Module.AutoReplenish.setting.searchHotbar.description"))
            .defaultValue(true)
            .build()
    );

    private final Setting<List<Item>> excludedItems = sgGeneral.add(new ItemListSetting.Builder()
            .name("excluded-items")
            .displayName(I18n.translate("Module.AutoReplenish.setting.excludedItems.displayName"))
            .description(I18n.translate("Module.AutoReplenish.setting.excludedItems.description"))
            .defaultValue(new ArrayList<>())
            .build()
    );

    private final Setting<Boolean> pauseInInventory = sgGeneral.add(new BoolSetting.Builder()
            .name("pause-in-inventory")
            .displayName(I18n.translate("Module.AutoReplenish.setting.pauseInInventory.displayName"))
            .description(I18n.translate("Module.AutoReplenish.setting.pauseInInventory.description"))
            .defaultValue(false)
            .build()
    );

    private final List<ItemStack> hotbar = new ArrayList<>();
    private ItemStack offhandStack;
    private ItemStack stack;
    private boolean sent = false;

    @Override
    public void onActivate() {
        offhandStack = mc.player.getOffHandStack();
    }

    @EventHandler
    private final Listener<TickEvent.Post> onTick = new Listener<>(event -> {
        if (mc.currentScreen instanceof GenericContainerScreen) sent = false;
        if (mc.player.currentScreenHandler.getStacks().size() < 45 || (pauseInInventory.get() && mc.currentScreen instanceof InventoryScreen)) return;
        //Hotbar
        for (int i = 0; i < 9; i++){
            stack = mc.player.inventory.getStack(i);
            if (!excludedItems.get().contains(stack.getItem()) && stack.getItem() != Items.AIR) {
                if (stack.isStackable()){
                    if (stack.getCount() <= amount.get()){
                        addSlots(i, findItem(stack, i));
                    }
                } else if (unstackable.get()) {
                    if (stack.isEmpty() && !hotbar.get(i).isStackable()){
                        addSlots(i, findItem(hotbar.get(i), i));
                    }
                }
            }
            hotbar.add(i, stack);
        }
        //Offhand
        if (offhand.get() && !ModuleManager.INSTANCE.get(AutoTotem.class).getLocked()){
            if (mc.player.getOffHandStack().getCount() <= amount.get()){
                addSlots(InvUtils.OFFHAND_SLOT, findItem(mc.player.getOffHandStack(), InvUtils.OFFHAND_SLOT));
            } else if (mc.player.getOffHandStack().isEmpty() || !offhandStack.isStackable()){
                addSlots(InvUtils.OFFHAND_SLOT, findItem(offhandStack, InvUtils.OFFHAND_SLOT));
            }
            offhandStack = mc.player.getOffHandStack();
        }
    });

    private int findItem(ItemStack itemStack, int excludedSlot){
        int slot = -1;
        int size = 0;
        for (int i = mc.player.inventory.size() - 2; i >= (searchHotbar.get() ? 0 : 9); i--){
            if (i != excludedSlot && mc.player.inventory.getStack(i).getItem().equals(itemStack.getItem()) && ItemStack.areTagsEqual(itemStack, mc.player.inventory.getStack(i))){
                if (mc.player.inventory.getStack(i).getCount() > size){
                    slot = i;
                    size = mc.player.inventory.getStack(i).getCount();
                }
            }
        }
        return slot;
    }

    private void addSlots(int to, int from){
        if (to == -1) {
            return;
        } else if (from == -1){
            if (alert.get() && !sent) {
                ChatUtils.moduleWarning(this, I18n.translate("Module.AutoReplenish.message.no_items"));
                sent = true;
            }
            return;
        }
        List<Integer> slots = new ArrayList<>();
        if (!mc.player.inventory.getCursorStack().isEmpty() && mc.player.inventory.getCursorStack().getItem().equals(mc.player.inventory.getStack(from).getItem())){
            slots.add(InvUtils.invIndexToSlotId(from));
        }
        slots.add(InvUtils.invIndexToSlotId(from));
        slots.add(InvUtils.invIndexToSlotId(to));
        slots.add(InvUtils.invIndexToSlotId(from));
        InvUtils.addSlots(slots, this.getClass());
    }
}
