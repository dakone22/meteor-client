/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.modules.render;

import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Module;
import minegame159.meteorclient.settings.BoolSetting;
import minegame159.meteorclient.settings.Setting;
import minegame159.meteorclient.settings.SettingGroup;
import net.minecraft.client.resource.language.I18n;

public class NoRender extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> noHurtCam = sgGeneral.add(new BoolSetting.Builder()
            .name("no-hurt-cam")
            .displayName(I18n.translate("Modules.NoRender.setting.noHurtCam.displayName"))
            .description(I18n.translate("Modules.NoRender.setting.noHurtCam.description"))
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> noWeather = sgGeneral.add(new BoolSetting.Builder()
            .name("no-weather")
            .displayName(I18n.translate("Modules.NoRender.setting.noWeather.displayName"))
            .description(I18n.translate("Modules.NoRender.setting.noWeather.description"))
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> noPortalOverlay = sgGeneral.add(new BoolSetting.Builder()
            .name("no-portal-overlay")
            .displayName(I18n.translate("Modules.NoRender.setting.noPortalOverlay.displayName"))
            .description(I18n.translate("Modules.NoRender.setting.noPortalOverlay.description"))
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> noPumpkinOverlay = sgGeneral.add(new BoolSetting.Builder()
            .name("no-pumpkin-overlay")
            .displayName(I18n.translate("Modules.NoRender.setting.noPumpkinOverlay.displayName"))
            .description(I18n.translate("Modules.NoRender.setting.noPumpkinOverlay.description"))
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> noFireOverlay = sgGeneral.add(new BoolSetting.Builder()
            .name("no-fire-overlay")
            .displayName(I18n.translate("Modules.NoRender.setting.noFireOverlay.displayName"))
            .description(I18n.translate("Modules.NoRender.setting.noFireOverlay.description"))
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> noWaterOverlay = sgGeneral.add(new BoolSetting.Builder()
            .name("no-water-overlay")
            .displayName(I18n.translate("Modules.NoRender.setting.noWaterOverlay.displayName"))
            .description(I18n.translate("Modules.NoRender.setting.noWaterOverlay.description"))
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> noVignette = sgGeneral.add(new BoolSetting.Builder()
            .name("no-vignette")
            .displayName(I18n.translate("Modules.NoRender.setting.noVignette.displayName"))
            .description(I18n.translate("Modules.NoRender.setting.noVignette.description"))
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> noBossBar = sgGeneral.add(new BoolSetting.Builder()
            .name("no-boss-bar")
            .displayName(I18n.translate("Modules.NoRender.setting.noBossBar.displayName"))
            .description(I18n.translate("Modules.NoRender.setting.noBossBar.description"))
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> noScoreboard = sgGeneral.add(new BoolSetting.Builder()
            .name("no-scoreboard")
            .displayName(I18n.translate("Modules.NoRender.setting.noScoreboard.displayName"))
            .description(I18n.translate("Modules.NoRender.setting.noScoreboard.description"))
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> noFog = sgGeneral.add(new BoolSetting.Builder()
            .name("no-fog")
            .displayName(I18n.translate("Modules.NoRender.setting.noFog.displayName"))
            .description(I18n.translate("Modules.NoRender.setting.noFog.description"))
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> noTotemAnimation = sgGeneral.add(new BoolSetting.Builder()
            .name("no-totem-animation")
            .displayName(I18n.translate("Modules.NoRender.setting.noTotemAnimation.displayName"))
            .description(I18n.translate("Modules.NoRender.setting.noTotemAnimation.description"))
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> noArmor = sgGeneral.add(new BoolSetting.Builder()
            .name("no-armor")
            .displayName(I18n.translate("Modules.NoRender.setting.noArmor.displayName"))
            .description(I18n.translate("Modules.NoRender.setting.noArmor.description"))
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> noNausea = sgGeneral.add(new BoolSetting.Builder()
            .name("no-nausea")
            .displayName(I18n.translate("Modules.NoRender.setting.noNausea.displayName"))
            .description(I18n.translate("Modules.NoRender.setting.noNausea.description"))
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> noItems = sgGeneral.add(new BoolSetting.Builder()
            .name("no-item")
            .displayName(I18n.translate("Modules.NoRender.setting.noItems.displayName"))
            .description(I18n.translate("Modules.NoRender.setting.noItems.description"))
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> noEnchTableBook = sgGeneral.add(new BoolSetting.Builder()
            .name("no-ench-table-book")
            .displayName(I18n.translate("Modules.NoRender.setting.noEnchTableBook.displayName"))
            .description(I18n.translate("Modules.NoRender.setting.noEnchTableBook.description"))
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> noSignText = sgGeneral.add(new BoolSetting.Builder()
            .name("no-sign-text")
            .displayName(I18n.translate("Modules.NoRender.setting.noSignText.displayName"))
            .description(I18n.translate("Modules.NoRender.setting.noSignText.description"))
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> noBlockBreakParticles = sgGeneral.add(new BoolSetting.Builder()
            .name("no-block-break-particles")
            .displayName(I18n.translate("Modules.NoRender.setting.noBlockBreakParticles.displayName"))
            .description(I18n.translate("Modules.NoRender.setting.noBlockBreakParticles.description"))
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> noFallingBlocks = sgGeneral.add(new BoolSetting.Builder()
            .name("no-falling-blocks")
            .displayName(I18n.translate("Modules.NoRender.setting.noFallingBlocks.displayName"))
            .description(I18n.translate("Modules.NoRender.setting.noFallingBlocks.description"))
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> noPotionIcons = sgGeneral.add(new BoolSetting.Builder()
            .name("no-potion-icons")
            .displayName(I18n.translate("Modules.NoRender.setting.noPotionIcons.displayName"))
            .description(I18n.translate("Modules.NoRender.setting.noPotionIcons.description"))
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> noArmorStands = sgGeneral.add(new BoolSetting.Builder()
            .name("no-armor-stands")
            .displayName(I18n.translate("Modules.NoRender.setting.noArmorStands.displayName"))
            .description(I18n.translate("Modules.NoRender.setting.noArmorStands.description"))
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> noGuiBackground = sgGeneral.add(new BoolSetting.Builder()
            .name("no-gui-background")
            .displayName(I18n.translate("Modules.NoRender.setting.noGuiBackground.displayName"))
            .description(I18n.translate("Modules.NoRender.setting.noGuiBackground.description"))
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> noXpOrbs = sgGeneral.add(new BoolSetting.Builder()
            .name("no-xp-orbs")
            .displayName(I18n.translate("Modules.NoRender.setting.noXpOrbs.displayName"))
            .description(I18n.translate("Modules.NoRender.setting.noXpOrbs.description"))
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> noEatParticles = sgGeneral.add(new BoolSetting.Builder()
            .name("no-eating-particles")
            .displayName(I18n.translate("Modules.NoRender.setting.noEatParticles.displayName"))
            .description(I18n.translate("Modules.NoRender.setting.noEatParticles.description"))
            .defaultValue(false)
            .build()
    );
    
    private final Setting<Boolean> noSkylightUpdates = sgGeneral.add(new BoolSetting.Builder()
            .name("no-skylight-updates")
            .displayName(I18n.translate("Modules.NoRender.setting.noSkylightUpdates.displayName"))
            .description(I18n.translate("Modules.NoRender.setting.noSkylightUpdates.description"))
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> noCrosshair = sgGeneral.add(new BoolSetting.Builder()
            .name("no-crosshair")
            .displayName(I18n.translate("Modules.NoRender.setting.noCrosshair.displayName"))
            .description(I18n.translate("Modules.NoRender.setting.noCrosshair.description"))
            .defaultValue(false)
            .build()
    );

    public NoRender() {
        super(Category.Render, "no-render", I18n.translate("Modules.NoRender.description"));
    }

    public boolean noHurtCam() {
        return isActive() && noHurtCam.get();
    }

    public boolean noWeather() {
        return isActive() && noWeather.get();
    }

    public boolean noPortalOverlay() {
        return isActive() && noPortalOverlay.get();
    }

    public boolean noPumpkinOverlay() {
        return isActive() && noPumpkinOverlay.get();
    }

    public boolean noFireOverlay() {
        return isActive() && noFireOverlay.get();
    }

    public boolean noWaterOverlay() {
        return isActive() && noWaterOverlay.get();
    }

    public boolean noVignette() {
        return isActive() && noVignette.get();
    }

    public boolean noBossBar() {
        return isActive() && noBossBar.get();
    }

    public boolean noScoreboard() {
        return isActive() && noScoreboard.get();
    }

    public boolean noFog() {
        return isActive() && noFog.get();
    }

    public boolean noTotemAnimation() {
        return isActive() && noTotemAnimation.get();
    }

    public boolean noArmor() {
        return isActive() && noArmor.get();
    }

    public boolean noNausea() {
        return isActive() && noNausea.get();
    }

    public boolean noItems() {
        return isActive() && noItems.get();
    }

    public boolean noEnchTableBook() {
        return isActive() && noEnchTableBook.get();
    }

    public boolean noSignText() {
        return isActive() && noSignText.get();
    }

    public boolean noBlockBreakParticles() {
        return isActive() && noBlockBreakParticles.get();
    }

    public boolean noFallingBlocks() {
        return isActive() && noFallingBlocks.get();
    }

    public boolean noPotionIcons() {
        return isActive() && noPotionIcons.get();
    }

    public boolean noArmorStands() {
        return isActive() && noArmorStands.get();
    }

    public boolean noGuiBackground() {
        return isActive() && noGuiBackground.get();
    }

    public boolean noXpOrbs() {
        return isActive() && noXpOrbs.get();
    }

    public boolean noEatParticles() {
        return isActive() && noEatParticles.get();
    }
    
    public boolean noSkylightUpdates() {
        return isActive() && noSkylightUpdates.get();
    }

    public boolean noCrosshair() {
        return isActive() && noCrosshair.get();
    }
}
