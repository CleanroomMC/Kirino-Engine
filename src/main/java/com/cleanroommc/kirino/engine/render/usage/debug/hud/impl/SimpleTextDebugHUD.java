package com.cleanroommc.kirino.engine.render.usage.debug.hud.impl;

import com.cleanroommc.kirino.ICS;
import com.cleanroommc.kirino.KirinoCommonCore;
import com.cleanroommc.kirino.engine.render.core.debug.hud.HUDContext;
import com.cleanroommc.kirino.engine.render.core.debug.hud.ImmediateHUD;
import com.cleanroommc.kirino.simpletext.command.TextCommandList;
import org.jspecify.annotations.NonNull;

public class SimpleTextDebugHUD implements ImmediateHUD {

    boolean flag = true;

    @Override
    public void draw(@NonNull HUDContext hud) {
        if (flag) {
            flag = false;
            TextCommandList cmdList = ICS.instance().text()
                    .begin()
                    .append("ABC", 0, 0)
                    .endPseudo();
            KirinoCommonCore.LOGGER.info("cmd list size: " + cmdList.size());
            for (int i = 0; i < cmdList.size(); i++) {
                KirinoCommonCore.LOGGER.info("debug: glyph x y size color hint {} {} {} {} {} {}",
                        cmdList.glyphIndex(i),
                        cmdList.x(i),
                        cmdList.y(i),
                        cmdList.size(i),
                        cmdList.color(i),
                        cmdList.hint(i));
            }
        }

        ICS.instance().text()
                .begin()
                .append("ABCabcijk", 0, 0)
                .append("Hello World JetBrains Mono NL Regular", 0, 12)
                .endDraw();
    }
}
