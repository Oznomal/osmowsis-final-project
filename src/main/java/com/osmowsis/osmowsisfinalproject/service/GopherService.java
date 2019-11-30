package com.osmowsis.osmowsisfinalproject.service;

import com.osmowsis.osmowsisfinalproject.model.SimulationDataModel;

import com.osmowsis.osmowsisfinalproject.pojo.Gopher;
import com.osmowsis.osmowsisfinalproject.pojo.Mower;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class GopherService
{
    // FIELDS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private final SimulationDataModel simulationDataModel;
    private final LawnService lawnService;
    private final MowerService mowerService;

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
    /**
     * Gets a gopher based on the lawn coordinates throws an exception if the gopher is not located
     *
     * @param x - The x coordinate
     * @param y - The y coordinate
     *
     * @return - The gopher that is located at the coordinates
     */
    public Gopher getGopherByCoordinates(final int x, final int y)
    {
        for(Gopher gopher : simulationDataModel.getGophers())
        {
            if(gopher.getXCoordinate() == x && gopher.getYCoordinate() == y)
            {
                return gopher;
            }
        }

        throw new RuntimeException("[INVALID GOPHER] :: getGopherByCoordinates - No gopher at coordinates");
    }

//    public void updateSimStateGopher(Move move) {
//        LawnSquare newSquare = lawnService.getLawnSquareByCoordinates(move.getNewXCoordinate(), move.getNewYCoordinate());
//
//        LawnSquare oldSquare =
//                lawnService.getLawnSquareByCoordinates(move.getCurrentXCoordinate(), move.getCurrentYCoordinate());
//
//        if (oldSquare.getLawnSquareContent().equals(LawnSquareContent.GOPHERGRASS)){
//            oldSquare.setLawnSquareContent(LawnSquareContent.GRASS);
//        }
//        else {
//            oldSquare.setLawnSquareContent(LawnSquareContent.EMPTY);
//        }
//
//        if (newSquare.getLawnSquareContent().equals(LawnSquareContent.MOWER)){
//            mowerService.removeMower(move);
//            newSquare.setLawnSquareContent(LawnSquareContent.GOPHER);
//        }
//        else if (newSquare.getLawnSquareContent().equals(LawnSquareContent.GRASS)){
//            newSquare.setLawnSquareContent(LawnSquareContent.GOPHERGRASS);
//        }
//
//    }
//
//    public Optional<Move> determineMove(Gopher gopher) {
//        for (Direction direction : Direction.values()) {
//            int newX = gopher.getxCoordinate() + direction.getxIncrement();
//            int newY = gopher.getyCoordinate() + direction.getyIncrement();
//            Move move = new Move(gopher.getName(), MowerMovementType.MOVE, direction,
//                    gopher.getxCoordinate(), gopher.getyCoordinate(), newX, newY);
//            if (lawnService.isValidMove(move)) {
//                if (lawnService.getLawnSquareByCoordinates(newX, newY).getLawnSquareContent().equals(LawnSquareContent.MOWER)) {
//                    gopher.move(direction);
//                    return Optional.of(move);
//                }
//            }
//        }
//        Direction randDirection = Direction.randomDirection();
//        int newX = gopher.getxCoordinate() + randDirection.getxIncrement();
//        int newY = gopher.getyCoordinate() + randDirection.getyIncrement();
//        Move move = new Move(gopher.getName(), MowerMovementType.MOVE, randDirection,
//                gopher.getxCoordinate(), gopher.getyCoordinate(), newX, newY);
//        if (lawnService.isValidMove(move)){
//            gopher.move(randDirection);
//            return Optional.of(move);
//        }
//        return Optional.empty();
//    }
//
//    public void moveGopher(){
//        Gopher gopher = gophers.get(currentGopherIndex);
//        determineMove(gopher)
//                .ifPresent(this::updateSimStateGopher);
//        currentGopherIndex++;
//        currentGopherIndex = currentGopherIndex&gophers.size();
//    }
//
//    public void moveAllGophers(){
//        this.currentGopherIndex = 0;
//        for(Gopher gopher: gophers){
//            moveGopher();
//        }
//    }
}
