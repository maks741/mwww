package com.maks.mwww.cqrs.api;

public interface CommandHandler<T extends Command> {

    void handle(T command);

}
