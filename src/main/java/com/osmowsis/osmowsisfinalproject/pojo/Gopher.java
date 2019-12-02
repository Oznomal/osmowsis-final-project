package com.osmowsis.osmowsisfinalproject.pojo;

import lombok.Data;

/**
 * POJO class for gophers
 *
 * Created om 11/27/2019
 */

@Data
public class Gopher implements MoveableLawnItem
{
    private int gopherNumber;
    private int xCoordinate;
    private int yCoordinate;
}
