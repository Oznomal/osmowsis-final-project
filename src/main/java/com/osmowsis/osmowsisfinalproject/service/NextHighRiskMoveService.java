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

/**
 * Concrete singleton implementation for determining the next high risk mower move
 *
 * Created on 9/28/2019
 */

@Service
@Qualifier("highRiskMoveService")
class NextHighRiskMoveService extends NextMowerMoveService
{
    // FIELDS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private static final int MAX_UNKNOWN_SQUARE_COUNT = 5;
    private static final int MAX_TURNS_SINCE_LAST_SCAN = 3;

    // PACKAGE METHODS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Determines the next high risk mower move
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
        else if(mower.getSurroundingSquares().isEmpty()
                || getSurroundingSquareUnknownCount(mower.getSurroundingSquares()) >= MAX_UNKNOWN_SQUARE_COUNT
                || mower.getTurnsSinceLastScan() >= MAX_TURNS_SINCE_LAST_SCAN)
        {
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

        final List<Integer> prefGrassMoves = getSubListForContentType(preferredMoves, surroundingSquares, LawnSquareContent.GRASS);
        final List<Integer> prefEmptyMoves = getSubListForContentType(preferredMoves, surroundingSquares, LawnSquareContent.EMPTY);
        final List<Integer> medGrassMoves  = getSubListForContentType(medRiskMoves, surroundingSquares, LawnSquareContent.GRASS);
        final List<Integer> medEmptyMoves  = getSubListForContentType(medRiskMoves, surroundingSquares, LawnSquareContent.EMPTY);

        // MOVE TOWARDS PREFERRED GRASS OR MEDIUM RISK GRASS
        if(prefGrassMoves.contains(currDirection.getIndex()) || medGrassMoves.contains(currDirection.getIndex()))
        {
            response = new MowerMove(mower, MowerMovementType.MOVE, currDirection,
                    currXCoor,
                    currYCoor,
                    currXCoor + currDirection.getxIncrement(),
                    currYCoor + currDirection.getyIncrement());
        }
        // IF NO PREF OR MED GRASS MOVES TO MOVE TO, STEER TOWARDS PREF GRASS
        else if(!prefGrassMoves.isEmpty())
        {
            response = getRandomMowerSteerMove(prefGrassMoves, mower);
        }
        // IF NO PREF OR MEDIUM GRASS TO MOVE TO THEN MOVE OR STEER FOR PREFERRED EMPTY
        else if(!prefEmptyMoves.isEmpty())
        {
            response = getMoveOrSteerMoveForSublist(prefEmptyMoves, mower);
        }
        // IF NO PREF EMPTY THEN MOVE OR STEER FOR MEDIUM EMPTY
        else if(!medEmptyMoves.isEmpty())
        {
            response = getMoveOrSteerMoveForSublist(medEmptyMoves, mower);
        }
        // IF ONLY HIGH RISK MOVES ARE AVAILABLE C_SCAN
        else{
            response = new MowerMove(mower, MowerMovementType.C_SCAN, currDirection, currXCoor, currYCoor);
        }

        return response;
    }
}
