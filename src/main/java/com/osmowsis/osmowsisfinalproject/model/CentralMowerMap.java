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

    public static int minDistanceToGopher(Coordinate coordinate){
        int distance = Integer.MAX_VALUE;
        if (gopherCoords.size() > 0){

        }
        return 0;
    }



}
