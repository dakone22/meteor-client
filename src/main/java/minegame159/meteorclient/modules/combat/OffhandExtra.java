/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.modules.combat;

//Created by squidoodly 25/04/2020

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.events.entity.player.RightClickEvent;
import minegame159.meteorclient.events.world.TickEvent;
import minegame159.meteorclient.gui.WidgetScreen;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Module;
import minegame159.meteorclient.modules.ModuleManager;
import minegame159.meteorclient.settings.*;
import minegame159.meteorclient.utils.player.ChatUtils;
import minegame159.meteorclient.utils.player.InvUtils;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.*;

import java.util.ArrayList;
import java.util.List;

@InvUtils.Priority(priority = 1)
public class OffhandExtra extends Module {
    public enum Mode{
        EGap,
        Gap,
        EXP,
        Crystal,
    }
    
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgExtra = settings.createGroup(I18n.translate("Module.OffhandExtra.group.sgExtra"));

    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
            .name("mode")
            .displayName(I18n.translate("Module.OffhandExtra.setting.mode.displayName"))
            .description(I18n.translate("Module.OffhandExtra.setting.mode.description"))
            .displayValues(new String[]{
                    I18n.translate("Module.OffhandExtra.enum.Mode.EGap"),
                    I18n.translate("Module.OffhandExtra.enum.Mode.Gap"),
                    I18n.translate("Module.OffhandExtra.enum.Mode.EXP"),
                    I18n.translate("Module.OffhandExtra.enum.Mode.Crystal"),
            })
            .defaultValue(Mode.EGap)
            .onChanged(mode -> currentMode = mode)
            .build()
    );

    private final Setting<Boolean> replace = sgGeneral.add(new BoolSetting.Builder()
            .name("replace")
            .displayName(I18n.translate("Module.OffhandExtra.setting.replace.displayName"))
            .description(I18n.translate("Module.OffhandExtra.setting.replace.description"))
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> asimov = sgGeneral.add(new BoolSetting.Builder()
            .name("asimov")
            .displayName(I18n.translate("Module.OffhandExtra.setting.asimov.displayName"))
            .description(I18n.translate("Module.OffhandExtra.setting.asimov.description"))
            .defaultValue(false)
            .build()
    );

    private final Setting<Integer> health = sgGeneral.add(new IntSetting.Builder()
            .name("health")
            .displayName(I18n.translate("Module.OffhandExtra.setting.health.displayName"))
            .description(I18n.translate("Module.OffhandExtra.setting.health.description"))
            .defaultValue(10)
            .min(0)
            .sliderMax(20)
            .build()
    );

    private final Setting<Boolean> selfToggle = sgGeneral.add(new BoolSetting.Builder()
            .name("self-toggle")
            .displayName(I18n.translate("Module.OffhandExtra.setting.selfToggle.displayName"))
            .description(I18n.translate("Module.OffhandExtra.setting.selfToggle.description"))
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> hotBar = sgGeneral.add(new BoolSetting.Builder()
            .name("search-hotbar")
            .displayName(I18n.translate("Module.OffhandExtra.setting.hotBar.displayName"))
            .description(I18n.translate("Module.OffhandExtra.setting.hotBar.description"))
            .defaultValue(false)
            .build()
    );

    // Extras

    private final Setting<Boolean> sword = sgExtra.add(new BoolSetting.Builder()
            .name("sword-gap")
            .displayName(I18n.translate("Module.OffhandExtra.setting.sword.displayName"))
            .description(I18n.translate("Module.OffhandExtra.setting.sword.description"))
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> offhandCrystal = sgExtra.add(new BoolSetting.Builder()
            .name("offhand-crystal-on-gap")
            .displayName(I18n.translate("Module.OffhandExtra.setting.offhandCrystal.displayName"))
            .description(I18n.translate("Module.OffhandExtra.setting.offhandCrystal.description"))
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> offhandCA = sgExtra.add(new BoolSetting.Builder()
            .name("offhand-crystal-on-ca")
            .displayName(I18n.translate("Module.OffhandExtra.setting.offhandCA.displayName"))
            .description(I18n.translate("Module.OffhandExtra.setting.offhandCA.description"))
            .defaultValue(false)
            .build()
    );

    public OffhandExtra() {
        super(Category.Combat, "offhand-extra", I18n.translate("Module.OffhandExtra.description"));
    }

    private boolean isClicking = false;
    private boolean sentMessage = false;
    private boolean noTotems = false;
    private Mode currentMode = mode.get();

    @Override
    public void onActivate() {
        currentMode = mode.get();
    }

    @Override
    public void onDeactivate() {
        assert mc.player != null;
        if (ModuleManager.INSTANCE.get(AutoTotem.class).isActive() && mc.player.getOffHandStack().getItem() != Items.TOTEM_OF_UNDYING) {
            InvUtils.FindItemResult result = InvUtils.findItemWithCount(Items.TOTEM_OF_UNDYING);
            if (result.slot != -1) {
                doMove(result.slot);
            }
        }
    }

    @EventHandler
    private final Listener<TickEvent.Post> onTick = new Listener<>(event -> {
        assert mc.player != null;
        currentMode = mode.get();

        if (mc.currentScreen != null && ((!(mc.currentScreen instanceof InventoryScreen) && !(mc.currentScreen instanceof WidgetScreen)) || !asimov.get())) return;
        if (!mc.player.isUsingItem()) isClicking = false;
        if (ModuleManager.INSTANCE.get(AutoTotem.class).getLocked()) return;

        if ((mc.player.getMainHandStack().getItem() instanceof SwordItem || mc.player.getMainHandStack().getItem() instanceof AxeItem) && sword.get()) currentMode = Mode.EGap;
        else if (mc.player.getMainHandStack().getItem() instanceof EnchantedGoldenAppleItem && offhandCrystal.get()) currentMode = Mode.Crystal;
        else if (ModuleManager.INSTANCE.isActive(CrystalAura.class) && offhandCA.get()) currentMode = Mode.Crystal;

        if ((asimov.get() || noTotems) && mc.player.getOffHandStack().getItem() != getItem()) {
            int result = findSlot(getItem());
            if (result == -1 && mc.player.getOffHandStack().getItem() != getItem()) {
                if (currentMode != mode.get()){
                    currentMode = mode.get();
                    if (mc.player.getOffHandStack().getItem() != getItem()) {
                        result = findSlot(getItem());
                        if (result != -1) {
                            doMove(result);
                            return;
                        }
                    }
                }
                if (!sentMessage) {
                    ChatUtils.moduleWarning(this, I18n.translate("Module.OffhandExtra.message.no_item"));
                    sentMessage = true;
                }
                if (selfToggle.get()) this.toggle();
                return;
            }
            if (mc.player.getOffHandStack().getItem() != getItem() && replace.get()) {
                doMove(result);
                sentMessage = false;
            }
        } else if (!asimov.get() && !isClicking && mc.player.getOffHandStack().getItem() != Items.TOTEM_OF_UNDYING) {
            int result = findSlot(Items.TOTEM_OF_UNDYING);
            if (result != -1) {
                doMove(result);
            }

        }
    });

    @EventHandler
    private final Listener<RightClickEvent> onRightClick = new Listener<>(event -> {
        assert mc.player != null;
        if (mc.currentScreen != null) return;
        if (ModuleManager.INSTANCE.get(AutoTotem.class).getLocked() || !canMove()) return;
        if ((mc.player.getOffHandStack().getItem() != Items.TOTEM_OF_UNDYING || (mc.player.getHealth() + mc.player.getAbsorptionAmount() > health.get())
               && (mc.player.getOffHandStack().getItem() != getItem()) && !(mc.currentScreen instanceof HandledScreen<?>))) {
            if (mc.player.getMainHandStack().getItem() instanceof SwordItem && sword.get()) currentMode = Mode.EGap;
            else if (mc.player.getMainHandStack().getItem() instanceof EnchantedGoldenAppleItem && offhandCrystal.get()) currentMode = Mode.Crystal;
            else if (ModuleManager.INSTANCE.isActive(CrystalAura.class) && offhandCA.get()) currentMode = Mode.Crystal;
            if (mc.player.getOffHandStack().getItem() == getItem()) return;
            isClicking = true;
            Item item = getItem();
            int result = findSlot(item);
            if (result == -1 && mc.player.getOffHandStack().getItem() != getItem()) {
                if (!sentMessage) {
                    ChatUtils.moduleWarning(this, I18n.translate("Module.OffhandExtra.message.no_item"));
                    sentMessage = true;
                }
                if (selfToggle.get()) this.toggle();
                return;
            }
            if (mc.player.getOffHandStack().getItem() != item && mc.player.getMainHandStack().getItem() != item && replace.get()) {
                doMove(result);
                sentMessage = false;
            }
            currentMode = mode.get();
        }
    });

    private Item getItem(){
        Item item = Items.TOTEM_OF_UNDYING;
        if (currentMode == Mode.EGap) {
            item = Items.ENCHANTED_GOLDEN_APPLE;
        } else if (currentMode == Mode.Gap) {
            item = Items.GOLDEN_APPLE;
        } else if (currentMode == Mode.Crystal) {
            item = Items.END_CRYSTAL;
        } else if (currentMode == Mode.EXP) {
            item = Items.EXPERIENCE_BOTTLE;
        }
        return item;
    }

    public void setTotems(boolean set) {
        noTotems = set;
    }

    private boolean canMove(){
        assert mc.player != null;
        return mc.player.getMainHandStack().getItem() != Items.BOW
                && mc.player.getMainHandStack().getItem() != Items.TRIDENT
                && mc.player.getMainHandStack().getItem() != Items.CROSSBOW;
    }

    private void doMove(int slot){
        assert mc.player != null;
        boolean empty = mc.player.getOffHandStack().isEmpty();
        List<Integer> slots = new ArrayList<>();
        if(mc.player.inventory.getCursorStack().getItem() != Items.TOTEM_OF_UNDYING) {
            slots.add(InvUtils.invIndexToSlotId(slot));
        }
        slots.add(InvUtils.invIndexToSlotId(InvUtils.OFFHAND_SLOT));
        if (!empty) slots.add(InvUtils.invIndexToSlotId(slot));
        InvUtils.addSlots(slots, this.getClass());
    }

    private int findSlot(Item item){
        assert mc.player != null;
        for (int i = 9; i < mc.player.inventory.size(); i++){
            if (mc.player.inventory.getStack(i).getItem() == item){
                return i;
            }
        }
        if (hotBar.get()){
            return InvUtils.findItemWithCount(item).slot;
        }
        return -1;
    }

    @Override
    public String getInfoString() {
        return mode.get().name();
    }
}
