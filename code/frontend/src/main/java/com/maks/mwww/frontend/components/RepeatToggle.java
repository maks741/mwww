package com.maks.mwww.frontend.components;

import com.maks.mwww.domain.enumeration.Icon;
import com.maks.mwww.frontend.utils.IconUtils;

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
