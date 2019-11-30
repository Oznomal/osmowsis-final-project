package com.osmowsis.osmowsisfinalproject.view.cell;

import com.osmowsis.osmowsisfinalproject.pojo.Mower;
import com.osmowsis.osmowsisfinalproject.view.controller.SidebarController;
import com.osmowsis.osmowsisfinalproject.view.controller.SidebarMowerCellController;
import javafx.scene.control.ListCell;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * This class represents the actual cell for the mower list view, this
 * class extends ListCell to build on the out of the box functionality for
 * List Cells in JavaFX, mainly this class specifies how the cell will be
 * updated when needed. For additional functionality look to the corresponding
 * controller
 *
 * Created on 11/28/2019
 */

@Component
@Scope("prototype")
public class SidebarMowerCell extends ListCell<Mower>
{
    // FIELDS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private final SidebarController sidebarController;

    private SidebarMowerCellController sidebarMowerCellController;

    // FIELDS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Autowired
    public SidebarMowerCell(final SidebarController sidebarController)
    {
        this.sidebarController = sidebarController;
    }

    // PROTECTED METHODS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void updateItem(Mower item, boolean empty)
    {
        super.updateItem(item, empty);

        if(item != null && !empty)
        {
            if(sidebarMowerCellController == null
                    || sidebarMowerCellController.getMower() != item)
            {
                sidebarMowerCellController = getSidebarMowerCellController();

                sidebarController.getMowerControllerMap().put(item, sidebarMowerCellController);
            }

            sidebarMowerCellController.setCellInfo(item);

            setGraphic(sidebarMowerCellController.getSidebarMowerCell());
        }
        else{
            setGraphic(null);
        }
    }

    // SPRING LOOKUPS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Spring magic for handling prototype scoped tings : )
     *
     * @return - A new instance of the Sidebar Mower Cell Controller
     */
    @Lookup
    public SidebarMowerCellController getSidebarMowerCellController(){return null;}
}
