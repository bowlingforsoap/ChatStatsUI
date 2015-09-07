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
    public static Map<String, String> aggregationMethodsForKey = new HashMap<>();
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
        new HashMap<String, String>().keySet();
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
                aggregationMethodsForKey.put(metric, "sum");
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
        //
        aggregationMethodsForKey.put(CONNECTIONS_METRIC, "max");
        keysToParse.add(CONNECTIONS_METRIC);
        legendLabels.add(CONNECTIONS_METRIC);
        metricNameWords = CONNECTIONS_METRIC.split(CAMEL_STYLE_SPLIT_REGEX);
        legendAbbreviations.add(abbreviationStringForMetric(metricNameWords, false));

        aggregationMethodsForKey.put(UNIQUE_CONNECTIONS_METRIC, "max");
        keysToParse.add(UNIQUE_CONNECTIONS_METRIC);
        legendLabels.add(UNIQUE_CONNECTIONS_METRIC);
        metricNameWords = UNIQUE_CONNECTIONS_METRIC.split(CAMEL_STYLE_SPLIT_REGEX);
        legendAbbreviations.add(abbreviationStringForMetric(metricNameWords, false));

        Logger.info(aggregationMethodsForKey.toString());

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

    public static Map<String, Object> aggregateResults(AggregateIterable<Document> aggrStats) {
        return aggrStats.first();
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

    public static Map<String, String> getAggregationMethodsForKey() { return aggregationMethodsForKey; }
}
