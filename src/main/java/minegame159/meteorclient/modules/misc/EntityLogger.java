/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.modules.misc;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.events.entity.EntityAddedEvent;
import minegame159.meteorclient.friends.FriendManager;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Module;
import minegame159.meteorclient.settings.BoolSetting;
import minegame159.meteorclient.settings.EntityTypeListSetting;
import minegame159.meteorclient.settings.Setting;
import minegame159.meteorclient.settings.SettingGroup;
import minegame159.meteorclient.utils.player.ChatUtils;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;

public class EntityLogger extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Object2BooleanMap<EntityType<?>>> entities = sgGeneral.add(new EntityTypeListSetting.Builder()
            .name("entites")
            .displayName(I18n.translate("Module.EntityLogger.setting.entities.displayName"))
            .description(I18n.translate("Module.EntityLogger.setting.entities.description"))
            .defaultValue(new Object2BooleanOpenHashMap<>(0))
            .build()
    );

    private final Setting<Boolean> playerNames = sgGeneral.add(new BoolSetting.Builder()
            .name("player-names")
            .displayName(I18n.translate("Module.EntityLogger.setting.playerNames.displayName"))
            .description(I18n.translate("Module.EntityLogger.setting.playerNames.description"))
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> friends = sgGeneral.add(new BoolSetting.Builder()
            .name("friends")
            .displayName(I18n.translate("Module.EntityLogger.setting.friends.displayName"))
            .description(I18n.translate("Module.EntityLogger.setting.friends.description"))
            .defaultValue(true)
            .build()
    );

    public EntityLogger() {
        super(Category.Misc, "entity-logger", I18n.translate("Module.EntityLogger.description"));
    }

    @EventHandler
    private final Listener<EntityAddedEvent> onEntityAdded = new Listener<>(event -> {
        if (event.entity.getUuid().equals(mc.player.getUuid())) return;

        if (entities.get().getBoolean(event.entity.getType())) {
            if (event.entity instanceof PlayerEntity) {
                if (!friends.get() && FriendManager.INSTANCE.get((PlayerEntity) event.entity) != null) return;
            }

            String name;
            if (playerNames.get() && event.entity instanceof PlayerEntity) name = ((PlayerEntity) event.entity).getGameProfile().getName() + " (Player)";
            else name = event.entity.getType().getName().getString();

            ChatUtils.moduleInfo(this, "(highlight)%s (default)has spawned at (highlight)%.0f(default), (highlight)%.0f(default), (highlight)%.0f(default).", name, event.entity.getX(), event.entity.getY(), event.entity.getZ());
        }
    });
}
