package minegame159.meteorclient.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import minegame159.meteorclient.modules.Module;
import minegame159.meteorclient.settings.Setting;
import net.minecraft.command.CommandSource;
import net.minecraft.text.LiteralText;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class SettingArgumentType implements ArgumentType<Setting<?>> {
    private static final DynamicCommandExceptionType NO_SUCH_SETTING = new DynamicCommandExceptionType(args ->
            new LiteralText(String.format("Module %s has no such setting as %s.", ((String[]) args)[0], ((String[]) args)[1])));

    public static SettingArgumentType setting(Module module) {
        return new SettingArgumentType(module);
    }

    private final Module module;
    private final Collection<String> examples;

    private SettingArgumentType(Module module) {
        this.module = module;
        this.examples = this.module.getAllSettings().stream().limit(3).map(setting -> setting.name).collect(Collectors.toList());
    }

    @Override
    public Setting<?> parse(StringReader reader) throws CommandSyntaxException {
        String argument = reader.readString();
        Setting<?> setting = module.getSetting(argument);

        if (setting == null) throw NO_SUCH_SETTING.create(new String[]{module.name, argument});

        return setting;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(module.getAllSettings().stream().map(setting -> setting.name), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return examples;
    }
}

