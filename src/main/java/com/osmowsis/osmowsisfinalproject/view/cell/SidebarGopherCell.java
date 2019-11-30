package com.osmowsis.osmowsisfinalproject.view.cell;

import com.osmowsis.osmowsisfinalproject.pojo.Gopher;
import com.osmowsis.osmowsisfinalproject.view.controller.SidebarController;
import com.osmowsis.osmowsisfinalproject.view.controller.SidebarGopherCellController;
import javafx.scene.control.ListCell;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * This class represents the actual cell for the gopher list view, this
 * class extends ListCell to build on the out of the box functionality for
 * List Cells in JavaFX, mainly this class specifies how the cell will be
 * updated when needed. For additional functionality look to the corresponding
 * controller
 *
 * Created on 11/28/2019
 */

@Component
@Scope("prototype")
public class SidebarGopherCell extends ListCell<Gopher>
{
    // FIELDS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private final SidebarController sidebarController;

    private SidebarGopherCellController sidebarGopherCellController;

    // CONSTRUCTORS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Autowired
    public SidebarGopherCell(final SidebarController sidebarController)
    {
        this.sidebarController = sidebarController;
    }

    // PROTECTED METHODS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void updateItem(Gopher item, boolean empty)
    {
        super.updateItem(item, empty);

        if(item != null && !empty)
        {
            if(sidebarGopherCellController == null
                    || sidebarGopherCellController.getGopher() != item)
            {
                sidebarGopherCellController = getSidebarGopherCellController();

                sidebarController.getGopherControllerMap().put(item, sidebarGopherCellController);
            }

            sidebarGopherCellController.setCellInfo(item);

            setGraphic(sidebarGopherCellController.getSidebarGopherCell());
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
     * @return - A new instance of the Sidebar Gopher Cell Controller
     */
    @Lookup
    public SidebarGopherCellController getSidebarGopherCellController(){return null;}
}
