package com.osmowsis.osmowsisfinalproject.service;

import com.osmowsis.osmowsisfinalproject.model.SimulationDataModel;
import com.osmowsis.osmowsisfinalproject.pojo.Gopher;
import com.osmowsis.osmowsisfinalproject.pojo.MoveableLawnItem;
import com.osmowsis.osmowsisfinalproject.pojo.Mower;
import com.osmowsis.osmowsisfinalproject.view.controller.AppContainerController;
import com.osmowsis.osmowsisfinalproject.view.controller.LawnGridController;
import com.osmowsis.osmowsisfinalproject.view.controller.ResultsDialogController;
import com.osmowsis.osmowsisfinalproject.view.controller.SidebarController;
import javafx.application.Platform;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class SimulationService {

    // FIELDS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private final SimulationDataModel simulationDataModel;
    private final SimulationRiskProfileService simulationRiskProfileService;
    private final GopherService gopherService;
    private final MowerService mowerService;
    private final LawnGridController lawnGridController;
    private final SidebarController sidebarController;
    private final ResultsDialogController resultsDialogController;
    private final AppContainerController appContainerController;

    @Getter
    @Setter
    private MoveableLawnItem nextMove;

    // CONSTRUCTOR
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Autowired
    @Lazy
    public SimulationService(final SimulationDataModel simulationDataModel,
                             final SimulationRiskProfileService simulationRiskProfileService,
                             final GopherService gopherService,
                             final MowerService mowerService,
                             final LawnGridController lawnGridController,
                             final SidebarController sidebarController,
                             final ResultsDialogController resultsDialogController,
                             final AppContainerController appContainerController)
    {
        this.simulationDataModel = simulationDataModel;
        this.simulationRiskProfileService = simulationRiskProfileService;
        this.gopherService = gopherService;
        this.mowerService = mowerService;
        this.lawnGridController = lawnGridController;
        this.sidebarController = sidebarController;
        this.resultsDialogController = resultsDialogController;
        this.appContainerController = appContainerController;
    }

    // PUBLIC METHODS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Takes the next turn in the simulation
     *
     * @return - The return is only used for the run full simulation, so that we can print the results from this method
     *           but still get the run full simulation to break the loop .... because the take next step also could be
     *           run by clicking next through the whole simulation, so there is no guarantee that runFullSimulation
     *           will ever get called, but we can always guarantee that takeNextStep will get called
     */
    public boolean takeNextMove()
    {
        int lastMowerMovedNumber;
        int lastGopherMovedNumber;

        if(nextMove instanceof  Mower)
        {
            Mower mower = (Mower) nextMove;

            lastMowerMovedNumber = mower.getMowerNumber();

            mowerService.makeMove(mower);

            if(!simulationDataModel.getMowerQueue().isEmpty()
                    && simulationDataModel.getMowerQueue().peekFirst().getMowerNumber() <= lastMowerMovedNumber)
            {
                simulationDataModel.incrementCurrentTurn();

                if(simulationDataModel.getCurrentTurn().get() % simulationDataModel.getGopherPeriod().get() == 0)
                {
                    log.info("[GOPHER PERIOD ALERT] :: GOPHERS WILL BEGIN MOVING NEXT");

                    nextMove = simulationDataModel.getNextGopher();
                }
                else{
                    nextMove = simulationDataModel.getNextMower();
                }
            }
            else{
                nextMove = simulationDataModel.getNextMower();
            }

        }
        else if(nextMove instanceof Gopher)
        {
            Gopher gopher = (Gopher) nextMove;

            lastGopherMovedNumber = gopher.getGopherNumber();

            gopherService.moveGopher(gopher);

            if(simulationDataModel.getGopherQueue().peekFirst().getGopherNumber() < lastGopherMovedNumber)
            {
                simulationDataModel.incrementCurrentTurn();

                log.info("[MOWER PERIOD ALERT] :: GOPHER PERIOD OVER, MOWERS START MOVING NEXT");

                nextMove = simulationDataModel.getNextMower();
            }
            else{
                nextMove = simulationDataModel.getNextGopher();
            }
        }

        simulationRiskProfileService.updateSimulationRiskProfile();


        Platform.runLater(() -> lawnGridController.updateLawnUI());

        Platform.runLater(() -> sidebarController.updateSidebarUI());

        if(isSimulationDone())
        {
            resultsDialogController.getResultsDialog().setDialogContainer(appContainerController.getAppContainer());
            resultsDialogController.updateResults();
            resultsDialogController.getResultsDialog().show();

            return true;
        }
        else{
            Platform.runLater(() -> sidebarController.updateCurrentMoveTitle(nextMove));
        }

        return false;
    }

    /**
     * Runs the full simulation from where ever it is currently at
     */
    public void runFullSimulation()
    {
        log.info("[SIM SERVICE] :: runFullSimulation - Running Full Simulation");

        while(!isSimulationDone())
        {
            if(takeNextMove())
            {
                break;
            }
        }
    }

    /**
     * Checks to see if the simulation is done based on the conditions
     *
     * @return - True if the simulation is done, false otherwise
     */
    private boolean isSimulationDone()
    {
        if(simulationDataModel.getCurrentTurn().get() < simulationDataModel.getMaxTurns().get()
                && simulationDataModel.getRemainingGrassToCut().get() > 0
                && simulationDataModel.getActiveMowerCount().get() > 0)
        {
            return false;
        }

        return true;
    }
}
