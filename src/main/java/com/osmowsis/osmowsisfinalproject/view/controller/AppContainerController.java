package com.osmowsis.osmowsisfinalproject.view.controller;

import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import lombok.Getter;
import org.springframework.stereotype.Controller;

/**
 * Controller for the container for the main app view
 *
 * This container contains the following components
 *
 * 1. Sidebar
 * 2. Header
 * 3. Lawn Content Area
 *
 * Created on 11/28/2019
 */

@Controller
public class AppContainerController
{
    // FIELDS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @FXML
    @Getter
    private StackPane appContainer;
}
