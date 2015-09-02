package controllers.utils;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import play.Logger;
import play.Play;
import play.inject.ApplicationLifecycle;
import play.libs.F;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Helper methods to work with files.
 * <p>
 * Created by qb-user on 9/2/15.
 */
@Singleton
public class FileUtil {
    public static final String DEFAULT_RESOURCE_FOLDER = "conf/resources/";
    public static final String DEFAULT_STATS_CSV_FILE = "dataset";

    @Inject
    public FileUtil(ApplicationLifecycle lifecycle) {
        lifecycle.addStopHook(() -> {

            String[] resourceFold = new File(DEFAULT_RESOURCE_FOLDER).list();
            if (resourceFold != null) {
                Logger.info("Began resource clean-up");

                for (int i = 0; i < resourceFold.length; i++) {
                    try {
                        if (resourceFold[i] != null && resourceFold[i].length() != 0)
                            new File(DEFAULT_RESOURCE_FOLDER + resourceFold[i]).delete();
                    } catch (Exception e) {
                        Logger.error("Exception when deleting source file : " + e.toString());
                    }
                }

                Logger.info("Finished resource clean-up");
            }

            return F.Promise.pure(null);
        });
    }

    /**
     * Erases the content of the CSV file for the corresponding {@code sessionId}.
     *
     * @param sessionId
     * @throws IOException
     */
    public void eraseDatafileContent(String sessionId) throws IOException {
        new FileWriter(Play.application().getFile(DEFAULT_RESOURCE_FOLDER + DEFAULT_STATS_CSV_FILE + sessionId + ".csv")).close();
    }

    /**
     * Writes {@code data} to the file named: ({@link Utils}.DEFAULT_RESOURCE_FOLDER + {@link Utils}.DEFAULT_STATS_CSV_FILE + {@code sessionId} + ".csv").
     *
     * @param data
     * @param sessionId
     * @throws IOException
     */
    public void writeDataToFile(String data, String sessionId) throws IOException {
        FileWriter cswWriter = new FileWriter(Play.application().getFile(DEFAULT_RESOURCE_FOLDER + DEFAULT_STATS_CSV_FILE + sessionId + ".csv"));
        cswWriter.write(data);
        cswWriter.close();
    }


}
