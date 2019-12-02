package com.osmowsis.osmowsisfinalproject.service;

import com.osmowsis.osmowsisfinalproject.constant.LawnSquareContent;
import com.osmowsis.osmowsisfinalproject.model.SimulationDataModel;
import com.osmowsis.osmowsisfinalproject.pojo.LawnSquare;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LawnService
{
    // FIELDS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private final SimulationDataModel simulationDataModel;

    // CONSTRUCTOR
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Autowired
    public LawnService(final SimulationDataModel simulationDataModel)
    {
        this.simulationDataModel = simulationDataModel;
    }

    // PUBLIC METHODS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Gets a lawn square based on the coordinates, returns null for invalid coordinates
     *
     * @param x - The x coordinate
     * @param y - The y coordinate
     *
     * @return - The lawn square for the coordinates if available
     */
    public LawnSquare getLawnSquareByCoordinates(final int x, final int y)
    {
        return simulationDataModel.getLawnSquareByCoordinates(x, y);
    }

    /**
     * Gets the lawn square content based on a set of coordinates
     *
     * @param x - the x coordinate
     * @param y - The y coordinate
     *
     * @return - The lawn square content, fence if the square doesn't exist or is invalid
     */
    public LawnSquareContent getLawnSquareContentByCoordinates(final int x, final int y)
    {
        final LawnSquare lawnSquare = getLawnSquareByCoordinates(x, y);

        return lawnSquare == null ? LawnSquareContent.FENCE : lawnSquare.getLawnSquareContent();
    }

    /**
     * Checks to see if the content is one of the obstacles
     *
     * Note:
     * Null will return true for obstacle becaue it normally represents fence in some cases
     *
     * @param content - The lawn square content
     * @return
     */
    public boolean doesContentContainObstacle(final LawnSquareContent content)
    {
        if(content == null
                || content == LawnSquareContent.FENCE
                || content == LawnSquareContent.EMPTY_MOWER
                || content == LawnSquareContent.EMPTY_GOPHER
                || content == LawnSquareContent.EMPTY_MOWER_CHARGER
                || content == LawnSquareContent.EMPTY_GOPHER_CHARGER
                || content == LawnSquareContent.GRASS_GOPHER)
        {
            return true;
        }

        return false;
    }

    /**
     * Gets the new lawn square content for a mower that is leaving the square
     *
     * @param current - The current content (Must include a mower)
     *
     * @return - The new content for what the square will be after the mower leaves (Must be empty)
     */
    public LawnSquareContent getNewLawnContentForDepartingMower(LawnSquareContent current)
    {
        if(current == LawnSquareContent.EMPTY_MOWER)
        {
            return LawnSquareContent.EMPTY;
        }
        else if(current == LawnSquareContent.EMPTY_MOWER_CHARGER)
        {
            return LawnSquareContent.EMPTY_CHARGER;
        }

        throw new RuntimeException("[MOWER ISSUE] :: getNewLawnContentForDepartingMower - Invalid current square" + " || Content=" + current.name());
    }

    /**
     * Increments the total grass cut by one and decreases the remaining grass cut by 1 in the model
     */
    public void incrementGrassCut()
    {
        simulationDataModel.incrementTotalGrassCut();
    }
}
