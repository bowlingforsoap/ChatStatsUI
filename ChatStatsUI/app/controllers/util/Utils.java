package controllers.util;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import org.bson.Document;
import play.Play;
import play.mvc.Http.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Helper methods for working with files and interpreting db queries results.<p/>
 * Contains
 * <p>
 * Created by Strelchenko Vadym on 29.05.15.
 */
public class Utils {
    public static final String DEFAULT_RESOURCE_FOLDER = "conf/resources/";
    public static final String DEFAULT_STATS_CSV_FILE = "dataset";
    /**
     * Format expected in dygraphs js.
     */
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    public static final Integer STATS_PERIOD_SEC; //default
    public static final String CHAT_HOME;
    static {
        CHAT_HOME = Play.application().configuration().getString("chat_home");
    }
    /**
     * Fields from db to work with. The first one will become the X-axis.
     */
    public static final String[] KEYS_TO_PARSE/*= { "created_at", "messagePerUnit", "presencePerUnit", "acceptVideoCallPerUnit", "connectionPerUnit", "uniqueConnections" }*/;
    /**
     * Statistics db.
     */
    public static final String CHAT_STATS_DB_URI;

    //Set all the static constants.
    static {
        ArrayList<String> keysToParse = new ArrayList<>();
        Properties props = new Properties();
        InputStream initProperties = null;
        String[] statisticsMetrics = null;
        int period = 5; // default value
        String chatStatsDbUri = null;

        keysToParse.add("created_at");
        try {
            initProperties = new FileInputStream(CHAT_HOME + "/etc/init.properties");
            props.load(initProperties);

            //parse KEYS_TO_PARSE
            statisticsMetrics = props.getProperty("--QB_CHAT_STATISTICS_METRICS").split(",");
            //parse STATS_PERIOD_SEC
            period = Integer.valueOf(props.getProperty("stats/stats-archiv/stats-per-logger/timeout"));
            //parse --QB_MONGODB_CHAT_STATS_DB_URI
            chatStatsDbUri = props.getProperty("--QB_MONGODB_CHAT_STATS_DB_URI");

        } catch (IOException e) {
            System.out.println("Utils.static initializer : IOException while initializing critical constants");
            e.printStackTrace(System.out);
            System.out.println("Application continues to run on defaults");
        } catch (NullPointerException e) {
            System.out.println("Utils.static initializer : NullPointerException while initializing critical constants");
            e.printStackTrace(System.out);
            System.out.println("Application continues to run on defaults");
        } finally {
            if (initProperties != null) {
                try {
                    initProperties.close();
                } catch (IOException e1) {
                    System.out.println("Utils.static initializer : IOException while closing the input stream");
                    e1.printStackTrace();
                }
            }
        }
        //set KEYS_TO_PARSE
        try {
            for (int i = 0; i < statisticsMetrics.length; i++) {
                statisticsMetrics[i] = statisticsMetrics[i] + "PerUnit";
            }
            keysToParse.addAll(Arrays.asList(statisticsMetrics));
        } catch (NullPointerException e) {
            System.out.println("Utils.static initializer : NullPointerException while parsing metrics");
            e.printStackTrace(System.out);
            System.out.println("Application continues to run on defaults");
        }
        keysToParse.add("connectionPerUnit");
        keysToParse.add("uniqueConnections");
        KEYS_TO_PARSE = keysToParse.toArray(new String[0]);

        //set STATS_PERIOD_SEC
        STATS_PERIOD_SEC = period;

        //set QB_MONGODB_CHAT_STATS_DB_URI
        CHAT_STATS_DB_URI = chatStatsDbUri;
    }

    /**
     * First letters from metric name in init.properties (plus "connectionPerUnit" and "uniqueConnections")
     */
    public static final String[] ABBR_METRICS = new String[KEYS_TO_PARSE.length - 1];
    static {
        for (int i = 1; i < KEYS_TO_PARSE.length; i++) {
            String[] separateMetricNameParts = KEYS_TO_PARSE[i].split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
            ABBR_METRICS[i - 1] = "";
            for (String metricNamePart : separateMetricNameParts) {
                ABBR_METRICS[i - 1] = ABBR_METRICS[i - 1] + metricNamePart.charAt(0);
            }
            ABBR_METRICS[i - 1] = ABBR_METRICS[i - 1].toUpperCase();
        }
    }

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

    /**
     * Retrieves id value from session. If not present -> creates a new one and stores it into the session
     *
     * @param session
     * @return
     */
    public static String getSessionId(Session session) {
        String sessionId = session.get("id");

        if (sessionId == null || sessionId.length() == 0) {
            sessionId = UUID.randomUUID().toString();
            session.put("id", sessionId);
        }
        return sessionId;
    }

    /**
     * Saves {@code appStats} statistics to file with respect to the {@code startingFrom} parameter and the current time.
     *
     * @param appStats
     * @param startingFrom
     * @param timezoneOffset
     * @param requestDate
     * @return
     */
    public static String statsToStringCsv(FindIterable<Document> appStats, long startingFrom, int timezoneOffset, long requestDate) {
        StringBuilder builder = new StringBuilder();
        int localTimezoneOffset = new Date().getTimezoneOffset();
        int timezoneDiff = Math.abs(localTimezoneOffset - timezoneOffset);

        if (localTimezoneOffset < timezoneOffset) {
            timezoneDiff *= -1;
        }

        //append an entry for the startingFrom date
        startingFrom = requestDate - startingFrom;
        Utils.appendZeroEntry(startingFrom, timezoneDiff, builder);

        //check appStats for entries
        Iterator<Document> appStatsIterator = appStats.iterator();
        Document tempEntry = null;
        if (appStatsIterator.hasNext()) {
            //get the first one
            tempEntry = appStatsIterator.next();

            if (tempEntry != null) {
                //handle the first entry
                long firstEntryDate = ((Date) tempEntry.get(KEYS_TO_PARSE[0])).getTime();
                if (startingFrom - Utils.STATS_PERIOD_SEC * 1000 < firstEntryDate) {
                    Utils.appendZeroEntry(firstEntryDate - Utils.STATS_PERIOD_SEC * 1000, timezoneDiff, builder);
                }
                Utils.appendEntry(tempEntry, timezoneDiff, builder);

                //handle all in the between
                while (appStatsIterator.hasNext()) {
                    tempEntry = appStatsIterator.next();
                    Utils.appendEntry(tempEntry, timezoneDiff, builder);
                }
            }
        }

        //handle the last entry
        if (tempEntry != null) {
            long lastEntryDate = ((Date) tempEntry.get(KEYS_TO_PARSE[0])).getTime();
            if (lastEntryDate + Utils.STATS_PERIOD_SEC * 1000 < requestDate) {
                Utils.appendZeroEntry(lastEntryDate + Utils.STATS_PERIOD_SEC * 1000, timezoneDiff, builder);
                //append an entry for the current date
                Utils.appendZeroEntry(requestDate, timezoneDiff, builder);
            }
        } else {
            Utils.appendZeroEntry(requestDate, timezoneDiff, builder);
        }

        return builder.toString();
    }

    /**
     * Aggregates statistics by the given key. Sums presences and messages and finds max for connections.
     *
     * @param appStats
     * @return
     */
    public static List<Long> aggregateResults(FindIterable<Document> appStats) {
        List<Long> aggregationResults = new ArrayList<>(ABBR_METRICS.length);
        Iterator<Document> appStatsIterator = appStats.iterator();
        Document entry;
        long temp;

        //fill the results list 0s
        for (int i = 0; i < ABBR_METRICS.length; i++) {
            aggregationResults.add((long) 0);
        }
        //fill it with actual data
        while (appStatsIterator.hasNext()) {
            entry = appStatsIterator.next();
            for (int i = 0; i < ABBR_METRICS.length; i++) {
                String key = KEYS_TO_PARSE[i + 1];
                Object metric = entry.get(key);
                if (metric != null) {
                    if (key.equals("connectionPerUnit") || key.equals("uniqueConnections")) {
                        temp = ((Number) metric).longValue();
                        if (temp > aggregationResults.get(i)) {
                            aggregationResults.set(i, temp);
                        }
                    } else {
                        aggregationResults.set(i, aggregationResults.get(i) + (long) Math.ceil(((double) metric * Utils.STATS_PERIOD_SEC)));
                    }
                }
            }
        }

        return aggregationResults;
    }

    private static void appendEntry(Document entry, int timezoneDiff, StringBuilder builder) {
        Object temp;

        //parse date
        builder.append(Utils.DATE_FORMAT.format(((Date) entry.get(KEYS_TO_PARSE[0])).getTime() + timezoneDiff * 60 * 1000));
        builder.append(",");
        //parse everything else
        for (int i = 1; i < KEYS_TO_PARSE.length - 1; i++) {
            temp = entry.get(KEYS_TO_PARSE[i]);
            if (temp == null) {
                builder.append(0);
            } else {
                builder.append(entry.get(KEYS_TO_PARSE[i]));
            }
            builder.append(",");
        }
        builder.append(entry.get(KEYS_TO_PARSE[KEYS_TO_PARSE.length - 1]));
        builder.append("\n");
    }

    private static void appendZeroEntry(long entryDate, int timezoneDiff, StringBuilder builder) {
        builder.append(Utils.DATE_FORMAT.format(entryDate + timezoneDiff * 60 * 1000));
        builder.append(",");
        //parse everything else
        for (int i = 1; i < KEYS_TO_PARSE.length - 1; i++) {
            builder.append(0);
            builder.append(",");
        }
        builder.append(0);
        builder.append("\n");
    }

    public static List<String> appsToList(AggregateIterable<Document> aggregateResult) {
        List<String> appsFromDB = new ArrayList<>();

        for (Document app : aggregateResult) {
            appsFromDB.add(app.getString("_id"));
        }
        return appsFromDB;
    }

}
