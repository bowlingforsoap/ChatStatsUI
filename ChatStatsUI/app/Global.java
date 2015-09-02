/**
 * Created by Strelchenko Vadym on 29.05.15.
 */
import models.DataFetcher;
import play.*;

public class Global extends GlobalSettings {
    //left for future improvements
    /*@Override
    public void onStart(Application app) {
        Akka.system().scheduler().schedule(
                Duration.create(1, TimeUnit.SECONDS),
                Duration.create(1, TimeUnit.SECONDS),
                new Runnable() {
                    @Override
                    public void run() {
                        String[] resourceFold = new File(Utils.DEFAULT_RESOURCE_FOLDER).list();
                        if (resourceFold != null) {
                            for (int i = 0; i < resourceFold.length; i++) {
                                System.out.println("Deleting -> " + resourceFold[i]);
                                try {
                                    if (resourceFold[i] != null && resourceFold[i].length() != 0)
                                    new File(Utils.DEFAULT_RESOURCE_FOLDER + resourceFold[i]).delete();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                },
                Akka.system().dispatcher()
        );

        super.onStart(app);
    }*/

    @Override
    public void onStop(Application app) {
        /*try {
            DataFetcher.getInstance().invalidate();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        super.onStop(app);
    }
}
