package org.maks.mwww_daemon.ui.components;

import org.maks.mwww_daemon.shared.domain.enumeration.Icon;
import org.maks.mwww_daemon.shared.utils.IconUtils;

public class RepeatToggle extends Toggle {

    @Override
    protected void toggleOn() {
        setImage(IconUtils.image(Icon.REPEAT_ON));
    }

    @Override
    protected void toggleOff() {
        setImage(IconUtils.image(Icon.REPEAT_OFF));
    }
}
