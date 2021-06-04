package com.example.augmentedreality.Modules.ObjectPlacer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.augmentedreality.R;

import java.util.ArrayList;

public class RecyclerViewAdapterTop extends RecyclerView.Adapter<RecyclerViewAdapterTop.ViewHolder>
{

    public ArrayList<String> itemimages = new ArrayList<>();
    public ArrayList<String> itemtext = new ArrayList<>();

    public ArrayList<ViewHolder> viewholderlist = new ArrayList<>();
    private Context mContext;
    public static int counter=0;

    public RecyclerViewAdapterTop(Context context, ArrayList<String> imageUrls,ArrayList<String> textUrls)
    {
        //mImageUrls.clear();
        itemimages = imageUrls;
        itemtext = textUrls;
        mContext = context;
        counter=0;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //Log.d(TAG,"onCreateViewHolder: Called.");
        //Toast.makeText(mContext,"onCreateviewHolder",Toast.LENGTH_SHORT).show();

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_list,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        //Log.d(TAG,"onBindViewHolder: Called");
        //Toast.makeText(mContext,"onBindViewHolder : "+position,Toast.LENGTH_SHORT).show();

        Glide.with(mContext)
                .asBitmap()
                .load(itemimages.get(position))
                .into(holder.image);

        holder.textView.setText(itemtext.get(position));

        viewholderlist.add(holder);

        holder.image.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                setImageAlpha();
                holder.image.setAlpha(0.5f);
                counter = position;

                // remove all previous view from recyclerview
                ObjectPlacer.recyclerViewBottom.removeAllViews();
                // re-initiate the recyclerView for adapting new models
                ObjectPlacer.initRecyclerViewBottom();
            }
        });

    }


    @Override
    public int getItemCount() {
        return itemimages.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView image;
        TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.itemimage);
            textView = itemView.findViewById(R.id.itemname);
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
