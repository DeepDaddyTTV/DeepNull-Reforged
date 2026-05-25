package dev.deepdaddyttv.deepnullreforged.devmode;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public final class DeepNullDevModeCommands {
    private DeepNullDevModeCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        if (!DeepNullDevModeSupport.isAvailable()) {
            return;
        }
        dispatcher.register(Commands.literal("deepnull")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("set")
                        .then(Commands.literal("dev-mode")
                                .then(Commands.argument("enabled", BoolArgumentType.bool())
                                        .executes(context -> setDevMode(
                                                context.getSource(),
                                                BoolArgumentType.getBool(context, "enabled")
                                        ))))));
    }

    private static int setDevMode(CommandSourceStack source, boolean enabled) {
        var level = source.getServer().overworld();
        DeepNullDevModeSavedData data = DeepNullDevModeSavedData.get(level);
        boolean changed = data.setEnabled(enabled);
        DeepNullDevModeSupport.syncToAll(source.getServer());
        source.sendSuccess(
                () -> Component.literal(changed
                        ? "DeepNull dev mode " + (enabled ? "enabled" : "disabled") + "."
                        : "DeepNull dev mode is already " + (enabled ? "enabled." : "disabled.")),
                true
        );
        return 1;
    }
}
