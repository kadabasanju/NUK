package models;

/**
 * Created by Sanj on 1/29/2018.
 */
public class Node {
    private long time;
    private String activity;


    public void setTime(long time) {
        this.time = time;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }



    public long getTime() {
        return time;
    }

    public String getActivity() {
        return activity;
    }


    public Node(long t, String a){
        this.time = t;
        this.activity = a;


    }


}

