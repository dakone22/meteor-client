package minegame159.meteorclient.gui.widgets;

import minegame159.meteorclient.utils.Utils;
import net.minecraft.client.resource.language.I18n;

public class WKeybind extends WTable {
    public Runnable action;
    public Runnable actionOnSet;

    private final WLabel label;

    private int key;
    private boolean listening;

    public WKeybind(int key) {
        this.key = key;

        label = add(new WLabel("")).getWidget();
        WButton set = add(new WButton(I18n.translate("ModuleScreen.WKeybind.buttons.Set"))).getWidget();
        WButton reset = add(new WButton(I18n.translate("ModuleScreen.WKeybind.buttons.Reset"))).getWidget();

        set.action = () -> {
            listening = true;
            label.setText(I18n.translate("ModuleScreen.WKeybind.bind.setting"));

            if (actionOnSet != null) actionOnSet.run();
        };

        reset.action = () -> {
            set(-1);

            if (action != null) action.run();
        };

        setLabelToKey();
    }

    public void onKey(int key) {
        if (listening) {
            set(key);

            if (action != null) action.run();
        }
    }

    public void set(int key) {
        this.key = key;
        listening = false;

        setLabelToKey();
    }

    public int get() {
        return key;
    }

    private void setLabelToKey() {
        label.setText(I18n.translate("ModuleScreen.WKeybind.bind") + (key == -1 ? I18n.translate("ModuleScreen.WKeybind.bind.none") :  Utils.getKeyName(key)));
    }
}
