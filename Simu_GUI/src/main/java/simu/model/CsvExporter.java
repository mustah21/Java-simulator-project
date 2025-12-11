package simu.model;

import java.io.FileWriter;
import java.io.IOException;

/**
 * Utility class for exporting simulation statistics to CSV format.
 * 
 * @author Group 8
 * @version 1.0
 */
public class CsvExporter {

    /**
     * Exports simulation statistics to a CSV file.
     * 
     * @param stats The SimulationStatistics object containing the statistics to export
     * @param file The file path where the CSV will be written
     * @throws IOException if an I/O error occurs while writing the file
     */
    public static void export(SimulationStatistics stats, String file)
            throws IOException {

        try (FileWriter writer = new FileWriter(file)) {
            writer.write("Customers,Throughput,AvgWait,PeakQueue,Time\n");
            writer.write(
                    stats.getCustomersServed() + "," +
                            stats.throughput + "," +
                            stats.getAverageWait() + "," +
                            stats.peakQueueLength  );
        }
    }
}


