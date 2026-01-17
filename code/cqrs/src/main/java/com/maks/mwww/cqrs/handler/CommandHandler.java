package com.maks.mwww.cqrs.handler;

import com.maks.mwww.cqrs.command.Command;

public interface CommandHandler<T extends Command, V> {

    V handle(T command);

}
