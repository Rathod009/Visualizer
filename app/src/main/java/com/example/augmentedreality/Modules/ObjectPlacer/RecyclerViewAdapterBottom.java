package com.example.augmentedreality.Modules.ObjectPlacer;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.augmentedreality.R;

import java.util.ArrayList;

public class RecyclerViewAdapterBottom extends RecyclerView.Adapter<RecyclerViewAdapterBottom.ViewHolder>
{

    private  static final String TAG = "RecyclerViewAdapter";

    public ArrayList<String> mImageUrls = new ArrayList<>();
    public ArrayList<ViewHolder> viewholderlist = new ArrayList<>();
    private Context mContext;
    private  static int flag=-1;
    public static int counter=0;

    public RecyclerViewAdapterBottom(Context context, ArrayList<String> imageUrls)
    {
        //mImageUrls.clear();
        mImageUrls = imageUrls;
        mContext = context;
        counter=0;

    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        Log.d(TAG,"onCreateViewHolder: Called.");
        //Toast.makeText(mContext,"onCreateviewHolder",Toast.LENGTH_SHORT).show();

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_listitem,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Log.d(TAG,"onBindViewHolder: Called");
        //Toast.makeText(mContext,"onBindViewHolder : "+position,Toast.LENGTH_SHORT).show();

        Glide.with(mContext)
                .asBitmap()
                .load(mImageUrls.get(position))
                .into(holder.image);

        viewholderlist.add(holder);

        holder.image.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                setImageAlpha();
                holder.image.setAlpha(0.5f);
                counter=position;
            }
        });

    }

    @Override
    public int getItemCount() {
        return mImageUrls.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image_view);
        }
    }

    private void setImageAlpha()
    {
        for(int i=0;i<viewholderlist.size();i++)
        {
            viewholderlist.get(i).image.setAlpha(1f);
        }
    }

}

