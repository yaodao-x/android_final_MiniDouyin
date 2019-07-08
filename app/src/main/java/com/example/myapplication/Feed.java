package com.example.myapplication;

import com.google.gson.annotations.SerializedName;

/**
 * @author Xavier.S
 * @date 2019.01.20 14:18
 */
public class Feed {

    // TODO-C2 (1) Implement your Feed Bean here according to the response json
    @SerializedName("student_id")
    private String student_id;

    @SerializedName("user_name")
    private String user_name;

    @SerializedName("image_url")
    private String image_url;

    @SerializedName("video_url")
    private String video_url;

    public String getImage_url(){
        return image_url;
    }

    public String getVideo_url(){
        return video_url;
    }

    public String getStudent_id(){
        return student_id;
    }

    public String getUser_name(){
        return user_name;
    }
}
