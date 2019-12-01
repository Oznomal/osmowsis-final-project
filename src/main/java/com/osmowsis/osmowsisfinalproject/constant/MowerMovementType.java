package com.osmowsis.osmowsisfinalproject.constant;

/**
 * Enum that describes the different types of actions a mower can take
 *
 * Created on 9/11/2019
 */
public enum MowerMovementType
{
    MOVE(2),
    STEER(1),
    C_SCAN(1),
    L_SCAN(3),
    PASS(0);

    private final int energyCost;

    private MowerMovementType(int energyCost){
        this.energyCost = energyCost;
    }

    public int getEnergyCost(){
        return this.energyCost;
    }
}