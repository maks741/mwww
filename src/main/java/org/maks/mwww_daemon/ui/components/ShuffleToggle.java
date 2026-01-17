package org.maks.mwww_daemon.ui.components;

import org.maks.mwww_daemon.shared.domain.enumeration.Icon;
import org.maks.mwww_daemon.shared.utils.IconUtils;

public class ShuffleToggle extends Toggle {

    @Override
    protected void toggleOn() {
        setImage(IconUtils.image(Icon.SHUFFLE_ON));
    }

    @Override
    protected void toggleOff() {
        setImage(IconUtils.image(Icon.SHUFFLE_OFF));
    }
}
