package controllers.utils;

import play.Play;

import java.io.FileWriter;
import java.io.IOException;

/**
 * Helper methods to work with files.
 *
 * Created by qb-user on 9/2/15.
 */
public class FileUtil {
    /**
     * Erases the content of the CSV file for the corresponding {@code sessionId}.
     *
     * @param sessionId
     * @throws IOException
     */
    public static void eraseDatafileContent(String sessionId) throws IOException {
        new FileWriter(Play.application().getFile(Utils.DEFAULT_RESOURCE_FOLDER + Utils.DEFAULT_STATS_CSV_FILE + sessionId + ".csv")).close();
    }

    /**
     * Writes {@code data} to the file named: ({@link Utils}.DEFAULT_RESOURCE_FOLDER + {@link Utils}.DEFAULT_STATS_CSV_FILE + {@code sessionId} + ".csv").
     *
     * @param data
     * @param sessionId
     * @throws IOException
     */
    public static void writeDataToFile(String data, String sessionId) throws IOException {
        FileWriter cswWriter = new FileWriter(Play.application().getFile(Utils.DEFAULT_RESOURCE_FOLDER + Utils.DEFAULT_STATS_CSV_FILE + sessionId + ".csv"));
        cswWriter.write(data);
        cswWriter.close();
    }
}
