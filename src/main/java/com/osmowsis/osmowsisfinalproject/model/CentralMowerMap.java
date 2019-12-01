package com.osmowsis.osmowsisfinalproject.model;

import com.osmowsis.osmowsisfinalproject.constant.LawnSquareContent;
import com.osmowsis.osmowsisfinalproject.pojo.Coordinate;
import com.osmowsis.osmowsisfinalproject.pojo.LawnSquare;
import com.osmowsis.osmowsisfinalproject.pojo.Mower;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CentralMowerMap {

    static List<LawnSquare> lawnSquares = new ArrayList<>();
    static List<Coordinate> gopherCoords = new ArrayList<>();

    public static void addMower(Mower mower){

       LawnSquare lawnSquare = getLawnSquareByCoords(mower.getCurrentXCoordinate(),
               mower.getCurrentYCoordinate());
       if (lawnSquare != null){
           lawnSquare.setLawnSquareContent(LawnSquareContent.EMPTY_MOWER_CHARGER);
       }
    }

    public static LawnSquare getLawnSquareByCoords(int x, int y){
        for (LawnSquare lawnSquare: lawnSquares){
            if (lawnSquare.getXCoordinate() == x && lawnSquare.getYCoordinate() == y){
                return lawnSquare;
            }
        }
        return null;
    }

    public static LawnSquareContent getLawnSquareContentByCoordinates(final int x, final int y)
    {
        final LawnSquare lawnSquare = getLawnSquareByCoords(x, y);

        return lawnSquare == null ? LawnSquareContent.FENCE : lawnSquare.getLawnSquareContent();
    }

    public static List<LawnSquareContent> getSurroundingSquares(Mower mower){
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

    public static void updateLawnSquareContent(LawnSquareContent lawnSquareContent, int x, int y){
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

    public static int minDistanceToGopher(Mower mower, int directionIndex){
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

    private static Coordinate getNextMoveCoord(Mower mower, int directionIndex) {
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

    private static int calculateDistance(Coordinate mowerCoord, Coordinate gopherCoord) {
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


}
