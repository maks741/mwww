package org.maks.mwww_daemon.components;

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

    protected abstract void toggleOn();

    protected abstract void toggleOff();

}
