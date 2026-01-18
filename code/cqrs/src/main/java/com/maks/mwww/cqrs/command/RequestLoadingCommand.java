package com.maks.mwww.cqrs.command;

import com.maks.mwww.cqrs.api.Command;
import com.maks.mwww.domain.model.LoadingCallback;

public record RequestLoadingCommand(LoadingCallback loadingCallback) implements Command {
}
