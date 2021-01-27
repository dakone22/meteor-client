/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.modules.misc;

//Updated by squidoodly 24/07/2020

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.events.entity.EntityAddedEvent;
import minegame159.meteorclient.friends.Friend;
import minegame159.meteorclient.friends.FriendManager;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Module;
import minegame159.meteorclient.settings.BoolSetting;
import minegame159.meteorclient.settings.Setting;
import minegame159.meteorclient.settings.SettingGroup;
import minegame159.meteorclient.settings.StringSetting;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.player.PlayerEntity;

public class MessageAura extends Module {
    public MessageAura() {
        super(Category.Misc, "message-aura", I18n.translate("Modules.MessageAura.description"));
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<String> message = sgGeneral.add(new StringSetting.Builder()
            .name("message")
            .displayName(I18n.translate("Modules.MessageAura.setting.message.displayName"))
            .description(I18n.translate("Modules.MessageAura.setting.message.description"))
            .defaultValue("Meteor on Crack!")
            .build()
    );

    private final Setting<Boolean> ignoreFriends = sgGeneral.add(new BoolSetting.Builder()
            .name("ignore-friends")
            .displayName(I18n.translate("Modules.MessageAura.setting.ignoreFriends.displayName"))
            .description(I18n.translate("Modules.MessageAura.setting.ignoreFriends.description"))
            .defaultValue(false)
            .build()
    );

    @EventHandler
    private final Listener<EntityAddedEvent> onEntityAdded = new Listener<>(event -> {
        if (!(event.entity instanceof PlayerEntity) || event.entity.getUuid().equals(mc.player.getUuid())) return;

        if (!ignoreFriends.get() || (ignoreFriends.get() && !FriendManager.INSTANCE.contains(new Friend((PlayerEntity)event.entity)))) {
            mc.player.sendChatMessage("/msg " + ((PlayerEntity) event.entity).getGameProfile().getName() + " " + message.get());
        }
    });
}
