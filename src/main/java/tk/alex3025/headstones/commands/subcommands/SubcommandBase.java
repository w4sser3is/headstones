package tk.alex3025.headstones.commands.subcommands;

import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class SubcommandBase {

    @Getter
    private static final List<SubcommandBase> registeredSubcommands = new ArrayList<>();

    @Getter
    private final String name;
    @Getter
    private String permission = null;
    @Getter
    private boolean playersOnly = false;

    public SubcommandBase(@NotNull String name) {
        this.name = name;
        this.registerSubcommand();
    }

    public SubcommandBase(@NotNull String name, String permission) {
        this(name);
        this.permission = permission;
    }

    public SubcommandBase(@NotNull String name, String permission, boolean playersOnly) {
        this(name, permission);
        this.playersOnly = playersOnly;
    }

    public abstract boolean onCommand(CommandSender sender, String[] args);

    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return List.of();
    }

    public boolean hasPermission(CommandSender sender) {
        return this.getPermission() == null || sender.hasPermission(this.getPermission());
    }


    public void registerSubcommand() {
        registeredSubcommands.add(this);
    }

    public static @Nullable SubcommandBase getSubcommand(String subcommand) {
        for (SubcommandBase registered : registeredSubcommands)
            if (registered.getName().equals(subcommand))
                return registered;
        return null;
    }

}
