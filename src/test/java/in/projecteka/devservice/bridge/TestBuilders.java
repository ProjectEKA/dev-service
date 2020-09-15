package in.projecteka.devservice.bridge;

import in.projecteka.devservice.bridge.model.BridgeRequest;
import in.projecteka.devservice.clients.model.Session;
import in.projecteka.devservice.support.model.CredentialRequest;
import in.projecteka.devservice.support.model.SupportBridgeResponse;
import in.projecteka.devservice.support.model.SupportRequest;
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

    public static SupportRequest.SupportRequestBuilder supportRequest() {
        return easyRandom.nextObject(SupportRequest.SupportRequestBuilder.class);
    }

    public static SupportBridgeResponse.SupportBridgeResponseBuilder supportBridgeResponse() {
        return easyRandom.nextObject(SupportBridgeResponse.SupportBridgeResponseBuilder.class);
    }

    public static CredentialRequest.CredentialRequestBuilder credentialRequest() {
        return easyRandom.nextObject(CredentialRequest.CredentialRequestBuilder.class);
    }
}
