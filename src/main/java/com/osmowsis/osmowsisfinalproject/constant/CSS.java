package com.osmowsis.osmowsisfinalproject.constant;

/**
 * CSS constants used throughout the application for changing styling dynamically
 *
 * Created 11/29/2019
 */
public final class CSS
{
    // FIELDS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // LAWN GRID CELLS
    public static final String GRASS_CLASS_NAME = "grass";
    public static final String EMPTY_CLASS_NAME = "empty";

    // DATA INPUT CELLS
    public static final String POWER_ICON_ACTIVE_CLASS = "active";
    public static final String POWER_ICON_DISABLED_CLASS = "disabled";

    public static final String FULL_BATTERY_ICON_NAME = "BATTERY_FULL";
    public static final String THREE_QUARTERS_BATTERY_ICON_NAME = "BATTERY_THREE_QUARTERS";
    public static final String HALF_BATTERY_ICON_NAME = "BATTERY_HALF";
    public static final String QUARTER_BATTERY_ICON_NAME = "BATTERY_QUARTER";
    public static final String EMPTY_BATTERY_ICON_NAME = "BATTERY_EMPTY";

    // MOWER
    public static final String MOWER_NAME_PREFIX = "Mower";

    public static final int BATTERY_FULL_THRESHOLD = 75;
    public static final int BATTERY_THREE_QUARTERS_THRESHOLD = 50;
    public static final int BATTERY_HALF_THRESHOLD = 25;
    public static final int BATTERY_QUARTER_THRESHOLD = 0;

    // SIDEBAR
    public static final String EXPANDED_SECTION_ICON_NAME = "ANGLE_DOWN";
    public static final String COLLAPSED_SECTION_ICON_NAME = "ANGLE_RIGHT";

    public static final String SMART_ICON_ACTIVE = "smart-active";
    public static final String SMART_ICON_DISABLED = "smart-disabled";
    public static final String ACTIVE_MOWER_LABEL = "active-label";
    public static final String DISABLED_MOWER_LABEL = "disabled-label";
    public static final String ACTIVE_COORDINATRE_CONTAINER = "coordinate-container";
    public static final String DISABLED_COORDINATE_CONTAINER = "coordinate-container-disabled";

    // CONSTRUCTOR
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private CSS()
    {
        throw new AssertionError();
    }
}
