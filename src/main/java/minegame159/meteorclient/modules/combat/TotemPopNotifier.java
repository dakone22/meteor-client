/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.modules.combat;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.events.game.GameJoinedEvent;
import minegame159.meteorclient.events.packets.PacketEvent;
import minegame159.meteorclient.events.world.TickEvent;
import minegame159.meteorclient.friends.FriendManager;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Module;
import minegame159.meteorclient.settings.BoolSetting;
import minegame159.meteorclient.settings.Setting;
import minegame159.meteorclient.settings.SettingGroup;
import minegame159.meteorclient.settings.StringSetting;
import minegame159.meteorclient.utils.player.ChatUtils;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;

import java.util.Random;
import java.util.UUID;

public class TotemPopNotifier extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> announce = sgGeneral.add(new BoolSetting.Builder()
            .name("announce-in-chat")
            .displayName(I18n.translate("Module.TotemPopNotifier.setting.announce.displayName"))
            .description(I18n.translate("Module.TotemPopNotifier.setting.announce.description"))
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> ignoreOwn = sgGeneral.add(new BoolSetting.Builder()
            .name("ignore-own")
            .displayName(I18n.translate("Module.TotemPopNotifier.setting.ignoreOwn.displayName"))
            .description(I18n.translate("Module.TotemPopNotifier.setting.ignoreOwn.description"))
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> ignoreFriend = sgGeneral.add(new BoolSetting.Builder()
            .name("ignore-friend")
            .displayName(I18n.translate("Module.TotemPopNotifier.setting.ignoreFriend.displayName"))
            .description(I18n.translate("Module.TotemPopNotifier.setting.ignoreFriend.description"))
            .defaultValue(true)
            .build()
    );

    private final Setting<String> popMessage = sgGeneral.add(new StringSetting.Builder()
            .name("pop-message")
            .displayName(I18n.translate("Module.TotemPopNotifier.setting.popMessage.displayName"))
            .description(I18n.translate("Module.TotemPopNotifier.setting.popMessage.description"))
            .defaultValue(I18n.translate("Module.TotemPopNotifier.setting.popMessage.defaultValue"))
            .build()
    );

    private final Setting<String> deathMessage = sgGeneral.add(new StringSetting.Builder()
            .name("death-message")
            .displayName(I18n.translate("Module.TotemPopNotifier.setting.deathMessage.displayName"))
            .description(I18n.translate("Module.TotemPopNotifier.setting.deathMessage.description"))
            .defaultValue(I18n.translate("Module.TotemPopNotifier.setting.deathMessage.defaultValue"))
            .build()
    );

    private final Object2IntMap<UUID> totemPops = new Object2IntOpenHashMap<>();
    private final Object2IntMap<UUID> chatIds = new Object2IntOpenHashMap<>();

    private final Random random = new Random();

    public TotemPopNotifier() {
        super(Category.Combat, "totem-pop-notifier", I18n.translate("Module.TotemPopNotifier.description"));
    }

    @Override
    public void onActivate() {
        totemPops.clear();
        chatIds.clear();
    }

    @EventHandler
    private final Listener<GameJoinedEvent> onGameJoin = new Listener<>(event -> {
        totemPops.clear();
        chatIds.clear();
    });

    @EventHandler
    private final Listener<PacketEvent.Receive> onReceivePacket = new Listener<>(event -> {
        if (!(event.packet instanceof EntityStatusS2CPacket)) return;

        EntityStatusS2CPacket p = (EntityStatusS2CPacket) event.packet;
        if (p.getStatus() != 35) return;

        Entity entity = p.getEntity(mc.world);
        if (entity == null || (entity.equals(mc.player) && ignoreOwn.get()) || (!FriendManager.INSTANCE.attack((PlayerEntity) entity) && ignoreFriend.get())) return;

        synchronized (totemPops) {
            int pops = totemPops.getOrDefault(entity.getUuid(), 0);
            totemPops.put(entity.getUuid(), ++pops);

            String msg = popMessage.get().replace("{player}", entity.getName().getString()).replace("{pops}", String.valueOf(pops)).replace("{totems}", getTotemStr(pops));

            if (announce.get()) mc.player.sendChatMessage(msg);
            else ChatUtils.info(getChatId(entity), I18n.translate("Module.TotemPopNotifier.message.pop"), entity.getName().getString(), pops, getTotemStr(pops));
        }
    });

    @EventHandler
    private final Listener<TickEvent.Post> onTick = new Listener<>(event -> {
        synchronized (totemPops) {
            for (PlayerEntity player : mc.world.getPlayers()) {
                if (!totemPops.containsKey(player.getUuid())) continue;

                if (player.deathTime > 0 || player.getHealth() <= 0) {
                    int pops = totemPops.removeInt(player.getUuid());

                    String msg = deathMessage.get().replace("{player}", player.getName().getString()).replace("{pops}", String.valueOf(pops)).replace("{totems}", getTotemStr(pops));

                    if (announce.get()) mc.player.sendChatMessage(msg);
                    else ChatUtils.info(getChatId(player), I18n.translate("Module.TotemPopNotifier.message.death"), player.getName().getString(), pops, getTotemStr(pops));

                    chatIds.removeInt(player.getUuid());
                }
            }
        }
    });

    private int getChatId(Entity entity) {
        return chatIds.computeIntIfAbsent(entity.getUuid(), value -> random.nextInt());
    }
    
    private String getTotemStr(int pops) {
        return I18n.translate("Module.TotemPopNotifier.misc.totem" + (pops == 1 ? "" : "s"));
    }
}