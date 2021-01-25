package minegame159.meteorclient.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import minegame159.meteorclient.commands.Command;
import minegame159.meteorclient.commands.arguments.ModuleArgumentType;
import minegame159.meteorclient.modules.Module;
import minegame159.meteorclient.settings.Setting;
import minegame159.meteorclient.settings.SettingGroup;
import minegame159.meteorclient.utils.player.ChatUtils;
import net.minecraft.command.CommandSource;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class Settings extends Command {  // TODO: grammar (maybe should call it `Setting`?)
    public Settings() {
        super("settings", "Manage settings of modules.");  // TODO: grammar
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("module", ModuleArgumentType.module())//.then(argument("setting", SettingArgumentType.setting())))
                .executes(context -> {
                            Module m = context.getArgument("module", Module.class);
                            for (SettingGroup group : m.settings)
                                for (Setting setting : group)
                                    ChatUtils.info(setting.name);
                            return SINGLE_SUCCESS;
                        }
                ));
    }
}
