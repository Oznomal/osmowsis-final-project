package com.osmowsis.osmowsisfinalproject.service.base;

import com.osmowsis.osmowsisfinalproject.constant.Direction;
import com.osmowsis.osmowsisfinalproject.constant.LawnSquareContent;
import com.osmowsis.osmowsisfinalproject.constant.MowerMovementType;
import com.osmowsis.osmowsisfinalproject.pojo.Coordinate;
import com.osmowsis.osmowsisfinalproject.pojo.Mower;
import com.osmowsis.osmowsisfinalproject.pojo.MowerMove;

import java.util.*;

/**
 * I created this abstract class because I decided on the strategy to have risk profiles after I had made my initial
 * design. I could have left all of this logic for determining the next move based on the risk levels in the
 * mower class but it makes more sense to break it out into this pattern. This allows me to add
 * more risk profiles and easily implement them while still keeping things nice and modular
 *
 * Created by L. Arroyo on 9/28/2019
 */
public abstract class NextMowerMoveService
{

    private Set<Coordinate> gopherCoordinates = new HashSet<>();
    // ABSTRACT METHODS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Gets the next mower move
     *
     * @param mower - The mower to determine the next move for
     *
     * @return - A mower move
     */
    public abstract MowerMove getNextMowerMove(final Mower mower);

    // DEFAULT PACKAGE ONLY METHODS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Gets a random mower move .... why? I have no idea ... kinda defeats the purpose of implementing an algorithm
     * to actually solve the problem but I digress.
     *
     * @return - A random mower move
     */
    protected MowerMove getRandomMowerMove(final Mower mower)
    {
        MowerMove response;

        final Direction currDirection = mower.getCurrentDirection();
        final int currXCoor = mower.getCurrentXCoordinate();
        final int currYCoor = mower.getCurrentYCoordinate();

        final Random random = new Random();

        final int moveType = random.nextInt(100);

        // PASS
        if(moveType <= 10)
        {
            response = new MowerMove(mower, MowerMovementType.PASS, currDirection, currXCoor, currYCoor);
        }
        // MOVE
        else if(moveType <= 45)
        {
            response = getMowerMoveForMovingInCurrentDirection(mower);
        }
        // C_SCAN
        else if(moveType <= 65)
        {
            response = new MowerMove(mower, MowerMovementType.C_SCAN, currDirection, currXCoor, currYCoor);
        }
        // STEER
        else if(moveType < 100)
        {
            response = getRandomMowerSteerMove(new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4, 5 , 6, 7)), mower);
        }
        else{
            // CODE SHOULD NEVER REACH HERE
            throw new RuntimeException("[RANDOM INT ERROR] :: getRandomMowerMove - An invalid random was returned");
        }

        return response;
    }

    /**
     * Ranks the possible moves based off of moves which are forbidden, high risk, medium risk, and preferred
     *
     * Note: This does not take into account whether or not a square is Grass or Empty when placing them in the
     *       preferred moves set, it just organizes them by risk
     *
     * Returns list of sets in the following order:
     *
     * 0 - Forbidden Moves: Moves which are guaranteed to cause a collision (Fence or Crater)
     *
     * 1 - High Risk Moves: Moves which represent another mower, not recommended but may work because mowers can move
     *                      and the surrounding square model can become outdated in the event that other mowers which
     *                      were picked up in a previous scan occur before this mower in the rotation of mowers in
     *                      the sim and have moved prior to this mowers current turn. These moves can also represent
     *                      an unknown square, which are very high risk because it could be anything
     *
     * 2 - Med Risk Moves:  Moves which represent the squares which a high risk move could have moved to in a previous
     *                      move, ex: if a mower is positioned at index 1 in the model (NORTHEAST), then
     *                      the squares it could have moved to in its next move would be index 0 (NORTH) OR 2 (EAST)
     *                      these are considered medium risk moves because the mower could have
     *                      moved there and the model may just be outdated
     *
     * 3 - Pref Moves:     These are the safest moves a mower can make, this is the subset of remaining moves that are
     *                     not considered forbidden, high risk, or medium risk.
     *
     * //@param surroundingSquares - The surrounding squares model for the mower
     *
     * @return - 4 lists of moves: forbidden, high risk, medium risk, and preferred (in that order)
     */
    protected List<List<Integer>> getPossibleMovesByRanking( Mower mower)
    {
        List<LawnSquareContent> surroundingSquares = mower.getSurroundingSquares();
        List<Integer> forbiddenMoves = new ArrayList<>();
        List<Integer> highRiskMoves  = new ArrayList<>();
        List<Integer> medRiskMoves   = new ArrayList<>();
        List<Integer> preferredMoves = new ArrayList<>();

        // 1. LOOP THROUGH THE LIST AND GET THE SQUARES THE SQUARE INDEXES WHICH ARE FORBIDDEN / HIGH RISK
        for(int i = 0; i < 8; i++)
        {
            LawnSquareContent content = surroundingSquares.get(i);

            if(content == LawnSquareContent.MOWER || content == LawnSquareContent.UNKNOWN
                    || content == LawnSquareContent.EMPTY_GOPHER || content == LawnSquareContent.GRASS_GOPHER)
            {
                highRiskMoves.add(i);
            }
            else if(content == LawnSquareContent.FENCE )
            {
                forbiddenMoves.add(i);
            }
        }

        // 2. LOOP THROUGH THE LIST CHECKING FOR MOWERS AND ADDING MED RISK SQUARES IF THEY ARE FOUND
        if(!highRiskMoves.isEmpty())
        {
            for(Integer idx : highRiskMoves)
            {
                if(surroundingSquares.get(idx) == LawnSquareContent.EMPTY_MOWER
                || surroundingSquares.get(idx) == LawnSquareContent.EMPTY_MOWER_CHARGER
                        || surroundingSquares.get(idx) == LawnSquareContent.EMPTY_GOPHER_CHARGER
                        || surroundingSquares.get(idx) == LawnSquareContent.EMPTY_GOPHER
                        || surroundingSquares.get(idx) == LawnSquareContent.GRASS_GOPHER

                )
                {
                    for(Integer riskyIndex : determineMedRiskMovesForHighRiskSquare(idx))
                    {
                        if(!forbiddenMoves.contains(riskyIndex)
                                && !highRiskMoves.contains(riskyIndex)
                                && !medRiskMoves.contains(riskyIndex))
                        {
                            medRiskMoves.add(riskyIndex);
                        }
                    }
                }
            }
        }

        // 3. USE THE REMAINING INDEXES TO COMPLETE THE PREFERRED MOVES SET
        for(int i = 0; i < 8; i++)
        {
            if(!forbiddenMoves.contains(i) && !highRiskMoves.contains(i) && !medRiskMoves.contains(i))
            {
                preferredMoves.add(i);
            }
        }

        return new ArrayList<>(Arrays.asList(forbiddenMoves, highRiskMoves, medRiskMoves, preferredMoves));
    }

    /**
     * Gets the number of unknown squares in the surrounding squares list
     *
     * Note:
     * If the list is null or empty it will return INT_MAX
     *
     * @return - The number of unknown squares in the surrounding square list
     */
    protected int getSurroundingSquareUnknownCount(final List<LawnSquareContent> surroundingSquares)
    {
        if(surroundingSquares == null || surroundingSquares.isEmpty())
        {
            return Integer.MAX_VALUE;
        }

        int count = 0;

        for(LawnSquareContent content : surroundingSquares)
        {
            if(content == LawnSquareContent.UNKNOWN)
            {
                count++;
            }
        }

        return count;
    }

    /**
     * Creates a Move mower move for the current direction
     *
     * @param mower - The mower for the move
     *
     * @return - The mower move
     */
    protected MowerMove getMowerMoveForMovingInCurrentDirection(final Mower mower)
    {
        int newXCoor = mower.getCurrentXCoordinate() + mower.getCurrentDirection().getxIncrement();
        int newYCoor = mower.getCurrentYCoordinate() + mower.getCurrentDirection().getyIncrement();

        return new MowerMove(mower,
                MowerMovementType.MOVE,
                mower.getCurrentDirection(),
                mower.getCurrentXCoordinate(),
                mower.getCurrentYCoordinate(),
                newXCoor,
                newYCoor);
    }

    /**
     * Gets a random directional move from a list of integers, where each integer represents a direction
     *
     * @param availableIndexList - The available directions to select from
     * @param mower - The mower to get the move for
     *
     * @return - A STEER mower move in a random direction
     */
    protected MowerMove getRandomMowerSteerMove(final List<Integer> availableIndexList, final Mower mower)
    {
        Random random = new Random();

        boolean foundNewDirection = false;
        Direction newDirection = null;

        while(!foundNewDirection)
        {
            int idx = random.nextInt(availableIndexList.size());

            newDirection = Direction.getDirectionByIndex(availableIndexList.get(idx));

            if(newDirection != mower.getCurrentDirection())
            {
                foundNewDirection = true;
            }
        }

        return new MowerMove(mower,
                MowerMovementType.STEER, newDirection, mower.getCurrentXCoordinate(), mower.getCurrentYCoordinate());
    }

    /**
     * Gets a sublist of indexes for a specific lawn square content type
     *
     * @param indexes - The list of indexes to check for the content type
     * @param surroundingSquares - The complete list of surrounding squares
     * @param contentToken - The content type to search for
     *
     * @return - A sublist of the indexes list that contains the content type
     */
    protected List<Integer> getSubListForContentType(final List<Integer> indexes,
                                           final List<LawnSquareContent> surroundingSquares,
                                           final LawnSquareContent contentToken)
    {
        List<Integer> response = new ArrayList<>();

        for(Integer idx : indexes)
        {
            if(surroundingSquares.get(idx) == contentToken)
            {
                response.add(idx);
            }
        }

        return response;
    }

    /**
     * Determines a move or steer move based on a sublist of moves, if the mower already facing one of the
     * moves then the move will be a move. However, if the mower is not facing one of the moves in the sublist
     * the move will be steering towards a random direction in the sublist
     *
     * @param sublist - The sublist of moves
     * @param mower - The mower
     *
     * @return - The move or steer move
     */
    protected MowerMove getMoveOrSteerMoveForSublist(final List<Integer> sublist, final Mower mower)
    {
        MowerMove response;

        if(sublist.contains(mower.getCurrentDirection().getIndex()))
        {
            response = new MowerMove(mower,
                    MowerMovementType.MOVE,
                    mower.getCurrentDirection(),
                    mower.getCurrentXCoordinate(),
                    mower.getCurrentYCoordinate(),
                    mower.getCurrentXCoordinate() + mower.getCurrentDirection().getxIncrement(),
                    mower.getCurrentYCoordinate() + mower.getCurrentDirection().getyIncrement());
        }
        else
        {
            response = getRandomMowerSteerMove(sublist, mower);
        }

        return response;
    }

    // PRIVATE METHODS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Determines med risk moves based on a surrounding square that contains a mower or movable obstacle
     *
     * @param surroundingSquareIndex - The index of the surrounding square that contains a mower
     *
     * @return - A set of indexes for surrounding squares that are considered a med risk move based on the mower
     */
    private Set<Integer> determineMedRiskMovesForHighRiskSquare(final int surroundingSquareIndex)
    {
        if(surroundingSquareIndex == 0)
        {
            return new HashSet<>(Arrays.asList(1, 2, 6, 7));
        }
        else if(surroundingSquareIndex == 1)
        {
            return new HashSet<>(Arrays.asList(0, 2));
        }
        else if(surroundingSquareIndex == 2)
        {
            return new HashSet<>(Arrays.asList(0, 1, 3, 4));
        }
        else if(surroundingSquareIndex == 3)
        {
            return new HashSet<>(Arrays.asList(2, 4));
        }
        else if(surroundingSquareIndex == 4)
        {
            return new HashSet<>(Arrays.asList(2, 3, 5, 6));
        }
        else if(surroundingSquareIndex == 5)
        {
            return new HashSet<>(Arrays.asList(4, 6));
        }
        else if(surroundingSquareIndex == 6)
        {
            return new HashSet<>(Arrays.asList(0, 4, 5, 7));
        }
        else if(surroundingSquareIndex == 7)
        {
            return new HashSet<>(Arrays.asList(0, 6));
        }
        else{
            // THIS SHOULD NOT BE REACHED EVER BECAUSE THE INDEX SHOULD ALWAYS BE BETWEEN 0-7
            throw new RuntimeException("[UNEXPECTED INDEX ERROR] :: determineMedRiskMovesForHighRiskSquare, idx="
                    + surroundingSquareIndex);
        }
    }
}
