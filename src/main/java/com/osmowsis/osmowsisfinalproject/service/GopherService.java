package com.osmowsis.osmowsisfinalproject.service;

import com.osmowsis.osmowsisfinalproject.constant.LawnSquareContent;
import com.osmowsis.osmowsisfinalproject.model.SimulationDataModel;

import com.osmowsis.osmowsisfinalproject.pojo.Coordinate;
import com.osmowsis.osmowsisfinalproject.pojo.Gopher;
import com.osmowsis.osmowsisfinalproject.pojo.LawnSquare;
import com.osmowsis.osmowsisfinalproject.pojo.Mower;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GopherService
{
    // FIELDS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private final SimulationDataModel simulationDataModel;
    private final LawnService lawnService;
    private final MowerService mowerService;

    private int currentGopherIndex = 0;


    // CONSTRUCTORS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Autowired
    public GopherService(final SimulationDataModel simulationDataModel,
                         final LawnService lawnService,
                         final MowerService mowerService)
    {
        this.simulationDataModel = simulationDataModel;
        this.lawnService = lawnService;
        this.mowerService = mowerService;
    }


    // PUBLIC METHODS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void moveGopher(){
        Gopher gopher = simulationDataModel.getGophers().get(currentGopherIndex);
        Coordinate nextCoord = determineMove(gopher);
        updateSimStateGopher(gopher, nextCoord);
        currentGopherIndex++;
        currentGopherIndex = currentGopherIndex%simulationDataModel.getGophers().size();
        if (currentGopherIndex == 0){
            simulationDataModel.incrementCurrentTurn();
        }
    }

    public void moveAllGophers(){
        this.currentGopherIndex = 0;
        for(Gopher gopher: simulationDataModel.getGophers()){
            moveGopher();
        }
    }

    public void updateSimStateGopher(Gopher gopher, Coordinate nextCoord) {
        LawnSquare newSquare = lawnService.getLawnSquareByCoordinates(nextCoord.getX(), nextCoord.getY());

        LawnSquare oldSquare =
                lawnService.getLawnSquareByCoordinates(gopher.getXCoordinate(), gopher.getYCoordinate());

        if (newSquare.getLawnSquareContent().equals(LawnSquareContent.EMPTY_MOWER) ){
            Mower collisionMower = simulationDataModel.getMowerByCoordinates(nextCoord.getX(), nextCoord.getY());

            String consoleText = getGopherMoveText(gopher, nextCoord)
                    + "\n\nGopher " + (gopher.getGopherNumber() + 1)
                    + " is destroying Mower " + (collisionMower.getMowerNumber() + 1);

            simulationDataModel.updateConsoleText(consoleText, true);

            mowerService.removeMowerInNewSquare(collisionMower);
            newSquare.setLawnSquareContent(LawnSquareContent.EMPTY_GOPHER);
            updateGopherPosition(gopher, nextCoord);
            updateOldSquare(oldSquare);
        }
        else if (newSquare.getLawnSquareContent().equals(LawnSquareContent.GRASS)){
            simulationDataModel.updateConsoleText(getGopherMoveText(gopher, nextCoord), true);
            newSquare.setLawnSquareContent(LawnSquareContent.GRASS_GOPHER);
            updateGopherPosition(gopher, nextCoord);
            updateOldSquare(oldSquare);
        }
        else if (newSquare.getLawnSquareContent().equals(LawnSquareContent.EMPTY_MOWER_CHARGER)){
            Mower collisionMower = simulationDataModel.getMowerByCoordinates(nextCoord.getX(), nextCoord.getY());

            String consoleText = getGopherMoveText(gopher, nextCoord)
                    + "\n\nGopher " + (gopher.getGopherNumber() + 1)
                    + " is destroying Mower " + (collisionMower.getMowerNumber() + 1);

            simulationDataModel.updateConsoleText(consoleText, true);

            mowerService.removeMowerInNewSquare(collisionMower);
            newSquare.setLawnSquareContent(LawnSquareContent.EMPTY_GOPHER_CHARGER);
            updateGopherPosition(gopher, nextCoord);
            updateOldSquare(oldSquare);
        }
        else if (newSquare.getLawnSquareContent().equals(LawnSquareContent.EMPTY)){
            simulationDataModel.updateConsoleText(getGopherMoveText(gopher, nextCoord), true);
            newSquare.setLawnSquareContent(LawnSquareContent.EMPTY_GOPHER);
            updateGopherPosition(gopher, nextCoord);
            updateOldSquare(oldSquare);
        }
        else if (newSquare.getLawnSquareContent().equals(LawnSquareContent.EMPTY_CHARGER)){
            simulationDataModel.updateConsoleText(getGopherMoveText(gopher, nextCoord), true);
            newSquare.setLawnSquareContent(LawnSquareContent.EMPTY_GOPHER_CHARGER);
            updateGopherPosition(gopher, nextCoord);
            updateOldSquare(oldSquare);
        }
        else if ((newSquare.getLawnSquareContent().equals(LawnSquareContent.GRASS_GOPHER)
                || (newSquare.getLawnSquareContent().equals(LawnSquareContent.EMPTY_GOPHER)
                || (newSquare.getLawnSquareContent().equals(LawnSquareContent.EMPTY_GOPHER_CHARGER))))) {
            // DO nothing.
            Gopher occupyingGopher = simulationDataModel.getGopherByCoordinates(nextCoord.getX(), nextCoord.getY());

            String text = "Gopher " + (gopher.getGopherNumber() + 1)
                    + " could not move to (" + nextCoord.getX() + "," + nextCoord.getY()
                    + ") because it is occupied by Gopher " +  (occupyingGopher.getGopherNumber() + 1);

            simulationDataModel.updateConsoleText(text, true);
        }
    }

    private void updateOldSquare(LawnSquare oldSquare) {
        if (oldSquare.getLawnSquareContent().equals(LawnSquareContent.GRASS_GOPHER)){
            oldSquare.setLawnSquareContent(LawnSquareContent.GRASS);
        }
        else if (oldSquare.getLawnSquareContent().equals(LawnSquareContent.EMPTY_GOPHER)){
            oldSquare.setLawnSquareContent(LawnSquareContent.EMPTY);
        }
        else if (oldSquare.getLawnSquareContent().equals(LawnSquareContent.EMPTY_GOPHER_CHARGER)){
            oldSquare.setLawnSquareContent(LawnSquareContent.EMPTY_CHARGER);
        }

    }

    private void updateGopherPosition(Gopher gopher, Coordinate nextCoord) {
        gopher.setXCoordinate(nextCoord.getX());
        gopher.setYCoordinate(nextCoord.getY());
    }

    public Coordinate determineMove(Gopher gopher) {
        List<Mower> mowers = simulationDataModel.getMowers();
        int shortestDistance = Integer.MAX_VALUE;
        Mower nearestMower = null;
        for (Mower mower: mowers){
            if (!mower.isDisabled()) {
                Coordinate mowerCoord = new Coordinate(mower.getCurrentXCoordinate(), mower.getCurrentYCoordinate());
                Coordinate gopherCoord = new Coordinate(gopher.getXCoordinate(), gopher.getYCoordinate());
                int distance = calculateDistance(mowerCoord, gopherCoord);
                if (distance < shortestDistance) {
                    shortestDistance = distance;
                    nearestMower = mower;
                }
            }
        }
        return createGopherMove(nearestMower, gopher);
    }

    private Coordinate createGopherMove(Mower nearestMower, Gopher gopher) {
        Coordinate mowerCoord = new Coordinate(nearestMower.getCurrentXCoordinate(), nearestMower.getCurrentYCoordinate());
        Coordinate gopherCoord = new Coordinate(gopher.getXCoordinate(), gopher.getYCoordinate());

        if (mowerCoord.getX() > gopherCoord.getX() && mowerCoord.getY() >gopherCoord.getY()){
            //NORTHEAST
            return new Coordinate(gopher.getXCoordinate()+1, gopher.getYCoordinate()+1);

        }
        else if (mowerCoord.getX() < gopherCoord.getX() && mowerCoord.getY() >gopherCoord.getY()) {
            //NORTHWEST
            return new Coordinate(gopher.getXCoordinate()-1, gopher.getYCoordinate()+1);
        }
        else if(mowerCoord.getX() < gopherCoord.getX() && mowerCoord.getY() < gopherCoord.getY()){
            //SOUTHWEST
            return new Coordinate(gopher.getXCoordinate()-1, gopher.getYCoordinate()-1);
        }
        else if (mowerCoord.getX() > gopherCoord.getX() && mowerCoord.getY() < gopherCoord.getY()){
            //SOUTHEAST
            return new Coordinate(gopher.getXCoordinate()+1, gopher.getYCoordinate()-1);

        }
        else if (mowerCoord.getX() == gopherCoord.getX()){
            if (mowerCoord.getY() < gopherCoord.getY()){
                //SOUTH
                return new Coordinate(gopher.getXCoordinate(), gopher.getYCoordinate()-1);
            }
            else {
                //NORTH
                return new Coordinate(gopher.getXCoordinate(), gopher.getYCoordinate()+1);
            }
        }
        else {
            if (mowerCoord.getX() < gopherCoord.getX()){
                //WEST
                return new Coordinate(gopher.getXCoordinate()-1, gopher.getYCoordinate());
            }
            else {
                //EAST
                return new Coordinate(gopher.getXCoordinate()+1, gopher.getYCoordinate());
            }
        }
    }

    private int calculateDistance(Coordinate mowerCoord, Coordinate gopherCoord) {
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

    private String getGopherMoveText(final Gopher gopher, final Coordinate nextCoord)
    {
        String text = "Gopher "
                + (gopher.getGopherNumber() + 1) + " is moving from ("
                + gopher.getXCoordinate() + "," + gopher.getYCoordinate() + ") to ("
                + nextCoord.getX() + "," + nextCoord.getY() + ")";

        return text;
    }
}
