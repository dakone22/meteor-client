/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2020 Meteor Development.
 */

package minegame159.meteorclient.modules.render.hud;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import minegame159.meteorclient.events.render.Render2DEvent;
import minegame159.meteorclient.gui.widgets.WButton;
import minegame159.meteorclient.gui.widgets.WLabel;
import minegame159.meteorclient.gui.widgets.WTable;
import minegame159.meteorclient.gui.widgets.WWidget;
import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Module;
import minegame159.meteorclient.modules.ModuleManager;
import minegame159.meteorclient.modules.combat.*;
import minegame159.meteorclient.modules.render.hud.modules.*;
import minegame159.meteorclient.settings.*;
import minegame159.meteorclient.utils.render.AlignmentX;
import minegame159.meteorclient.utils.render.AlignmentY;
import minegame159.meteorclient.utils.render.color.Color;
import minegame159.meteorclient.utils.render.color.SettingColor;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;

public class HUD extends Module {
    private static final HudRenderer RENDERER = new HudRenderer();

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgActiveModules = settings.createGroup(I18n.translate("Modules.HUD.group.sgActiveModules"));
    private final SettingGroup sgInvViewer = settings.createGroup(I18n.translate("Modules.HUD.group.sgInvViewer"));
    private final SettingGroup sgPlayerModel = settings.createGroup(I18n.translate("Modules.HUD.group.sgPlayerModel"));
    private final SettingGroup sgArmor = settings.createGroup(I18n.translate("Modules.HUD.group.sgArmor"));
    private final SettingGroup sgModuleInfo = settings.createGroup(I18n.translate("Modules.HUD.group.sgModuleInfo"));
    private final SettingGroup sgCompass = settings.createGroup(I18n.translate("Modules.HUD.group.sgCompass"));


    private final ActiveModulesHud activeModulesHud = new ActiveModulesHud(this);
    private final ModuleInfoHud moduleInfoHud = new ModuleInfoHud(this);

    // General
    private final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
            .name("scale")
            .displayName(I18n.translate("Modules.HUD.setting.scale.displayName"))
            .description(I18n.translate("Modules.HUD.setting.scale.description"))
            .defaultValue(1)
            .min(1)
            .max(3)
            .sliderMin(1)
            .sliderMax(3)
            .build()
    );

    private final Setting<SettingColor> primaryColor = sgGeneral.add(new ColorSetting.Builder()
            .name("primary-color")
            .displayName(I18n.translate("Modules.HUD.setting.primaryColor.displayName"))
            .description(I18n.translate("Modules.HUD.setting.primaryColor.description"))
            .defaultValue(new SettingColor(255, 255, 255))
            .build()
    );

    private final Setting<SettingColor> secondaryColor = sgGeneral.add(new ColorSetting.Builder()
            .name("secondary-color")
            .displayName(I18n.translate("Modules.HUD.setting.secondaryColor.displayName"))
            .description(I18n.translate("Modules.HUD.setting.secondaryColor.description"))
            .defaultValue(new SettingColor(175, 175, 175))
            .build()
    );

    private final Setting<SettingColor> welcomeColor = sgGeneral.add(new ColorSetting.Builder()
            .name("welcome-color")
            .displayName(I18n.translate("Modules.HUD.setting.welcomeColor.displayName"))
            .description(I18n.translate("Modules.HUD.setting.welcomeColor.description"))
            .defaultValue(new SettingColor(120, 43, 153))
            .build()
    );

    // Active Modules
    private final Setting<ActiveModulesHud.Sort> activeModulesSort = sgActiveModules.add(new EnumSetting.Builder<ActiveModulesHud.Sort>()
            .name("active-modules-sort")
            .displayName(I18n.translate("Modules.HUD.setting.activeModulesSort.displayName"))
            .description(I18n.translate("Modules.HUD.setting.activeModulesSort.description"))
            .displayValues(new String[]{I18n.translate("Modules.HUD.ActiveModulesHud.Sort.Biggest"),
                                        I18n.translate("Modules.HUD.ActiveModulesHud.Sort.Smallest")})
            .defaultValue(ActiveModulesHud.Sort.Biggest)
//            .onChanged(sort -> activeModulesHud.recalculate())
            .build()
    );

    private final Setting<Boolean> activeInfo = sgActiveModules.add(new BoolSetting.Builder()
            .name("additional-info")
            .displayName(I18n.translate("Modules.HUD.setting.activeInfo.displayName"))
            .description(I18n.translate("Modules.HUD.setting.activeInfo.description"))
            .defaultValue(true)
            .build()
    );

    private final Setting<ActiveModulesHud.ColorMode> activeModulesColorMode = sgActiveModules.add(new EnumSetting.Builder<ActiveModulesHud.ColorMode>()
            .name("active-modules-color-mode")
            .displayName(I18n.translate("Modules.HUD.setting.activeModulesColorMode.displayName"))
            .description(I18n.translate("Modules.HUD.setting.activeModulesColorMode.description"))
            .displayValues(new String[]{I18n.translate("Modules.HUD.ActiveModulesHud.ColorMode.Flat"),
                                        I18n.translate("Modules.HUD.ActiveModulesHud.ColorMode.Random"),
                                        I18n.translate("Modules.HUD.ActiveModulesHud.ColorMode.Rainbow")})
            .defaultValue(ActiveModulesHud.ColorMode.Rainbow)
            .build()
    );

    private final Setting<SettingColor> activeModulesFlatColor = sgActiveModules.add(new ColorSetting.Builder()
            .name("active-modules-flat-color")
            .displayName(I18n.translate("Modules.HUD.setting.activeModulesFlatColor.displayName"))
            .description(I18n.translate("Modules.HUD.setting.activeModulesFlatColor.description"))
            .defaultValue(new SettingColor(225, 25, 25))
            .build()
    );

    private final Setting<Double> activeModulesRainbowSpeed = sgActiveModules.add(new DoubleSetting.Builder()
            .name("active-modules-rainbow-speed")
            .displayName(I18n.translate("Modules.HUD.setting.activeModulesRainbowSpeed.displayName"))
            .description(I18n.translate("Modules.HUD.setting.activeModulesRainbowSpeed.description"))
            .defaultValue(0.05)
            .sliderMax(0.1)
            .decimalPlaces(4)
            .build()
    );

    private final Setting<Double> activeModulesRainbowSpread = sgActiveModules.add(new DoubleSetting.Builder()
            .name("active-modules-rainbow-spread")
            .displayName(I18n.translate("Modules.HUD.setting.activeModulesRainbowSpread.displayName"))
            .description(I18n.translate("Modules.HUD.setting.activeModulesRainbowSpread.description"))
            .defaultValue(0.025)
            .sliderMax(0.05)
            .decimalPlaces(4)
            .build()
    );

    // Inventory Viewer
    private final Setting<InventoryViewerHud.Background> invViewerBackground = sgInvViewer.add(new EnumSetting.Builder<InventoryViewerHud.Background>()
            .name("inventory-viewer-background")
            .displayName(I18n.translate("Modules.HUD.setting.invViewerBackground.displayName"))
            .description(I18n.translate("Modules.HUD.setting.invViewerBackground.description"))
            .displayValues(new String[]{
                    I18n.translate("Modules.HUD.InventoryViewerHud.Background.None"),
                    I18n.translate("Modules.HUD.InventoryViewerHud.Background.Light"),
                    I18n.translate("Modules.HUD.InventoryViewerHud.Background.LightTransparent"),
                    I18n.translate("Modules.HUD.InventoryViewerHud.Background.Dark"),
                    I18n.translate("Modules.HUD.InventoryViewerHud.Background.DarkTransparent"),
                    I18n.translate("Modules.HUD.InventoryViewerHud.Background.Flat")})
            .defaultValue(InventoryViewerHud.Background.Light)
            .build()
    );

    private final Setting<SettingColor> invViewerColor = sgInvViewer.add(new ColorSetting.Builder()
            .name("flat-mode-color")
            .displayName(I18n.translate("Modules.HUD.setting.invViewerColor.displayName"))
            .description(I18n.translate("Modules.HUD.setting.invViewerColor.description"))
            .defaultValue(new SettingColor(0, 0, 0, 64))
            .build()
    );

    private final Setting<Double> invViewerScale = sgInvViewer.add(new DoubleSetting.Builder()
            .name("inventory-viewer-scale")
            .displayName(I18n.translate("Modules.HUD.setting.invViewerScale.displayName"))
            .description(I18n.translate("Modules.HUD.setting.invViewerScale.description"))
            .defaultValue(2)
            .min(1)
            .max(4)
            .sliderMin(1)
            .sliderMax(4)
            .build()
    );

    // Player Model
    private final Setting<Double> playerModelScale = sgPlayerModel.add(new DoubleSetting.Builder()
            .name("player-model-scale")
            .displayName(I18n.translate("Modules.HUD.setting.playerModelScale.displayName"))
            .description(I18n.translate("Modules.HUD.setting.playerModelScale.description"))
            .defaultValue(2)
            .min(1)
            .sliderMin(1)
            .sliderMax(4)
            .build()
    );

    private final Setting<Boolean> copyYaw = sgPlayerModel.add(new BoolSetting.Builder()
            .name("copy-yaw")
            .displayName(I18n.translate("Modules.HUD.setting.copyYaw.displayName"))
            .description(I18n.translate("Modules.HUD.setting.copyYaw.description"))
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> copyPitch = sgPlayerModel.add(new BoolSetting.Builder()
            .name("copy-pitch")
            .displayName(I18n.translate("Modules.HUD.setting.copyPitch.displayName"))
            .description(I18n.translate("Modules.HUD.setting.copyPitch.description"))
            .defaultValue(true)
            .build()
    );

    private final Setting<Integer> customYaw = sgPlayerModel.add(new IntSetting.Builder()
            .name("custom-yaw")
            .displayName(I18n.translate("Modules.HUD.setting.customYaw.displayName"))
            .description(I18n.translate("Modules.HUD.setting.customYaw.description"))
            .defaultValue(0)
            .min(-180)
            .max(180)
            .sliderMin(-180)
            .sliderMax(180)
            .build()
    );

    private final Setting<Integer> customPitch = sgPlayerModel.add(new IntSetting.Builder()
            .name("custom-pitch")
            .displayName(I18n.translate("Modules.HUD.setting.customPitch.displayName"))
            .description(I18n.translate("Modules.HUD.setting.customPitch.description"))
            .defaultValue(0)
            .min(-180)
            .max(180)
            .sliderMin(-180)
            .sliderMax(180)
            .build()
    );

    private final Setting<Boolean> playerModelBackground = sgPlayerModel.add(new BoolSetting.Builder()
            .name("player-model-background")
            .displayName(I18n.translate("Modules.HUD.setting.playerModelBackground.displayName"))
            .description(I18n.translate("Modules.HUD.setting.playerModelBackground.description"))
            .defaultValue(true)
            .build()
    );

    private final Setting<SettingColor> playerModelColor = sgPlayerModel.add(new ColorSetting.Builder()
            .name("player-model-background-color")
            .displayName(I18n.translate("Modules.HUD.setting.playerModelColor.displayName"))
            .description(I18n.translate("Modules.HUD.setting.playerModelColor.description"))
            .defaultValue(new SettingColor(0, 0, 0, 64))
            .build()
    );

    // Armor
    private final Setting<Boolean> armorFlip = sgArmor.add(new BoolSetting.Builder()
            .name("armor-flip-order")
            .displayName(I18n.translate("Modules.HUD.setting.armorFlip.displayName"))
            .description(I18n.translate("Modules.HUD.setting.armorFlip.description"))
            .defaultValue(true)
            .build()
    );

    private final Setting<ArmorHud.Orientation> armorOrientation = sgArmor.add(new EnumSetting.Builder<ArmorHud.Orientation>()
            .name("orientation")
            .displayName(I18n.translate("Modules.HUD.setting.armorOrientation.displayName"))
            .description(I18n.translate("Modules.HUD.setting.armorOrientation.description"))
            .displayValues(new String[]{I18n.translate("Modules.HUD.ArmorHud.Orientation.Horizontal"),
                                        I18n.translate("Modules.HUD.ArmorHud.Orientation.Vertical")})
            .defaultValue(ArmorHud.Orientation.Horizontal)
            .build()
    );


    private final Setting<ArmorHud.Durability> armorDurability = sgArmor.add(new EnumSetting.Builder<ArmorHud.Durability>()
            .name("armor-durability")
            .displayName(I18n.translate("Modules.HUD.setting.armorDurability.displayName"))
            .description(I18n.translate("Modules.HUD.setting.armorDurability.description"))
            .displayValues(new String[]{I18n.translate("Modules.HUD.ArmorHud.Durability.None"),
                                        I18n.translate("Modules.HUD.ArmorHud.Durability.Default"),
                                        I18n.translate("Modules.HUD.ArmorHud.Durability.Numbers"),
                                        I18n.translate("Modules.HUD.ArmorHud.Durability.Percentage")})
            .defaultValue(ArmorHud.Durability.Default)
            .build()
    );

    private final Setting<Double> armorScale = sgArmor.add(new DoubleSetting.Builder()
            .name("armor-scale")
            .displayName(I18n.translate("Modules.HUD.setting.armorScale.displayName"))
            .description(I18n.translate("Modules.HUD.setting.armorScale.description"))
            .defaultValue(3.5)
            .min(2)
            .sliderMin(2)
            .sliderMax(5)
            .build()
    );

    // Module Info
    private final Setting<List<Module>> moduleInfoModules = sgModuleInfo.add(new ModuleListSetting.Builder()
            .name("module-info-modules")
            .displayName(I18n.translate("Modules.HUD.setting.moduleInfoModules.displayName"))
            .description(I18n.translate("Modules.HUD.setting.moduleInfoModules.description"))
            .defaultValue(moduleInfoModulesDefaultValue())
//            .onChanged(toggleModules -> moduleInfoHud.recalculate())
            .build()
    );

    private final Setting<Boolean> moduleInfo = sgModuleInfo.add(new BoolSetting.Builder()
            .name("additional-info")
            .displayName(I18n.translate("Modules.HUD.setting.moduleInfo.displayName"))
            .description(I18n.translate("Modules.HUD.setting.moduleInfo.description"))
            .defaultValue(true)
            .build()
    );

    private final Setting<SettingColor> moduleInfoOnColor = sgModuleInfo.add(new ColorSetting.Builder()
            .name("module-info-on-color")
            .displayName(I18n.translate("Modules.HUD.setting.moduleInfoOnColor.displayName"))
            .description(I18n.translate("Modules.HUD.setting.moduleInfoOnColor.description"))
            .defaultValue(new SettingColor(25, 225, 25))
            .build()
    );

    private final Setting<SettingColor> moduleInfoOffColor = sgModuleInfo.add(new ColorSetting.Builder()
            .name("module-info-off-color")
            .displayName(I18n.translate("Modules.HUD.setting.moduleInfoOffColor.displayName"))
            .description(I18n.translate("Modules.HUD.setting.moduleInfoOffColor.description"))
            .defaultValue(new SettingColor(225, 25, 25))
            .build()
    );

    //Compass

    private final Setting<CompassHud.Mode> compassMode = sgCompass.add(new EnumSetting.Builder<CompassHud.Mode>()
            .name("compass-mode")
            .displayName(I18n.translate("Modules.HUD.setting.compassMode.displayName"))
            .description(I18n.translate("Modules.HUD.setting.compassMode.description"))
            .displayValues(new String[]{I18n.translate("Modules.HUD.CompassHud.Mode.Axis"),
                                        I18n.translate("Modules.HUD.CompassHud.Mode.Pole")})
            .defaultValue(CompassHud.Mode.Pole)
            .build()
    );

    private final Setting<Double> compassScale = sgCompass.add(new DoubleSetting.Builder()
            .name("compass-scale")
            .displayName(I18n.translate("Modules.HUD.setting.compassScale.displayName"))
            .description(I18n.translate("Modules.HUD.setting.compassScale.description"))
            .defaultValue(1)
            .sliderMin(2)
            .sliderMax(4)
            .build()
    );

    public final List<HudModule> modules = new ArrayList<>();

    public HUD() {
        super(Category.Render, "HUD", I18n.translate("Modules.HUD.description"));

        init();
    }

    private static List<Module> moduleInfoModulesDefaultValue() {
        List<Module> modules = new ArrayList<>();
        modules.add(ModuleManager.INSTANCE.get(KillAura.class));
        modules.add(ModuleManager.INSTANCE.get(CrystalAura.class));
        modules.add(ModuleManager.INSTANCE.get(AnchorAura.class));
        modules.add(ModuleManager.INSTANCE.get(BedAura.class));
        modules.add(ModuleManager.INSTANCE.get(Surround.class));
        return modules;
    }

    private void init() {
        modules.clear();
        RENDERER.begin(scale(), 0, true);

        // Top Left
        HudModuleLayer topLeft = new HudModuleLayer(RENDERER, modules, AlignmentX.Left, AlignmentY.Top, 2, 2);
        topLeft.add(new WatermarkHud(this));
        topLeft.add(new FpsHud(this));
        topLeft.add(new PingHud(this));
        topLeft.add(new TpsHud(this));
        topLeft.add(new SpeedHud(this));
        topLeft.add(new BiomeHud(this));
        topLeft.add(new TimeHud(this));
        topLeft.add(new DurabilityHud(this));
        topLeft.add(new BreakingBlockHud(this));
        topLeft.add(new LookingAtHud(this));
        topLeft.add(moduleInfoHud);
        topLeft.add(new InfiniteMinerHud(this));

        // Top Center
        HudModuleLayer topCenter = new HudModuleLayer(RENDERER, modules, AlignmentX.Center, AlignmentY.Top, 0, 2);
        topCenter.add(new InventoryViewerHud(this));
        topCenter.add(new WelcomeHud(this));
        topCenter.add(new LagNotifierHud(this));

        // Top Right
        HudModuleLayer topRight = new HudModuleLayer(RENDERER, modules, AlignmentX.Right, AlignmentY.Top, 2, 2);
        topRight.add(activeModulesHud);

        // Bottom Left
        HudModuleLayer bottomLeft = new HudModuleLayer(RENDERER, modules, AlignmentX.Left, AlignmentY.Bottom, 2, 2);
        bottomLeft.add(new PlayerModelHud(this));

        // Bottom Center
        HudModuleLayer bottomCenter = new HudModuleLayer(RENDERER, modules, AlignmentX.Center, AlignmentY.Bottom, 48, 64);
        bottomCenter.add(new ArmorHud(this));
        bottomCenter.add(new CompassHud(this));
        bottomCenter.add(new TotemHud(this));

        // Bottom Right
        HudModuleLayer bottomRight = new HudModuleLayer(RENDERER, modules, AlignmentX.Right, AlignmentY.Bottom, 2, 2);
        bottomRight.add(new PositionHud(this));
        bottomRight.add(new RotationHud(this));
        bottomRight.add(new PotionTimersHud(this));

        RENDERER.end();
    }

    @EventHandler
    public final Listener<Render2DEvent> onRender = new Listener<>(event -> {
        if (mc.options.debugEnabled) return;

        RENDERER.begin(scale(), event.tickDelta, false);

        for (HudModule module : modules) {
            if (module.active || mc.currentScreen instanceof HudEditorScreen) {
                module.update(RENDERER);
                module.render(RENDERER);
            }
        }

        RENDERER.end();
    });

    @Override
    public WWidget getWidget() {
        WTable table = new WTable();

        WButton reset = table.add(new WButton(I18n.translate("Modules.HUD.buttons.Reset"))).getWidget();
        reset.action = this::init;
        table.add(new WLabel(I18n.translate("Modules.HUD.buttons.Reset.description")));
        table.row();

        WButton editor = table.add(new WButton(I18n.translate("Modules.HUD.buttons.Editor"))).getWidget();
        editor.action = () -> mc.openScreen(new HudEditorScreen());
        table.add(new WLabel(I18n.translate("Modules.HUD.buttons.Editor.description")));

        return table;
    }

    @Override
    public CompoundTag toTag() {
        CompoundTag tag = super.toTag();

        ListTag modulesTag = new ListTag();
        for (HudModule module : modules) modulesTag.add(module.toTag());
        tag.put("modules", modulesTag);

        return tag;
    }

    @Override
    public Module fromTag(CompoundTag tag) {
        if (tag.contains("modules")) {
            ListTag modulesTag = tag.getList("modules", 10);

            for (Tag t : modulesTag) {
                CompoundTag moduleTag = (CompoundTag) t;

                HudModule module = getModule(moduleTag.getString("name"));
                if (module != null) module.fromTag(moduleTag);
            }
        }

        return super.fromTag(tag);
    }

    private HudModule getModule(String name) {
        for (HudModule module : modules) {
            if (module.name.equals(name)) return module;
        }

        return null;
    }

    public double scale() {
        return scale.get();
    }
    public Color primaryColor() {
        return primaryColor.get();
    }
    public Color secondaryColor() {
        return secondaryColor.get();
    }
    public Color welcomeColor() {
        return welcomeColor.get();
    }

    public ActiveModulesHud.Sort activeModulesSort() {
        return activeModulesSort.get();
    }
    public boolean activeInfo() {
        return activeInfo.get();
    }
    public ActiveModulesHud.ColorMode activeModulesColorMode() {
        return activeModulesColorMode.get();
    }
    public SettingColor activeModulesFlatColor() {
        return activeModulesFlatColor.get();
    }
    public double activeModulesRainbowSpeed() {
        return activeModulesRainbowSpeed.get();
    }
    public double activeModulesRainbowSpread() {
        return activeModulesRainbowSpread.get();
    }

    public InventoryViewerHud.Background invViewerBackground() {
        return invViewerBackground.get();
    }
    public Color invViewerColor() {
        return invViewerColor.get();
    }
    public double invViewerScale() {
        return invViewerScale.get();
    }

    public double playerModelScale() {
        return playerModelScale.get();
    }
    public boolean playerModelCopyYaw() {
        return copyYaw.get();
    }
    public boolean playerModelCopyPitch() {
        return copyPitch.get();
    }
    public int playerModelCustomYaw() {
        return customYaw.get();
    }
    public int playerModelCustomPitch() {
        return customPitch.get();
    }
    public boolean playerModelBackground() {
        return playerModelBackground.get();
    }
    public Color playerModelColor() {
        return playerModelColor.get();
    }


    public boolean armorFlip() {
        return armorFlip.get();
    }
    public ArmorHud.Orientation armorOrientation() {
        return armorOrientation.get();
    }
    public ArmorHud.Durability armorDurability() {
        return armorDurability.get();
    }
    public double armorScale() {
        return armorScale.get();
    }

    public List<Module> moduleInfoModules() {
        return moduleInfoModules.get();
    }
    public boolean moduleInfo() {
        return moduleInfo.get();
    }
    public Color moduleInfoOnColor() {
        return moduleInfoOnColor.get();
    }
    public Color moduleInfoOffColor() {
        return moduleInfoOffColor.get();
    }

    public double compassScale() {
        return compassScale.get();
    }
    public CompassHud.Mode compassMode() {
        return compassMode.get();
    }
}
