package com.osmowsis.osmowsisfinalproject.view.controller;

import com.osmowsis.osmowsisfinalproject.constant.LawnSquareContent;
import com.osmowsis.osmowsisfinalproject.model.SimulationDataModel;
import com.osmowsis.osmowsisfinalproject.pojo.LawnSquare;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.util.*;

/**
 * Controller for the lawn grid view
 * <p>
 * Created on 11/28/2019
 */

@Slf4j
@Controller
public class LawnGridController implements Initializable {
    // FIELDS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private final SimulationDataModel simulationDataModel;

    @FXML
    private HBox gridPaneContainer;

    @FXML
    private GridPane lawnGridPane;

    private Map<LawnSquare, LawnGridCellController> cellMap;

    // CONSTRUCTOR
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Autowired
    public LawnGridController(final SimulationDataModel simulationDataModel) {
        this.simulationDataModel = simulationDataModel;
    }

    // INIT METHOD
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        buildLawnModel();
    }

    // PUBLIC METHODS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Updates the UI of the lawn based on the current information in the model
     */
    public void updateLawnUI()
    {
        for (LawnGridCellController controller : cellMap.values())
        {
            controller.updateLawnCellUI();
        }
    }

    // PRIVATE METHODS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Builds the lawn model based on the lawn squares in the
     */
    private void buildLawnModel()
    {
        final int prefGridCellWidth = (int) lawnGridPane.getPrefWidth() / simulationDataModel.getLawnXDimensionInt();
        final int prefGridCellHeight = (int) lawnGridPane.getPrefHeight() / simulationDataModel.getLawnYDimensionInt();

        log.info("[PREF HEIGHT] - {}", prefGridCellHeight);
        log.info("[PREF WIDTH] - {}", prefGridCellWidth);

        cellMap = new HashMap<>();

        // ADD THE COLUMN CONSTRAINTS
        for (int i = 0; i < simulationDataModel.getLawnXDimensionInt(); i++)
        {
            ColumnConstraints constraint = new ColumnConstraints();
            constraint.setPrefWidth(prefGridCellWidth);
            constraint.setMaxWidth(250);
            constraint.setHgrow(Priority.SOMETIMES);
            lawnGridPane.getColumnConstraints().add(constraint);
        }

        // ADD ROW CONSTRAINTS
        for (int i = 0; i < simulationDataModel.getLawnYDimensionInt(); i++)
        {
            RowConstraints constraint = new RowConstraints();
            constraint.setPrefHeight(prefGridCellHeight);
            constraint.setMaxHeight(250);
            constraint.setVgrow(Priority.SOMETIMES);
            lawnGridPane.getRowConstraints().add(constraint);
        }

        // ADD THE LAWN CONTENT TO THE GRID
        for (LawnSquare square : simulationDataModel.getLawnSquares())
        {
            int x = square.getXCoordinate();
            int y = square.getYCoordinate();

            final LawnGridCellController controller = getLawnGridCellController();
            controller.setLawnSquare(square);

            final HBox gridCell = controller.getLawnGridCellContainer();

            gridCell.setPrefHeight(prefGridCellHeight);
            gridCell.setPrefWidth(prefGridCellWidth);

            gridCell.getStyleClass().add(LawnSquareContent.GRASS.getCssClass());

            // OBJECT REFERENCE MEANS THAT THE SQUARE USED FOR THE KEY IS THE SAME OBJECT THAT IS STORED IN THE MODEL
            // SO ALTHOUGH THE UPDATE METHOD WILL NEED TO BE MANUALLY CALLED TO UPDATE THE LAWN GRID, WE CAN STILL
            // COUNT ON IT USING THE UP TO DATE SQUARES THAT ARE STORED IN THE MODEL AS LONG AS WE UPDATE BASED ON MAP
            cellMap.put(square, controller);

            lawnGridPane.add(gridCell, x, getInvertedYAxisValue(y));
        }
    }

    /**
     * Since JavaFX by default inverts the Y-Axis so that the origin (0) is at the top
     * instead of the bottom, we need convert the original value to the inverted value
     *
     * @param originalValue - The original value
     * @return - The inverted value
     */
    private int getInvertedYAxisValue(final int originalValue)
    {
        final int yMax = simulationDataModel.getLawnYDimensionInt() - 1;

        return yMax - originalValue;
    }

    // SPRING LOOKUPS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Lookup
    LawnGridCellController getLawnGridCellController()
    {
        return null;
    }
}
