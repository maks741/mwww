package com.maks.mwww.cqrs.command;

import com.maks.mwww.cqrs.api.Command;

public record SearchTrackCommand(String query) implements Command {
}
