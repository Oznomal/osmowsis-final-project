package com.osmowsis.osmowsisfinalproject.view.controller;

import com.jfoenix.controls.JFXListView;
import com.osmowsis.osmowsisfinalproject.config.StageManager;
import com.osmowsis.osmowsisfinalproject.constant.CSS;
import com.osmowsis.osmowsisfinalproject.constant.FXMLView;
import com.osmowsis.osmowsisfinalproject.model.SimulationDataModel;
import com.osmowsis.osmowsisfinalproject.pojo.Gopher;
import com.osmowsis.osmowsisfinalproject.pojo.Mower;
import com.osmowsis.osmowsisfinalproject.service.SimulationService;
import com.osmowsis.osmowsisfinalproject.view.cell.SidebarGopherCell;
import com.osmowsis.osmowsisfinalproject.view.cell.SidebarMowerCell;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Controller for the Sidebar component in the app container
 *
 * Created on 11/28/2019
 */

@Slf4j
@Controller
public class SidebarController implements Initializable
{
    // FIELDS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private final SimulationDataModel simulationDataModel;
    private final LawnGridController lawnGridController;
    private final StageManager stageManager;
    private final SimulationService simulationService;
    private final ResultsDialogController resultsDialogController;
    private final AppContainerController appContainerController;

    @FXML
    private FontAwesomeIconView simulationDetailsCollapsibleIcon;

    @FXML
    private FontAwesomeIconView mowerDetailsCollapsibleIcon;

    @FXML
    private FontAwesomeIconView gopherDetailsCollapsibleIcon;

    @FXML
    private VBox simulationDetailsArea;

    @FXML
    private Label simDetailsCurrentMowerLabel;

    @FXML
    private Label simDetailsActiveMowerCountLabel;

    @FXML
    private Label simDetailsCurrentTurnLabel;

    @FXML
    private Label simDetailsMaxTurnsLabel;

    @FXML
    private Label simDetailsTotalGrassCutLabel;

    @FXML
    private Label simDetailsStaringGrassLabel;

    @FXML
    private Label simDetailsGopherPeriodLabel;

    @FXML
    private HBox btnGroup1;

    @FXML
    private HBox btnGroup2;

    @FXML
    private HBox btnGroup3;

    @FXML
    private JFXListView<Mower> mowerListView;

    @FXML
    private JFXListView<Gopher> gopherListView;

    @FXML
    private TextArea consoleTextArea;

    @Getter
    private Map<Mower, SidebarMowerCellController> mowerControllerMap;

    @Getter
    private Map<Gopher, SidebarGopherCellController> gopherControllerMap;

    // CONSTRUCTORS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Autowired
    public SidebarController(final SimulationDataModel simulationDataModel,
                             final LawnGridController lawnGridController,
                             final SimulationService simulationService,
                             final ResultsDialogController resultsDialogController,
                             final AppContainerController appContainerController,
                             @Lazy final StageManager stageManager)
    {
        this.simulationDataModel = simulationDataModel;
        this.lawnGridController = lawnGridController;
        this.stageManager = stageManager;
        this.simulationService = simulationService;
        this.resultsDialogController = resultsDialogController;
        this.appContainerController = appContainerController;

        mowerControllerMap = new HashMap<>();
        gopherControllerMap = new HashMap<>();
    }

    // INIT METHODS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        // BINDS THE SIM DETAILS WITH THE MODEL BUT NOTE THAT WE WANT TO DEFAULT SOME OF THE VALUES,
        // SO THE VALUES WE WANT TO DEFAULT WON'T BE BOUND UNTIL THE START BUTTON IS CLICKED
        simDetailsCurrentTurnLabel.textProperty().bind(simulationDataModel.getCurrentTurn().asString());
        simDetailsMaxTurnsLabel.textProperty().bind(simulationDataModel.getMaxTurns().asString());
        simDetailsStaringGrassLabel.textProperty().bind(simulationDataModel.getStartingGrassToCut().asString());
        simDetailsGopherPeriodLabel.textProperty().bind(simulationDataModel.getGopherPeriod().asString());

        // BINDS THE MOWER LIST VIEW TO THE ITEMS IN THE MODEL
        mowerListView.setItems(simulationDataModel.getMowers());
        mowerListView.setCellFactory(column -> getSidebarMowerCell());

        gopherListView.setItems(simulationDataModel.getGophers());
        gopherListView.setCellFactory(column -> getSidebarGopherCell());

        // BINDS THE HEIGHT OF THE LIST VIEW TO THE HEIGHTS OF THE CELLS TO SUPPORT DYNAMIC RE-SIZING
        mowerListView.minHeightProperty().bind(Bindings.size(simulationDataModel.getMowers())
                .multiply(getSidebarMowerCell().getSidebarMowerCellController().getSidebarMowerCell().getPrefHeight()));
        mowerListView.prefHeightProperty().bind(mowerListView.minHeightProperty());

        gopherListView.minHeightProperty().bind(Bindings.size(simulationDataModel.getGophers())
                .multiply(getSidebarGopherCell().getSidebarGopherCellController()
                        .getSidebarGopherCell().getPrefHeight()));
        gopherListView.prefHeightProperty().bind(gopherListView.minHeightProperty());

        // BINDS THE MANAGED PROPERTY TO THE VISIBLE PROPERTY FOR DYNAMIC RE-SIZING
        simulationDetailsArea.managedProperty().bind(simulationDetailsArea.visibleProperty());

        mowerListView.managedProperty().bind(mowerListView.visibleProperty());

        gopherListView.managedProperty().bind(gopherListView.visibleProperty());


        consoleTextArea.setText("Click Start to begin the simulation!");
    }


    // PUBLIC METHODS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Handles when the simulation details section is clicked
     */
    public void handleSimulationDetailsBtnClick()
    {
        handleDetailsBtnClickInternal(simulationDetailsArea, simulationDetailsCollapsibleIcon);
    }

    /**
     * Expands or collapses the section when the mower details section is clicked
     */
    public void handleMowerDetailsBtnClick()
    {
        handleDetailsBtnClickInternal(mowerListView, mowerDetailsCollapsibleIcon);
    }

    /**
     * Expands or collapses the section when the gopher details section is clicked
     */
    public void handleGopherDetailsBtnClick()
    {
        handleDetailsBtnClickInternal(gopherListView, gopherDetailsCollapsibleIcon);
    }

    /**
     * Handles when the settings button is clicked
     */
    public void handleSimulationSettingsBtnClick()
    {
        log.info("The settings button was clicked");
    }

    /**
     * Handles when the start button is clicked
     */
    public void handleStartBtnClick()
    {
        // BIND SIM DETAILS THAT WERE DEFAULTED
        simDetailsActiveMowerCountLabel.textProperty().bind(simulationDataModel.getActiveMowerCount().asString());
        simDetailsTotalGrassCutLabel.textProperty().bind(simulationDataModel.getTotalGrassCut().asString());

        // CLEAR THE CONSOLE
        printConsoleMessage("", true);

        simulationDataModel.incrementCurrentTurn();

        lawnGridController.updateLawnUI();

        displayButtonGroup(btnGroup2);
    }

    /**
     * Handles when the next button is clicked
     */
    public void handleNextBtnClick()
    {
        simulationService.takeNextMove();
    }

    /**
     * Handles when the fast forward button is clicked
     */
    public void handleFastForwardBtnClick()
    {
        simulationService.runFullSimulation();

        // TODO: CHANGE THE VIEW TO THE FINAL VIEW HERE
    }

    /**
     * Handles when the stop button is clicked
     */
    public void handleStopBtnClick()
    {
        log.info("STOPPING THE SIMULATION");
        resultsDialogController.getResultsDialog().setDialogContainer(appContainerController.getAppContainer());
        resultsDialogController.getResultsDialog().show();

        // TODO: CHANGE THE VIEW HERE TO GET THE FINAL VIEW AND DISABLE STUFF IN THE SIDEBAR

        displayButtonGroup(btnGroup3);
    }

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

    /**
     * Updates the sidebar UI based on the information that is in the model
     */
    public void updateSidebarUI()
    {
        for(Map.Entry<Mower, SidebarMowerCellController> entry : mowerControllerMap.entrySet())
        {
            entry.getValue().setCellInfo(entry.getKey());
        }

        for(Map.Entry<Gopher, SidebarGopherCellController> entry : gopherControllerMap.entrySet())
        {
            entry.getValue().setCellInfo(entry.getKey());
        }
    }

    /**
     * Prints a message to the console
     *
     * @param textToPrint - The message to print to the console
     */
    public void printConsoleMessage(final String textToPrint, boolean deletePreviousMessage)
    {
        if(deletePreviousMessage)
        {
            consoleTextArea.setText(textToPrint.trim());
        }
        else{
            consoleTextArea.setText(consoleTextArea.getText() + "\n\n" + textToPrint);
        }
    }

    // PRIVATE METHODS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Collapses or expands a header and the corresponding list view
     *
     * @param detailsArea - The details area to expand or collapse
     * @param icon - The icon to change
     */
    private void handleDetailsBtnClickInternal(final Region detailsArea, final FontAwesomeIconView icon)
    {
        detailsArea.setVisible(!detailsArea.isVisible());

        if(detailsArea.isVisible())
        {
            icon.setGlyphName(CSS.EXPANDED_SECTION_ICON_NAME);
        }
        else{
            icon.setGlyphName(CSS.COLLAPSED_SECTION_ICON_NAME);
        }
    }

    /**
     * Displays a particular button group and ensures the others are hidden
     *
     * @param btnGroup - The button group to display
     */
    private void displayButtonGroup(final HBox btnGroup)
    {
        btnGroup1.setVisible(btnGroup1 == btnGroup);
        btnGroup2.setVisible(btnGroup2 == btnGroup);
        btnGroup3.setVisible(btnGroup3 == btnGroup);
    }

    // SPRING LOOKUPS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Spring magic for handling prototype scoped tings : )
     *
     * @return - A new instance of the Sidebar Mower Cell
     */
    @Lookup
    SidebarMowerCell getSidebarMowerCell(){ return null; }

    /**
     * Spring magic for handling prototype scoped tings : )
     *
     * @return - A new instance of the Sidebar Gopher Cell
     */
    @Lookup
    SidebarGopherCell getSidebarGopherCell() { return null; }
}
