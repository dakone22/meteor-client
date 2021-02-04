/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.modules.player;

import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.GoalXZ;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.events.entity.TookDamageEvent;
import minegame159.meteorclient.gui.widgets.WButton;
import minegame159.meteorclient.gui.widgets.WLabel;
import minegame159.meteorclient.gui.widgets.WTable;
import minegame159.meteorclient.gui.widgets.WWidget;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Module;
import minegame159.meteorclient.settings.BoolSetting;
import minegame159.meteorclient.settings.Setting;
import minegame159.meteorclient.settings.SettingGroup;
import minegame159.meteorclient.utils.Utils;
import minegame159.meteorclient.utils.player.ChatUtils;
import minegame159.meteorclient.waypoints.Waypoint;
import minegame159.meteorclient.waypoints.Waypoints;
import net.minecraft.client.resource.language.I18n;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DeathPosition extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> createWaypoint = sgGeneral.add(new BoolSetting.Builder()
            .name("create-waypoint")
            .displayName(I18n.translate("Module.DeathPosition.setting.createWaypoint.displayName"))
            .description(I18n.translate("Module.DeathPosition.setting.createWaypoint.description"))
            .defaultValue(true)
            .build()
    );

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    private final WLabel label = new WLabel(I18n.translate("Module.DeathPosition.no_last_death_found"));

    public DeathPosition() {
        super(Category.Player, "death-position", I18n.translate("Module.DeathPosition.description"));
    }

    private final Map<String, Double> deathPos = new HashMap<>();
    private Waypoint waypoint;

    @SuppressWarnings("unused")
    @EventHandler
    private final Listener<TookDamageEvent> onTookDamage = new Listener<>(event -> {
        if (mc.player == null) return;
        if (event.entity.getUuid() != null && event.entity.getUuid().equals(mc.player.getUuid()) && event.entity.getHealth() <= 0) {
            deathPos.put("x", mc.player.getX());
            deathPos.put("z", mc.player.getZ());
            label.setText(I18n.translate("Module.DeathPosition.latest_death", mc.player.getX(), mc.player.getY(), mc.player.getZ()));

            String time = dateFormat.format(new Date());
            ChatUtils.moduleInfo(this, I18n.translate("Module.DeathPosition.message.died_at"), mc.player.getX(), mc.player.getY(), mc.player.getZ(), time);

            // Create waypoint
            if (createWaypoint.get()) {
                waypoint = new Waypoint();
                waypoint.name = I18n.translate("Module.DeathPosition.waypoint_text", time);

                waypoint.x = (int) mc.player.getX();
                waypoint.y = (int) mc.player.getY() + 2;
                waypoint.z = (int) mc.player.getZ();
                waypoint.maxVisibleDistance = Integer.MAX_VALUE;
                waypoint.actualDimension = Utils.getDimension();

                switch (Utils.getDimension()) {
                    case Overworld:
                        waypoint.overworld = true;
                        break;
                    case Nether:
                        waypoint.nether = true;
                        break;
                    case End:
                        waypoint.end = true;
                        break;
                }

                Waypoints.INSTANCE.add(waypoint);
            }
        }
    });

    @Override
    public WWidget getWidget() {
        WTable table = new WTable();
        table.add(label);
        WButton path = new WButton(I18n.translate("Module.DeathPosition.button.Path"));
        table.add(path);
        path.action = this::path;
        WButton clear = new WButton(I18n.translate("Module.DeathPosition.button.Clear"));
        table.add(clear);
        clear.action = this::clear;
        return table;
    }

    private void path() {
        if (deathPos.isEmpty() && mc.player != null) {
            ChatUtils.moduleWarning(this,I18n.translate("Module.DeathPosition.message.no_last_death_found"));
        } else {
            if (mc.world != null) {
                double x = deathPos.get("x"), z = deathPos.get("z");
                if (BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().isPathing())
                    BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().cancelEverything();
                BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalXZ((int) x, (int) z));
            }
        }
    }

    private void clear() {
        Waypoints.INSTANCE.remove(waypoint);
        label.setText(I18n.translate("Module.DeathPosition.no_last_death"));
    }
}
