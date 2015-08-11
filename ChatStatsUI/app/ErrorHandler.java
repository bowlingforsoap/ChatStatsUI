import controllers.Application;
import play.Configuration;
import play.Environment;
import play.api.OptionalSourceMapper;
import play.api.routing.Router;
import play.http.DefaultHttpErrorHandler;
import play.libs.F;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Created by qb-user on 8/6/15.
 */
public class ErrorHandler extends DefaultHttpErrorHandler {

    @Inject
    public ErrorHandler(Configuration configuration, Environment environment, OptionalSourceMapper optionalSourceMapper, Provider<Router> provider) {
        super(configuration, environment, optionalSourceMapper, provider);
    }

    /*@Override
    public F.Promise<Result> onClientError(Http.RequestHeader requestHeader, int i, String s) {
        System.out.println("ErrorHandler.onClientError : requestHeader = [" + requestHeader + "], i = [" + i + "], s = [" + s + "]");
        Application.session().clear();
        return super.onClientError(requestHeader, i, s);
    }*/

    @Override
    public F.Promise<Result> onServerError(Http.RequestHeader requestHeader, Throwable throwable) {
        System.out.println("ErrorHandler.onServerError : requestHeader = [" + requestHeader + "], throwable = [" + throwable + "]");
        Application.session().clear();
        return super.onServerError(requestHeader, throwable);
    }
}
