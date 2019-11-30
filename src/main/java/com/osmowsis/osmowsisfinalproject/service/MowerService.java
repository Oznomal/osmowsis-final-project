package com.osmowsis.osmowsisfinalproject.service;

import com.osmowsis.osmowsisfinalproject.constant.*;
import com.osmowsis.osmowsisfinalproject.model.SimulationDataModel;
import com.osmowsis.osmowsisfinalproject.pojo.Gopher;
import com.osmowsis.osmowsisfinalproject.pojo.LawnSquare;
import com.osmowsis.osmowsisfinalproject.pojo.Mower;
import com.osmowsis.osmowsisfinalproject.pojo.MowerMove;
import com.osmowsis.osmowsisfinalproject.service.base.NextMowerMoveService;
import com.osmowsis.osmowsisfinalproject.view.controller.SidebarController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class MowerService
{
    // FIELDS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private final SimulationDataModel simulationDataModel;
    private final LawnService lawnService;
    private final GopherService gopherService;
    private final NextMowerMoveService lowRiskMoveService;
    private final NextMowerMoveService medRiskMoveService;
    private final NextMowerMoveService highRiskMoveService;
    private final SimulationRiskProfileService simulationRiskProfileService;
    private final SidebarController sidebarController;

    // CONSTRUCTORS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Lazy
    @Autowired
    public MowerService(final SimulationDataModel simulationDataModel,
                        final LawnService lawnService,
                        final SimulationRiskProfileService simulationRiskProfileService,
                        final SidebarController sidebarController,
                        final GopherService gopherService,
                        @Qualifier("lowRiskMoveService") final NextMowerMoveService lowRiskMoveService,
                        @Qualifier("lowRiskMoveService") final NextMowerMoveService medRiskMoveService,
                        @Qualifier("highRiskMoveService") final NextMowerMoveService highRiskMoveService)
    {
        this.simulationDataModel = simulationDataModel;
        this.lawnService = lawnService;
        this.lowRiskMoveService = lowRiskMoveService;
        this.medRiskMoveService = medRiskMoveService;
        this.highRiskMoveService = highRiskMoveService;
        this.simulationRiskProfileService = simulationRiskProfileService;
        this.sidebarController = sidebarController;
        this.gopherService = gopherService;
    }

    // PUBLIC METHODS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Makes the mower move
     *
     * @param mower - The mower to make the move for
     */
    public void makeMove(Mower mower)
    {
        if(mower == null || mower.isDisabled())
        {
            throw new RuntimeException("[MOWER MOVE ERROR] :: makeMove - Invalid Mower");
        }

        final MowerMove nextMove = determineMove(mower);

        displayMowerMove(nextMove);

        if(isValidMoveForMower(nextMove))
        {
            if(nextMove.getMowerMovementType() == MowerMovementType.MOVE)
            {
                moveMower(mower);
            }
            else if(nextMove.getMowerMovementType() == MowerMovementType.STEER)
            {
                steerMower(mower, nextMove.getDirection());
            }
            else if(nextMove.getMowerMovementType() == MowerMovementType.C_SCAN)
            {
                preformCScan(mower);
            }
            else if(nextMove.getMowerMovementType() == MowerMovementType.L_SCAN)
            {
                preformLScan(mower);
            }
            else{
                preformPass(mower);
            }
        }
        else{
            disableMower(nextMove);
        }

        mower.setTurnTaken(true);
    }

    /**
     * Checks to see if all mowers have made a move for this round
     *
     * @return - True if true, false other wise
     */
    public boolean areAllMowerTurnsTaken()
    {
        for(Mower mower : simulationDataModel.getMowers())
        {
            if(!mower.isDisabled() && !mower.isTurnTaken())
            {
                return false;
            }
        }

        return true;
    }

    /**
     * Resets the turn taken flag for all active mowers, this should be called at the beginning of each new turn
     */
    public void resetTurnInfoForActiveMowers()
    {
        for(Mower mower : simulationDataModel.getMowers())
        {
            if(!mower.isDisabled())
            {
                mower.setTurnTaken(false);
            }
        }
    }

    // PRIVATE METHODS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Determines the next mower move
     *
     * @param mower - The mower to detrmine the move for
     *
     * @return - The next mower move the mower will attempt to make
     */
    private MowerMove determineMove(final Mower mower)
    {
        final SimulationRiskProfile riskProfile = simulationRiskProfileService.getCurrentSimulationRiskProfile();

        MowerMove response;

        if(riskProfile == SimulationRiskProfile.LOW)
        {
            response = lowRiskMoveService.getNextMowerMove(mower);
        }
        else if(riskProfile == SimulationRiskProfile.MEDIUM)
        {
            response = medRiskMoveService.getNextMowerMove(mower);
        }
        else if(riskProfile == SimulationRiskProfile.HIGH)
        {
            response = highRiskMoveService.getNextMowerMove(mower);
        }
        else{
            // THIS SHOULD NEVER BE REACHED BECAUSE RISK PROFILE SHOULD ALWAYS BE SET
            throw new RuntimeException("[RISK PROFILE ERROR] :: determineMove - The risk profile is invalid");
        }

        return response;
    }

    /**
     * Checks to see if a mower move is valid
     *
     * @param move - The mower move to check for
     *
     * @return - True if the move is valid, false otherwise
     */
    private boolean isValidMoveForMower(final MowerMove move)
    {
        boolean response = true;

        // STEER, C_SCAN , AND PASS WILL ALWAYS BE VALID MOVES BECAUSE THEY DON'T ACTUALLY CHANGE THE MOWERS POSITION
        if(move.getMowerMovementType() == MowerMovementType.MOVE)
        {
            final int x = move.getNewXCoordinate();
            final int y = move.getNewYCoordinate();

            LawnSquareContent content = lawnService.getLawnSquareContentByCoordinates(x, y);

            if(lawnService.doesContentContainObstacle(content))
            {
                response = false;
            }
        }

        updateSimStateForMowerMove(move);

        return response;
    }

    /**
     * Updates the simulation data model based on the move that is going to be made
     *
     * Note:
     * This happens before the move is actually made
     *
     * @param move - The move that is going to be made
     */
    private void updateSimStateForMowerMove(final MowerMove move)
    {
        switch(move.getMowerMovementType())
        {
            case PASS:
                // NO SIM STATE UPDATES FOR PASS NEED TO OCCUR
                break;

            case STEER:
                updateSimForMowerSteer(move);
                break;

            case C_SCAN:
                updateSimForMowerCScan(move);
                break;

            case L_SCAN:
                updateSimForMowerLScan(move);
                break;

            case MOVE:
                updateSimForMowerMove(move);
                break;

            default:
                throw new RuntimeException("[UPDATE ERROR] :: updateSimStateForMowerMove - Invalid move type");
        }
    }

    /**
     * Updates the sim for STEER move types
     *
     * @param move - The move
     */
    private void updateSimForMowerSteer(final MowerMove move)
    {
        if(!isMowerMoveOnChargingSquare(move))
        {
            // TODO: TAKE AWAY THE ENERGY FOR THE MOVE
        }
    }

    /**
     * Updates the sim for LSCAN move types
     *
     * @param move - The move
     */
    private void updateSimForMowerLScan(final MowerMove move)
    {
        if(!isMowerMoveOnChargingSquare(move))
        {
            // TODO: TAKE AWAY THE ENERGY FOR THE MOVE
        }
    }

    /**
     * Updates the sim state for CSCAN move types
     *
     * @param move - The move
     */
    private void updateSimForMowerCScan(final MowerMove move)
    {
        if(!isMowerMoveOnChargingSquare(move))
        {
            // TODO: TAKE AWAY THE ENERGY FOR THE MOVE
        }
    }

    /**
     * Updates the sim state for MOVE move types
     *
     * @param move - The move
     */
    private void updateSimForMowerMove(final MowerMove move)
    {
        final LawnSquare newSquare = lawnService.getLawnSquareByCoordinates(
                move.getNewXCoordinate(), move.getNewYCoordinate());

        final LawnSquareContent newContent =
                newSquare == null ? LawnSquareContent.FENCE : newSquare.getLawnSquareContent();

        final LawnSquare oldSquare = lawnService.getLawnSquareByCoordinates(
                move.getCurrentXCoordinate(), move.getCurrentYCoordinate());

        // UPDATE THE SQUARE THE MOWER WAS IN SINCE THE MOWER IS MOVING TO A NEW SQUARE
        oldSquare.setLawnSquareContent(
                lawnService.getNewLawnContentForDepartingMower(oldSquare.getLawnSquareContent()));

        boolean recharged = false;

        StringBuilder sb = new StringBuilder();

        if(lawnService.doesContentContainObstacle(newContent))
        {
            decrementActiveMowers();

            // TODO: CHECK TO SEE IF THE STATEMENT BELOW IS TRUE
            // WHEN THE MOWER GOES OVER GOPHER, IT GETS CHEWED BUT STILL CUTS THE GRASS FIRST
            if(newContent == LawnSquareContent.GRASS_GOPHER)
            {
                final Gopher gopher =
                        gopherService.getGopherByCoordinates(newSquare.getXCoordinate(), newSquare.getYCoordinate());

                sb.append("Gopher ")
                        .append((gopher.getGopherNumber() + 1))
                        .append(" destroyed Mower ")
                        .append((move.getMower().getMowerNumber() + 1));

                lawnService.incrementGrassCut();

                newSquare.setLawnSquareContent(LawnSquareContent.EMPTY_GOPHER);
            }
            else if(newContent == LawnSquareContent.EMPTY_MOWER)
            {
                Mower collisionMower = getMowerByCoordinates(newSquare.getXCoordinate(), newSquare.getXCoordinate());

                removeMowerInNewSquare(collisionMower);

                sb.append("Mower ")
                        .append((move.getMower().getMowerNumber() + 1))
                        .append(" collided with Mower ")
                        .append((collisionMower.getMowerNumber() + 1));

                newSquare.setLawnSquareContent(LawnSquareContent.EMPTY);
            }
            else if(newContent == LawnSquareContent.EMPTY_MOWER_CHARGER)
            {
                Mower collisionMower = getMowerByCoordinates(newSquare.getXCoordinate(), newSquare.getXCoordinate());

                removeMowerInNewSquare(collisionMower);

                sb.append("Mower ")
                        .append((move.getMower().getMowerNumber() + 1))
                        .append(" collided with Mower ")
                        .append((collisionMower.getMowerNumber() + 1));

                newSquare.setLawnSquareContent(LawnSquareContent.EMPTY_CHARGER);
            }
            else{
                sb.append("Mower ")
                        .append((move.getMower().getMowerNumber() + 1))
                        .append(" collided with a Fence");
            }
        }
        else if(newContent == LawnSquareContent.EMPTY)
        {
            newSquare.setLawnSquareContent(LawnSquareContent.EMPTY_MOWER);
        }
        else if(newContent == LawnSquareContent.GRASS)
        {
            newSquare.setLawnSquareContent(LawnSquareContent.EMPTY_MOWER);

            sb.append("Mower ")
                    .append((move.getMower().getMowerNumber() + 1))
                    .append(" successfully cut 1 square!");

            lawnService.incrementGrassCut();
        }
        else if(newContent == LawnSquareContent.EMPTY_CHARGER)
        {
            newSquare.setLawnSquareContent(LawnSquareContent.EMPTY_MOWER_CHARGER);

            sb.append("MOWER ")
                    .append((move.getMower().getMowerNumber() + 1))
                    .append(" is now recharged!");

            recharged = true;
            // TODO: NEED TO IMPLEMENT CHARGING STUFF HERE, UPDATE THE MOWERS BATTERY BACK TO THE STARTING AMOUNT
        }
        else{
            throw new RuntimeException("[UPDATE ERROR] :: updateSimStateForMowerMove - Invalid new content scenario");
        }

        // DISPLAY MESSAGES THAT SHOULD BE DISPLAYED AFTER ORIGINAL MOVE MESSAGE
        if(!sb.toString().trim().isEmpty())
        {
            sidebarController.printConsoleMessage(sb.toString(), false);
        }

        if(!recharged)
        {
            // TODO: TAKE AWAY THE ENERGY FOR THE MOVE
        }
    }

    /**
     * Checks to see if a stationary mower move (Pass, Scan, Steer) is happening on charging pad
     *
     * @param move - The move
     *
     * @return - True if the move is on a pad, false otherwise
     */
    private boolean isMowerMoveOnChargingSquare(final MowerMove move)
    {
        LawnSquareContent currentContent = lawnService.getLawnSquareContentByCoordinates(
                move.getCurrentXCoordinate(), move.getCurrentYCoordinate());

        return currentContent == LawnSquareContent.EMPTY_MOWER_CHARGER;
    }

    /**
     * Removes the mower in the new square
     *
     * @param mower - The mower to remove
     */
    private void removeMowerInNewSquare(Mower mower)
    {
        mower.setDisabled(true);

        mower.setCurrentXCoordinate(Integer.MIN_VALUE);
        mower.setCurrentYCoordinate(Integer.MIN_VALUE);

        decrementActiveMowers();
    }

    /**
     * Gets a mower by its x and y coordinates, throws exception if not found
     *
     * @param x - The x coordinate
     * @param y - The y coordinate
     *
     * @return - The mower at the coordinates
     */
    private Mower getMowerByCoordinates(final int x, final int y)
    {
        for(Mower mower : simulationDataModel.getMowers())
        {
            if(mower.getCurrentXCoordinate() == x && mower.getCurrentYCoordinate() == y)
            {
                return mower;
            }
        }

        throw new RuntimeException("[INVALID MOWER] :: getMowerByCoordinates - No mower at coordinates");
    }

    /**
     * Decrements the active mowers by 1
     */
    private void decrementActiveMowers()
    {
        simulationDataModel.decrementActiveMowers();
    }

    /**
     * Moves mower forward
     *
     * @param mower - The mower
     */
    private void moveMower(final Mower mower)
    {
        mower.setCurrentXCoordinate(
                mower.getCurrentXCoordinate() + mower.getCurrentDirection().getxIncrement());
        mower.setCurrentYCoordinate(
                mower.getCurrentYCoordinate() + mower.getCurrentDirection().getyIncrement());

        updateSurroundingSquaresAfterMove(mower);

        mower.setTurnsSinceLastScan(mower.getTurnsSinceLastScan() + 1);
    }

    /**
     * Steers the mower in a new direction
     *
     * @param mower - The mower
     * @param newDirection - The new direction
     */
    private void steerMower(final Mower mower, final Direction newDirection)
    {
        if(newDirection == null)
        {
            throw new RuntimeException("[MOWER DIRECTION ERROR] :: steerMower - Null Values Not Supported");
        }

        mower.setCurrentDirection(newDirection);

        mower.setTurnsSinceLastScan(mower.getTurnsSinceLastScan() + 1);
    }

    /**
     * Preforms a scan for the mower
     *
     * @param mower - The mower
     */
    private void preformCScan(final Mower mower)
    {
        final int x = mower.getCurrentXCoordinate();
        final int y = mower.getCurrentYCoordinate();

        List<LawnSquareContent> surroundingSquares = mower.getSurroundingSquares();

        surroundingSquares.set(0, lawnService.getLawnSquareContentByCoordinates(x, y + 1));
        surroundingSquares.set(1, lawnService.getLawnSquareContentByCoordinates(x + 1, y + 1));
        surroundingSquares.set(2, lawnService.getLawnSquareContentByCoordinates(x + 1, y));
        surroundingSquares.set(3, lawnService.getLawnSquareContentByCoordinates(x + 1, y - 1));
        surroundingSquares.set(4, lawnService.getLawnSquareContentByCoordinates(x, y - 1));
        surroundingSquares.set(5, lawnService.getLawnSquareContentByCoordinates(x - 1, y - 1));
        surroundingSquares.set(6, lawnService.getLawnSquareContentByCoordinates(x - 1, y));
        surroundingSquares.set(7, lawnService.getLawnSquareContentByCoordinates(x - 1, y + 1));

        mower.setTurnsSinceLastScan(0);
    }

    /**
     * Preforms a L Scan for the mower
     *
     * @param mower - The mower
     */
    private void preformLScan(final Mower mower)
    {
        // TODO: IMPLEMENT THE L SCAN FUNCTIONALITY

        mower.setTurnsSinceLastScan(0);
    }

    /**
     * Passes turn
     *
     * @param mower - The mower
     */
    private void preformPass(final Mower mower)
    {
        mower.setTurnsSinceLastScan(mower.getTurnsSinceLastScan() + 1);
    }

    /**
     * Updates the surrounding squares a move
     *
     * @param mower - The mower to update squares for
     */
    private void updateSurroundingSquaresAfterMove(final Mower mower)
    {
        final Direction direction = mower.getCurrentDirection();

        List<LawnSquareContent> surroundingSquares = mower.getSurroundingSquares();

        List<LawnSquareContent> newModel = new ArrayList<>(Collections.nCopies(8, null));

        if(direction == Direction.NORTH) // MOVING TO POSITION 0 IN THE EXISTING MODEL
        {
            // CONVERT THE EXISTING SQUARES TO THEIR NEW POSITION IN THE MODEL
            newModel.set(2, surroundingSquares.get(1));
            newModel.set(3, surroundingSquares.get(2));
            newModel.set(5, surroundingSquares.get(6));
            newModel.set(6, surroundingSquares.get(7));

            // SET THE VALUE OF THE SQUARE THE MOWER MOVED FROM TO EMPTY
            newModel.set(4, LawnSquareContent.EMPTY);

            // SET THE VALUES OF THE UNKNOWN SQUARES
            newModel.set(0, LawnSquareContent.UNKNOWN);
            newModel.set(1, LawnSquareContent.UNKNOWN);
            newModel.set(7, LawnSquareContent.UNKNOWN);
        }
        else if(direction == Direction.NORTHEAST) // MOVING TO POSITION 1 IN THE EXISTING MODEL
        {
            // CONVERT THE EXISTING SQUARES TO THEIR NEW POSITION IN THE MODEL
            newModel.set(4, surroundingSquares.get(2));
            newModel.set(6, surroundingSquares.get(0));

            // SET THE VALUE OF THE SQUARE THE MOWER MOVED FROM TO EMPTY
            newModel.set(5, LawnSquareContent.EMPTY);

            // SET THE VALUES OF THE UNKNOWN SQUARES
            newModel.set(0, LawnSquareContent.UNKNOWN);
            newModel.set(1, LawnSquareContent.UNKNOWN);
            newModel.set(2, LawnSquareContent.UNKNOWN);
            newModel.set(3, LawnSquareContent.UNKNOWN);
            newModel.set(7, LawnSquareContent.UNKNOWN);
        }
        else if(direction == Direction.EAST) // MOVING TO POSITION 2 IN THE EXISTING MODEL
        {
            // CONVERT THE EXISTING SQUARES TO THEIR NEW POSITION IN THE MODEL
            newModel.set(0, surroundingSquares.get(1));
            newModel.set(4, surroundingSquares.get(3));
            newModel.set(5, surroundingSquares.get(4));
            newModel.set(7, surroundingSquares.get(0));

            // SET THE VALUE OF THE SQUARE THE MOWER MOVED FROM TO EMPTY
            newModel.set(6, LawnSquareContent.EMPTY);

            // SET THE VALUES OF THE UNKNOWN SQUARES
            newModel.set(1, LawnSquareContent.UNKNOWN);
            newModel.set(2, LawnSquareContent.UNKNOWN);
            newModel.set(3, LawnSquareContent.UNKNOWN);
        }
        else if(direction == Direction.SOUTHEAST) // MOVING TO POSITION 3 IN THE EXISTING MODEL
        {
            // CONVERT THE EXISTING SQUARES TO THEIR NEW POSITION IN THE MODEL
            newModel.set(0, surroundingSquares.get(2));
            newModel.set(6, surroundingSquares.get(4));

            // SET THE VALUE OF THE SQUARE THE MOWER MOVED FROM TO EMPTY
            newModel.set(7, LawnSquareContent.EMPTY);

            // SET THE VALUES OF THE UNKNOWN SQUARES
            newModel.set(1, LawnSquareContent.UNKNOWN);
            newModel.set(2, LawnSquareContent.UNKNOWN);
            newModel.set(3, LawnSquareContent.UNKNOWN);
            newModel.set(4, LawnSquareContent.UNKNOWN);
            newModel.set(5, LawnSquareContent.UNKNOWN);
        }
        else if(direction == Direction.SOUTH) // MOVING TO POSITION 4 IN THE EXISTING MODEL
        {
            // CONVERT THE EXISTING SQUARES TO THEIR NEW POSITION IN THE MODEL
            newModel.set(1, surroundingSquares.get(2));
            newModel.set(2, surroundingSquares.get(3));
            newModel.set(6, surroundingSquares.get(5));
            newModel.set(7, surroundingSquares.get(6));

            // SET THE VALUE OF THE SQUARE THE MOWER MOVED FROM TO EMPTY
            newModel.set(0, LawnSquareContent.EMPTY);

            // SET THE VALUES OF THE UNKNOWN SQUARES
            newModel.set(3, LawnSquareContent.UNKNOWN);
            newModel.set(4, LawnSquareContent.UNKNOWN);
            newModel.set(5, LawnSquareContent.UNKNOWN);
        }
        else if(direction == Direction.SOUTHWEST) // MOVING TO POSITION 5 IN THE EXISTING MODEL
        {
            // CONVERT THE EXISTING SQUARES TO THEIR NEW POSITION IN THE MODEL
            newModel.set(0, surroundingSquares.get(6));
            newModel.set(2, surroundingSquares.get(4));

            // SET THE VALUE OF THE SQUARE THE MOWER MOVED FROM TO EMPTY
            newModel.set(1, LawnSquareContent.EMPTY);

            // SET THE VALUES OF THE UNKNOWN SQUARES
            newModel.set(3, LawnSquareContent.UNKNOWN);
            newModel.set(4, LawnSquareContent.UNKNOWN);
            newModel.set(5, LawnSquareContent.UNKNOWN);
            newModel.set(6, LawnSquareContent.UNKNOWN);
            newModel.set(7, LawnSquareContent.UNKNOWN);
        }
        else if(direction == Direction.WEST) // MOVING TO POSITION 6 IN THE EXISTING MODEL
        {
            // CONVERT THE EXISTING SQUARES TO THEIR NEW POSITION IN THE MODEL
            newModel.set(0, surroundingSquares.get(7));
            newModel.set(1, surroundingSquares.get(0));
            newModel.set(3, surroundingSquares.get(4));
            newModel.set(4, surroundingSquares.get(5));

            // SET THE VALUE OF THE SQUARE THE MOWER MOVED FROM TO EMPTY
            newModel.set(2, LawnSquareContent.EMPTY);

            // SET THE VALUES OF THE UNKNOWN SQUARES
            newModel.set(5, LawnSquareContent.UNKNOWN);
            newModel.set(6, LawnSquareContent.UNKNOWN);
            newModel.set(7, LawnSquareContent.UNKNOWN);
        }
        else if(direction == Direction.NORTHWEST) // MOVING TO POSITION 7 IN THE EXISTING MODEL
        {
            // CONVERT THE EXISTING SQUARES TO THEIR NEW POSITION IN THE MODEL
            newModel.set(2, surroundingSquares.get(0));
            newModel.set(4, surroundingSquares.get(6));

            // SET THE VALUE OF THE SQUARE THE MOWER MOVED FROM TO EMPTY
            newModel.set(3, LawnSquareContent.EMPTY);

            // SET THE VALUES OF THE UNKNOWN SQUARES
            newModel.set(0, LawnSquareContent.UNKNOWN);
            newModel.set(1, LawnSquareContent.UNKNOWN);
            newModel.set(5, LawnSquareContent.UNKNOWN);
            newModel.set(6, LawnSquareContent.UNKNOWN);
            newModel.set(7, LawnSquareContent.UNKNOWN);
        }
        else{
            // SHOULD NEVER REACH THIS BECAUSE ALL DIRECTIONS ARE COVERED
            throw new RuntimeException("[UPDATE MODEL ERROR] :: updateSurroundingSquaresAfterMove - Invalid Direction");
        }

        mower.setSurroundingSquares(newModel);
    }

    /**
     * Disables the mower and removes it from the lawn if applicable
     *
     * @param nextMove - The next move
     */
    private void disableMower(final MowerMove nextMove)
    {
        nextMove.getMower().setDisabled(true);

        LawnSquareContent newContent = lawnService.getLawnSquareContentByCoordinates(
                nextMove.getNewXCoordinate(), nextMove.getNewYCoordinate());

        if(newContent != LawnSquareContent.EMPTY_MOWER)
        {
            nextMove.getMower().setCurrentXCoordinate(Integer.MIN_VALUE);
            nextMove.getMower().setCurrentYCoordinate(Integer.MIN_VALUE);
        }
    }

    /**
     * Displays the mower move
     */
    private void displayMowerMove(final MowerMove mowerMove)
    {
        final StringBuilder sb = new StringBuilder();

        if(mowerMove.getMowerMovementType() == MowerMovementType.MOVE)
        {
            sb.append(CSS.MOWER_NAME_PREFIX)
                    .append(" ")
                    .append(mowerMove.getMower().getMowerNumber() + 1).append(" moved ")
                    .append(mowerMove.getDirection().getAbbreviation())
                    .append(" from (")
                    .append(mowerMove.getCurrentXCoordinate())
                    .append(",")
                    .append(mowerMove.getCurrentYCoordinate())
                    .append(") to (")
                    .append(mowerMove.getNewXCoordinate())
                    .append(",")
                    .append(mowerMove.getNewYCoordinate())
                    .append(")");
        }
        else if(mowerMove.getMowerMovementType() == MowerMovementType.C_SCAN)
        {
            sb.append(CSS.MOWER_NAME_PREFIX)
                    .append(" ")
                    .append(mowerMove.getMower().getMowerNumber() + 1)
                    .append(" performed cscan while located at (")
                    .append(mowerMove.getCurrentXCoordinate())
                    .append(",")
                    .append(mowerMove.getCurrentYCoordinate())
                    .append(")");
        }
        else if(mowerMove.getMowerMovementType() == MowerMovementType.L_SCAN)
        {
            sb.append(CSS.MOWER_NAME_PREFIX)
                    .append(" ")
                    .append(mowerMove.getMower().getMowerNumber() + 1)
                    .append(" performed lscan while located at (")
                    .append(mowerMove.getCurrentXCoordinate())
                    .append(",")
                    .append(mowerMove.getCurrentYCoordinate())
                    .append(")");
        }
        else if(mowerMove.getMowerMovementType() == MowerMovementType.STEER)
        {
            sb.append(CSS.MOWER_NAME_PREFIX)
                    .append(" ")
                    .append(mowerMove.getMower().getMowerNumber() + 1)
                    .append(" steered ")
                    .append(mowerMove.getDirection().getAbbreviation())
                    .append(" while located at (")
                    .append(mowerMove.getCurrentXCoordinate())
                    .append(",")
                    .append(mowerMove.getCurrentYCoordinate())
                    .append(")");
        }
        else if(mowerMove.getMowerMovementType() == MowerMovementType.PASS)
        {
            sb.append(CSS.MOWER_NAME_PREFIX)
                    .append(" ")
                    .append(mowerMove.getMower().getMowerNumber() + 1)
                    .append(" passed while located at (")
                    .append(mowerMove.getCurrentXCoordinate())
                    .append(",")
                    .append(mowerMove.getCurrentYCoordinate())
                    .append(")");
        }

        sidebarController.printConsoleMessage(sb.toString(), true);
    }
}
