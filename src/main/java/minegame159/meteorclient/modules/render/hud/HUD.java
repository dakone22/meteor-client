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
import minegame159.meteorclient.modules.ModuleManager;
import minegame159.meteorclient.modules.ToggleModule;
import minegame159.meteorclient.modules.combat.*;
import minegame159.meteorclient.modules.render.hud.modules.*;
import minegame159.meteorclient.settings.*;
import minegame159.meteorclient.utils.AlignmentX;
import minegame159.meteorclient.utils.AlignmentY;
import minegame159.meteorclient.utils.Color;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;

public class HUD extends ToggleModule {
    private static final HudRenderer RENDERER = new HudRenderer();

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgActiveModules = settings.createGroup("minegame159.meteorclient.modules.render.hud.HUD.sgActiveModules");
    private final SettingGroup sgInvViewer = settings.createGroup("minegame159.meteorclient.modules.render.hud.HUD.sgInvViewer");
    private final SettingGroup sgPlayerModel = settings.createGroup("minegame159.meteorclient.modules.render.hud.HUD.sgPlayerModel");
    private final SettingGroup sgArmor = settings.createGroup("minegame159.meteorclient.modules.render.hud.HUD.sgArmor");
    private final SettingGroup sgModuleInfo = settings.createGroup("minegame159.meteorclient.modules.render.hud.HUD.sgModuleInfo");
    private final SettingGroup sgCompass = settings.createGroup("minegame159.meteorclient.modules.render.hud.HUD.sgCompass");


    private final ActiveModulesHud activeModulesHud = new ActiveModulesHud(this);
    private final ModuleInfoHud moduleInfoHud = new ModuleInfoHud(this);

    // General
    private final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
            .name("scale")
            .description("minegame159.meteorclient.modules.render.hud.HUD.sgGeneral.scale")
            .defaultValue(1)
            .min(1)
            .max(3)
            .sliderMin(1)
            .sliderMax(3)
            .build()
    );

    private final Setting<Color> primaryColor = sgGeneral.add(new ColorSetting.Builder()
            .name("primary-color")
            .description("minegame159.meteorclient.modules.render.hud.HUD.sgGeneral.primaryColor.description")
            .defaultValue(new Color(255, 255, 255))
            .build()
    );

    private final Setting<Color> secondaryColor = sgGeneral.add(new ColorSetting.Builder()
            .name("secondary-color")
            .description("minegame159.meteorclient.modules.render.hud.HUD.sgGeneral.secondaryColor.description")
            .defaultValue(new Color(175, 175, 175))
            .build()
    );

    private final Setting<Color> welcomeColor = sgGeneral.add(new ColorSetting.Builder()
            .name("welcome-color")
            .description("minegame159.meteorclient.modules.render.hud.HUD.sgGeneral.welcomeColor.description")
            .defaultValue(new Color(120, 43, 153))
            .build()
    );

    // Active Modules
    private final Setting<ActiveModulesHud.Sort> activeModulesSort = sgActiveModules.add(new EnumSetting.Builder<ActiveModulesHud.Sort>()
            .name("active-modules-sort")
            .description("minegame159.meteorclient.modules.render.hud.HUD.sgActiveModules.activeModulesSort.description")
            .defaultValue(ActiveModulesHud.Sort.ByBiggest)
            .onChanged(sort -> activeModulesHud.recalculate())
            .build()
    );

    // Inventory Viewer
    private final Setting<InventoryViewerHud.Background> invViewerBackground = sgInvViewer.add(new EnumSetting.Builder<InventoryViewerHud.Background>()
            .name("inventory-viewer-background")
            .description("minegame159.meteorclient.modules.render.hud.HUD.sgInvViewer.invViewerBackground.description")
            .defaultValue(InventoryViewerHud.Background.Light)
            .build()
    );

    private final Setting<Color> invViewerColor = sgInvViewer.add(new ColorSetting.Builder()
            .name("flat-mode-color")
            .description("minegame159.meteorclient.modules.render.hud.HUD.sgInvViewer.invViewerColor.description")
            .defaultValue(new Color(0, 0, 0, 64))
            .build()
    );

    private final Setting<Double> invViewerScale = sgInvViewer.add(new DoubleSetting.Builder()
            .name("inventory-viewer-scale")
            .description("minegame159.meteorclient.modules.render.hud.HUD.sgInvViewer.invViewerScale.description")
            .defaultValue(2)
            .min(1)
            .max(4)
            .sliderMin(1)
            .sliderMax(4)
            .build()
    );

    // Player Model
    private final Setting<Boolean> playerModelBackground = sgPlayerModel.add(new BoolSetting.Builder()
            .name("player-model-background")
            .description("minegame159.meteorclient.modules.render.hud.HUD.sgPlayerModel.playerModelBackground.description")
            .defaultValue(true)
            .build()
    );

    private final Setting<Color> playerModelColor = sgPlayerModel.add(new ColorSetting.Builder()
            .name("player-model-background-color")
            .description("minegame159.meteorclient.modules.render.hud.HUD.sgPlayerModel.playerModelColor.description")
            .defaultValue(new Color(0, 0, 0, 64))
            .build()
    );

    private final Setting<Double> playerModelScale = sgPlayerModel.add(new DoubleSetting.Builder()
            .name("player-model-scale")
            .description("minegame159.meteorclient.modules.render.hud.HUD.sgPlayerModel.playerModelScale.description")
            .defaultValue(2)
            .min(1)
            .max(4)
            .sliderMin(1)
            .sliderMax(4)
            .build()
    );

    // Armor
    private final Setting<Boolean> armorFlip = sgArmor.add(new BoolSetting.Builder()
            .name("armor-flip-order")
            .description("minegame159.meteorclient.modules.render.hud.HUD.sgArmor.armorFlip.description")
            .defaultValue(true)
            .build()
    );

    private final Setting<ArmorHud.Durability> armorDurability = sgArmor.add(new EnumSetting.Builder<ArmorHud.Durability>()
            .name("armor-durability")
            .description("minegame159.meteorclient.modules.render.hud.HUD.sgArmor.armorDurability.description")
            .defaultValue(ArmorHud.Durability.Default)
            .build()
    );

    private final Setting<Double> armorScale = sgArmor.add(new DoubleSetting.Builder()
            .name("armor-scale")
            .description("minegame159.meteorclient.modules.render.hud.HUD.sgArmor.armorScale.description")
            .defaultValue(2)
            .min(2)
            .max(4)
            .sliderMin(2)
            .sliderMax(4)
            .build()
    );

    // Module Info
    private final Setting<List<ToggleModule>> moduleInfoModules = sgModuleInfo.add(new ModuleListSetting.Builder()
            .name("module-info-modules")
            .description("minegame159.meteorclient.modules.render.hud.HUD.sgModuleInfo.moduleInfoModules.description")
            .defaultValue(moduleInfoModulesDefaultValue())
            .onChanged(toggleModules -> moduleInfoHud.recalculate())
            .build()
    );

    private final Setting<Color> moduleInfoOnColor = sgModuleInfo.add(new ColorSetting.Builder()
            .name("module-info-on-color")
            .description("minegame159.meteorclient.modules.render.hud.HUD.sgModuleInfo.moduleInfoOnColor.description")
            .defaultValue(new Color(25, 225, 25))
            .build()
    );

    private final Setting<Color> moduleInfoOffColor = sgModuleInfo.add(new ColorSetting.Builder()
            .name("module-info-off-color")
            .description("minegame159.meteorclient.modules.render.hud.HUD.sgModuleInfo.moduleInfoOffColor.description")
            .defaultValue(new Color(225, 25, 25))
            .build()
    );

    //Compass

    private final Setting<CompassHud.Mode> compassMode = sgCompass.add(new EnumSetting.Builder<CompassHud.Mode>()
            .name("inventory-viewer-background")
            .description("minegame159.meteorclient.modules.render.hud.HUD.sgCompass.compassMode.description")
            .defaultValue(CompassHud.Mode.Pole)
            .build()
    );

    private final Setting<Double> compassScale = sgCompass.add(new DoubleSetting.Builder()
            .name("compass-scale")
            .description("minegame159.meteorclient.modules.render.hud.HUD.sgCompass.compassScale.description")
            .defaultValue(1)
            .sliderMin(2)
            .sliderMax(4)
            .build()
    );

    public final List<HudModule> modules = new ArrayList<>();

    public HUD() {
        super(Category.Render, "HUD", "minegame159.meteorclient.modules.render.hud.HUD.description");

        init();
    }

    private static List<ToggleModule> moduleInfoModulesDefaultValue() {
        List<ToggleModule> modules = new ArrayList<>();
        modules.add(ModuleManager.INSTANCE.get(KillAura.class));
        modules.add(ModuleManager.INSTANCE.get(CrystalAura.class));
        modules.add(ModuleManager.INSTANCE.get(AnchorAura.class));
        modules.add(ModuleManager.INSTANCE.get(BedAura.class));
        modules.add(ModuleManager.INSTANCE.get(Surround.class));
        return modules;
    }

    private void init() {
        modules.clear();
        RENDERER.setScale(scale());

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
        topLeft.add(new InfiniteMineHud(this));

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

        // Bottom Right
        HudModuleLayer bottomRight = new HudModuleLayer(RENDERER, modules, AlignmentX.Right, AlignmentY.Bottom, 2, 2);
        bottomRight.add(new PositionHud(this));
        bottomRight.add(new RotationHud(this));
        bottomRight.add(new PotionTimersHud(this));
    }

    @EventHandler
    public final Listener<Render2DEvent> onRender = new Listener<>(event -> {
        if (mc.options.debugEnabled) return;

        RENDERER.begin(scale());

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

        WButton reset = table.add(new WButton("minegame159.meteorclient.modules.render.hud.HUD.buttons.reset")).getWidget();
        reset.action = this::init;
        table.add(new WLabel("minegame159.meteorclient.modules.render.hud.HUD.buttons.reset.description"));
        table.row();

        WButton editor = table.add(new WButton("minegame159.meteorclient.modules.render.hud.HUD.buttons.editor")).getWidget();
        editor.action = () -> mc.openScreen(new HudEditorScreen());
        table.add(new WLabel("minegame159.meteorclient.modules.render.hud.HUD.buttons.editor.description"));

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
    public ToggleModule fromTag(CompoundTag tag) {
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

    public InventoryViewerHud.Background invViewerBackground() {
        return invViewerBackground.get();
    }
    public Color invViewerColor() {
        return invViewerColor.get();
    }
    public double invViewerScale() {
        return invViewerScale.get();
    }

    public boolean playerModelBackground() {
        return playerModelBackground.get();
    }
    public Color playerModelColor() {
        return playerModelColor.get();
    }
    public double playerModelScale() {
        return playerModelScale.get();
    }

    public boolean armorFlip() {
        return armorFlip.get();
    }
    public ArmorHud.Durability armorDurability() {
        return armorDurability.get();
    }
    public double armorScale() {
        return armorScale.get();
    }

    public List<ToggleModule> moduleInfoModules() {
        return moduleInfoModules.get();
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