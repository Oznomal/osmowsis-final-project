package com.osmowsis.osmowsisfinalproject.view.controller;

import com.osmowsis.osmowsisfinalproject.config.StageManager;
import com.osmowsis.osmowsisfinalproject.model.SimulationDataModel;
import com.osmowsis.osmowsisfinalproject.service.FileService;
import com.osmowsis.osmowsisfinalproject.service.SimulationRiskProfileService;
import com.osmowsis.osmowsisfinalproject.constant.FXMLView;
import javafx.stage.FileChooser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.io.File;

/**
 * The controller that controls the welcome.fxml view
 *
 * Created on 11/25/2019
 */

@Slf4j
@Controller
public class WelcomeController
{
    // FIELDS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private final StageManager stageManager;
    private final FileService fileService;
    private final SimulationDataModel simulationDataModel;
    private final SimulationRiskProfileService simulationRiskProfileService;

    // CONSTRUCTORS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Autowired
    public WelcomeController(@Lazy final StageManager stageManager,
                             final FileService fileService,
                             final SimulationDataModel simulationDataModel,
                             final SimulationRiskProfileService simulationRiskProfileService)
    {
        this.stageManager = stageManager;
        this.fileService = fileService;
        this.simulationDataModel = simulationDataModel;
        this.simulationRiskProfileService = simulationRiskProfileService;
    }

    // PUBLIC METHODS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Handles when the import file button is clicked
     */
    public void handleImportFileBtnClick()
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));

        File selectedFile = fileChooser.showOpenDialog(stageManager.getPrimaryStage());

        if(selectedFile != null)
        {
            try {
                fileService.parseFile(selectedFile);

                simulationRiskProfileService.updateSimulationRiskProfile();

                stageManager.switchScene(FXMLView.MAIN_APP_CONTAINER);
            }
            catch (Exception e)
            {
                log.error("[PARSING ERROR] :: handleImportFileBtnClick - An unknown error occurred", e);

                // CLEAR OUT THE DATA MODEL IN CASE SOMETHING WAS SAVED
                simulationDataModel.resetDataModel();

                // TODO: DISPLAY THE ERROR MESSAGE ON THE UI
            }
        }
    }

    /**
     * Handles when the input data button is clicked
     */
    public void handleInputDataButtonClick()
    {
        stageManager.switchScene(FXMLView.DATA_INPUT_DIMENSIONS);
    }

    /**
     * Handles when the generate data button is clicked
     */
    public void handleGenerateDataButtonClick()
    {
        // TODO: CREATE A SERVICE TO GENERATE RANDOM DATA

        // TODO: GENERATE A RANDOM SCENARIO

        // TODO: SHOW THE THE VIEW FOR THE MAIN SIMULATION

        log.info("The generate random button data button was clicked");
    }
}
