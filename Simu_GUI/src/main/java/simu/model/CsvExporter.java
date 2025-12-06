package simu.model;

import java.io.FileWriter;
import java.io.IOException;

public class CsvExporter {

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


