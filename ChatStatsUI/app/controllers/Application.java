package controllers;

import com.google.inject.Inject;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import controllers.utils.FileUtil;
import controllers.utils.Utils;
import models.DataFetcher;
import org.bson.Document;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.Arrays;
import java.util.Map;

public class Application extends Controller {
    public static final long HOUR = 60 * 60 * 1000;
    public static final long DAY = 24 * HOUR;
    public static final long MONTH = 31 * DAY;

    public static final String[] TIME_LENGTHS = {"hour", "day", "month"};

    private final DataFetcher dataFetcher;
    private final FileUtil fileUtil;

    @Inject
    public Application(DataFetcher dataFetcher, FileUtil fileUtil) {
        this.dataFetcher = dataFetcher;
        this.fileUtil = fileUtil;
    }

    /**
     * Gets invoked when you access the main page.
     *
     * @return
     * @throws Exception
     */
    public Result index() throws Exception {
        String sessionId = Utils.getSessionId(session());
        String appId = session(Utils.APP_ID_KEY);
        String timeLength = session("timeLength");
        long requestDate = System.currentTimeMillis();
        long timeLengthValue;
        int timezoneOffset;
        Map<String, Object> aggrResults = null;
        AggregateIterable<Document> fetchedApps = dataFetcher.fetchApps();
        FindIterable<Document> fetchedStats = null;

        //store request time
        session("requestDate", String.valueOf(requestDate));

        //check the data
        try {
            timeLengthValue = Long.valueOf(session("timeLengthValue"));
            timezoneOffset = Integer.valueOf(session("timezoneOffset"));
        } catch (NumberFormatException e) {
            // default values
            timeLengthValue = 1;
            timezoneOffset = 0;
            session("timeLengthValue", String.valueOf(timeLengthValue));
            session("timezoneOffset", String.valueOf(timezoneOffset));
        }

        if (appId == null || timeLength == null || timeLength.length() == 0 || timeLengthValue <= 0 || appId.length() == 0) {
            //if the given parameters are not valid -> erase the contents of the CSV dataset for the corresponding session
            fileUtil.eraseDatafileContent(sessionId);
        } else {
            //calculate the required time length period
            long requestedTimePeriod = 0;
            switch (timeLength) {
                case "hour":
                    requestedTimePeriod = Application.HOUR * timeLengthValue;
                    break;
                case "day":
                    requestedTimePeriod = Application.DAY * timeLengthValue;
                    break;
                case "month":
                    requestedTimePeriod = Application.MONTH * timeLengthValue;
                    break;
            }

            //store the required period in the session object
            session("requestedTimePeriod", String.valueOf(requestedTimePeriod));

            //write statistics to file
            fetchedStats = dataFetcher.fetchStats(appId, requestedTimePeriod, requestDate);

            //get aggregated stats
            if (session("aggregateResults") != null) {
                aggrResults = Utils.aggregateResults(dataFetcher.aggregateStats(appId, requestedTimePeriod, requestDate));
            }
        }

        return ok(views.html.index.render(Utils.appsToList(fetchedApps),
                aggrResults,
                session(),
                fetchedStats));
    }

    /**
     * Changes {@code this.appId, this.timeLength, this.timeLengthValue} and redirects to {@code #index} method.
     *
     * @return
     * @throws Exception
     */
    public Result updateSettings() throws Exception {
        //get the form
        DynamicForm dForm = Form.form().bindFromRequest();
        //retrieve the data
        String appId = dForm.get(Utils.APP_ID_KEY);
        String timeLengthValue = dForm.get("timeLengthValue");
        String timeLength = dForm.get("timeLength");
        String timezoneOffset = dForm.get("timezoneOffset");
        String aggregateResults = dForm.get("aggregateResults");

        //store data in a session
        session("timeLengthValue", timeLengthValue);
        session("appId", appId);
        session("timeLength", timeLength);
        session("timezoneOffset", timezoneOffset);
        if (aggregateResults != null) {
            session("aggregateResults", aggregateResults);
        }

        //redirect to Application.index method
        return redirect("/");
    }

    /**
     * Returns the required resource.
     *
     * @param file file name
     * @return the resource
     */
    public Result getResource(String file) {
        return ok(play.Play.application().getFile(FileUtil.DEFAULT_RESOURCE_FOLDER + file));
    }
}
