package com.maks.mwww.cqrs.bus;

import com.maks.mwww.cqrs.api.Command;
import com.maks.mwww.cqrs.api.CommandHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public final class CommandBus {

    private static final Logger LOG = Logger.getLogger(CommandBus.class.getName());

    private static final Map<Class<? extends Command>, CommandHandler<? extends Command>> handlers = new HashMap<>();

    private CommandBus() {}

    public static <T extends Command> void subscribe(Class<T> type, CommandHandler<T> handler) {
        if (handlers.containsKey(type)) {
            LOG.warning("Handler already registered for " + type.getName());
        }
        handlers.put(type, handler);
    }

    public static void send(Command command) {
        @SuppressWarnings("unchecked")
        CommandHandler<Command> handler = (CommandHandler<Command>) handlers.get(command.getClass());

        if (handler == null) {
            throw new IllegalStateException("No handler registered for " + command.getClass().getName());
        }

        handler.handle(command);
    }
}

