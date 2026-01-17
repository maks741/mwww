package com.maks.mwww.cqrs.command;

import com.maks.mwww.domain.model.Track;

public record UpdateTrackCommand(Track track) implements Command {
}
