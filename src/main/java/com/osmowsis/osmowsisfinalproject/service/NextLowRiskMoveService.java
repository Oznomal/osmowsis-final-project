package com.osmowsis.osmowsisfinalproject.service;

import com.osmowsis.osmowsisfinalproject.constant.Direction;
import com.osmowsis.osmowsisfinalproject.constant.LawnSquareContent;
import com.osmowsis.osmowsisfinalproject.constant.MowerMovementType;
import com.osmowsis.osmowsisfinalproject.pojo.Mower;
import com.osmowsis.osmowsisfinalproject.pojo.MowerMove;
import com.osmowsis.osmowsisfinalproject.service.base.NextMowerMoveService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

/**
 * Concrete singleton implementation for determining the next low risk mower move
 *
 * Created on 9/28/2019
 */

@Service
@Qualifier("lowRiskMoveService")
class NextLowRiskMoveService extends NextMowerMoveService
{
    // FIELDS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private static final int MAX_UNKNOWN_SQUARE_COUNT = 3;
    private static final int MAX_TURNS_SINCE_LAST_SCAN = 2;

    // PUBLIC METHODS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Determines the next low risk mower move
     *
     * @return - The mower move the mower should attempt to make
     */
    // THIS METHOD PATTERN WILL BE THE SAME FOR THE LOW, MED, AND HIGH PATTERNS BUT DIFFERENT FOR NO RISK
    // IN ORDER TO AVOID DUPLICATION WE WOULD NEED TO IMPLEMENT THIS METHOD IN THE ABSTRACT CLASS AND THEN
    // ABSTRACT THE 2 METHODS TO DETERMINE THE ELIGIBLE/INELIGIBLE MOVES AND IMPLEMENT THEM IN EACH CONCRETE
    // CLASS. 1, THAT WILL NOT WORK 100% BECAUSE THE NO RISK CLASS DOES NOT NEED BOTH METHODS AND 2, THAT WOULD
    // BE A REALLY WEIRD PATTERN BECAUSE THIS IS THE METHOD THAT SHOULD BE CALLED BY THE MOWER AND IT FEELS
    // BACKWARDS TO ONLY CALL A SINGLE METHOD WHICH RESIDES IN THE ABSTRACT CLASS BUT SOLELY DEPENDS ON METHODS
    // IN THE CONCRETE CLASS, SO THAT IS WHY I AM ALLOWING DUPLICATES FOR THIS METHODS IMPLEMENTATION
    @SuppressWarnings("Duplicates")
    @Override
    public MowerMove getNextMowerMove(final Mower mower)
    {
        MowerMove response;

        if(!mower.isStrategic())
        {
            response = getRandomMowerMove(mower);
        }
        // IF THE SURROUNDING SQUARES ARE EMPTY, HAVE TOO MANY UNKNOWNS, OR MAX TURNS SINCE LAST C_SCAN WE WANT TO C_SCAN
        else if(mower.getSurroundingSquares() == null
                || mower.getSurroundingSquares().isEmpty()
                || getSurroundingSquareUnknownCount(mower.getSurroundingSquares()) >= MAX_UNKNOWN_SQUARE_COUNT
                || mower.getTurnsSinceLastScan() >= MAX_TURNS_SINCE_LAST_SCAN)
        {
            // TODO: THIS WILL NEED TO BE UPDATED AFTER WE ADD IN THE NEW SCANNING METHOD
            response = new MowerMove(mower,
                    MowerMovementType.C_SCAN,
                    mower.getCurrentDirection(),
                    mower.getCurrentXCoordinate(),
                    mower.getCurrentYCoordinate());
        }
        else
        {
            response = determineMoveEligibleMove(mower);
        }

        return response;
    }
    // PRIVATE METHODS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Determines the next mower move for move eligible moves
     *
     * @param mower - The mower that the move is for
     *
     * @return - The next mower move
     */
    @SuppressWarnings("Duplicates") // FOR THE VARIABLE INITIALIZATIONS AT THE TOP
    private MowerMove determineMoveEligibleMove(final Mower mower)
    {
        MowerMove response;

        // GET THE VALUES FROM THE OBJECT TO MAKE THE CODE CLEANER BELOW THIS
        final List<LawnSquareContent> surroundingSquares = mower.getSurroundingSquares();
        final Direction currDirection = mower.getCurrentDirection();
        final int currXCoor = mower.getCurrentXCoordinate();
        final int currYCoor = mower.getCurrentYCoordinate();

        final List<List<Integer>> possibleMovesList = getPossibleMovesByRanking(mower);
        final List<Integer> medRiskMoves   = possibleMovesList.get(2);
        final List<Integer> preferredMoves = possibleMovesList.get(3);

        LawnSquareContent facingContent = surroundingSquares.get(currDirection.getIndex());

        // IF THE MOWER IS ALREADY POINTING TOWARDS A GRASS SQUARE AND IT IS A PREFERRED MOVE, TAKE IT!
        if(facingContent == LawnSquareContent.GRASS && preferredMoves.contains(currDirection.getIndex()))
        {
            response = getMowerMoveForMovingInCurrentDirection(mower);
        }
        // IF THE MOWER IS NOT POINTING TOWARDS A GRASS PREFERRED MOVE THEN SEE WHICH PREFERRED MOVE TO TAKE
        else if(!preferredMoves.isEmpty())
        {
            // CHECK ALL OF THE PREFERRED MOVES TO SEE WHICH ONES ARE GRASS
            List<Integer> preferredGrassSquares =
                    getSubListForContentType(preferredMoves, surroundingSquares, LawnSquareContent.GRASS);

            // 1. IF THERE ARE GRASS MOVES AVAILABLE STEER TO ONE OF THEM
            if(!preferredGrassSquares.isEmpty())
            {
                response = getRandomMowerSteerMove(preferredGrassSquares, mower);
            }
            // 2. IF GRASS MOVES ARE NOT AVAILABLE BUT FACING A PREFERRED MOVE, TAKE IT
            else if(preferredMoves.contains(currDirection.getIndex()))
            {
                response = getMowerMoveForMovingInCurrentDirection(mower);
            }
            // 3. IF NO GRASS PREF MOVES AVAILABLE AND NOT FACING A PREFERRED MOVE,
            //    THEN SELECT A RANDOM PREF MOVE TO STEER TOWARDS
            else{
                response = getRandomMowerSteerMove(preferredMoves, mower);
            }
        }
        // IF MED MOVES ARE NOT EMPTY WE ARE GOING TO MAKE A 50/50 CHOICE TO EITHER C_SCAN OR TAKE MED RISK MOVE
        else if(!medRiskMoves.isEmpty())
        {
            Random random = new Random();

            // 50/50 OPTION 1: C_SCAN
            if(random.nextBoolean())
            {
                response = new MowerMove(mower, MowerMovementType.C_SCAN, currDirection, currXCoor, currYCoor);
            }
            // 50/50 OPTION 2: SELECT A MEDIUM RISK MOVE
            else{
                // IF ALREADY FACING A MEDIUM RISK MOVE, THEN TAKE IT
                if(medRiskMoves.contains(currDirection.getIndex()))
                {
                    response = getMowerMoveForMovingInCurrentDirection(mower);
                }
                // CHOOSE A RANDOM MEDIUM RISK MOVE TO STEER TO
                else{
                    response = getRandomMowerSteerMove(medRiskMoves, mower);
                }
            }
        }
        // IF ONLY HIGH RISK MOVES ARE AVAILABLE THEN C_SCAN
        else{
            response = new MowerMove(mower, MowerMovementType.C_SCAN, currDirection, currXCoor, currYCoor);
        }

        return response;
    }
}
