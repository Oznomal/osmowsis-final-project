package com.osmowsis.osmowsisfinalproject.constant;

import lombok.Getter;

/**
 * Enum that describes the different types of actions a mower can take
 *
 * Created on 9/11/2019
 */

@Getter
public enum MowerMovementType
{
    MOVE(2),
    STEER(1),
    C_SCAN(1),
    L_SCAN(3),
    PASS(0);

    private final int energyCost;

    MowerMovementType(int energyCost){
        this.energyCost = energyCost;
    }
}