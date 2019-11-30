package com.osmowsis.osmowsisfinalproject.view.controller;

import com.osmowsis.osmowsisfinalproject.constant.CSS;
import com.osmowsis.osmowsisfinalproject.constant.FXMLView;
import com.osmowsis.osmowsisfinalproject.model.SimulationDataModel;
import com.osmowsis.osmowsisfinalproject.pojo.Mower;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.io.IOException;


/**
 * This is the controller for the SideBarMowerCell, each cell (Mower) gets its own controller
 *
 * Note:
 * All of these fields are needed, even though IntelliJ will mark them as unused, this is because
 * we are dynamically creating the cells and instances of this controller as they get added to the model
 * so when you are looking in the IDE, it does not know to link the FXML annotated fields to the FXML file
 * and the fields annotated with the lombok getter annotation are called through the getter in the actual cell
 *
 * Created on 11/28/2019
 */

@Controller
@Scope("prototype")
public class SidebarMowerCellController
{
    // FIELDS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private final SimulationDataModel simulationDataModel;

    @FXML
    @Getter
    private HBox sidebarMowerCell;

    @FXML
    @Getter
    private HBox dataInputMowerCell;

    @FXML
    private FontAwesomeIconView powerIcon;

    @FXML
    private Label mowerName;

    @FXML
    private FontAwesomeIconView smartIcon;

    @FXML
    private FontAwesomeIconView batteryIcon;

    @FXML
    private Label batteryEnergyLabel;

    @FXML
    private Label directionAbbreviation;

    @FXML
    private FontAwesomeIconView directionIcon;

    @FXML
    private Label mowerPositionLabel;

    @Getter
    private Mower mower;

    // CONSTRUCTORS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Lazy
    @Autowired
    public SidebarMowerCellController(final SimulationDataModel simulationDataModel)
    {
        this.simulationDataModel = simulationDataModel;

        // PULL THE CELL FROM THE FXML FILE AND LOAD IT HERE, THIS IS HOW TO ACHIEVE DYNAMICALLY LOADING FXML FILES
        FXMLLoader loader = new FXMLLoader(getClass().getResource(FXMLView.SIDEBAR_MOWER_CELL.getFxmlFile()));
        loader.setController(this);

        try
        {
            loader.load();
        }
        catch(IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    // PUBLIC METHODS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Sets the cell info based on the mower info
     *
     * @param mower - The mower that the cell info should correlate to
     */
    public void setCellInfo(final Mower mower)
    {
        this.mower = mower;
        updatePowerIcon(mower);
        updateMowerName(mower);
        updateSmartIcon(mower);
        updateBatteryIcon(mower);
        updateBatteryEnergyLabel(mower);
        updateDirectionInfo(mower);
        updatePositionInfo(mower);
    }

    // PRIVATE METHODS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Updates the power icon to be active (green) or disabled (red) based on the status of the mower
     *
     * @param mower - The mower
     */
    private void updatePowerIcon(final Mower mower)
    {
        if(mower.isDisabled()
                && !powerIcon.getStyleClass().contains(CSS.POWER_ICON_DISABLED_CLASS))
        {
            powerIcon.getStyleClass().remove(CSS.POWER_ICON_ACTIVE_CLASS);
            powerIcon.getStyleClass().add(CSS.POWER_ICON_DISABLED_CLASS);
        }
        else if(!mower.isDisabled()
                && !powerIcon.getStyleClass().contains(CSS.POWER_ICON_ACTIVE_CLASS))
        {
            powerIcon.getStyleClass().remove(CSS.POWER_ICON_DISABLED_CLASS);
            powerIcon.getStyleClass().add(CSS.POWER_ICON_ACTIVE_CLASS);
        }
    }

    /**
     * Updates the name of the mower for the label
     *
     * @param mower - The mower
     */
    private void updateMowerName(final Mower mower)
    {
        mowerName.setText(CSS.MOWER_NAME_PREFIX + " " + (mower.getMowerNumber() + 1));
    }

    /**
     * Updates the smart icon
     */
    private void updateSmartIcon(final Mower mower)
    {
        if(mower.isStrategic() && !smartIcon.getStyleClass().contains(CSS.SMART_ICON_ACTIVE))
        {
            smartIcon.getStyleClass().remove(CSS.SMART_ICON_DISABLED);
            smartIcon.getStyleClass().add(CSS.SMART_ICON_ACTIVE);
        }
        else if(!mower.isStrategic() && !smartIcon.getStyleClass().contains(CSS.SMART_ICON_DISABLED))
        {
            smartIcon.getStyleClass().remove(CSS.SMART_ICON_ACTIVE);
            smartIcon.getStyleClass().add(CSS.SMART_ICON_DISABLED);
        }
    }

    /**
     * Updates the battery icon based on the remaining energy vs the starting energy
     *
     * @param mower - The mower
     */
    private void updateBatteryIcon(final Mower mower)
    {
        double percentRemaining = convertRemainingEnergyToPercent(mower.getCurrentEnergy());

        if(percentRemaining > CSS.BATTERY_FULL_THRESHOLD)
        {
            batteryIcon.setGlyphName(CSS.FULL_BATTERY_ICON_NAME);
        }
        else if(percentRemaining > CSS.BATTERY_THREE_QUARTERS_THRESHOLD)
        {
            batteryIcon.setGlyphName(CSS.THREE_QUARTERS_BATTERY_ICON_NAME);
        }
        else if(percentRemaining > CSS.BATTERY_HALF_THRESHOLD)
        {
            batteryIcon.setGlyphName(CSS.HALF_BATTERY_ICON_NAME);
        }
        else if(percentRemaining > CSS.BATTERY_QUARTER_THRESHOLD)
        {
            batteryIcon.setGlyphName(CSS.QUARTER_BATTERY_ICON_NAME);
        }
        else{
            batteryIcon.setGlyphName(CSS.EMPTY_BATTERY_ICON_NAME);
        }
    }

    /**
     * Converts the mowers remaining energy to a percent based on the starting value
     *
     * @param remainingEnergy - The mowers current remaining energy
     *
     * @return - The remaining energy converted to a percent
     */
    private double convertRemainingEnergyToPercent(int remainingEnergy)
    {
        return Math.floor(remainingEnergy / (double) simulationDataModel.getStartingMowerEnergy() * 100);
    }

    /**
     * Updates the remaining battery energy based on the mower
     *
     * @param mower - The mower
     */
    private void updateBatteryEnergyLabel(final Mower mower)
    {
        batteryEnergyLabel.setText(Integer.toString(mower.getCurrentEnergy()));
    }

    /**
     * Updates the direction abbreviation and arrow based on the mower
     *
     * @param mower - The mower
     */
    private void updateDirectionInfo(final Mower mower)
    {
        directionAbbreviation.setText(mower.getCurrentDirection().getAbbreviation());
        directionIcon.setRotate(mower.getCurrentDirection().getIconAngle());
    }

    /**
     * Updates the mowers position info
     *
     * @param mower - The mower to update
     */
    private void updatePositionInfo(final Mower mower)
    {
        final int x = mower.getCurrentXCoordinate();
        final int y = mower.getCurrentYCoordinate();

        if(x == Integer.MIN_VALUE && y == Integer.MIN_VALUE)
        {
            mowerPositionLabel.setText("N/A");
        }
        else{
            mowerPositionLabel.setText(mower.getCurrentXCoordinate() + ", " + mower.getCurrentYCoordinate());
        }
    }
}
