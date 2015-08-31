package models;

/**
 * Created by Strelchenko Vadym on 28.05.15.
 */

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoException;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import controllers.util.Utils;
import org.bson.Document;
import org.bson.types.ObjectId;

import javax.rmi.CORBA.Util;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.*;

/**
 * Performs operations with data in MongoDB and manages MongoClient connection.
 */
public class DataFetcher {
    public static final String DEFAULT_STATS_HOST = "localhost";
    public static final String DEFAULT_STATS_DB = "admin_chat";
    /**
     * Collection to work with.
     */
    public static final String DEFAULT_STATS_COLL = "chat_statistics_per_unit";

    private static DataFetcher instance = new DataFetcher();

    private MongoClient client;

    /**
     * DB to work with.
     */
    private String db;

    private DataFetcher() {
        try {
            String dbUriValue = Utils.CHAT_STATS_DB_URI;
            MongoClientURI dbUri = new MongoClientURI(dbUriValue);
            db = dbUri.getDatabase();
            client = new MongoClient(dbUri);
        } catch (MongoException | NullPointerException e) {
            client = new MongoClient(DEFAULT_STATS_HOST);
        }
    }

    public static synchronized DataFetcher getInstance() throws Exception {
        if (instance == null) {
            instance = new DataFetcher();
        }
        return instance;
    }

    public void invalidate() {
        instance.client.close();
        instance = null;
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
        MongoDatabase adminChat = getDB();
        MongoCollection<Document> statistics = getCollection(adminChat);
        //find all apps with the given appId, where 'created_at' field is greater than new Date(System.currentTimeMillis() - startingFrom))
        FindIterable<Document> stats = statistics.find(new BasicDBObject("app_id", appId)
                .append("created_at", new BasicDBObject("$gte", new Date(requestDate - startingFrom))))
                .sort(new BasicDBObject("created_at", 1));
        return stats;
    }


    /**
     * Fetches all apps from DB. Uses aggregation.
     *
     * @return
     */
    public List<String> fetchApps() {
        MongoDatabase adminChat = getDB();
        MongoCollection<Document> statistics = getCollection(adminChat);
        AggregateIterable<Document> apps = statistics.aggregate(Arrays.asList(new BasicDBObject("$group", new BasicDBObject("_id", "$app_id"))));
        return Utils.appsToList(apps);
    }

    private MongoCollection<Document> getCollection(MongoDatabase adminChat) {
        MongoCollection<Document> statistics;
        if (DEFAULT_STATS_COLL == null || DEFAULT_STATS_COLL.length() == 0) {
            statistics = adminChat.getCollection(DEFAULT_STATS_COLL);
        } else {
            statistics = adminChat.getCollection(DEFAULT_STATS_COLL);
        }
        return statistics;
    }

    private MongoDatabase getDB() {
        MongoDatabase adminChat;
        if (db == null || db.length() == 0) {
            adminChat = instance.client.getDatabase(DEFAULT_STATS_DB);
        } else {
            adminChat = instance.client.getDatabase(db);
        }
        return adminChat;
    }

    //TODO: REMOVE
    //in case you need to generate some data
    public void insertData(int n) {
        MongoDatabase adminChat = instance.client.getDatabase(DEFAULT_STATS_DB);
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
                    doc.put("created_at", new Date(System.currentTimeMillis() - timeLenght * randGenerator.nextInt(100) / 100));
                    doc.put("app_id", app);
                    for (int i1 = 1; i1 < Utils.KEYS_TO_PARSE.length; i1++) {
                        String key = Utils.KEYS_TO_PARSE[i1];
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
