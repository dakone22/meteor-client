/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.modules.movement;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.events.world.TickEvent;
import minegame159.meteorclient.mixin.AbstractBlockAccessor;
import minegame159.meteorclient.mixininterface.IVec3d;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Module;
import minegame159.meteorclient.settings.*;
import minegame159.meteorclient.utils.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class Anchor extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> maxHeight = sgGeneral.add(new IntSetting.Builder()
            .name("max-height")
            .displayName(I18n.translate("Module.Anchor.setting.maxHeight.displayName"))
            .description(I18n.translate("Module.Anchor.setting.maxHeight.description"))
            .defaultValue(10)
            .min(0)
            .max(255)
            .sliderMax(20)
            .build()
    );

    private final Setting<Integer> minPitch = sgGeneral.add(new IntSetting.Builder()
            .name("min-pitch")
            .displayName(I18n.translate("Module.Anchor.setting.minPitch.displayName"))
            .description(I18n.translate("Module.Anchor.setting.minPitch.description"))
            .defaultValue(-90)
            .min(-90)
            .max(90)
            .sliderMin(-90)
            .sliderMax(90)
            .build()
    );

    private final Setting<Boolean> cancelMove = sgGeneral.add(new BoolSetting.Builder()
            .name("cancel-jump-in-hole")
            .displayName(I18n.translate("Module.Anchor.setting.cancelMove.displayName"))
            .description(I18n.translate("Module.Anchor.setting.cancelMove.description"))
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> pull = sgGeneral.add(new BoolSetting.Builder()
            .name("pull")
            .displayName(I18n.translate("Module.Anchor.setting.pull.displayName"))
            .description(I18n.translate("Module.Anchor.setting.pull.description"))
            .defaultValue(false)
            .build()
    );

    private final Setting<Double> pullSpeed = sgGeneral.add(new DoubleSetting.Builder()
            .name("pull-speed")
            .displayName(I18n.translate("Module.Anchor.setting.pullSpeed.displayName"))
            .description(I18n.translate("Module.Anchor.setting.pullSpeed.description"))
            .defaultValue(0.3)
            .min(0)
            .sliderMax(5)
            .build()
    );

    private final BlockPos.Mutable blockPos = new BlockPos.Mutable();
    private boolean wasInHole;
    private boolean foundHole;
    private int holeX, holeZ;

    public boolean cancelJump;

    public boolean controlMovement;
    public double deltaX, deltaZ;

    public Anchor() {
        super(Category.Movement, "anchor", I18n.translate("Module.Anchor.description"));
    }

    @Override
    public void onActivate() {
        wasInHole = false;
        holeX = holeZ = 0;
    }

    @EventHandler
    private final Listener<TickEvent.Pre> onPreTick = new Listener<>(event -> cancelJump = foundHole && cancelMove.get() && mc.player.pitch >= minPitch.get());

    @EventHandler
    private final Listener<TickEvent.Post> onPostTick = new Listener<>(event -> {
        controlMovement = false;

        int x = MathHelper.floor(mc.player.getX());
        int y = MathHelper.floor(mc.player.getY());
        int z = MathHelper.floor(mc.player.getZ());

        if (isHole(x, y, z)) {
            wasInHole = true;
            holeX = x;
            holeZ = z;
            return;
        }

        if (wasInHole && holeX == x && holeZ == z) return;
        else if (wasInHole) wasInHole = false;

        if (mc.player.pitch < minPitch.get()) return;

        foundHole = false;
        double holeX = 0;
        double holeZ = 0;

        for (int i = 0; i < maxHeight.get(); i++) {
            y--;
            if (y <= 0 || !isAir(x, y, z)) break;

            if (isHole(x, y, z)) {
                foundHole = true;
                holeX = x + 0.5;
                holeZ = z + 0.5;
                break;
            }
        }

        if (foundHole) {
            controlMovement = true;
            deltaX = Utils.clamp(holeX - mc.player.getX(), -0.05, 0.05);
            deltaZ = Utils.clamp(holeZ - mc.player.getZ(), -0.05, 0.05);

            ((IVec3d) mc.player.getVelocity()).set(deltaX, mc.player.getVelocity().y - (pull.get() ? pullSpeed.get() : 0), deltaZ);
        }
    });

    private boolean isHole(int x, int y, int z) {
        return isHoleBlock(x, y - 1, z) &&
                isHoleBlock(x + 1, y, z) &&
                isHoleBlock(x - 1, y, z) &&
                isHoleBlock(x, y, z + 1) &&
                isHoleBlock(x, y, z - 1);
    }

    private boolean isHoleBlock(int x, int y, int z) {
        blockPos.set(x, y, z);
        Block block = mc.world.getBlockState(blockPos).getBlock();
        return block == Blocks.BEDROCK || block == Blocks.OBSIDIAN || block == Blocks.CRYING_OBSIDIAN;
    }

    private boolean isAir(int x, int y, int z) {
        blockPos.set(x, y, z);
        return !((AbstractBlockAccessor)mc.world.getBlockState(blockPos).getBlock()).isCollidable();
    }
}
