package org.maks.mwww_daemon.shared.domain.enumeration;

public enum SearchShortcut {
    HOME("me");

    private final String shortcut;

    SearchShortcut(String shortcut) {
        this.shortcut = shortcut;
    }

    public String shortcut() {
        return shortcut;
    }
}
