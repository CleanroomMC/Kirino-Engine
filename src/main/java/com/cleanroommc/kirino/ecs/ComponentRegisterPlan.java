package com.cleanroommc.kirino.ecs;

record ComponentRegisterPlan(
        String componentName,
        String componentClass,
        MemberLayout memberLayout,
        String[] fieldTypeNames) {
}
