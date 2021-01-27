package minegame159.meteorclient.modules.misc;

import minegame159.meteorclient.modules.Category;
import minegame159.meteorclient.modules.Module;
import minegame159.meteorclient.settings.BoolSetting;
import minegame159.meteorclient.settings.IntSetting;
import minegame159.meteorclient.settings.Setting;
import minegame159.meteorclient.settings.SettingGroup;
import minegame159.meteorclient.utils.misc.ThreadUtils;
import minegame159.meteorclient.utils.player.ChatUtils;
import minegame159.meteorclient.utils.player.InvUtils;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

import java.util.concurrent.ThreadLocalRandom;

public class AutoSteal extends Module {
    public AutoSteal() {
        super(Category.Player, "auto-steal", I18n.translate("Modules.AutoSteal.description"));
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgDelays = settings.createGroup(I18n.translate("Modules.AutoSteal.group.sgDelays"));

    // General

    private final Setting<Boolean> stealButtonEnabled = sgGeneral.add(new BoolSetting.Builder()
            .name("steal-button-enabled")
            .displayName(I18n.translate("Modules.AutoSteal.setting.stealButtonEnabled.displayName"))
            .description(I18n.translate("Modules.AutoSteal.setting.stealButtonEnabled.description"))
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> dumpButtonEnabled = sgGeneral.add(new BoolSetting.Builder()
            .name("dump-button-enabled")
            .displayName(I18n.translate("Modules.AutoSteal.setting.dumpButtonEnabled.displayName"))
            .description(I18n.translate("Modules.AutoSteal.setting.dumpButtonEnabled.description"))
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> autoStealEnabled = sgGeneral.add(new BoolSetting.Builder()
            .name("auto-steal-enabled")
            .displayName(I18n.translate("Modules.AutoSteal.setting.autoStealEnabled.displayName"))
            .description(I18n.translate("Modules.AutoSteal.setting.autoStealEnabled.description"))
            .defaultValue(false)
            .onChanged((bool_1) -> checkAutoSettings())
            .build()
    );

    private final Setting<Boolean> autoDumpEnabled = sgGeneral.add(new BoolSetting.Builder()
            .name("auto-dump-enabled")
            .displayName(I18n.translate("Modules.AutoSteal.setting.autoDumpEnabled.displayName"))
            .description(I18n.translate("Modules.AutoSteal.setting.autoDumpEnabled.description"))
            .defaultValue(false)
            .onChanged((bool_1) -> checkAutoSettings())
            .build()
    );

    // Delay

    private final Setting<Integer> minimumDelay = sgDelays.add(new IntSetting.Builder()
            .name("min-delay")
            .displayName(I18n.translate("Modules.AutoSteal.setting.minimumDelay.displayName"))
            .description(I18n.translate("Modules.AutoSteal.setting.minimumDelay.description"))
            .sliderMax(1000)
            .defaultValue(180)
            .build()
    );

    private final Setting<Integer> randomDelay = sgDelays.add(new IntSetting.Builder()
            .name("random-delay")
            .displayName(I18n.translate("Modules.AutoSteal.setting.randomDelay.displayName"))
            .description(I18n.translate("Modules.AutoSteal.setting.randomDelay.description")) // Actually ms - 1, due to the RNG excluding upper bound
            .min(0)
            .sliderMax(1000)
            .defaultValue(50)
            .build()
    );

    private void checkAutoSettings() {
        if (autoStealEnabled.get() && autoDumpEnabled.get()) {
            ChatUtils.error("You can't enable Auto Steal and Auto Dump at the same time!");
            autoDumpEnabled.set(false);
        }
    }

    private int getSleepTime() {
        return minimumDelay.get() + (randomDelay.get() > 0 ? ThreadLocalRandom.current().nextInt(0, randomDelay.get()) : 0);
    }

    private int getRows(ScreenHandler handler) {
        return (handler instanceof GenericContainerScreenHandler ? ((GenericContainerScreenHandler) handler).getRows() : 3);
    }

    private void moveSlots(ScreenHandler handler, int start, int end) {
        for (int i = start; i < end; i++) {
            if (!handler.getSlot(i).hasStack())
                continue;

            int sleep = getSleepTime();
            if (sleep > 0)
                ThreadUtils.sleep(sleep);

            // Exit if user closes screen
            if (mc.currentScreen == null)
                break;

            InvUtils.clickSlot(i, 0, SlotActionType.QUICK_MOVE);
        }
    }

    /**
     * Thread-blocking operation to steal from containers. You REALLY should use {@link #stealAsync(ScreenHandler)}
     *
     * @param handler Passed in from {@link minegame159.meteorclient.mixin.GenericContainerScreenMixin}
     */
    private void steal(ScreenHandler handler) {
        moveSlots(handler, 0, getRows(handler) * 9);
    }

    /**
     * Thread-blocking operation to dump to containers. You REALLY should use {@link #dumpAsync(ScreenHandler)}
     *
     * @param handler Passed in from {@link minegame159.meteorclient.mixin.GenericContainerScreenMixin}
     */
    private void dump(ScreenHandler handler) {
        int playerInvOffset = getRows(handler) * 9;
        moveSlots(handler, playerInvOffset, playerInvOffset + 4 * 9);
    }

    /**
     * Runs {@link #steal(ScreenHandler)} in a separate thread
     *
     * @param handler Passed in from {@link minegame159.meteorclient.mixin.GenericContainerScreenMixin}
     */
    public void stealAsync(ScreenHandler handler) {
        ThreadUtils.runInThread(() -> steal(handler));
    }

    /**
     * Runs {@link #dump(ScreenHandler)} in a separate thread
     *
     * @param handler Passed in from {@link minegame159.meteorclient.mixin.GenericContainerScreenMixin}
     */
    public void dumpAsync(ScreenHandler handler) {
        ThreadUtils.runInThread(() -> dump(handler));
    }

    public boolean getStealButtonEnabled() {
        return stealButtonEnabled.get();
    }

    public boolean getDumpButtonEnabled() {
        return dumpButtonEnabled.get();
    }

    public boolean getAutoStealEnabled() {
        return autoStealEnabled.get();
    }

    public boolean getAutoDumpEnabled() {
        return autoDumpEnabled.get();
    }

}
