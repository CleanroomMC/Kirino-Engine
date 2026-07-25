package com.cleanroommc.kirino.engine.render.usage.debug.hud;

import com.cleanroommc.kirino.ICS;
import com.cleanroommc.kirino.engine.render.core.debug.hud.HUDContext;
import com.cleanroommc.kirino.engine.render.core.debug.hud.ImmediateHUD;
import com.cleanroommc.kirino.ui.simpletext.SimpleTextProducer;
import net.minecraft.client.renderer.GlStateManager;
import org.jspecify.annotations.NonNull;

import java.awt.*;

public class SimpleTextDebugHUD implements ImmediateHUD {

    @Override
    public void draw(@NonNull HUDContext hud) {
        GlStateManager.disableCull();
        GlStateManager.enableBlend();

        ICS.instance().text()
                .begin()
                .append("ABCabcijk", 0, 0, 12f, Color.GRAY.getRGB())
                .endDraw();

        SimpleTextProducer.LineInfo lineInfo = ICS.instance().text().simulate("ABCabcijk", 0, 0, 12f);
        ICS.instance().text()
                .begin()
                .append("min xy max xy: " + lineInfo.getMinX() + ", " + lineInfo.getMinY() + ", " + lineInfo.getMaxX() + ", " + lineInfo.getMaxY(), 0, 14)
                .appendBelow("LineTopToBaseline: " + lineInfo.getLineTopToBaseline())
                .endDraw();

        hud.drawRectOutline(
                lineInfo.getMinX(),
                lineInfo.getMinY(),
                lineInfo.getMaxX() - lineInfo.getMinX(),
                lineInfo.getMaxY() - lineInfo.getMinY(),
                0.5f,
                Color.GREEN.getRGB());
        hud.drawRect(
                lineInfo.getMinX(),
                lineInfo.getMinY() + lineInfo.getLineTopToBaseline(),
                lineInfo.getLineWidth(),
                0.5f,
                Color.RED.getRGB());

        ICS.instance().text()
                .begin()
                .append("font size 20", 0, 40, 20, Color.WHITE.getRGB())
                .appendBelow("font size 15", 15, Color.WHITE.getRGB())
                .appendBelow("font size 10", 10, Color.WHITE.getRGB())
                .appendBelow("font size 9", 9, Color.WHITE.getRGB())
                .appendBelow("font size 8", 8, Color.WHITE.getRGB())
                .appendBelow("font size 7", 7, Color.WHITE.getRGB())
                .appendBelow("font size 6", 6, Color.WHITE.getRGB())
                .appendBelow("font size 5", 5, Color.WHITE.getRGB())
                .appendBelow("font size 4", 4, Color.WHITE.getRGB())
                .appendBelow("font size 3", 3, Color.WHITE.getRGB())
                .appendBelow("font size 2", 2, Color.WHITE.getRGB())
                .appendEmptyLineBelow()
                .appendBelow("——————————————————", 7, Color.WHITE.getRGB())
                .appendEmptyLineBelow()
                .appendBelow("CJK Test 么麽さかㄱㄴㄷ", 7, Color.WHITE.getRGB())
                .endDraw();

//        ICS.instance().text()
//                .begin()
//                .appendParagraph(
//                        "Hello world, this is a paragraph wrapping test. " +
//                                "这是一个用于测试自动换行的中文段落，包含逗号、句号，以及一些比较长的内容。" +
//                                "日本語のテストです。これは自動改行、句読点、そして英単語との混在を確認するための文章です。" +
//                                "한국어 테스트입니다. 한글과 English words, 中文字符, 日本語を一緒に表示して、줄바꿈이 자연스럽게 되는지 확인합니다. " +
//                                "Finally, here is aVeryVeryVeryVeryVeryLongEnglishWordThatShouldForceEmergencyWrapping.",
//                        150,
//                        30,
//                        80)
//                .endDraw();

        ICS.instance().text()
                .begin()
                .appendParagraph(
                        "Test:\nHello world, this is a paragraph wrapping test. " +
                                "Finally, here is aVeryVeryVeryVeryVeryLongEnglishWordThatShouldForceEmergencyWrapping.",
                        150,
                        30,
                        80)
                .endDraw();
    }
}
