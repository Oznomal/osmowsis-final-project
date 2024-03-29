package com.osmowsis.osmowsisfinalproject.view.controller;

import com.jfoenix.controls.JFXListView;
import com.osmowsis.osmowsisfinalproject.config.StageManager;
import com.osmowsis.osmowsisfinalproject.model.SimulationDataModel;
import com.osmowsis.osmowsisfinalproject.pojo.Mower;
import com.osmowsis.osmowsisfinalproject.constant.FXMLView;
import com.osmowsis.osmowsisfinalproject.view.cell.DataInputMowerCell;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the view for adding mower info on the data input flow
 *
 * Created on 11/26/2019
 */

@Controller
public class DataInputMowersController implements Initializable
{
    // FIELDS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private final StageManager stageManager;
    private final SimulationDataModel simulationDataModel;

    @FXML
    private JFXListView<Mower> dataInputMowersListView;

    // CONSTRUCTORS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Autowired
    public DataInputMowersController(@Lazy final StageManager stageManager,
                                     final SimulationDataModel simulationDataModel)
    {
        this.stageManager = stageManager;
        this.simulationDataModel = simulationDataModel;
    }

    // INIT METHODS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        // BINDS THE MOWER LIST VIEW TO THE ITEMS IN THE MODEL
        dataInputMowersListView.setItems(simulationDataModel.getMowers());
        dataInputMowersListView.setCellFactory(column -> getDataInputMowerCell());
    }


    // PUBLIC METHODS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Handles when a key is pressed and the back button is in focus
     */
    public void handleBackBtnKeyPress(KeyEvent ke)
    {
        if(ke.getCode() == KeyCode.ENTER)
        {
            handleBackBtnClick();
        }
    }

    /**
     * Handles when the back button is pressed
     */
    public void handleBackBtnClick()
    {
        stageManager.switchScene(FXMLView.DATA_INPUT_DIMENSIONS);
    }

    /**
     * Handles when a key is pressed and the add mower button is in focus
     */
    public void handleAddMowerKeyPress(KeyEvent ke)
    {
        if(ke.getCode() == KeyCode.ENTER)
        {
            handleAddMowerBtnClick();
        }
    }

    /**
     * Handles when the add mower button is clicked
     */
    public void handleAddMowerBtnClick()
    {
        // TODO: IMPL MODAL POP UP FOR ADDING MOWER INFO
    }

    /**
     * Handles when a key is pressed and the next button is in focus
     */
    public void handleNextBtnKeyPress(KeyEvent ke)
    {
        if(ke.getCode() == KeyCode.ENTER)
        {
            handleNextBtnClick();
        }
    }

    /**
     * Handles when the next button is clicked
     */
    public void handleNextBtnClick()
    {
        // TODO: PERFORM VALIDATION AND THEN MOVE ON TO THE NEXT PAGE
    }

    // SPRING LOOKUPS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Spring magic for handling prototype scoped tings : )
     *
     * @return - A new instance of the Data Input Mower Cell
     */
    @Lookup
    DataInputMowerCell getDataInputMowerCell(){ return null; }
}
