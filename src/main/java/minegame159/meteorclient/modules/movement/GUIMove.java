/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.modules.movement;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.events.world.TickEvent;
import minegame159.meteorclient.gui.WidgetScreen;
import minegame159.meteorclient.mixininterface.ICreativeInventoryScreen;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Module;
import minegame159.meteorclient.modules.ModuleManager;
import minegame159.meteorclient.modules.render.Freecam;
import minegame159.meteorclient.settings.*;
import minegame159.meteorclient.utils.Utils;
import minegame159.meteorclient.utils.misc.input.Input;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.ingame.*;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.ItemGroup;
import org.lwjgl.glfw.GLFW;

public class GUIMove extends Module {

    public enum Screens {
        GUI,
        Inventory,
        Both
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Screens> screens = sgGeneral.add(new EnumSetting.Builder<Screens>()
            .name("screens")
            .displayName(I18n.translate("Module.GUIMove.setting.screens.displayName"))
            .description(I18n.translate("Module.GUIMove.setting.screens.description"))
            .displayValues(new String[]{
                    I18n.translate("Module.GUIMove.enum.Screens.GUI"),
                    I18n.translate("Module.GUIMove.enum.Screens.Inventory"),
                    I18n.translate("Module.GUIMove.enum.Screens.Both"),
            })
            .defaultValue(Screens.Inventory)
            .build()
    );

    private final Setting<Boolean> sneak = sgGeneral.add(new BoolSetting.Builder()
            .name("sneak")
            .displayName(I18n.translate("Module.GUIMove.setting.sneak.displayName"))
            .description(I18n.translate("Module.GUIMove.setting.sneak.description"))
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> jump = sgGeneral.add(new BoolSetting.Builder()
            .name("jump")
            .displayName(I18n.translate("Module.GUIMove.setting.jump.displayName"))
            .description(I18n.translate("Module.GUIMove.setting.jump.description"))
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> sprint = sgGeneral.add(new BoolSetting.Builder()
            .name("sprint")
            .displayName(I18n.translate("Module.GUIMove.setting.sprint.displayName"))
            .description(I18n.translate("Module.GUIMove.setting.sprint.description"))
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> arrowsRotate = sgGeneral.add(new BoolSetting.Builder()
            .name("arrows-rotate")
            .displayName(I18n.translate("Module.GUIMove.setting.arrowsRotate.displayName"))
            .description(I18n.translate("Module.GUIMove.setting.arrowsRotate.description"))
            .defaultValue(true)
            .build()
    );

    private final Setting<Double> rotateSpeed = sgGeneral.add(new DoubleSetting.Builder()
            .name("rotate-speed")
            .displayName(I18n.translate("Module.GUIMove.setting.rotateSpeed.displayName"))
            .description(I18n.translate("Module.GUIMove.setting.rotateSpeed.description"))
            .defaultValue(4)
            .min(0)
            .build()
    );

    public GUIMove() {
        super(Category.Movement, "gui-move", I18n.translate("Module.GUIMove.description"));
    }

    @EventHandler
    private final Listener<TickEvent.Post> onTick = new Listener<>(event -> {
        if (!skip()) {
            switch (screens.get()) {
                case GUI:
                    if (mc.currentScreen instanceof WidgetScreen) tickSneakJumpAndSprint();
                    break;
                case Inventory:
                    if (!(mc.currentScreen instanceof WidgetScreen)) tickSneakJumpAndSprint();
                    break;
                case Both:
                    tickSneakJumpAndSprint();
                    break;
            }
        }
    });

    public void tick() {
        if (!isActive() || skip()) return;

        mc.player.input.movementForward = 0;
        mc.player.input.movementSideways = 0;

        if (Input.isPressed(mc.options.keyForward)) {
            mc.player.input.pressingForward = true;
            mc.player.input.movementForward++;
        } else mc.player.input.pressingForward = false;

        if (Input.isPressed(mc.options.keyBack)) {
            mc.player.input.pressingBack = true;
            mc.player.input.movementForward--;
        } else mc.player.input.pressingBack = false;

        if (Input.isPressed(mc.options.keyRight)) {
            mc.player.input.pressingRight = true;
            mc.player.input.movementSideways--;
        } else mc.player.input.pressingRight = false;

        if (Input.isPressed(mc.options.keyLeft)) {
            mc.player.input.pressingLeft = true;
            mc.player.input.movementSideways++;
        } else mc.player.input.pressingLeft = false;

        tickSneakJumpAndSprint();

        if (arrowsRotate.get()) {
            for (int i = 0; i < (rotateSpeed.get() * 2); i++) {
                if (Input.isPressed(GLFW.GLFW_KEY_LEFT)) mc.player.yaw -= 0.5;
                if (Input.isPressed(GLFW.GLFW_KEY_RIGHT)) mc.player.yaw += 0.5;
                if (Input.isPressed(GLFW.GLFW_KEY_UP)) mc.player.pitch -= 0.5;
                if (Input.isPressed(GLFW.GLFW_KEY_DOWN)) mc.player.pitch += 0.5;
            }

            mc.player.pitch = Utils.clamp(mc.player.pitch, -90, 90);
        }
    }

    private void tickSneakJumpAndSprint() {
        mc.player.input.jumping = jump.get() && Input.isPressed(mc.options.keyJump);
        mc.player.input.sneaking = sneak.get() && Input.isPressed(mc.options.keySneak);
        mc.player.setSprinting(sprint.get() && Input.isPressed(mc.options.keySprint));
    }

    private boolean skip() {
        return mc.currentScreen == null || ModuleManager.INSTANCE.isActive(Freecam.class) || (mc.currentScreen instanceof CreativeInventoryScreen && ((ICreativeInventoryScreen) mc.currentScreen).getSelectedTab() == ItemGroup.SEARCH.getIndex()) || mc.currentScreen instanceof ChatScreen || mc.currentScreen instanceof SignEditScreen || mc.currentScreen instanceof AnvilScreen || mc.currentScreen instanceof AbstractCommandBlockScreen || mc.currentScreen instanceof StructureBlockScreen;
    }
}
