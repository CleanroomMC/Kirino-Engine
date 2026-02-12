package com.cleanroommc.kirino.experimental.api;

import com.cleanroommc.kirino.experimental.api.render.RenderCommandImpl;
import com.cleanroommc.kirino.experimental.api.render.RenderContext;
import com.cleanroommc.kirino.experimental.api.render.RenderQueue;
import net.minecraft.tileentity.TileEntity;

public interface KirinoTERenderer {
    RenderQueue<RenderCommandImpl> render(TileEntity tileEntity, RenderContext<RenderCommandImpl> renderContext);
}
