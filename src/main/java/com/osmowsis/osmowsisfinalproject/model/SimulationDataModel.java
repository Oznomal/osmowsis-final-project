package com.osmowsis.osmowsisfinalproject.model;

import com.osmowsis.osmowsisfinalproject.constant.Direction;
import com.osmowsis.osmowsisfinalproject.constant.LawnSquareContent;
import com.osmowsis.osmowsisfinalproject.constant.SimulationRiskProfile;
import com.osmowsis.osmowsisfinalproject.pojo.Gopher;
import com.osmowsis.osmowsisfinalproject.pojo.LawnSquare;
import com.osmowsis.osmowsisfinalproject.pojo.Mower;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * Central data model for the simulation
 *
 * Created on 11/26/2019
 */

@Repository
public class SimulationDataModel implements BaseDataModel
{
    // FIELDS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private SimpleIntegerProperty lawnXDimension;
    private SimpleIntegerProperty lawnYDimension;
    private SimpleIntegerProperty lawnArea;

    @Getter
    private SimpleIntegerProperty startingGrassToCut;

    @Getter
    private SimpleIntegerProperty remainingGrassToCut;

    @Getter
    private SimpleIntegerProperty totalGrassCut;

    @Getter
    private SimpleIntegerProperty activeMowerCount;
    private SimpleIntegerProperty startingMowerEnergy;

    private SimpleIntegerProperty gopherCount;

    @Getter
    private SimpleIntegerProperty gopherPeriod;

    @Getter
    private SimpleIntegerProperty turnsRemainingInPeriod;

    @Getter
    private SimpleIntegerProperty currentTurn;

    @Getter
    private SimpleIntegerProperty maxTurns;

    @Getter
    @Setter
    private SimulationRiskProfile simulationRiskProfile;

    @Getter
    private ObservableList<Mower> mowers;
    private Deque<Mower> mowerQueue;

    @Getter
    private Mower currentMower;

    @Getter
    private ObservableList<LawnSquare> lawnSquares;

    @Getter
    private ObservableList<Gopher> gophers;

    // CONSTRUCTORS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public SimulationDataModel()
    {
        // SET DEFAULT VALUES
        resetDataModel();
    }

    // PUBLIC METHODS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Resets the data model
     */
    @Override
    public void resetDataModel()
    {
        mowers = FXCollections.observableArrayList();
        lawnSquares = FXCollections.observableArrayList();
        gophers = FXCollections.observableArrayList();

        mowerQueue = new ArrayDeque<>();

        lawnXDimension = new SimpleIntegerProperty();
        lawnYDimension = new SimpleIntegerProperty();
        lawnArea = new SimpleIntegerProperty();
        startingGrassToCut = new SimpleIntegerProperty();
        remainingGrassToCut = new SimpleIntegerProperty();
        totalGrassCut = new SimpleIntegerProperty();

        activeMowerCount = new SimpleIntegerProperty();
        startingMowerEnergy = new SimpleIntegerProperty();

        gopherCount = new SimpleIntegerProperty();
        gopherPeriod = new SimpleIntegerProperty();
        turnsRemainingInPeriod = new SimpleIntegerProperty();

        currentTurn = new SimpleIntegerProperty();
        maxTurns = new SimpleIntegerProperty();

        simulationRiskProfile = SimulationRiskProfile.LOW;
    }

    /**
     * Gets a specific lawn square by the x and y coordinate
     *
     * @param x - The x coordinate
     * @param y - The y coordinate
     *
     * @return - The lawn square if it is located, if the square does not exist then null is returned
     */
    public LawnSquare getLawnSquareByCoordinates(final int x, final int y)
    {
        LawnSquare result = null;

        for(LawnSquare lawnSquare : lawnSquares)
        {
            if(lawnSquare.getXCoordinate() == x && lawnSquare.getYCoordinate() == y)
            {
                result = lawnSquare;
            }
        }

        return result;
    }

    /**
     * Gets a mower by the coordinates, throws exception if mower does not exist for those coordinates
     *
     * @param x - The x coordinate
     * @param y - The y coordinate
     *
     * @return - The mower that is located at the coordinates
     */
    public Mower getMowerByCoordinates(final int x, final int y)
    {
        for(Mower mower : mowers)
        {
            if(mower.getCurrentXCoordinate() == x && mower.getCurrentYCoordinate() == y)
            {
                return mower;
            }
        }

        throw new RuntimeException("[MOWER NOT FOUND] :: getMowerByCoordinates - No mower located at coordinates");
    }

    /**
     * Gets a gopher by the coordinates, throws an exception if the gopher does not exist at coordinates
     *
     * @param x - The x coordinate
     * @param y - The y coordinate
     *
     * @return - The gopher
     */
    public Gopher getGopherByCoordinates(final int x, final int y)
    {
        for(Gopher gopher : gophers)
        {
            if(gopher.getXCoordinate() == x && gopher.getYCoordinate() == y)
            {
                return gopher;
            }
        }

        throw new RuntimeException("[GOPHER NOT FOUND] :: getGopherByCoordinates - No gopher located at coordinates");
    }

    /**
     * Gets the next mower from the queue and then adds it to the back of the queue
     *
     * Removes disabled mowers from the queue
     *
     * @return - The next mower
     */
    public Mower getNextMower()
    {
        while(mowerQueue.size() > 0)
        {
            Mower nextMower = mowerQueue.removeFirst();

            if(!nextMower.isDisabled())
            {
                mowerQueue.addLast(nextMower);

                currentMower = nextMower;

                return nextMower;
            }
        }

        throw new RuntimeException("[MOWER QUEUE] :: getNextMower - Mower queue is empty");
    }


    // INITIAL SETUP (SHOULD ONLY BE CALLED FOR PARSING OR DATA INPUT)
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void setInitialStartingMowerEnergy(final int startingEnergy) { startingMowerEnergy.set(startingEnergy); }

    public void setInitialGopherCount(final int count) { gopherCount.set(count);}

    public void setInitialGopherPeriod(final int period) { gopherPeriod.set(period); }

    public void setInitialMaxTurns(final int maxTurns) { this.maxTurns.set(maxTurns); }

    /**
     * Sets the initial lawn dimensions, calculates the lawn area, and builds the lawn model
     *
     * @param xValue - The lawns X dimension
     * @param yValue - The lawns y dimension
     */
    public void setInitialLawnDimensions(final int xValue, final int yValue)
    {
        lawnXDimension.set(xValue);
        lawnYDimension.set(yValue);
        lawnArea.set(xValue * yValue);
        startingGrassToCut.set(lawnArea.get()); // SHOULD ALWAYS BE EQUAL TO ALL SQUARES
        remainingGrassToCut.set(startingGrassToCut.get());

        buildInitialLawnSquaresModel(xValue, yValue);
    }

    /**
     * Adds a new mower to the model
     *
     * Note:
     * This method should only be getting called when initially processing the file or data input
     *
     * @param xCoordinate - The X Coordinate that the mower is starting at
     * @param yCoordinate - The Y Coordinate that the mower is starting at
     * @param startingDirection - The direction that the mower is facing
     * @param strategic - Is the mower following a strategic algorithm (true) or random (false)
     */
    public void addNewMowerToModel(final int xCoordinate,
                                   final int yCoordinate,
                                   final Direction startingDirection,
                                   final boolean strategic)
    {
        //List<LawnSquareContent> surroundingSquares = new ArrayList<>(Collections.nCopies(8, LawnSquareContent.UNKNOWN));

        Mower mower = new Mower();
        mower.setMowerNumber(mowers.size());
        mower.setCurrentDirection(startingDirection);
        mower.setStrategic(strategic);
        mower.setDisabled(false); // DISABLED IS FALSE BY DEFAULT
        mower.setCurrentEnergy(getStartingMowerEnergy());
        mower.setCurrentXCoordinate(xCoordinate);
        mower.setCurrentYCoordinate(yCoordinate);
        CentralMowerMap.addMower(mower);
        mower.setSurroundingSquares(CentralMowerMap.getSurroundingSquares(mower));
        mowers.add(mower);
        mowerQueue.addLast(mower);

        LawnSquare lawnSquare = getLawnSquareByCoordinates(xCoordinate, yCoordinate);

        if(lawnSquare != null)
        {
            lawnSquare.setLawnSquareContent(LawnSquareContent.EMPTY_MOWER_CHARGER);

            incrementTotalGrassCut();
        }
        else{
            throw new RuntimeException("[INVALID MOWER] :: addNewMowerToModel - Lawn Square does not exist");
        }
    }

    /**
     * Adds a new gopher to the model
     *
     * Note:
     * This method should only be getting called when initially processing the file or data input
     *
     * @param gopher - The gopher to be added to the model
     */
    public void addNewGopherToModel(final Gopher gopher)
    {
        gopher.setGopherNumber(gophers.size());

        gophers.add(gopher);

        LawnSquare lawnSquare = getLawnSquareByCoordinates(gopher.getXCoordinate(), gopher.getYCoordinate());

        if(lawnSquare != null && lawnSquare.getLawnSquareContent() == LawnSquareContent.GRASS)
        {
            lawnSquare.setLawnSquareContent(LawnSquareContent.GRASS_GOPHER);
        }
        else{
            throw new RuntimeException("[INVALID GOPHER] :: addNewGopherToModel - Gophers can only be added to grass");
        }
    }


    // PUBLIC UPDATE METHODS (USED TO UPDATE VALUES IN THE MODEL)
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Updates the active mower count
     *
     * @param count - The new active mower count
     */
    public void updateActiveMowerCount(final int count) { activeMowerCount.set(count);}

    /**
     * Increments the grass that has been cut by one and decrements the remaining grass
     */
    public void incrementTotalGrassCut()
    {
        totalGrassCut.set(totalGrassCut.get() + 1);

        remainingGrassToCut.set(remainingGrassToCut.get() - 1);
    }

    /**
     * Increments the current turn by 1
     */
    public void incrementCurrentTurn ()
    {
        currentTurn.set(currentTurn.get() + 1);
    }

    /**
     * Decrements the active mower count by 1
     */
    public void decrementActiveMowers() { activeMowerCount.set(activeMowerCount.get() - 1); }

    // GETTER ACCESS METHODS FOR SIMPLE PROPERTIES
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public int getStartingMowerEnergy()
    {
        return startingMowerEnergy.get();
    }

    public int getLawnXDimension(){ return lawnXDimension.get(); }

    public int getLawnYDimension(){ return lawnYDimension.get(); }


    // PRIVATE METHODS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////\
    /**
     * Builds the initial lawn square model based on the lawns dimensions (Defaults all squares to grass)
     *
     * @param x - The lawns X dimension
     * @param y - The lawns Y dimension
     */
    private void buildInitialLawnSquaresModel(final int x, final int y)
    {
        for(int i = 0; i < x; i++)
        {
            for(int j = 0; j < y; j++)
            {
                lawnSquares.add(new LawnSquare(i, j, LawnSquareContent.GRASS));
                CentralMowerMap.lawnSquares.add(new LawnSquare(i,j, LawnSquareContent.UNKNOWN));
            }
        }
    }
}
