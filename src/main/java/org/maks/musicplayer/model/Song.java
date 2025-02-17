package org.maks.musicplayer.model;

import org.maks.musicplayer.components.SongInfo;

public record Song(SongInfo songInfo, SongPlayer songPlayer) {}
