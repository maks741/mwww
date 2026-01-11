package org.maks.mwww_daemon.enumeration;

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
