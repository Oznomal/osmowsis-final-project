package com.osmowsis.osmowsisfinalproject.view.controller;

import com.jfoenix.controls.JFXDialog;
import com.osmowsis.osmowsisfinalproject.config.StageManager;
import com.osmowsis.osmowsisfinalproject.constant.FXMLView;
import com.osmowsis.osmowsisfinalproject.model.SimulationDataModel;
import com.osmowsis.osmowsisfinalproject.service.FileService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the results dialog window
 *
 * Created on 11/30/2019
 */

@Controller
public class ResultsDialogController implements Initializable
{
    // FIELDS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private final StageManager stageManager;
    private final SimulationDataModel simulationDataModel;
    private final FileService fileService;

    @FXML
    @Getter
    private JFXDialog resultsDialog;

    @FXML
    private Label totalGrassCutLabel;

    @FXML
    private Label startingGrassLabel;

    @FXML
    private Label completionPercentageLabel;

    @FXML
    private Label xDimensionLabel;

    @FXML
    private Label yDimensionLabel;

    @FXML
    private Label lawnAreaLabel;

    @FXML
    private Label gopherPeriodLabel;

    @FXML
    private Label gopherCountLabel;

    @FXML
    private Label totalTurnsTakenLabel;

    @FXML
    private Label maxTurnsLabel;

    @FXML
    private Label activeMowersLabel;

    @FXML
    private Label totalMowersLabel;

    // CONSTRUCTORS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Lazy
    @Autowired
    public ResultsDialogController(final StageManager stageManager, final SimulationDataModel simulationDataModel,
                                   final FileService fileService)
    {
        this.stageManager = stageManager;
        this.simulationDataModel = simulationDataModel;
        this.fileService = fileService;
    }

    // INIT METHODS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        // BINDING THE PROPERTIES TO THE DATA MODEL
        totalGrassCutLabel.textProperty().bind(simulationDataModel.getTotalGrassCut().asString());
        startingGrassLabel.textProperty().bind(simulationDataModel.getStartingGrassToCut().asString());
        xDimensionLabel.textProperty().bind(simulationDataModel.getLawnXDimension().asString());
        yDimensionLabel.textProperty().bind(simulationDataModel.getLawnYDimension().asString());
        lawnAreaLabel.textProperty().bind(simulationDataModel.getLawnArea().asString());
        gopherPeriodLabel.textProperty().bind(simulationDataModel.getGopherPeriod().asString());
        totalTurnsTakenLabel.textProperty().bind(simulationDataModel.getCurrentTurn().asString());
        maxTurnsLabel.textProperty().bind(simulationDataModel.getMaxTurns().asString());
        activeMowersLabel.textProperty().bind(simulationDataModel.getActiveMowerCount().asString());
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

    public void updateResults()
    {
        gopherCountLabel.setText(Integer.toString(simulationDataModel.getGophers().size()));
        totalMowersLabel.setText(Integer.toString(simulationDataModel.getMowers().size()));
        completionPercentageLabel.setText(calculateCompletionPercentage());
    }

    // PRIVATE METHODS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Calculates the completion percentage and returns it as a string
     *
     * @return the completion percentage as a string
     */
    private String calculateCompletionPercentage()
    {
        final int totalCut = simulationDataModel.getTotalGrassCut().get();
        final int starting = simulationDataModel.getStartingGrassToCut().get();

        final int percentage =  (int) Math.floor(totalCut / (double) starting * 100);

        return percentage + "%";
    }
}
