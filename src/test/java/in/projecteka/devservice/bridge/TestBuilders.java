package in.projecteka.devservice.bridge;

import in.projecteka.devservice.bridge.model.BridgeRequest;
import in.projecteka.devservice.clients.model.Session;
import org.jeasy.random.EasyRandom;

public class TestBuilders {

    private static final EasyRandom easyRandom = new EasyRandom();

    public static String string() {
        return easyRandom.nextObject(String.class);
    }

    public static BridgeRequest.BridgeRequestBuilder bridgeRequest() {
        return easyRandom.nextObject(BridgeRequest.BridgeRequestBuilder.class);
    }

    public static Session.SessionBuilder session() {
        return easyRandom.nextObject(Session.SessionBuilder.class);
    }
}
