/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.modules.misc;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.events.meteor.MiddleMouseButtonEvent;
import minegame159.meteorclient.friends.Friend;
import minegame159.meteorclient.friends.FriendManager;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Module;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.player.PlayerEntity;

public class MiddleClickFriend extends Module {
    public MiddleClickFriend() {
        super(Category.Misc, "middle-click-friend", I18n.translate("Module.MiddleClickFriend.description"));
    }

    @EventHandler
    private final Listener<MiddleMouseButtonEvent> onMiddleMouseButton = new Listener<>(event -> {
        if (mc.currentScreen != null) return;
        if (mc.targetedEntity instanceof PlayerEntity) FriendManager.INSTANCE.addOrRemove(new Friend((PlayerEntity) mc.targetedEntity));
    });
}
