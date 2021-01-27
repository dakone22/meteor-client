/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.modules.misc;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.events.world.TickEvent;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Module;
import minegame159.meteorclient.settings.*;
import minegame159.meteorclient.utils.player.PlayerUtils;
import minegame159.meteorclient.utils.player.RotationUtils;
import minegame159.meteorclient.utils.world.BlockIterator;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

import java.util.ArrayList;
import java.util.List;

public class LiquidFiller extends Module {
    public enum PlaceIn {
        Lava,
        Water,
        Both
    }

    private final SettingGroup sgGeneral  = settings.getDefaultGroup();

    private final Setting<PlaceIn> placeInLiquids = sgGeneral.add(new EnumSetting.Builder<PlaceIn>()
            .name("place-in")
            .displayName(I18n.translate("Modules.LiquidFiller.setting.placeInLiquids.displayName"))
            .description(I18n.translate("Modules.LiquidFiller.setting.placeInLiquids.description"))
            .defaultValue(PlaceIn.Lava)
            .build()
    );

    private final Setting<Integer> horizontalRadius = sgGeneral.add(new IntSetting.Builder()
            .name("horizontal-radius")
            .displayName(I18n.translate("Modules.LiquidFiller.setting.horizontalRadius.displayName"))
            .description(I18n.translate("Modules.LiquidFiller.setting.horizontalRadius.description"))
            .defaultValue(4)
            .min(0)
            .sliderMax(6)
            .build()
    );

    private final Setting<Integer> verticalRadius = sgGeneral.add(new IntSetting.Builder()
            .name("vertical-radius")
            .displayName(I18n.translate("Modules.LiquidFiller.setting.verticalRadius.displayName"))
            .description(I18n.translate("Modules.LiquidFiller.setting.verticalRadius.description"))
            .defaultValue(4)
            .min(0)
            .sliderMax(6)
            .build()
    );

    private final Setting<List<Block>> whitelist = sgGeneral.add(new BlockListSetting.Builder()
            .name("block-whitelist")
            .displayName(I18n.translate("Modules.LiquidFiller.setting.whitelist.displayName"))
            .description(I18n.translate("Modules.LiquidFiller.setting.whitelist.description"))
            .defaultValue(new ArrayList<>(0))
            .build()
    );

    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
            .name("rotate")
            .displayName(I18n.translate("Modules.LiquidFiller.setting.rotate.displayName"))
            .description(I18n.translate("Modules.LiquidFiller.setting.rotate.description"))
            .defaultValue(true)
            .build()
    );

    public LiquidFiller(){
        super(Category.Misc, "liquid-filler", I18n.translate("Modules.LiquidFiller.description"));
    }

    @EventHandler
    private final Listener<TickEvent.Pre> onTick = new Listener<>(event -> BlockIterator.register(horizontalRadius.get(), verticalRadius.get(), (blockPos, blockState) -> {
        if (blockState.getFluidState().getLevel() == 8 && blockState.getFluidState().isStill()) {
            Block liquid = blockState.getBlock();

            PlaceIn placeIn = placeInLiquids.get();
            if (placeIn == PlaceIn.Both || (placeIn == PlaceIn.Lava && liquid == Blocks.LAVA) || (placeIn == PlaceIn.Water && liquid == Blocks.WATER)) {
                if (rotate.get()) RotationUtils.packetRotate(blockPos);
                if (PlayerUtils.placeBlock(blockPos, findSlot(), Hand.MAIN_HAND)) BlockIterator.disableCurrent();
            }
        }
    }));

    private int findSlot() {
        int slot = -1;

        for (int i = 0; i < 9; i++){
            ItemStack block = mc.player.inventory.getStack(i);
            if ((block.getItem() instanceof BlockItem) && whitelist.get().contains(Block.getBlockFromItem(block.getItem()))) {
                slot = i;
                break;
            }
        }

        return slot;
    }
}