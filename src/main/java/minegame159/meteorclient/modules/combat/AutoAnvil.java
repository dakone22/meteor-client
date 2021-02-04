/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.modules.combat;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.events.game.OpenScreenEvent;
import minegame159.meteorclient.events.world.TickEvent;
import minegame159.meteorclient.friends.FriendManager;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Module;
import minegame159.meteorclient.modules.player.FakePlayer;
import minegame159.meteorclient.settings.*;
import minegame159.meteorclient.utils.entity.FakePlayerEntity;
import minegame159.meteorclient.utils.player.ChatUtils;
import minegame159.meteorclient.utils.player.PlayerUtils;
import minegame159.meteorclient.utils.player.RotationUtils;
import net.minecraft.block.AbstractButtonBlock;
import net.minecraft.block.AbstractPressurePlateBlock;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.Block;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

// Created by Eureka

public class AutoAnvil extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgPlace = settings.createGroup(I18n.translate("Module.AutoAnvil.group.sgPlace"));

    // General

    private final Setting<Boolean> toggleOnBreak = sgGeneral.add(new BoolSetting.Builder()
            .name("toggle-on-break")
            .displayName(I18n.translate("Module.AutoAnvil.setting.toggleOnBreak.displayName"))
            .description(I18n.translate("Module.AutoAnvil.setting.toggleOnBreak.description"))
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
            .name("rotate")
            .displayName(I18n.translate("Module.AutoAnvil.setting.rotate.displayName"))
            .description(I18n.translate("Module.AutoAnvil.setting.rotate.description"))
            .defaultValue(true)
            .build()
    );

    // Place

    private final Setting<Double> range = sgPlace.add(new DoubleSetting.Builder()
            .name("range")
            .displayName(I18n.translate("Module.AutoAnvil.setting.range.displayName"))
            .description(I18n.translate("Module.AutoAnvil.setting.range.description"))
            .defaultValue(4)
            .min(0)
            .build()
    );

    private final Setting<Integer> delay = sgPlace.add(new IntSetting.Builder()
            .name("delay")
            .displayName(I18n.translate("Module.AutoAnvil.setting.delay.displayName"))
            .description(I18n.translate("Module.AutoAnvil.setting.delay.description"))
            .min(0)
            .defaultValue(0)
            .sliderMax(50)
            .build()
    );

    private final Setting<Integer> height = sgPlace.add(new IntSetting.Builder()
            .name("height")
            .displayName(I18n.translate("Module.AutoAnvil.setting.height.displayName"))
            .description(I18n.translate("Module.AutoAnvil.setting.height.description"))
            .defaultValue(5)
            .min(0)
            .max(10)
            .sliderMin(0)
            .sliderMax(10)
            .build()
    );

    private final Setting<Boolean> placeButton = sgPlace.add(new BoolSetting.Builder()
            .name("place-at-feet")
            .displayName(I18n.translate("Module.AutoAnvil.setting.placeButton.displayName"))
            .description(I18n.translate("Module.AutoAnvil.setting.placeButton.description"))
            .defaultValue(true)
            .build()
    );

    public AutoAnvil() {
        super(Category.Combat, "auto-anvil", I18n.translate("Module.AutoAnvil.description"));
    }

    private PlayerEntity target;
    private int timer;

    @Override
    public void onActivate() {
        timer = 0;
        target = null;
    }

    @EventHandler
    private final Listener<OpenScreenEvent> onOpenScreen = new Listener<>(event -> {
        if (event.screen instanceof AnvilScreen) mc.player.closeScreen();
    });

    @EventHandler
    private final Listener<TickEvent.Post> onTick = new Listener<>(event -> {

        if (isActive() && toggleOnBreak.get() && target != null && target.inventory.getArmorStack(3).isEmpty()) {
            ChatUtils.moduleError(this, I18n.translate("Module.AutoAnvil.message.head_slot_empty"));
            toggle();
            return;
        }

        if (target != null && (mc.player.distanceTo(target) > range.get() || !target.isAlive())) target = null;

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player || !FriendManager.INSTANCE.attack(player) || !player.isAlive() || mc.player.distanceTo(player) > range.get()) continue;

            if (target == null) target = player;
            else if (mc.player.distanceTo(target) > mc.player.distanceTo(player)) target = player;
        }

        if (target == null) {
            for (FakePlayerEntity player : FakePlayer.players.keySet()) {
                if (!FriendManager.INSTANCE.attack(player) || !player.isAlive() || mc.player.distanceTo(player) > range.get()) continue;

                if (target == null) target = player;
                else if (mc.player.distanceTo(target) > mc.player.distanceTo(player)) target = player;
            }
        }

        if (timer >= delay.get() && target != null) {

            timer = 0;

            int prevSlot = mc.player.inventory.selectedSlot;

            if (getAnvilSlot() == -1) return;

            if (placeButton.get()) {

                if (getFloorSlot() == -1) return;
                mc.player.inventory.selectedSlot = getFloorSlot();

                if (mc.world.getBlockState(target.getBlockPos()).isAir()) mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND, new BlockHitResult(mc.player.getPos(), Direction.UP, target.getBlockPos(), true));
            }

            mc.player.inventory.selectedSlot = getAnvilSlot();

            BlockPos placePos = target.getBlockPos().up().add(0, height.get(), 0);

            if (rotate.get()) RotationUtils.packetRotate(placePos);

            PlayerUtils.placeBlock(placePos, Hand.MAIN_HAND);

            mc.player.inventory.selectedSlot = prevSlot;
        } else timer++;
    });

    public int getFloorSlot() {
        int slot = -1;
        for (int i = 0; i < 9; i++) {
            Item item = mc.player.inventory.getStack(i).getItem();
            Block block = Block.getBlockFromItem(item);

            if (block instanceof AbstractPressurePlateBlock || block instanceof AbstractButtonBlock) {
                slot = i;
                break;
            }
        }
        return slot;
    }

    private int getAnvilSlot() {
        int slot = -1;
        for (int i = 0; i < 9; i++) {
            Item item = mc.player.inventory.getStack(i).getItem();
            Block block = Block.getBlockFromItem(item);

            if (block instanceof AnvilBlock) {
                slot = i;
                break;
            }
        }
        return slot;
    }

    @Override
    public String getInfoString() {
        if (target != null && target instanceof PlayerEntity) return target.getEntityName();
        if (target != null) return target.getType().getName().getString();
        return null;
    }
}
