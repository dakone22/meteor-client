/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.modules.render;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.events.render.RenderEvent;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Module;
import minegame159.meteorclient.rendering.Renderer;
import minegame159.meteorclient.rendering.ShapeMode;
import minegame159.meteorclient.settings.*;
import minegame159.meteorclient.utils.render.color.Color;
import minegame159.meteorclient.utils.render.color.SettingColor;
import minegame159.meteorclient.utils.world.Dir;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.*;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.resource.language.I18n;

import java.util.Arrays;
import java.util.List;

public class StorageESP extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<BlockEntityType<?>>> storageBlocks = sgGeneral.add(new StorageBlockListSetting.Builder()
            .name("storage-blocks")
            .displayName(I18n.translate("Modules.StorageESP.setting.storageBlocks.displayName"))
            .description(I18n.translate("Modules.StorageESP.setting.storageBlocks.description"))
            .defaultValue(Arrays.asList(StorageBlockListSetting.STORAGE_BLOCKS))
            .build()
    );

    private final Setting<ShapeMode> shapeMode = sgGeneral.add(new EnumSetting.Builder<ShapeMode>()
            .name("shape-mode")
            .displayName(I18n.translate("Modules.StorageESP.setting.shapeMode.displayName"))
            .description(I18n.translate("Modules.StorageESP.setting.shapeMode.description"))
            .defaultValue(ShapeMode.Both)
            .build()
    );

    private final Setting<SettingColor> chest = sgGeneral.add(new ColorSetting.Builder()
            .name("chest")
            .displayName(I18n.translate("Modules.StorageESP.setting.chest.displayName"))
            .description(I18n.translate("Modules.StorageESP.setting.chest.description"))
            .defaultValue(new SettingColor(255, 160, 0, 255))
            .build()
    );

    private final Setting<SettingColor> trappedChest = sgGeneral.add(new ColorSetting.Builder()
            .name("trapped-chest")
            .displayName(I18n.translate("Modules.StorageESP.setting.trappedChest.displayName"))
            .description(I18n.translate("Modules.StorageESP.setting.trappedChest.description"))
            .defaultValue(new SettingColor(255, 0, 0, 255))
            .build()
    );

    private final Setting<SettingColor> barrel = sgGeneral.add(new ColorSetting.Builder()
            .name("barrel")
            .displayName(I18n.translate("Modules.StorageESP.setting.barrel.displayName"))
            .description(I18n.translate("Modules.StorageESP.setting.barrel.description"))
            .defaultValue(new SettingColor(255, 160, 0, 255))
            .build()
    );

    private final Setting<SettingColor> shulker = sgGeneral.add(new ColorSetting.Builder()
            .name("shulker")
            .displayName(I18n.translate("Modules.StorageESP.setting.shulker.displayName"))
            .description(I18n.translate("Modules.StorageESP.setting.shulker.description"))
            .defaultValue(new SettingColor(255, 160, 0, 255))
            .build()
    );

    private final Setting<SettingColor> enderChest = sgGeneral.add(new ColorSetting.Builder()
            .name("ender-chest")
            .displayName(I18n.translate("Modules.StorageESP.setting.enderChest.displayName"))
            .description(I18n.translate("Modules.StorageESP.setting.enderChest.description"))
            .defaultValue(new SettingColor(120, 0, 255, 255))
            .build()
    );

    private final Setting<SettingColor> other = sgGeneral.add(new ColorSetting.Builder()
            .name("other")
            .displayName(I18n.translate("Modules.StorageESP.setting.other.displayName"))
            .description(I18n.translate("Modules.StorageESP.setting.other.description"))
            .defaultValue(new SettingColor(140, 140, 140, 255))
            .build()
    );

    private final Setting<Double> fadeDistance = sgGeneral.add(new DoubleSetting.Builder()
            .name("fade-distance")
            .displayName(I18n.translate("Modules.StorageESP.setting.fadeDistance.displayName"))
            .description(I18n.translate("Modules.StorageESP.setting.fadeDistance.description"))
            .defaultValue(6)
            .min(0)
            .sliderMax(12)
            .build()
    );

    private final Color lineColor = new Color(0, 0, 0, 0);
    private final Color sideColor = new Color(0, 0, 0, 0);
    private boolean render;
    private int count;

    public StorageESP() {
        super(Category.Render, "storage-esp", I18n.translate("Modules.StorageESP.description"));
    }

    private void getTileEntityColor(BlockEntity blockEntity) {
        render = false;

        if (!storageBlocks.get().contains(blockEntity.getType())) return;

        if (blockEntity instanceof TrappedChestBlockEntity) lineColor.set(trappedChest.get()); // Must come before ChestBlockEntity as it is the superclass of TrappedChestBlockEntity
        else if (blockEntity instanceof ChestBlockEntity) lineColor.set(chest.get());
        else if (blockEntity instanceof BarrelBlockEntity) lineColor.set(barrel.get());
        else if (blockEntity instanceof ShulkerBoxBlockEntity) lineColor.set(shulker.get());
        else if (blockEntity instanceof EnderChestBlockEntity) lineColor.set(enderChest.get());
        else if (blockEntity instanceof FurnaceBlockEntity || blockEntity instanceof DispenserBlockEntity || blockEntity instanceof HopperBlockEntity) lineColor.set(other.get());
        else return;

        render = true;

        if (shapeMode.get() == ShapeMode.Sides || shapeMode.get() == ShapeMode.Both) {
            sideColor.set(lineColor);
            sideColor.a -= 225;
            if (sideColor.a < 0) sideColor.a = 0;
        }
    }

    @EventHandler
    private final Listener<RenderEvent> onRender = new Listener<>(event -> {
        count = 0;

        for (BlockEntity blockEntity : mc.world.blockEntities) {
            if (blockEntity.isRemoved()) continue;

            getTileEntityColor(blockEntity);

            if (render) {
                double x1 = blockEntity.getPos().getX();
                double y1 = blockEntity.getPos().getY();
                double z1 = blockEntity.getPos().getZ();

                double x2 = blockEntity.getPos().getX() + 1;
                double y2 = blockEntity.getPos().getY() + 1;
                double z2 = blockEntity.getPos().getZ() + 1;

                int excludeDir = 0;
                if (blockEntity instanceof ChestBlockEntity) {
                    BlockState state = mc.world.getBlockState(blockEntity.getPos());
                    if ((state.getBlock() == Blocks.CHEST || state.getBlock() == Blocks.TRAPPED_CHEST) && state.get(ChestBlock.CHEST_TYPE) != ChestType.SINGLE) {
                        excludeDir = Dir.get(ChestBlock.getFacing(state));
                    }
                }

                if (blockEntity instanceof ChestBlockEntity || blockEntity instanceof EnderChestBlockEntity) {
                    double a = 1.0 / 16.0;

                    if (Dir.is(excludeDir, Dir.WEST)) x1 += a;
                    if (Dir.is(excludeDir, Dir.NORTH)) z1 += a;

                    if (Dir.is(excludeDir, Dir.EAST)) x2 -= a;
                    y2 -= a * 2;
                    if (Dir.is(excludeDir, Dir.SOUTH)) z2 -= a;
                }

                double dist = mc.player.squaredDistanceTo(blockEntity.getPos().getX() + 1, blockEntity.getPos().getY() + 1, blockEntity.getPos().getZ() + 1);
                double a = 1;
                if (dist <= fadeDistance.get() * fadeDistance.get()) a = dist / (fadeDistance.get() * fadeDistance.get());

                int prevLineA = lineColor.a;
                int prevSideA = sideColor.a;

                lineColor.a *= a;
                sideColor.a *= a;

                if (a >= 0.075) {
                    Renderer.boxWithLines(Renderer.NORMAL, Renderer.LINES, x1, y1, z1, x2, y2, z2, sideColor, lineColor, shapeMode.get(), excludeDir);
                }

                lineColor.a = prevLineA;
                sideColor.a = prevSideA;

                count++;
            }
        }
    });

    @Override
    public String getInfoString() {
        return Integer.toString(count);
    }
}
