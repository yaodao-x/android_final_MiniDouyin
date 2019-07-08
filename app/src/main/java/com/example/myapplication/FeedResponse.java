package com.example.myapplication;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * @author Xavier.S
 * @date 2019.01.20 14:17
 */
public class FeedResponse {

    // TODO-C2 (2) Implement your FeedResponse Bean here according to the response json
    @SerializedName("feeds")
    private List<Feed> feeds;
    @SerializedName("success")
    private boolean success;

    public List<Feed> getFeeds(){
        return this.feeds;
    }

}
