package com.example.augmentedreality.Modules.FaceFilter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.augmentedreality.R;

import java.util.ArrayList;

public class RecyclerViewAdapterfilter extends RecyclerView.Adapter<RecyclerViewAdapterfilter.ViewHolder>
{

    private  static final String TAG = "RecyclerViewAdapterFilter";

    private static ArrayList<String> mImageUrls = new ArrayList<>();
    private static ArrayList<ViewHolder> viewholderlist = new ArrayList<>();
    private Context mContext;
    private  static int flag=-1;
    private static int counter=0;

    RecyclerViewAdapterfilter(Context context, ArrayList<String> imageUrls)
    {
        mImageUrls = imageUrls;
        mContext = context;
        viewholderlist.clear();

    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        Log.d(TAG,"onCreateViewHolder: Called.");
        //Toast.makeText(mContext,"onCreateviewHolder",Toast.LENGTH_SHORT).show();

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_listfilter,parent,false);

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
                Log.d(TAG,"onClick: clicked on an image: "+ mImageUrls.get(position));
                //Toast.makeText(mContext,"image clicked:"+mImageUrls.get(position),Toast.LENGTH_SHORT).show();
                holder.cardView.setAlpha(0.3f);
                counter=position;

                if(FaceFilters.augmentedFaceNode!=null)
                {
                    FaceFilters.removefromdisplay();
                }
                FaceFilters.placefilter(position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mImageUrls.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView image;
        CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image_view);
            cardView = itemView.findViewById(R.id.filterscardview);
        }
    }

    private void setImageAlpha()
    {
        for(int i=0;i<viewholderlist.size();i++)
        {
            viewholderlist.get(i).cardView.setAlpha(0f);
        }
    }

}
