package com.cleanroommc.kirino.ecs.schemafixtures;

import com.cleanroommc.kirino.ecs.component.scan.CleanStructSignature;

@CleanStructSignature
public class CyclicStruct {
    public CyclicStruct self;
}
