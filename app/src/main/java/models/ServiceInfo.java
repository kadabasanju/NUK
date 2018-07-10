package models;

import io.realm.RealmObject;

/**
 * Created by Hasala on 26/4/2018.
 */

public class ServiceInfo extends RealmObject {
    private long timestamp;

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
