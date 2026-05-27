package com.cleanroommc.kirino.simpletext.sdf;

import com.cleanroommc.kirino.simpletext.ST_Bitmap;
import org.jspecify.annotations.NonNull;

public interface SDFGenerator {
    /**
     * <p>Note: {@link SDFBitmap} must be freed later.</p>
     * <p>Note: This method will not free the input <code>bitmap</code>.</p>
     */
    @NonNull SDFBitmap compute(@NonNull ST_Bitmap bitmap);
}
