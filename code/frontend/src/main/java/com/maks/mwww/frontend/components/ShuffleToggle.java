package com.maks.mwww.frontend.components;

import com.maks.mwww.domain.enumeration.Icon;
import com.maks.mwww.frontend.utils.IconUtils;

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
