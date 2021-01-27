/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.modules.combat;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.events.world.TickEvent;
import minegame159.meteorclient.mixininterface.IKeyBinding;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Module;
import minegame159.meteorclient.settings.BoolSetting;
import minegame159.meteorclient.settings.IntSetting;
import minegame159.meteorclient.settings.Setting;
import minegame159.meteorclient.settings.SettingGroup;
import minegame159.meteorclient.utils.player.ChatUtils;
import minegame159.meteorclient.utils.player.InvUtils;
import minegame159.meteorclient.utils.player.RotationUtils;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.screen.slot.SlotActionType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Quiver extends Module {

    public enum ArrowType {
        Strength,
        Speed
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> charge = sgGeneral.add(new IntSetting.Builder()
            .name("charge-delay")
            .displayName(I18n.translate("Modules.Quiver.setting.charge.displayName"))
            .description(I18n.translate("Modules.Quiver.setting.charge.description"))
            .defaultValue(6)
            .min(5)
            .max(20)
            .sliderMin(5)
            .sliderMax(20)
            .build()
    );

    private final Setting<Boolean> checkEffects = sgGeneral.add(new BoolSetting.Builder()
            .name("check-effects")
            .displayName(I18n.translate("Modules.Quiver.setting.checkEffects.displayName"))
            .description(I18n.translate("Modules.Quiver.setting.checkEffects.description"))
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> chatInfo = sgGeneral.add(new BoolSetting.Builder()
            .name("chat-info")
            .displayName(I18n.translate("Modules.Quiver.setting.chatInfo.displayName"))
            .description(I18n.translate("Modules.Quiver.setting.chatInfo.description"))
            .defaultValue(true)
            .build()
    );

    public Quiver() {
        super(Category.Combat, "quiver", I18n.translate("Modules.Quiver.description"));
    }

    private boolean shooting;

    private ArrowType shootingArrow;

    private int prevSlot;

    private int strengthSlot;
    private int speedSlot;

    private boolean foundStrength;
    private boolean foundSpeed;

    private boolean shotStrength;
    private boolean shotSpeed;

    private boolean shouldShoot;

    @Override
    public void onActivate() {
        shooting = false;
        int arrowsToShoot = 0;
        prevSlot = mc.player.inventory.selectedSlot;

        shotStrength = false;
        shotSpeed = false;

        foundStrength = false;
        foundSpeed = false;

        shootingArrow = null;

        strengthSlot = -1;
        speedSlot = -1;

        int bowSlot = findBow();

        if (bowSlot == -1) {
            if (chatInfo.get()) ChatUtils.moduleError(this, "No bow found... disabling.");
            toggle();
            return;
        } else mc.player.inventory.selectedSlot = bowSlot;

        for (Map.Entry<ArrowType, Integer> slot : getAllArrows().entrySet()) {
            if (slot.getKey() == ArrowType.Strength && !foundStrength) {
                strengthSlot = slot.getValue();
                foundStrength = true;
            }
            if (slot.getKey() == ArrowType.Speed && !foundSpeed) {
                speedSlot = slot.getValue();
                foundSpeed = true;
            }
        }

        if (strengthSlot != -1) arrowsToShoot++;
        if (speedSlot != -1) arrowsToShoot++;

        if (arrowsToShoot == 0) {
            if (chatInfo.get()) ChatUtils.moduleError(this, "No appropriate arrows found... disabling.");
            toggle();
            return;
        }

        shouldShoot = true;

        if (!foundSpeed) shotSpeed = true;
        if (!foundStrength) shotStrength = true;
    }

    @Override
    public void onDeactivate() {
        mc.player.inventory.selectedSlot = prevSlot;
    }

    @EventHandler
    private final Listener<TickEvent.Post> onTick = new Listener<>(event -> {

        RotationUtils.packetRotate(mc.player.yaw, -90);

        boolean canStop = false;

        if (shooting && mc.player.getItemUseTime() >= charge.get()) {
            if (shootingArrow == ArrowType.Strength) endShooting(strengthSlot);
            if (shootingArrow == ArrowType.Speed) endShooting(speedSlot);
            canStop = true;
        }

        if (shotStrength && shotSpeed && canStop) {
            if (chatInfo.get()) ChatUtils.moduleInfo(this, "Quiver complete... disabling.");
            toggle();
            return;
        }

        if (shouldShoot) {
            if (!shooting && !shotStrength && foundStrength) {
                shoot(strengthSlot);
                shootingArrow = ArrowType.Strength;
                if (chatInfo.get()) ChatUtils.moduleInfo(this, "Quivering a strength arrow.");
                shotStrength = true;
            }

            if (!shooting && !shotSpeed && foundSpeed && shotStrength) {
                shoot(speedSlot);
                shootingArrow = ArrowType.Speed;
                if (chatInfo.get()) ChatUtils.moduleInfo(this, "Quivering a speed arrow.");
                shotSpeed = true;
            }

        }
    });

    private void shoot(int moveSlot) {
        if (moveSlot != 9) moveItems(moveSlot, 9);
        setPressed(true);
        shooting = true;
    }

    private void endShooting(int moveSlot) {
        setPressed(false);
        mc.player.stopUsingItem();
        mc.interactionManager.stopUsingItem(mc.player);
        if (moveSlot != 9) moveItems(9, moveSlot);
        shooting = false;
    }

    private Map<ArrowType, Integer> getAllArrows() {

        Map<ArrowType, Integer> arrowSlotMap = new HashMap<>();

        boolean hasStrength = mc.player.getActiveStatusEffects().containsKey(StatusEffects.STRENGTH);
        boolean hasSpeed = mc.player.getActiveStatusEffects().containsKey(StatusEffects.SPEED);

        for (int i = 35; i >= 0; i--) {
            if (mc.player.inventory.getStack(i).getItem() != Items.TIPPED_ARROW || i == mc.player.inventory.selectedSlot) continue;

            if (checkEffects.get()) {
                if (isType("effect.minecraft.strength", i) && !hasStrength)  arrowSlotMap.put(ArrowType.Strength, i);
                else if (isType("effect.minecraft.speed", i) && !hasSpeed) arrowSlotMap.put(ArrowType.Speed, i);
            } else {
                if (isType("effect.minecraft.strength", i)) arrowSlotMap.put(ArrowType.Strength, i);
                else if (isType("effect.minecraft.speed", i)) arrowSlotMap.put(ArrowType.Speed, i);
            }
        }

        return arrowSlotMap;
    }

    private boolean isType(String type, int slot) {
        assert mc.player != null;
        ItemStack stack = mc.player.inventory.getStack(slot);
        if (stack.getItem() == Items.TIPPED_ARROW) {
            List<StatusEffectInstance> effects = PotionUtil.getPotion(stack).getEffects();
            if (effects.size() > 0) {
                StatusEffectInstance effect = effects.get(0);
                return effect.getTranslationKey().equals(type);
            }
        }
        return false;
    }

    private void setPressed(boolean pressed) {
        ((IKeyBinding) mc.options.keyUse).setPressed(pressed);
    }

    private void moveItems(int from, int to) {
        InvUtils.clickSlot(InvUtils.invIndexToSlotId(from), 0, SlotActionType.PICKUP);
        InvUtils.clickSlot(InvUtils.invIndexToSlotId(to), 0, SlotActionType.PICKUP);
        InvUtils.clickSlot(InvUtils.invIndexToSlotId(from), 0, SlotActionType.PICKUP);
    }

    private int findBow() {
        int slot = -1;
        assert mc.player != null;

        for (int i = 0; i < 9; i++) if (mc.player.inventory.getStack(i).getItem() == Items.BOW) slot = i;

        return slot;
    }
}
