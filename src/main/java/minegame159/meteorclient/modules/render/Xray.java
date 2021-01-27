/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.modules.render;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.MeteorClient;
import minegame159.meteorclient.events.Cancellable;
import minegame159.meteorclient.events.render.RenderBlockEntityEvent;
import minegame159.meteorclient.events.world.AmbientOcclusionEvent;
import minegame159.meteorclient.events.world.ChunkOcclusionEvent;
import minegame159.meteorclient.mixin.BlockEntityTypeAccessor;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Module;
import minegame159.meteorclient.modules.ModuleManager;
import minegame159.meteorclient.settings.BlockListSetting;
import minegame159.meteorclient.settings.Setting;
import minegame159.meteorclient.settings.SettingGroup;
import minegame159.meteorclient.utils.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.resource.language.I18n;

import java.util.Arrays;
import java.util.List;

public class Xray extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    
    private final Setting<List<Block>> blocks = sgGeneral.add(new BlockListSetting.Builder()
            .name("blocks")
            .displayName(I18n.translate("Modules.Xray.setting.blocks.displayName"))
            .description(I18n.translate("Modules.Xray.setting.blocks.description"))
            .defaultValue(Arrays.asList(Blocks.COAL_ORE, Blocks.IRON_ORE, Blocks.GOLD_ORE, Blocks.LAPIS_ORE,
                    Blocks.REDSTONE_ORE, Blocks.DIAMOND_ORE, Blocks.EMERALD_ORE,
                    Blocks.NETHER_GOLD_ORE, Blocks.NETHER_QUARTZ_ORE, Blocks.ANCIENT_DEBRIS))
            .onChanged(blocks1 -> {
                if (isActive()) mc.worldRenderer.reload();
            })
            .build()
    );

    private boolean fullBrightWasActive = false;

    public Xray() {
        super(Category.Render, "xray", I18n.translate("Modules.Xray.description"));
    }

    @Override
    public void onActivate() {
        Fullbright fullBright = ModuleManager.INSTANCE.get(Fullbright.class);
        fullBrightWasActive = fullBright.isActive();
        if (!fullBright.isActive()) fullBright.toggle();

        mc.worldRenderer.reload();
    }

    @Override
    public void onDeactivate() {
        Fullbright fullBright = ModuleManager.INSTANCE.get(Fullbright.class);
        if (!fullBrightWasActive && fullBright.isActive()) fullBright.toggle();

        if (!MeteorClient.IS_DISCONNECTING) mc.worldRenderer.reload();
    }

    @EventHandler
    private final Listener<RenderBlockEntityEvent> onRenderBlockEntity = new Listener<>(event -> {
        if (!Utils.blockRenderingBlockEntitiesInXray) return;

        for (Block block : ((BlockEntityTypeAccessor) event.blockEntity.getType()).getBlocks()) {
            if (isBlocked(block)) {
                event.cancel();
                break;
            }
        }
    });

//    @EventHandler  // TODO: Xray: async DrawSideEvent
//    private final Listener<DrawSideEvent> onDrawSide = new Listener<>(event -> {
//        event.setDraw(!isBlocked(event.state.getBlock()));
//        DrawSideEvent.returnDrawSideEvent(event);
//    });

    @EventHandler
    private final Listener<ChunkOcclusionEvent> onChunkOcclusion = new Listener<>(Cancellable::cancel);

    @EventHandler
    private final Listener<AmbientOcclusionEvent> onAmbientOcclusion = new Listener<>(event -> event.lightLevel = 1);

    public boolean isBlocked(Block block) {
        return isActive() && !blocks.get().contains(block);
    }
}
