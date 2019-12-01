package com.osmowsis.osmowsisfinalproject.service;

import com.osmowsis.osmowsisfinalproject.model.SimulationDataModel;
import com.osmowsis.osmowsisfinalproject.pojo.Mower;
import com.osmowsis.osmowsisfinalproject.view.controller.AppContainerController;
import com.osmowsis.osmowsisfinalproject.view.controller.LawnGridController;
import com.osmowsis.osmowsisfinalproject.view.controller.ResultsDialogController;
import com.osmowsis.osmowsisfinalproject.view.controller.SidebarController;
import javafx.application.Platform;
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
        log.info("[SIM SERVICE] :: takeNextMove - Taking next move");

        final int turnsTaken = simulationDataModel.getCurrentTurn().get();
        final int gopherPeriod = simulationDataModel.getGopherPeriod().get();

        if(turnsTaken == 0 || mowerService.areAllMowerTurnsTaken())
        {
            simulationDataModel.incrementCurrentTurn();

            mowerService.resetTurnInfoForActiveMowers();
        }

        if (turnsTaken % gopherPeriod == 0)
        {
            // TODO: THIS CAN BE REMOVED AFTER DEBUGGING AND DETERMINING THAT EVERYTHING WORKS
            log.info("[GOPHER PERIOD ALERT] - The gophers are changing their position");

            gopherService.moveGopher();
        }
        else {
            Mower mower = simulationDataModel.getNextMower();

            mowerService.makeMove(mower);

            simulationRiskProfileService.updateSimulationRiskProfile();
        }

        Platform.runLater(() -> lawnGridController.updateLawnUI());

        Platform.runLater(() -> sidebarController.updateSidebarUI());

        if(isSimulationDone())
        {
            resultsDialogController.getResultsDialog().setDialogContainer(appContainerController.getAppContainer());
            resultsDialogController.updateResults();
            resultsDialogController.getResultsDialog().show();

            return true;
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
