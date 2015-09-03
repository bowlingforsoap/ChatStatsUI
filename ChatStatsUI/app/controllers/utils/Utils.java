package controllers.utils;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import org.bson.Document;
import play.Play;
import play.mvc.Http.*;
import play.Logger;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Helper constants and methods for interpreting the results of db queries.<p/>
 * <p>
 * Created by Strelchenko Vadym on 29.05.15.
 */
public class Utils {
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
    private static List<String> keysToParse = new ArrayList<>();
    private static List<String> legendLabels = new ArrayList<>();
    private static List<String> legendAbbreviations = new ArrayList<>();
    /**
     * Keys - legend labels; values - abbreviations for the bottom docker and left aggregation menu
     */
    private static Map<String, String> legendLabelsAndAbbreviations = new HashMap<>();
    /**
     * Statistics db.
     */
    public static final String CHAT_STATS_DB_URI;
    public static final String CHAT_STATS_DB_URI_KEY = "--QB_MONGODB_CHAT_STATS_DB_URI";

    public static final String CONNECTIONS_METRIC = "connections";
    public static final String UNIQUE_CONNECTIONS_METRIC = "uniqueConnections";

    private static final String CAMEL_STYLE_SPLIT_REGEX = "(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])";

    public static final String CREATED_AT_KEY = "createdAt";
    public static final String APP_ID_KEY = "appId";

    //Set all the static constants.
    static {
        Properties props = new Properties();
        InputStream initProperties = null;
        String[] statisticsMetrics = null;
        int period = 5; // default value
        String chatStatsDbUri = null;
        String[] metricNameWords;

        keysToParse.add(CREATED_AT_KEY);
        legendLabels.add(CREATED_AT_KEY);
        metricNameWords = CREATED_AT_KEY.split(CAMEL_STYLE_SPLIT_REGEX);
        legendAbbreviations.add(abbreviationStringForMetric(metricNameWords, false));

        try {

            initProperties = new FileInputStream(CHAT_HOME + "/etc/init.properties");
            props.load(initProperties);

            //parse KEYS_TO_PARSE
            statisticsMetrics = props.getProperty("--QB_CHAT_STATISTICS_METRICS").split(",");
            //parse STATS_PERIOD_SEC
            period = Integer.valueOf(props.getProperty("stats/stats-archiv/stats-per-logger/timeout"));
            Logger.info("Statistics period set to : " + period);
            //parse --QB_MONGODB_CHAT_STATS_DB_URI
            chatStatsDbUri = props.getProperty(CHAT_STATS_DB_URI_KEY);
            Logger.info("Chat statistics db uri set to : " + chatStatsDbUri);

        } catch (IOException e) {
            Logger.error("IOException while initializing critical constants");
            e.printStackTrace(System.out);
            Logger.error("Application continues to run on defaults");
        } catch (NullPointerException e) {
            Logger.error("NullPointerException while initializing critical constants");
            e.printStackTrace(System.out);
            Logger.error("Application continues to run on defaults");
        } finally {
            if (initProperties != null) {
                try {
                    initProperties.close();
                } catch (IOException e1) {
                    Logger.error("IOException while closing the input stream");
                    e1.printStackTrace();
                }
            }
        }

        //prepare KEYS_TO_PARSE, LEGEND_LABELS and LEGEND_ABBREVIATIONS
        try {
            for (String metric : statisticsMetrics) {
                legendLabels.add(metric + "PerUnit");
                //separate metric by words
                metricNameWords = metric.split(CAMEL_STYLE_SPLIT_REGEX);
                legendAbbreviations.add(abbreviationStringForMetric(metricNameWords, true));
            }
            keysToParse.addAll(Arrays.asList(statisticsMetrics));
        } catch (NullPointerException e) {
            Logger.error("NullPointerException while parsing metrics");
            e.printStackTrace(System.out);
            Logger.error("Application continues to run on defaults");
        }

        //custom metrics
        keysToParse.add(CONNECTIONS_METRIC);
        legendLabels.add(CONNECTIONS_METRIC);
        metricNameWords = CONNECTIONS_METRIC.split(CAMEL_STYLE_SPLIT_REGEX);
        legendAbbreviations.add(abbreviationStringForMetric(metricNameWords, false));

        keysToParse.add(UNIQUE_CONNECTIONS_METRIC);
        legendLabels.add(UNIQUE_CONNECTIONS_METRIC);
        metricNameWords = UNIQUE_CONNECTIONS_METRIC.split(CAMEL_STYLE_SPLIT_REGEX);
        legendAbbreviations.add(abbreviationStringForMetric(metricNameWords, false));

        //set STATS_PERIOD_SEC
        STATS_PERIOD_SEC = period;

        //set QB_MONGODB_CHAT_STATS_DB_URI
        CHAT_STATS_DB_URI = chatStatsDbUri;
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
                long firstEntryDate = ((Date) tempEntry.get(keysToParse.get(0))).getTime();
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
            long lastEntryDate = ((Date) tempEntry.get(keysToParse.get(0))).getTime();
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
        List<Long> aggregationResults = new ArrayList<>(keysToParse.size() - 1);
        Iterator<Document> appStatsIterator = appStats.iterator();
        Document entry;
        long temp;

        //fill the results list 0s
        for (int i = 0; i < keysToParse.size() - 1; i++) {
            aggregationResults.add((long) 0);
        }
        //fill it with actual data
        while (appStatsIterator.hasNext()) {
            entry = appStatsIterator.next();

            for (int i = 1; i < keysToParse.size(); i++) {
                String key = keysToParse.get(i);
                Object metric = entry.get(key);

                if (metric != null) {
                    if (key.equals(CONNECTIONS_METRIC) || key.equals(UNIQUE_CONNECTIONS_METRIC)) {
                        temp = ((Number) metric).longValue();
                        if (temp > aggregationResults.get(i - 1)) {
                            aggregationResults.set(i - 1, temp);
                        }
                    } else {
                        aggregationResults.set(i - 1, aggregationResults.get(i - 1) + (long) Math.ceil(((double) metric * Utils.STATS_PERIOD_SEC)));
                    }
                }
            }
        }

        return aggregationResults;
    }

    private static void appendEntry(Document entry, int timezoneDiff, StringBuilder builder) {
        Object temp;

        //parse date
        builder.append(Utils.DATE_FORMAT.format(((Date) entry.get(keysToParse.get(0))).getTime() + timezoneDiff * 60 * 1000));
        builder.append(",");
        //parse everything else
        for (int i = 1; i < keysToParse.size() - 1; i++) {
            temp = entry.get(keysToParse.get(i));
            if (temp == null) {
                builder.append(0);
            } else {
                builder.append(entry.get(keysToParse.get(i)));
            }
            builder.append(",");
        }
        builder.append(entry.get(keysToParse.get(keysToParse.size() - 1)));
        builder.append("\n");
    }

    private static void appendZeroEntry(long entryDate, int timezoneDiff, StringBuilder builder) {
        builder.append(Utils.DATE_FORMAT.format(entryDate + timezoneDiff * 60 * 1000));
        builder.append(",");
        //parse everything else
        for (int i = 1; i < keysToParse.size() - 1; i++) {
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


    private static String abbreviationStringForMetric(String[] metricSeparateWords, boolean perUnit) {
        StringBuilder sb = new StringBuilder();

        for (String metricPart : metricSeparateWords) {
            sb.append(metricPart.charAt(0));
        }

        if (perUnit) {
            sb.append("PU");
        }

        return sb.toString().toUpperCase();
    }

    public static List<String> getKeysToParse() {
        return keysToParse;
    }

    public static List<String> getLegendLabels() {
        return legendLabels;
    }

    public static List<String> getLegendAbbreviations() {
        return legendAbbreviations;
    }
}
