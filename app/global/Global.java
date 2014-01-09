package global;

import play.*;

public class Global extends GlobalSettings {

    public void onStart(Application app) {
        System.out.println("Application has started");
    }

    public void onStop(Application app) {
    	System.out.println("Application shutdown...");
    }

}