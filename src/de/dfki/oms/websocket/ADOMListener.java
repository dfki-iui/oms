package de.dfki.oms.websocket;

import de.dfki.adom.rest.APIObserver;
import de.dfki.adom.rest.MetaAttribute;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by crishushu on 02/11/15.
 */
public class ADOMListener implements APIObserver{

    private String memoryName;

    public ADOMListener(String name) {
        this.memoryName = name;
    }


    @Override
    public void notifyFeatureNegotiation(String response) {

    }

    @Override
    public void notifyTableOfContents(String response) {

    }

    @Override
    public void notifyBlockIDs(String response) {

    }

    @Override
    public void notifyPostBlock(String response, String blockAsXMLSnipped) {

    }

    @Override
    public void notifyDeleteBlock(boolean response, String blockID) {

    }

    @Override
    public void notifyMeta(String response, String blockID) {

    }

    @Override
    public void notifyMetaAttribute(String response, String blockID, MetaAttribute metaAttribute) {

    }

    @Override
    public void notifyPostMetaAttribute(boolean response, String blockID, MetaAttribute metaAttribute, String attribXMLSnipped) {

    }

    @Override
    public void notifyPayload(String response, String blockID) {
        JSONObject json = new JSONObject();
        try {
                json.put("event_name", "notify_payload");
                json.put("memory_name", memoryName);
                json.put("block_id", blockID);
                json.put("response", response);
                System.out.println(json.toString());
//                OMSWebSocket.send(json.toString());
        } catch (JSONException e) {
            System.err.println(e);
        }

    }

    @Override
    public void notifyPostPayload(boolean response, String blockID, String payload) {
        JSONObject json = new JSONObject();
        try {
            if (response) {
                json.put("event_name", "notify_post_payload");
                json.put("memory_name", memoryName);
                json.put("block_id", blockID);
//                json.put("payload", payload);

                OMSWebSocket.send(json.toString());
            }
        } catch (JSONException e) {
            System.err.println(e);
        }

    }

    @Override
    public void notifyDeletePayload(boolean response, String blockID) {

    }

    @Override
    public void notifyPayloadEncoding(String response, String blockID) {

    }
}
