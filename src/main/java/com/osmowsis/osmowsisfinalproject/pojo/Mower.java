package com.osmowsis.osmowsisfinalproject.pojo;

import com.osmowsis.osmowsisfinalproject.constant.Direction;
import com.osmowsis.osmowsisfinalproject.constant.LawnSquareContent;
import lombok.Data;

import java.util.List;

/**
 * POJO to represent mowers
 *
 * Created on 11/26/2019
 */

@Data
public class Mower implements MoveableLawnItem
{
    private int mowerNumber; // INDEX STARTS AT 0, UI WILL ADD 1 ON THIS NUMBER TO GET THE DISPLAY NUMBER
    private Direction currentDirection;
    private int currentEnergy;
    private int currentXCoordinate;
    private int currentYCoordinate;
    private int turnsSinceLastScan;
    private boolean strategic;
    private boolean disabled;
    private List<LawnSquareContent> surroundingSquares;
}
