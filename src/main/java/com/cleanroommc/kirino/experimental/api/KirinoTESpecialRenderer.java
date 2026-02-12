package com.cleanroommc.kirino.experimental.api;

import com.cleanroommc.kirino.experimental.api.render.RenderCommandSpecialImpl;
import com.cleanroommc.kirino.experimental.api.render.RenderContext;
import com.cleanroommc.kirino.experimental.api.render.RenderQueue;
import net.minecraft.tileentity.TileEntity;

public interface KirinoTESpecialRenderer {
    RenderQueue<RenderCommandSpecialImpl> render(TileEntity tileEntity, RenderContext<RenderCommandSpecialImpl> renderContext);
}
