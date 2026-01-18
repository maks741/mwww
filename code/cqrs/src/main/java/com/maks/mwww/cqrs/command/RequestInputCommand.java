package com.maks.mwww.cqrs.command;

import com.maks.mwww.cqrs.api.Command;

import java.util.function.Consumer;

public record RequestInputCommand(
        String placeholderText,
        Consumer<String> onInputConsumer
) implements Command {
}
