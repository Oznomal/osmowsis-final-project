package com.osmowsis.osmowsisfinalproject.service;

import com.osmowsis.osmowsisfinalproject.constant.Direction;
import com.osmowsis.osmowsisfinalproject.constant.LawnSquareContent;
import com.osmowsis.osmowsisfinalproject.constant.MowerMovementType;
import com.osmowsis.osmowsisfinalproject.model.CentralMowerMap;
import com.osmowsis.osmowsisfinalproject.pojo.Mower;
import com.osmowsis.osmowsisfinalproject.pojo.MowerMove;
import com.osmowsis.osmowsisfinalproject.service.base.NextMowerMoveService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
    private static final int MAX_UNKNOWN_SQUARE_COUNT = 6;
    private static final int MAX_TURNS_SINCE_LAST_SCAN = 2;
    private static int numberOfScans = 0;

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
            if (numberOfScans%2 == 0){
                response = new MowerMove(mower,
                        MowerMovementType.C_SCAN,
                        mower.getCurrentDirection(),
                        mower.getCurrentXCoordinate(),
                        mower.getCurrentYCoordinate());
            }
            else {
                response = new MowerMove(mower,
                        MowerMovementType.L_SCAN,
                        mower.getCurrentDirection(),
                        mower.getCurrentXCoordinate(),
                        mower.getCurrentYCoordinate());
            }
            numberOfScans++;

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
//    @SuppressWarnings("Duplicates") // FOR THE VARIABLE INITIALIZATIONS AT THE TOP
//    private MowerMove determineMoveEligibleMove(final Mower mower)
//    {
//        MowerMove response;
//
//        // GET THE VALUES FROM THE OBJECT TO MAKE THE CODE CLEANER BELOW THIS
//        final List<LawnSquareContent> surroundingSquares = mower.getSurroundingSquares();
//        final Direction currDirection = mower.getCurrentDirection();
//        final int currXCoor = mower.getCurrentXCoordinate();
//        final int currYCoor = mower.getCurrentYCoordinate();
//
//        final List<List<Integer>> possibleMovesList = getPossibleMovesByRanking(mower);
//        final List<Integer> medRiskMoves   = possibleMovesList.get(2);
//        final List<Integer> preferredMoves = possibleMovesList.get(3);
//
//        LawnSquareContent facingContent = surroundingSquares.get(currDirection.getIndex());
//
//        // IF THE MOWER IS ALREADY POINTING TOWARDS A GRASS SQUARE AND IT IS A PREFERRED MOVE, TAKE IT!
//        if(facingContent == LawnSquareContent.GRASS && preferredMoves.contains(currDirection.getIndex()))
//        {
//            response = getMowerMoveForMovingInCurrentDirection(mower);
//        }
//        // IF THE MOWER IS NOT POINTING TOWARDS A GRASS PREFERRED MOVE THEN SEE WHICH PREFERRED MOVE TO TAKE
//        else if(!preferredMoves.isEmpty())
//        {
//            // CHECK ALL OF THE PREFERRED MOVES TO SEE WHICH ONES ARE GRASS
//            List<Integer> preferredGrassSquares =
//                    getSubListForContentType(preferredMoves, surroundingSquares, LawnSquareContent.GRASS);
//
//            // 1. IF THERE ARE GRASS MOVES AVAILABLE STEER TO ONE OF THEM
//            if(!preferredGrassSquares.isEmpty())
//            {
//                response = getRandomMowerSteerMove(preferredGrassSquares, mower);
//            }
//            // 2. IF GRASS MOVES ARE NOT AVAILABLE BUT FACING A PREFERRED MOVE, TAKE IT
//            else if(preferredMoves.contains(currDirection.getIndex()))
//            {
//                response = getMowerMoveForMovingInCurrentDirection(mower);
//            }
//            // 3. IF NO GRASS PREF MOVES AVAILABLE AND NOT FACING A PREFERRED MOVE,
//            //    THEN SELECT A RANDOM PREF MOVE TO STEER TOWARDS
//            else{
//                response = getRandomMowerSteerMove(preferredMoves, mower);
//            }
//        }
//        // IF MED MOVES ARE NOT EMPTY WE ARE GOING TO MAKE A 50/50 CHOICE TO EITHER C_SCAN OR TAKE MED RISK MOVE
//        else if(!medRiskMoves.isEmpty())
//        {
//            Random random = new Random();
//
//            // 50/50 OPTION 1: C_SCAN
//            if(random.nextBoolean())
//            {
//                response = new MowerMove(mower, MowerMovementType.C_SCAN, currDirection, currXCoor, currYCoor);
//            }
//            // 50/50 OPTION 2: SELECT A MEDIUM RISK MOVE
//            else{
//                // IF ALREADY FACING A MEDIUM RISK MOVE, THEN TAKE IT
//                if(medRiskMoves.contains(currDirection.getIndex()))
//                {
//                    response = getMowerMoveForMovingInCurrentDirection(mower);
//                }
//                // CHOOSE A RANDOM MEDIUM RISK MOVE TO STEER TO
//                else{
//                    response = getRandomMowerSteerMove(medRiskMoves, mower);
//                }
//            }
//        }
//        // IF ONLY HIGH RISK MOVES ARE AVAILABLE THEN C_SCAN
//        else{
//            response = new MowerMove(mower, MowerMovementType.C_SCAN, currDirection, currXCoor, currYCoor);
//        }
//
//        return response;
//    }

    private MowerMove determineMoveEligibleMove(final Mower mower) {

        //Find moves that move away from nearby gophers
        List<Integer> fences = new ArrayList<>();
        List<Integer> possibleDirections = findMovesAwayFromGophers(mower, fences);
        List<Integer> mowerDirections = findMoveWithNoMower(possibleDirections, mower);
        List<Integer> directionsWithGrass = findDirectionsWithGrass(mower, possibleDirections);
        List<Integer> directionsWithEmptySquares = findDirectionsWithEmptySquares(mower, possibleDirections);
        if (possibleDirections.size() + fences.size() == 8) {
            for (int grassIdx : directionsWithGrass) {
                if (mower.getCurrentDirection().getIndex() == grassIdx) {
                    return new MowerMove(mower,
                            MowerMovementType.MOVE,
                            mower.getCurrentDirection(),
                            mower.getCurrentXCoordinate(),
                            mower.getCurrentYCoordinate(),
                            mower.getCurrentXCoordinate()+mower.getCurrentDirection().getxIncrement(),
                            mower.getCurrentYCoordinate() + mower.getCurrentDirection().getyIncrement());
                }
            }
            if (directionsWithGrass.size() > 0) {
                Direction newDirection = Direction.getDirectionByIndex(directionsWithGrass.get(0));
                return new MowerMove(mower,
                        MowerMovementType.STEER,
                        newDirection,
                        mower.getCurrentXCoordinate(),
                        mower.getCurrentYCoordinate());
            }
            else {
                for (int emptyIdx : directionsWithEmptySquares) {
                    if (mower.getCurrentDirection().getIndex() == emptyIdx) {
                        MowerMove move = new MowerMove(mower,
                                MowerMovementType.MOVE,
                                mower.getCurrentDirection(),
                                mower.getCurrentXCoordinate(),
                                mower.getCurrentYCoordinate(),
                                mower.getCurrentXCoordinate()+mower.getCurrentDirection().getxIncrement(),
                                mower.getCurrentYCoordinate() + mower.getCurrentDirection().getyIncrement());
                    }
                }
                if (directionsWithEmptySquares.size() > 0) {
                    Direction newDirection = Direction.getDirectionByIndex(directionsWithEmptySquares.get(0));
                    return new MowerMove(mower,
                            MowerMovementType.STEER,
                            newDirection,
                            mower.getCurrentXCoordinate(),
                            mower.getCurrentYCoordinate());
            }
        }
        }
        else {
            for (int idx: possibleDirections){
                if (mower.getCurrentDirection().getIndex() == idx
                  && !mowerDirections.contains(idx)) {
                    return new MowerMove(mower,
                            MowerMovementType.MOVE,
                            mower.getCurrentDirection(),
                            mower.getCurrentXCoordinate(),
                            mower.getCurrentYCoordinate(),
                            mower.getCurrentXCoordinate()+mower.getCurrentDirection().getxIncrement(),
                            mower.getCurrentYCoordinate() + mower.getCurrentDirection().getyIncrement());
                }

            }
            if (possibleDirections.size() > 0) {
                Direction newDirection = Direction.getDirectionByIndex(possibleDirections.get(0));
                return new MowerMove(mower,
                        MowerMovementType.STEER,
                        newDirection,
                        mower.getCurrentXCoordinate(),
                        mower.getCurrentYCoordinate());
            }
        }
        return  new MowerMove(mower,
                MowerMovementType.PASS,
                mower.getCurrentDirection(),
                mower.getCurrentXCoordinate(),
                mower.getCurrentYCoordinate());
    }

    private List<Integer> findDirectionsWithEmptySquares(Mower mower, List<Integer> possibleDirections) {
        List<Integer> directionsWithEmptySquares = new ArrayList<>();
        for (int idx: possibleDirections){
            if (mower.getSurroundingSquares().get(idx).equals(LawnSquareContent.EMPTY)
            || mower.getSurroundingSquares().get(idx).equals(LawnSquareContent.EMPTY_CHARGER)){
                directionsWithEmptySquares.add(idx);
            }
        }
        return directionsWithEmptySquares;
    }

    private List<Integer> findDirectionsWithGrass(Mower mower, List<Integer> possibleDirections) {
        List<Integer> directionsWithGrass = new ArrayList<>();
        for (int idx: possibleDirections){
            if (mower.getSurroundingSquares().get(idx).equals(LawnSquareContent.GRASS)){
                directionsWithGrass.add(idx);
            }
        }
        return directionsWithGrass;
    }

    private List<Integer> findMoveWithNoMower(List<Integer> possibleDirections, Mower mower) {
        List<Integer> mowerLocations = new ArrayList<>();
        //Iterate again and remove mowers
        for (int idx: possibleDirections){
            if (mower.getSurroundingSquares().get(idx).equals(LawnSquareContent.EMPTY_MOWER_CHARGER)
                    || mower.getSurroundingSquares().get(idx).equals(LawnSquareContent.EMPTY_MOWER)){
                mowerLocations.add(idx);
            }
        }
        return mowerLocations;
    }

    private List<Integer> findMovesAwayFromGophers(Mower mower, List<Integer> fences) {
        List<Integer> possibleDirections = new ArrayList<>();
        for (int i =0; i < 8; i++){
            possibleDirections.add(i);
        }
        for (int j=0; j < 8; j++){
           LawnSquareContent lawnSquareContent = mower.getSurroundingSquares().get(j);
           if (lawnSquareContent.equals(LawnSquareContent.EMPTY_GOPHER)
           || lawnSquareContent.equals(LawnSquareContent.GRASS_GOPHER)
           || lawnSquareContent.equals(LawnSquareContent.EMPTY_GOPHER_CHARGER)){
               if (j ==0 || j == 2 || j == 4 || j == 6){
                   removeFiveIndices(j, possibleDirections);
               }
               else {
                   removeThreeIndices(j, possibleDirections);
               }
           }
           else if (lawnSquareContent.equals(LawnSquareContent.FENCE)){
               int index = possibleDirections.indexOf(j);
               possibleDirections.remove(index);
               fences.add(j);
           }
        }
        return possibleDirections;
    }

    private void removeThreeIndices(int j, List<Integer> possibleDirections) {

        int before = j-1;
        int next = j+1;
        if (next == 8) next = 0;

        removeIndex(before, possibleDirections);
        removeIndex(j, possibleDirections);
        removeIndex(next, possibleDirections);

    }

    private void removeFiveIndices(int j, List<Integer> possibleDirections) {
        if (j == 0){
            removeIndex(6, possibleDirections);
            removeIndex(7, possibleDirections);
            removeIndex(0, possibleDirections);
            removeIndex(1, possibleDirections);
            removeIndex(2, possibleDirections);

        }
        else if (j == 2){
            removeIndex(0, possibleDirections);
            removeIndex(1, possibleDirections);
            removeIndex(2, possibleDirections);
            removeIndex(3, possibleDirections);
            removeIndex(4, possibleDirections);

        }
        else if (j == 4){
            removeIndex(2, possibleDirections);
            removeIndex(3, possibleDirections);
            removeIndex(4, possibleDirections);
            removeIndex(5, possibleDirections);
            removeIndex(6, possibleDirections);

        }
        else if (j == 6){
            removeIndex(4, possibleDirections);
            removeIndex(5, possibleDirections);
            removeIndex(6, possibleDirections);
            removeIndex(7, possibleDirections);
            removeIndex(0, possibleDirections);

        }
    }

    private void removeIndex(int j, List<Integer> possibleDirections){{
        if (possibleDirections.indexOf(j) != -1){
            possibleDirections.remove(possibleDirections.indexOf(j));
        }
    }

    }
}
