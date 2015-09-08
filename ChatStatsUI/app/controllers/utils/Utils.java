package controllers.utils;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import org.bson.Document;
import play.Play;
import play.mvc.Http.*;
import play.Logger;
import scala.App;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Helper constants and methods for interpreting the results of db queries.<p/>
 * <p>
 * Created by Strelchenko Vadym on 29.05.15.
 */
public class Utils {
    public static final Integer STATS_PERIOD_SEC;
    public static final String CHAT_HOME;
    static {
        CHAT_HOME = Play.application().configuration().getString("chat_home");
    }

    /**
     * Keys in db object.
     */
    public static final String[] KEYS_TO_PARSE;

    /**
     * Names for dygraph labels.
     */
    private static Map<String, String> legendLabelsMap = new HashMap<>();
    /**
     * Methods to use during an aggregation for certain keys.
     */
    private static Map<String, String> aggrMethodsMap = new HashMap<>();
    /**
     * Abbreviations for the bottom docker.
     */
    private static Map<String, String> abbreviationsMap = new HashMap<>();

    /**
     * Statistics db.
     */
    public static final String CHAT_STATS_DB_URI;
    public static final String CHAT_STATS_DB_URI_KEY = "--QB_MONGODB_CHAT_STATS_DB_URI";

    //custom metrics (not in init.properties)
    public static final String CONNECTIONS_METRIC = "connections";
    public static final String UNIQUE_CONNECTIONS_METRIC = "uniqueConnections";

    /**
     * Regex to split camel style words.
     */
    private static final String CAMEL_STYLE_SPLIT_REGEX = "(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])";

    //Keys in db object
    public static final String CREATED_AT_KEY = "createdAt";
    public static final String APP_ID_KEY = "appId";

    //Set all the static constants.
    static {
        new HashMap<String, String>().keySet();
        Properties props = new Properties();
        InputStream initProperties = null;
        String[] statisticsMetrics = null;
        int period = 5; // default value
        String chatStatsDbUri = null;
        String[] metricNameWords;
        List<String> keysToParseList = new ArrayList<>();

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

        //prepare KEYS_TO_PARSE
        try {
            for (String metric : statisticsMetrics) {
                keysToParseList.add(metric);
                legendLabelsMap.put(metric, metric + "PerUnit");
                aggrMethodsMap.put(metric, "sum");

                metricNameWords = metric.split(CAMEL_STYLE_SPLIT_REGEX);
                abbreviationsMap.put(metric, abbreviationStringForMetric(metricNameWords, true));
            }

        } catch (NullPointerException e) {
            Logger.error("NullPointerException while parsing metrics");
            e.printStackTrace(System.out);
            Logger.error("Application continues to run on defaults");
        }

        //custom metrics
        //
        keysToParseList.add(CONNECTIONS_METRIC);
        legendLabelsMap.put(CONNECTIONS_METRIC, CONNECTIONS_METRIC);
        aggrMethodsMap.put(CONNECTIONS_METRIC, "max");
        metricNameWords = CONNECTIONS_METRIC.split(CAMEL_STYLE_SPLIT_REGEX);
        abbreviationsMap.put(CONNECTIONS_METRIC, abbreviationStringForMetric(metricNameWords, false));

        keysToParseList.add(UNIQUE_CONNECTIONS_METRIC);
        legendLabelsMap.put(UNIQUE_CONNECTIONS_METRIC, UNIQUE_CONNECTIONS_METRIC);
        aggrMethodsMap.put(UNIQUE_CONNECTIONS_METRIC, "max");
        metricNameWords = UNIQUE_CONNECTIONS_METRIC.split(CAMEL_STYLE_SPLIT_REGEX);
        abbreviationsMap.put(UNIQUE_CONNECTIONS_METRIC, abbreviationStringForMetric(metricNameWords, false));

        //set STATS_PERIOD_SEC
        STATS_PERIOD_SEC = period;

        //set QB_MONGODB_CHAT_STATS_DB_URI
        CHAT_STATS_DB_URI = chatStatsDbUri;

        //set KEYS_TO_PARSE
        KEYS_TO_PARSE = keysToParseList.toArray(new String[keysToParseList.size()]);
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

    public static Map<String, Object> aggregateResults(AggregateIterable<Document> aggrStats) {
        Map<String, Object> result = null;

        try {
            result = aggrStats.first();
        } finally {
            if (result == null) {
                result = new HashMap<>();
            }
        }

        return result;
    }

    public static List<String> appsToList(AggregateIterable<Document> aggregateResult) {
        List<String> appsFromDB = new ArrayList<>();

        for (Document app : aggregateResult) {
            appsFromDB.add(app.getString("_id"));
        }
        return appsFromDB;
    }

    /**
     * Returns appropriate abbreviations (aka clamped first letters) for each object in {@code metricSeparateWords}.
     *
     * @param metricSeparateWords
     * @param perUnit
     * @return
     */
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

    public static Map<String, String> getLegendLabelsMap() {
        return legendLabelsMap;
    }

    public static Map<String, String> getAggrMethodsMap() {
        return aggrMethodsMap;
    }

    public static Map<String, String> getAbbreviationsMap() {
        return abbreviationsMap;
    }
}
