package com.osmowsis.osmowsisfinalproject.model;

import com.osmowsis.osmowsisfinalproject.constant.LawnSquareContent;
import com.osmowsis.osmowsisfinalproject.pojo.Coordinate;
import com.osmowsis.osmowsisfinalproject.pojo.LawnSquare;
import com.osmowsis.osmowsisfinalproject.pojo.Mower;
import lombok.Getter;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * Model of the map that mowers can utilize
 */

@Repository
public class MapModel implements BaseDataModel
{
    // FIELDS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Getter
    private List<LawnSquare> lawnSquares;
    private List<Coordinate> gopherCoords;

    // CONSTRUCTORS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public MapModel()
    {
        resetDataModel();
    }

    // PUBLIC METHODS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void addMower(Mower mower){

        LawnSquare lawnSquare = getLawnSquareByCoords(mower.getCurrentXCoordinate(),
                mower.getCurrentYCoordinate());
        if (lawnSquare != null){
            lawnSquare.setLawnSquareContent(LawnSquareContent.EMPTY_MOWER_CHARGER);
        }
    }

    public LawnSquare getLawnSquareByCoords(int x, int y){
        for (LawnSquare lawnSquare: lawnSquares){
            if (lawnSquare.getXCoordinate() == x && lawnSquare.getYCoordinate() == y){
                return lawnSquare;
            }
        }
        return null;
    }

    public LawnSquareContent getLawnSquareContentByCoordinates(final int x, final int y)
    {
        final LawnSquare lawnSquare = getLawnSquareByCoords(x, y);

        return lawnSquare == null ? LawnSquareContent.FENCE : lawnSquare.getLawnSquareContent();
    }

    public List<LawnSquareContent> getSurroundingSquares(Mower mower){
        int x = mower.getCurrentXCoordinate();
        int y = mower.getCurrentYCoordinate();
        List<LawnSquareContent> surroundingSquares = new ArrayList<>();
        surroundingSquares.add(0, getLawnSquareContentByCoordinates(x, y + 1));
        surroundingSquares.add(1, getLawnSquareContentByCoordinates(x + 1, y + 1));
        surroundingSquares.add(2, getLawnSquareContentByCoordinates(x + 1, y));
        surroundingSquares.add(3, getLawnSquareContentByCoordinates(x + 1, y - 1));
        surroundingSquares.add(4, getLawnSquareContentByCoordinates(x, y - 1));
        surroundingSquares.add(5, getLawnSquareContentByCoordinates(x - 1, y - 1));
        surroundingSquares.add(6, getLawnSquareContentByCoordinates(x - 1, y));
        surroundingSquares.add(7, getLawnSquareContentByCoordinates(x - 1, y + 1));

        return surroundingSquares;
    }

    public void updateLawnSquareContent(LawnSquareContent lawnSquareContent, int x, int y){
        if (!lawnSquareContent.equals(LawnSquareContent.FENCE)){
            LawnSquare lawnSquare = getLawnSquareByCoords(x, y);
            if (lawnSquare != null){
                lawnSquare.setLawnSquareContent(lawnSquareContent);
                Coordinate coord = new Coordinate(x,y);
                if (lawnSquareContent.equals(LawnSquareContent.EMPTY_GOPHER_CHARGER)
                        || lawnSquareContent.equals(LawnSquareContent.EMPTY_GOPHER)
                        || lawnSquareContent.equals(LawnSquareContent.GRASS_GOPHER)){
                    gopherCoords.add(coord);
                }
                else {
                    gopherCoords.remove(coord);
                }
            }
        }
    }

    public int minDistanceToGopher(Mower mower, int directionIndex){
        int distance = Integer.MAX_VALUE;
        if (gopherCoords.size() > 0){
            for (Coordinate gopherCoord: gopherCoords) {
                Coordinate nextMoveCoord = getNextMoveCoord(mower, directionIndex);
                int tempDistance = calculateDistance(nextMoveCoord, gopherCoord);
                if (tempDistance < distance) distance = tempDistance;
            }
        }
        return distance;
    }

    public int calculateDistance(Coordinate mowerCoord, Coordinate gopherCoord)
    {
        if (mowerCoord.equals(gopherCoord)) return 0;

        if (mowerCoord.getX() > gopherCoord.getX() && mowerCoord.getY() >gopherCoord.getY()){
            //NORTHEAST
            Coordinate newCoord = new Coordinate(gopherCoord.getX()+1, gopherCoord.getY()+1);
            return calculateDistance(mowerCoord, newCoord) + 1;
        }
        else if (mowerCoord.getX() < gopherCoord.getX() && mowerCoord.getY() >gopherCoord.getY()) {
            //NORTHWEST
            Coordinate newCoord = new Coordinate(gopherCoord.getX()-1, gopherCoord.getY()+1);
            return calculateDistance(mowerCoord, newCoord) + 1;
        }
        else if(mowerCoord.getX() < gopherCoord.getX() && mowerCoord.getY() < gopherCoord.getY()){
            //SOUTHWEST
            Coordinate newCoord = new Coordinate(gopherCoord.getX()-1, gopherCoord.getY()-1);
            return calculateDistance(mowerCoord, newCoord) + 1;
        }
        else if (mowerCoord.getX() > gopherCoord.getX() && mowerCoord.getY() < gopherCoord.getY()){
            //SOUTHEAST
            Coordinate newCoord = new Coordinate(gopherCoord.getX()+1, gopherCoord.getY()-1);
            return calculateDistance(mowerCoord, newCoord) + 1;
        }
        else if (mowerCoord.getX() == gopherCoord.getX()){
            if (mowerCoord.getY() < gopherCoord.getY()){
                //SOUTH
                Coordinate newCoord = new Coordinate(gopherCoord.getX(), gopherCoord.getY()-1);
                return calculateDistance(mowerCoord, newCoord) + 1;
            }
            else {
                //NORTH
                Coordinate newCoord = new Coordinate(gopherCoord.getX(), gopherCoord.getY()+1);
                return calculateDistance(mowerCoord, newCoord) + 1;
            }
        }
        else {
            if (mowerCoord.getX() < gopherCoord.getX()){
                //WEST
                Coordinate newCoord = new Coordinate(gopherCoord.getX()-1, gopherCoord.getY());
                return calculateDistance(mowerCoord, newCoord) + 1;
            }
            else {
                //EAST
                Coordinate newCoord = new Coordinate(gopherCoord.getX()+1, gopherCoord.getY());
                return calculateDistance(mowerCoord, newCoord) + 1;
            }
        }
    }

    @Override
    public void resetDataModel()
    {
        lawnSquares = new ArrayList<>();
        gopherCoords = new ArrayList<>();
    }

    // PRIVATE METHODS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private Coordinate getNextMoveCoord(Mower mower, int directionIndex) {
        if (directionIndex == 0){
            return new Coordinate(mower.getCurrentXCoordinate(), mower.getCurrentYCoordinate()+1);
        }
        else if (directionIndex == 1){
            return new Coordinate(mower.getCurrentXCoordinate()+1, mower.getCurrentYCoordinate()+1);
        }
        else if (directionIndex == 2){
            return new Coordinate(mower.getCurrentXCoordinate()+1, mower.getCurrentYCoordinate());
        }
        else if (directionIndex == 3){
            return new Coordinate(mower.getCurrentXCoordinate()-1, mower.getCurrentYCoordinate()-1);
        }
        else if (directionIndex == 4){
            return new Coordinate(mower.getCurrentXCoordinate(), mower.getCurrentYCoordinate()-1);
        }
        else if (directionIndex == 5){
            return new Coordinate(mower.getCurrentXCoordinate()-1, mower.getCurrentYCoordinate()-1);
        }
        else if (directionIndex == 6){
            return new Coordinate(mower.getCurrentXCoordinate()-1, mower.getCurrentYCoordinate());
        }
        else  {
            return new Coordinate(mower.getCurrentXCoordinate()-1, mower.getCurrentYCoordinate()+1);
        }
    }
}
