package com.cleanroommc.kirino.ui.simpletext.text;

/**
 * @param consumeEnd UTF-16 offset at which the next line starts (>= <code>renderEnd</code>)
 * @param renderEnd UTF-16 offset up to which text should be rendered
 */
public record WrapBreak(int consumeEnd, int renderEnd) {
}
