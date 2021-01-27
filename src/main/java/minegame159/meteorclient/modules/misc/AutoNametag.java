/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.modules.misc;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.events.world.TickEvent;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Module;
import minegame159.meteorclient.settings.*;
import minegame159.meteorclient.utils.player.RotationUtils;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.NameTagItem;
import net.minecraft.util.Hand;

public class AutoNametag extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Object2BooleanMap<EntityType<?>>> entities = sgGeneral.add(new EntityTypeListSetting.Builder()
            .name("entities")
            .displayName(I18n.translate("Modules.AutoNametag.setting.entities.displayName"))
            .description(I18n.translate("Modules.AutoNametag.setting.entities.description"))
            .defaultValue(new Object2BooleanOpenHashMap<>(0))
            .build()
    );
    
    private final Setting<Double> distance = sgGeneral.add(new DoubleSetting.Builder()
            .name("distance")
            .displayName(I18n.translate("Modules.AutoNametag.setting.distance.displayName"))
            .description(I18n.translate("Modules.AutoNametag.setting.distance.description"))
            .min(0.0)
            .defaultValue(5.0)
            .build()
    );

    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
            .name("rotate")
            .displayName(I18n.translate("Modules.AutoNametag.setting.rotate.displayName"))
            .description(I18n.translate("Modules.AutoNametag.setting.rotate.description"))
            .defaultValue(true)
            .build()
    );

    public AutoNametag() {
        super(Category.Misc, "auto-nametag", I18n.translate("Modules.AutoNametag.description"));
    }

    @EventHandler
    private final Listener<TickEvent.Post> onTick = new Listener<>(event -> {
        for (Entity entity : mc.world.getEntities()) {
            if (!entities.get().getBoolean(entity.getType()) || entity.hasCustomName() || mc.player.distanceTo(entity) > distance.get()) continue;

            boolean findNametag = true;
            boolean offHand = false;
            if (mc.player.inventory.getMainHandStack().getItem() instanceof NameTagItem) {
                findNametag = false;
            }
            else if (mc.player.inventory.offHand.get(0).getItem() instanceof NameTagItem) {
                findNametag = false;
                offHand = true;
            }

            boolean foundNametag = !findNametag;
            if (findNametag) {
                for (int i = 0; i < 9; i++) {
                    ItemStack itemStack = mc.player.inventory.getStack(i);
                    if (itemStack.getItem() instanceof NameTagItem) {
                        mc.player.inventory.selectedSlot = i;
                        foundNametag = true;
                        break;
                    }
                }
            }

            if (foundNametag) {
                if (rotate.get()) RotationUtils.packetRotate(entity);
                mc.interactionManager.interactEntity(mc.player, entity, offHand ? Hand.OFF_HAND : Hand.MAIN_HAND);
                return;
            }
        }
    });
}
