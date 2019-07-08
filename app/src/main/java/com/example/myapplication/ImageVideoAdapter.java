package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;


public class ImageVideoAdapter extends RecyclerView.Adapter<ImageVideoAdapter.ViewHolder> {
    private static final String TAG = ImageVideoAdapter.class.getSimpleName();
    private Context mContext;

    ImageVideoAdapter(Context mContext) {
        this.mContext = mContext;
        Log.e(TAG, "ImageVideoAdapter: ");
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView videoCover;
        TextView videoMessage;

        public ViewHolder(View itemView) {
            super(itemView);
            videoCover = (ImageView) itemView.findViewById(R.id.imageView);
            videoMessage = (TextView) itemView.findViewById(R.id.textView);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.vedo_cover, parent, false);
        Log.e(TAG, "onCreateViewHolder: ");
        return new ViewHolder(view);

    }

    private String vedio_url;

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ImageView iv = (ImageView) holder.videoCover;
        TextView tv = (TextView) holder.videoMessage;
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(TAG, "onClick:  click the image");
                Toast.makeText(mContext, "click this vedio", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(mContext, VideoPlayActivity.class);
                intent.putExtra("video_url", vedio_url);
                Log.e(TAG, "onClick: " + vedio_url);
                ContextCompat.startActivity(mContext, intent, null);
                Log.e(TAG, "onClick:  start to play");
            }
        });

        // TODO-C2 (10) Uncomment these 2 lines, assign image url of Feed to this url variable
        String url = MainActivity.mFeeds.get(position).getImage_url();
        vedio_url = MainActivity.mFeeds.get(position).getVideo_url();
        Glide.with(iv.getContext()).load(url).into(iv);
        tv.setText(MainActivity.mFeeds.get(position).getStudent_id() + "\n" + MainActivity.mFeeds.get(position).getUser_name());
        Log.e(TAG, "onBindViewHolder: " + MainActivity.mFeeds.size());
    }

    @Override
    public int getItemCount() {
        return MainActivity.mFeeds.size();
    }

    public String toString() {
        return "xjp";
    }

}