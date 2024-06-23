package xyz.sirblobman.joincommands.common.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import xyz.sirblobman.joincommands.common.utility.Validate;

public abstract class JoinCommand {
    private final String id;
    private final List<String> commandList;

    private String permissionName;
    private boolean firstJoinOnly;
    private long delay;

    public JoinCommand(@NotNull String id) {
        this.id = id;
        this.commandList = new ArrayList<>();
        this.permissionName = null;
        this.firstJoinOnly = false;
        this.delay = 0L;
    }

    public @NotNull String getId() {
        return this.id;
    }

    public @NotNull List<String> getCommandList() {
        return Collections.unmodifiableList(this.commandList);
    }

    public void setCommandList(@NotNull List<String> commandList) {
        Validate.notEmpty(commandList, "commandList must not be empty!");
        this.commandList.clear();
        this.commandList.addAll(commandList);
    }

    public @Nullable String getPermissionName() {
        return this.permissionName;
    }

    public void setPermissionName(@Nullable String permissionName) {
        this.permissionName = permissionName;
    }

    public boolean isFirstJoinOnly() {
        return this.firstJoinOnly;
    }

    public void setFirstJoinOnly(boolean firstJoinOnly) {
        this.firstJoinOnly = firstJoinOnly;
    }

    public long getDelay() {
        return this.delay;
    }

    public void setDelay(long delay) {
        if (delay < 0L) {
            throw new IllegalArgumentException("delay must be zero or a positive value.");
        }

        this.delay = delay;
    }
}
