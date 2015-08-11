package controllers;

import com.mongodb.client.FindIterable;
import controllers.util.Utils;
import models.DataFetcher;
import org.bson.Document;
import play.Play;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Application extends Controller {
    public static final long HOUR = 60 * 60 * 1000;
    public static final long DAY = 24 * HOUR;
    public static final long MONTH = 31 * DAY;

    /**
     * Gets invoked when you access the main page.
     *
     * @return
     * @throws Exception
     */
    public Result index() throws Exception {
        String sessionId = Utils.getSessionId(session());
        String appId = session("appId");
        String timeLength = session("timeLength");
        long requestDate = System.currentTimeMillis();
        long timeLengthValue;
        int timezoneOffset;
        List<Long> aggrResults = null;

        //check the data
        try {
            timeLengthValue = Long.valueOf(session("timeLengthValue"));
            timezoneOffset = Integer.valueOf(session("timezoneOffset"));
        } catch (NumberFormatException e) {
            // default values
            timeLengthValue = 1;
            timezoneOffset = 0;
        }
        if (appId == null || timeLength == null || timeLength.length() == 0 || timeLengthValue <= 0 || appId.length() == 0) {
            //if the given parameters are not valid -> erase the contents of the CSV dataset for the corresponding session
            Utils.eraseDatafileContent(sessionId);
        } else {
            //calculate the required time length period
            long startingFrom = 0;
            switch (timeLength) {
                case "hour":
                    startingFrom = Application.HOUR * timeLengthValue;
                    break;
                case "day":
                    startingFrom = Application.DAY * timeLengthValue;
                    break;
                case "month":
                    startingFrom = Application.MONTH * timeLengthValue;
                    break;
            }
            //write statistics to file
            FindIterable<Document> stats = DataFetcher.getInstance().fetchStats(appId, startingFrom, requestDate);
            String statsCsvString = Utils.statsToStringCsv(stats, startingFrom, timezoneOffset, requestDate);
            Utils.writeDataToFile(statsCsvString, sessionId);

            //aggregate the results
            if (session("aggregateResults") != null) {
                aggrResults = Utils.aggregateResults(stats);
            }
        }

        return ok(views.html.index.render(DataFetcher.getInstance().fetchApps(), Arrays.asList("hour", "day", "month"),
                Arrays.asList(Utils.KEYS_TO_PARSE), aggrResults, session(), Arrays.asList(Utils.ABBR_METRICS), Utils.STATS_PERIOD_SEC));
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
        String appId = dForm.get("appId");
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

    //TODO: check if needed

    /**
     * Returns the required resource.
     *
     * @param file file name
     * @return the resource
     */
    public Result getResource(String file) {
        return ok(play.Play.application().getFile(Utils.DEFAULT_RESOURCE_FOLDER + file));
    }
}
