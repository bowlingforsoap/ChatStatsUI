package models;

/**
 * Created by Strelchenko Vadym on 28.05.15.
 */

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoException;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Projections;
import controllers.utils.Utils;
import org.bson.Document;
import org.bson.types.ObjectId;
import play.Logger;
import play.inject.ApplicationLifecycle;
import play.libs.F;

import java.io.IOException;
import java.util.*;

/**
 * Performs operations with data in MongoDB and manages MongoClient connection.
 */
@Singleton
public class DataFetcher {
    public static final String DEFAULT_STATS_HOST = "localhost";
    public static final String DEFAULT_STATS_DB = "admin_chat";
    /**
     * Collection to work with.
     */
    public static final String DEFAULT_STATS_COLL = "chat_statistics_per_unit";

    private MongoClient client;
    /**
     * DB to work with.
     */
    private String db;

    @Inject
    public DataFetcher(ApplicationLifecycle lifecycle) {
        try {
            String dbUriValue = Utils.CHAT_STATS_DB_URI;
            MongoClientURI dbUri = new MongoClientURI(dbUriValue);

            db = dbUri.getDatabase();
            client = new MongoClient(dbUri);
        } catch (MongoException | NullPointerException e) {
            Logger.error("Exception while parsing " + Utils.CHAT_STATS_DB_URI_KEY + ", using defaults : [ host : " + DEFAULT_STATS_HOST + " ], [ db : " + DEFAULT_STATS_DB + " ]");
            client = new MongoClient(DEFAULT_STATS_HOST);
            db = DEFAULT_STATS_DB;
        }

        //index creation phase
        //
        try {
            client.getDatabase(db).getCollection(DEFAULT_STATS_COLL)
                    .createIndex(new BasicDBObject(Utils.APP_ID_KEY, 1).append(Utils.CREATED_AT_KEY, 1), new IndexOptions().background(true));
            Logger.info("Finished index creation");
        } catch (Exception e) {
            Logger.error("Index creation failed - " + e.toString());
        }

        //clean-up phase setup
        //
        lifecycle.addStopHook(() -> {

            client.close();

            return F.Promise.pure(null);
        });
    }

    /**
     * Fetches statistics data from DB.
     *
     * @param appId        the id of the app to fetch;
     * @param startingFrom specifies from which date to start fetching data. Date is calculated as follows: {@code new Date(Sytem.currentTimeMillis() - startingFrom)}.
     * @param requestDate
     * @throws IOException
     */
    public FindIterable<Document> fetchStats(String appId, long startingFrom, long requestDate) throws IOException {
        //initialize db and collection objects
        //if config values are not given, then use defaults
        MongoDatabase adminChat;
        MongoCollection<Document> statistics;

        try {
            adminChat = getDB();
            statistics = getCollection(adminChat);
        } catch (Exception e) {
            Logger.error("Error accessing database : " + e.toString());
            return null;
        }

        //find all apps with the given appId, where 'created_at' field is greater than new Date(System.currentTimeMillis() - startingFrom))
        FindIterable<Document> stats = statistics.find(new BasicDBObject(Utils.APP_ID_KEY, appId)
                .append(Utils.CREATED_AT_KEY, new BasicDBObject("$gte", /*new Date(*/requestDate - startingFrom/*)*/)))
                .projection(new BasicDBObject("_id", 0).append(Utils.APP_ID_KEY, 0)).sort(new BasicDBObject(Utils.CREATED_AT_KEY, 1));

        return stats;
    }

    /**
     * Fetches all apps from DB. Uses aggregation.
     *
     * @return
     */
    public AggregateIterable<Document> fetchApps() {
        MongoDatabase adminChat;
        MongoCollection<Document> statistics;

        try {
            adminChat = getDB();
            statistics = getCollection(adminChat);
        } catch (Exception e) {
            Logger.error("Error accessing database : " + e.toString());
            return null;
        }

        return statistics.aggregate(Arrays.asList(
                new BasicDBObject("$project", new BasicDBObject(Utils.APP_ID_KEY, 1).append("_id", 0)),
                new BasicDBObject("$group", new BasicDBObject("_id", "$" + Utils.APP_ID_KEY))));

    }

    public AggregateIterable<Document> aggregateStats(String appId, long startingFrom, long requestDate) {
        MongoDatabase adminChat;
        MongoCollection<Document> statistics;

        try {
            adminChat = getDB();
            statistics = getCollection(adminChat);
        } catch (Exception e) {
            Logger.error("Error accessing database : " + e.toString());
            return null;
        }

        BasicDBObject groupBody = new BasicDBObject("_id", null);

        for (Map.Entry<String, String> entry : Utils.getAggregationMethodsForKey().entrySet()) {
            groupBody.append(entry.getKey(), new BasicDBObject("$" + entry.getValue(), "$" + entry.getKey()));
        }

        return statistics.aggregate(Arrays.asList(new BasicDBObject("$match",
                        new BasicDBObject(Utils.CREATED_AT_KEY, new BasicDBObject("$gte", requestDate - startingFrom)).append(Utils.APP_ID_KEY, appId)),
                new BasicDBObject("$group", groupBody)));
    }

    private MongoCollection<Document> getCollection(MongoDatabase adminChat) {
        return adminChat.getCollection(DEFAULT_STATS_COLL);
    }

    private MongoDatabase getDB() {
        return client.getDatabase(db);
    }

    //TODO: REMOVE
    //in case you need to generate some data
    public void insertData(int n) {
        MongoDatabase adminChat = client.getDatabase(DEFAULT_STATS_DB);
        MongoCollection<Document> statistics = adminChat.getCollection(DEFAULT_STATS_COLL);
        Random randGenerator = new Random();
        List<Document> docs = new ArrayList<>(n);
        long hour = 1000 * 60 * 60;
        long day = 24 * hour;
        long month = 31 * day;
        long[] timeLengths = {hour, day, month};
        String[] apps = {"13065", "13066", "666"};
        Document doc;

        for (String app : apps) {
            for (long timeLenght : timeLengths) {
                for (int i = 0; i < n; i++) {
                    doc = new Document();
                    doc.put("_id", new ObjectId());
                    doc.put(Utils.CREATED_AT_KEY, System.currentTimeMillis() - timeLenght * randGenerator.nextInt(100) / 100);
                    doc.put(Utils.APP_ID_KEY, app);
                    for (int i1 = 1; i1 < Utils.getKeysToParse().size(); i1++) {
                        String key = Utils.getKeysToParse().get(i1);
                        if (key.equals(Utils.CONNECTIONS_METRIC) || key.equals(Utils.UNIQUE_CONNECTIONS_METRIC)) {
                            doc.put(key, new Long(randGenerator.nextInt(1000)));
                        } else {
                            doc.put(key, randGenerator.nextDouble() * 10000);
                        }
                    }
                    docs.add(doc);
                }
            }
        }
        statistics.insertMany(docs);
    }
}
