/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.modules.player;

import minegame159.meteorclient.gui.widgets.WButton;
import minegame159.meteorclient.gui.widgets.WTable;
import minegame159.meteorclient.gui.widgets.WWidget;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Module;
import minegame159.meteorclient.settings.*;
import minegame159.meteorclient.utils.entity.FakePlayerEntity;
import minegame159.meteorclient.utils.player.ChatUtils;
import net.minecraft.client.resource.language.I18n;

import java.util.HashMap;
import java.util.Map;

public class FakePlayer extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<String> name = sgGeneral.add(new StringSetting.Builder()
            .name("name")
            .displayName(I18n.translate("Modules.FakePlayer.setting.name.displayName"))
            .description(I18n.translate("Modules.FakePlayer.setting.name.description"))
            .defaultValue("MeteorOnCrack")
            .build()
    );

    private final Setting<Boolean> copyInv = sgGeneral.add(new BoolSetting.Builder()
            .name("copy-inv")
            .displayName(I18n.translate("Modules.FakePlayer.setting.copyInv.displayName"))
            .description(I18n.translate("Modules.FakePlayer.setting.copyInv.description"))
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> glowing = sgGeneral.add(new BoolSetting.Builder()
            .name("glowing")
            .displayName(I18n.translate("Modules.FakePlayer.setting.glowing.displayName"))
            .description(I18n.translate("Modules.FakePlayer.setting.glowing.description"))
            .defaultValue(true)
            .build()
    );

    private final Setting<Integer> health = sgGeneral.add(new IntSetting.Builder()
            .name("health")
            .displayName(I18n.translate("Modules.FakePlayer.setting.health.displayName"))
            .description(I18n.translate("Modules.FakePlayer.setting.health.description"))
            .defaultValue(20)
            .min(1)
            .sliderMax(100)
            .build()
    );

    private final Setting<Boolean> idInNametag = sgGeneral.add(new BoolSetting.Builder()
            .name("id-in-nametag")
            .displayName(I18n.translate("Modules.FakePlayer.setting.idInNametag.displayName"))
            .description(I18n.translate("Modules.FakePlayer.setting.idInNametag.description"))
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> chatInfo = sgGeneral.add(new BoolSetting.Builder()
            .name("chat-info")
            .displayName(I18n.translate("Modules.FakePlayer.setting.chatInfo.displayName"))
            .description(I18n.translate("Modules.FakePlayer.setting.chatInfo.description"))
            .defaultValue(false)
            .build()
    );

    public FakePlayer() {
        super(Category.Player, "fake-player", I18n.translate("Modules.FakePlayer.description"));
    }

    public static Map<FakePlayerEntity, Integer> players = new HashMap<>();
    private int ID;

    public Map<FakePlayerEntity, Integer> getPlayers() {
        if (!players.isEmpty()) {
            return players;
        } else return null;
    }

    @Override
    public void onActivate() {
        ID = 0;
    }

    @Override
    public void onDeactivate() {
        ID = 0;
        clearFakePlayers(false);
    }

    @Override
    public WWidget getWidget() {
        WTable table = new WTable();

        WButton spawn = table.add(new WButton("Spawn")).getWidget();
        spawn.action = () -> spawnFakePlayer(name.get(), copyInv.get(), glowing.get(), health.get().floatValue());

        WButton clear = table.add(new WButton("Clear")).getWidget();
        clear.action = () -> clearFakePlayers(true);

        return table;
    }

    public void spawnFakePlayer(String name, boolean copyInv, boolean glowing, float health) {
        if (isActive()) {
            if (mc.world == null) return;
            FakePlayerEntity fakePlayer = new FakePlayerEntity(name, copyInv, glowing, health);
            if (chatInfo.get()) ChatUtils.moduleInfo(this, "Spawned a fakeplayer");
            players.put(fakePlayer, ID);
            ID++;
        }
    }

    public void removeFakePlayer(int id) {
        if (isActive()) {
            if (players.isEmpty()) {
                if (chatInfo.get()) ChatUtils.moduleError(this, "There are no active fake players to remove!");
                return;
            }
            for (Map.Entry<FakePlayerEntity, Integer> player : players.entrySet()) {
                if (player.getValue() == id) {
                    player.getKey().despawn();
                    if (chatInfo.get()) ChatUtils.moduleInfo(this, "Removed fake player with ID (highlight)" + id);
                }
            }
        }
    }

    public void clearFakePlayers( boolean shouldCheckActive) {
        if (shouldCheckActive && isActive()) {
            if (players.isEmpty()) {
                if (chatInfo.get()) ChatUtils.moduleInfo(this, "There are no active fake players to remove!");
                return;
            } else {
                for (Map.Entry<FakePlayerEntity, Integer> player : players.entrySet()) {
                    player.getKey().despawn();
                }
                if (chatInfo.get()) ChatUtils.moduleInfo(this, "Removed all fake players.");

            }
        } else if (!shouldCheckActive) {
            for (Map.Entry<FakePlayerEntity, Integer> player : players.entrySet()) {
                player.getKey().despawn();
            }
            if (chatInfo.get()) ChatUtils.moduleInfo(this, "Removed all fake players.");
        }
        players.clear();
    }

    public String getName() {
        return name.get();
    }

    public int getID(FakePlayerEntity entity) {
        int id = -1;

        if (!players.isEmpty()) {
            for (Map.Entry<FakePlayerEntity, Integer> player : players.entrySet()) {
                if (player.getKey() == entity) id = player.getValue();
            }
        }

        return id;
    }

    public boolean showID() {
        return idInNametag.get();
    }

    public boolean copyInv() {
        return copyInv.get();
    }

    public boolean setGlowing() {
        return glowing.get();
    }

    public float getHealth() {
        return health.get().floatValue();
    }

    @Override
    public String getInfoString() {
        if (!players.isEmpty()) return String.valueOf(players.size());
        return null;
    }
}