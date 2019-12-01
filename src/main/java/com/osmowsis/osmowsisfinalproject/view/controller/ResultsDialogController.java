package com.osmowsis.osmowsisfinalproject.view.controller;

import com.jfoenix.controls.JFXDialog;
import com.osmowsis.osmowsisfinalproject.config.StageManager;
import com.osmowsis.osmowsisfinalproject.constant.FXMLView;
import com.osmowsis.osmowsisfinalproject.model.SimulationDataModel;
import javafx.fxml.FXML;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

/**
 * Controller for the results dialog window
 *
 * Created on 11/30/2019
 */

@Controller
public class ResultsDialogController
{
    // FIELDS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private final StageManager stageManager;
    private final SimulationDataModel simulationDataModel;

    @FXML
    @Getter
    private JFXDialog resultsDialog;

    // CONSTRUCTORS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Lazy
    @Autowired
    public ResultsDialogController(final StageManager stageManager, final SimulationDataModel simulationDataModel)
    {
        this.stageManager = stageManager;
        this.simulationDataModel = simulationDataModel;
    }

    // PUBLIC METHODS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Handles when the restart button is clicked
     */
    public void handleRestartBtnClick()
    {
        simulationDataModel.resetDataModel();

        stageManager.switchScene(FXMLView.WELCOME);
    }

    /**
     * Handles when the exit button is clicked
     */
    public void handleExitBtnClick()
    {
        simulationDataModel.resetDataModel();

        stageManager.closeMainStage();

        System.exit(0);
    }
}
