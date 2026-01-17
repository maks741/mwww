package org.maks.mwww_daemon.ui.components;

import javafx.scene.image.ImageView;

public abstract class Toggle extends ImageView {

    private boolean toggle = false;

    public void toggle() {
        if (toggle) {
            toggleOff();
        } else {
            toggleOn();
        }

        toggle = !toggle;
    }

    public void reset() {
        if (toggle) {
            toggleOff();
        }

        toggle = false;
    }

    protected abstract void toggleOn();

    protected abstract void toggleOff();

}
