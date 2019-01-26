package com.sk.quantumsudio.operationweb.myapplication.homepage;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sk.quantumsudio.operationweb.myapplication.R;
import com.sk.quantumsudio.operationweb.myapplication.webpage.WebPageActivity;

import java.util.List;


public class ShortcutAdapter extends RecyclerView.Adapter<ShortcutViewHolder> {
    private static final String TAG = "ShortcutAdapter";
    private Context mContext;
    private List<HomepageShortcutData> mShortcutData;

    ShortcutAdapter(Context mContext, List<HomepageShortcutData> mShortcutData){
        this.mContext = mContext;
        this.mShortcutData = mShortcutData;
    }
    @NonNull
    @Override
    public ShortcutViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        // setting items layout view
        View mView = LayoutInflater.from(mContext).inflate(R.layout.recyclerview_item_row,viewGroup, false);
        return new ShortcutViewHolder(mView);
    }

    @Override
    public void onBindViewHolder(@NonNull ShortcutViewHolder holder, final int position) {

        holder.mImage.setImageResource(mShortcutData.get(position).getShortcutImage()); //binding everything with the layout
        holder.mTitle.setText(mShortcutData.get(position).getShortcutName());

        holder.shortCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //onClick function of every items
                String Url = itemsUrl(position);
                Intent intent = new Intent(mContext, WebPageActivity.class);
                intent.putExtra("ItemUrl", Url);
                intent.putExtra("activityId", 2);
                mContext.startActivity(intent);
            }
        });
    }
    @Override
    public int getItemCount() {
        return mShortcutData.size();
    }
    private String itemsUrl(int position){  //method for managing items url
        String[] url = new String[15];
        url[0] = "https://www.google.com";
        url[1] = "https://www.facebook.com";
        url[2] = "https://www.youtube.com";
        url[3] = "https://www.twitter.com";
        url[4] = "https://www.instagram.com";
        url[5] = "https://www.amazon.com";
        url[6] = "https://www.flipkart.com";
        url[7] = "https://www.ebay.com";
        url[8] = "https://www.snapdeal.com";
        url[9] = "https://www.cricbuzz.com/cricket-match/live-scores";
        url[10] = "https://www.zomato.com";
        url[11] = "https://www.uber.com/in/en";

        return url[position];
    }
}
class ShortcutViewHolder extends RecyclerView.ViewHolder{
    ImageView mImage;
    TextView mTitle;
    CardView shortCardView;

    ShortcutViewHolder(@NonNull View itemView) {
        super(itemView);

        mImage = itemView.findViewById(R.id.iv_itemImage);
        mTitle = itemView.findViewById(R.id.tv_itemTitle);
        shortCardView = itemView.findViewById(R.id.cardview);
    }
}
