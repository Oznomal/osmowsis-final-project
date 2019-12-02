package com.osmowsis.osmowsisfinalproject.service;

import com.osmowsis.osmowsisfinalproject.constant.Direction;
import com.osmowsis.osmowsisfinalproject.pojo.Gopher;
import com.osmowsis.osmowsisfinalproject.model.SimulationDataModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for parsing the initial file
 *
 * Created on 11/27/2019
 */

@Slf4j
@Service
public class FileService
{
    // FIELDS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private static final String DELIMITER = ",";
    private static final String RESULTS_FILE_SUFFIX = "_results.txt";

    // LAWN PROCESSING
    private static final int X_DIMENSION_LINE_NUMBER = 0;
    private static final int Y_DIMENSION_LINE_NUMBER = 1;

    // MOWER PROCESSING
    private static final int MOWER_COUNT_LINE_NUMBER = 2;
    private static final int FIRST_MOWER_LINE_NUMBER = 3;
    private static final int MOWER_X_POSITION_ARR_INDEX = 0;
    private static final int MOWER_Y_POSITION_ARR_INDEX = 1;
    private static final int MOWER_DIRECTION_ARR_INDEX = 2;
    private static final int MOWER_STRATEGIC_ARR_INDEX = 3;
    private static final int MOWER_STRATEGIC_INDICATOR = 1;

    // GOPHER PROCESSING
    private static final int GOPHER_X_POSITION_ARR_INDEX = 0;
    private static final int GOPHER_Y_POSITION_ARR_INDEX = 1;

    private final SimulationDataModel simulationDataModel;

    // FIELDS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Autowired
    public FileService(final SimulationDataModel simulationDataModel)
    {
        this.simulationDataModel = simulationDataModel;
    }

    // PUBLIC METHODS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Parses the starting file and then updates the data model
     *
     * @param file - The file to parse
     */
    public void parseFile(final File file) throws IOException
    {
        int currentIndex;

        final List<String> lines = convertFileToLineList(file);

        processLawnInfo(lines);

        currentIndex = processMowerInfo(lines);

        currentIndex = processGopherInfo(lines, currentIndex);

        processMaxTurnsInfo(lines, currentIndex);

        String filename = file.getName().trim().substring(0, file.getName().lastIndexOf('.'));

        simulationDataModel.setResultsFileName(filename + RESULTS_FILE_SUFFIX);
    }

    /**
     * Writes out the results file
     */
    public void writeResultsFile()
    {
        try{
            Files.write(Paths.get("./" + simulationDataModel.getResultsFileName()), simulationDataModel.getResultsSb().toString().getBytes());

            File resultsFile = new File(simulationDataModel.getResultsFileName());

            simulationDataModel.updateResultsFilePath(resultsFile.getAbsolutePath());
        }
        catch(IOException e)
        {
            log.error("Something went wrong with the file writing");
        }
    }

    // PRIVATE METHODS
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Converts the file into a list of lines
     *
     * @param file - The file to convert
     *
     * @return - A list of lines
     */
    private List<String> convertFileToLineList(final File file) throws IOException
    {
        final BufferedReader reader = new BufferedReader(new FileReader(file));

        final List<String> lines = new ArrayList<>();

        String line = reader.readLine();

        while(line != null)
        {
            if(!line.trim().isEmpty())
            {
                lines.add(line);
            }

            line = reader.readLine();
        }

        reader.close();

        return lines;
    }

    /**
     * Processes the lawn information (dimensions) from the file
     *
     * @param lines - The list of lines from the file
     */
    private void processLawnInfo(final List<String> lines)
    {
        final int x = Integer.parseInt(lines.get(X_DIMENSION_LINE_NUMBER).trim());
        final int y = Integer.parseInt(lines.get(Y_DIMENSION_LINE_NUMBER).trim());

        simulationDataModel.setInitialLawnDimensions(x, y);
    }

    /**
     * Processes the mower information from the file
     *
     * @param lines - The list of lines from the file
     *
     * @return - The current line number after all of the mower info has been processed
     */
    private int processMowerInfo(final List<String> lines)
    {
        // GET THE NUMBER OF ACTIVE MOWERS
        final int activeMowers = Integer.parseInt(lines.get(MOWER_COUNT_LINE_NUMBER));
        simulationDataModel.updateActiveMowerCount(activeMowers);

        // GET THE STARTING ENERGY FOR THE MOWERS
        final int startingEnergy = Integer.parseInt(lines.get(FIRST_MOWER_LINE_NUMBER + activeMowers));
        simulationDataModel.setInitialStartingMowerEnergy(startingEnergy);

        // ADD EACH INDIVIDUAL MOWER TO THE MODEL
        int idx = FIRST_MOWER_LINE_NUMBER;

        for(int i = 0; i < activeMowers; i++)
        {
            String [] mowerInfo = lines.get(idx++).trim().split(DELIMITER);

            for(Direction direction : Direction.values())
            {
                if(direction.name().equalsIgnoreCase(mowerInfo[MOWER_DIRECTION_ARR_INDEX]))
                {
                    int mowerX = Integer.parseInt(mowerInfo[MOWER_X_POSITION_ARR_INDEX].trim());
                    int mowerY = Integer.parseInt(mowerInfo[MOWER_Y_POSITION_ARR_INDEX].trim());

                    boolean strategic =
                            Integer.parseInt(mowerInfo[MOWER_STRATEGIC_ARR_INDEX].trim()) == MOWER_STRATEGIC_INDICATOR;

                    simulationDataModel.addNewMowerToModel(mowerX, mowerY, direction, strategic);
                }
            }
        }

        // INCREMENT 1 MORE TIME BECAUSE THE ENERGY HAS ALREADY BEEN PROCESSED
        return ++idx;
    }

    /**
     * Processes the gopher info from the file
     *
     * @param lines - The list of lines from the file
     * @param idx - The starting index of the gopher info
     *
     * @return - The current line number after all of the gopher info has been processed
     */
    private int processGopherInfo(final List<String> lines, int idx)
    {
        // GET THE GOPHER COUNT
        final int gopherCount = Integer.parseInt(lines.get(idx++).trim());
        simulationDataModel.setInitialGopherCount(gopherCount);

        // GET THE INFORMATION FOR EACH GOPHER
        for(int i = 0; i < gopherCount; i++)
        {
            String [] gopherInfo = lines.get(idx++).trim().split(DELIMITER);

            Gopher gopher = new Gopher();
            gopher.setXCoordinate(Integer.parseInt(gopherInfo[GOPHER_X_POSITION_ARR_INDEX].trim()));
            gopher.setYCoordinate(Integer.parseInt(gopherInfo[GOPHER_Y_POSITION_ARR_INDEX].trim()));

            simulationDataModel.addNewGopherToModel(gopher);
        }

        simulationDataModel.setInitialGopherPeriod(Integer.parseInt(lines.get(idx++).trim()));

        return idx;
    }

    /**
     * Processes the max turn info from the file
     *
     * @param lines - the list of lines from the file
     * @param currentIndex - The current index
     */
    private void processMaxTurnsInfo(final List<String> lines, final int currentIndex)
    {
        final int maxTurns = Integer.parseInt(lines.get(currentIndex).trim());
        simulationDataModel.setInitialMaxTurns(maxTurns);
    }
}
