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
import minegame159.meteorclient.utils.player.PlayerUtils;
import minegame159.meteorclient.utils.player.RotationUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

public class Surround extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> doubleHeight = sgGeneral.add(new BoolSetting.Builder()
            .name("double-height")
            .displayName(I18n.translate("Module.Surround.setting.doubleHeight.displayName"))
            .description(I18n.translate("Module.Surround.setting.doubleHeight.description"))
            .defaultValue(false)
            .build()
    );
    
    private final Setting<Boolean> onlyOnGround = sgGeneral.add(new BoolSetting.Builder()
            .name("only-on-ground")
            .displayName(I18n.translate("Module.Surround.setting.onlyOnGround.displayName"))
            .description(I18n.translate("Module.Surround.setting.onlyOnGround.description"))
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> onlyWhenSneaking = sgGeneral.add(new BoolSetting.Builder()
            .name("only-when-sneaking")
            .displayName(I18n.translate("Module.Surround.setting.onlyWhenSneaking.displayName"))
            .description(I18n.translate("Module.Surround.setting.onlyWhenSneaking.description"))
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> turnOff = sgGeneral.add(new BoolSetting.Builder()
            .name("turn-off")
            .displayName(I18n.translate("Module.Surround.setting.turnOff.displayName"))
            .description(I18n.translate("Module.Surround.setting.turnOff.description"))
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> center = sgGeneral.add(new BoolSetting.Builder()
            .name("center")
            .displayName(I18n.translate("Module.Surround.setting.center.displayName"))
            .description(I18n.translate("Module.Surround.setting.center.description"))
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> disableOnJump = sgGeneral.add(new BoolSetting.Builder()
            .name("disable-on-jump")
            .displayName(I18n.translate("Module.Surround.setting.disableOnJump.displayName"))
            .description(I18n.translate("Module.Surround.setting.disableOnJump.description"))
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
            .name("rotate")
            .displayName(I18n.translate("Module.Surround.setting.rotate.displayName"))
            .description(I18n.translate("Module.Surround.setting.rotate.description"))
            .defaultValue(true)
            .build()
    );

    // TODO: Make a render for Surround monkeys.
    private int prevSlot;
    private final BlockPos.Mutable blockPos = new BlockPos.Mutable();
    private boolean return_;

    public Surround() {
        super(Category.Combat, "surround", I18n.translate("Module.Surround.description"));
    }

    @Override
    public void onActivate() {
        if (center.get()) PlayerUtils.centerPlayer();
    }

    @EventHandler
    private final Listener<TickEvent.Post> onTick = new Listener<>(event -> {
        if (disableOnJump.get() && mc.options.keyJump.isPressed()) {
            toggle();
            return;
        }

        if (onlyOnGround.get() && !mc.player.isOnGround()) return;
        if (onlyWhenSneaking.get() && !mc.options.keySneak.isPressed()) return;

        // Place
        return_ = false;

        // Bottom
        boolean p1 = place(0, -1, 0);
        if (return_) return;

        // Sides
        boolean p2 = place(1, 0, 0);
        if (return_) return;
        boolean p3 = place(-1, 0, 0);
        if (return_) return;
        boolean p4 = place(0, 0, 1);
        if (return_) return;
        boolean p5 = place(0, 0, -1);
        if (return_) return;

        // Sides up
        boolean doubleHeightPlaced = false;
        if (doubleHeight.get()) {
            boolean p6 = place(1, 1, 0);
            if (return_) return;
            boolean p7 = place(-1, 1, 0);
            if (return_) return;
            boolean p8 = place(0, 1, 1);
            if (return_) return;
            boolean p9 = place(0, 1, -1);
            if (return_) return;

            if (p6 && p7 && p8 && p9) doubleHeightPlaced = true;
        }

        // Auto turn off
        if (turnOff.get() && p1 && p2 && p3 && p4 && p5) {
            if (doubleHeightPlaced || !doubleHeight.get()) toggle();
        }
    });

    private boolean place(int x, int y, int z) {
        setBlockPos(x, y, z);

        BlockState blockState = mc.world.getBlockState(blockPos);
        boolean wasObby = blockState.getBlock() == Blocks.OBSIDIAN;
        boolean placed = !blockState.getMaterial().isReplaceable();

        if (!placed && findSlot()) {
            if (rotate.get()) RotationUtils.packetRotate(blockPos);
            placed = PlayerUtils.placeBlock(blockPos, Hand.MAIN_HAND);
            resetSlot();

            boolean isObby = mc.world.getBlockState(blockPos).getBlock() == Blocks.OBSIDIAN;
            if (!wasObby && isObby) return_ = true;
        }

        return placed;
    }

    private void setBlockPos(int x, int y, int z) {
        blockPos.set(mc.player.getX() + x, mc.player.getY() + y, mc.player.getZ() + z);
    }

    private boolean findSlot() {
        prevSlot = mc.player.inventory.selectedSlot;

        for (int i = 0; i < 9; i++) {
            Item item = mc.player.inventory.getStack(i).getItem();

            if (!(item instanceof BlockItem)) continue;

            if (item == Items.OBSIDIAN) {
                mc.player.inventory.selectedSlot = i;
                return true;
            }
        }

        return false;
    }

    private void resetSlot() {
        mc.player.inventory.selectedSlot = prevSlot;
    }
}
