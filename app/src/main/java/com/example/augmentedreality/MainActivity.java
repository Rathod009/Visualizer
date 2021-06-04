package com.example.augmentedreality;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.augmentedreality.Modules.FaceFilter.FaceFilters;
import com.example.augmentedreality.Modules.FunMode.FunMode;
import com.example.augmentedreality.Modules.ObjectPlacer.ObjectPlacer;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity
{
    private ArFragment arFragment;
    private DatabaseReference databaseReference;

    // ImageView and TextView
    ImageView objplacerimg,facefilterimg,doodlerimg,funmodeimg;

    // Context
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // remove status bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.arfragmentmainpage);
        arFragment.getPlaneDiscoveryController().hide();
        arFragment.getPlaneDiscoveryController().setInstructionView(null);
        arFragment.getArSceneView().getPlaneRenderer().setEnabled(false);
        //arFragment.getArSceneView().getForeground().setAlpha(220);

        objplacerimg = (ImageView)findViewById(R.id.imageicon1);
        facefilterimg = (ImageView)findViewById(R.id.imageicon2);
        doodlerimg = (ImageView)findViewById(R.id.imageicon3);
        funmodeimg = (ImageView)findViewById(R.id.imageicon4);

        getDataFromFirebase();

    }

    private void getDataFromFirebase()
    {
        databaseReference = FirebaseDatabase.getInstance().getReference().child("mainpage");

        ValueEventListener eventListenerobjplacer = new ValueEventListener()
        {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Glide.with(context)
                        .asBitmap()
                        .load(dataSnapshot.child("icon2").getValue().toString())
                        .into(objplacerimg);

                Glide.with(context)
                        .asBitmap()
                        .load(dataSnapshot.child("icon1").getValue().toString())
                        .into(facefilterimg);

                Glide.with(context)
                        .asBitmap()
                        .load(dataSnapshot.child("icon3").getValue().toString())
                        .into(doodlerimg);

                Glide.with(context)
                        .asBitmap()
                        .load(dataSnapshot.child("icon4").getValue().toString())
                        .into(funmodeimg);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("TAG", databaseError.getMessage()); //Don't ignore potential errors!
            }
        };
        databaseReference.addListenerForSingleValueEvent(eventListenerobjplacer);


    }

    public void callIntent1(View view)
    {
        Intent intent1 = new Intent(this, ObjectPlacer.class);
        startActivity(intent1);
    }

    public void callIntent2(View view)
    {
        Intent intent2 = new Intent(this, FaceFilters.class);
        startActivity(intent2);
    }

    public void callIntent3(View view)
    {
        Intent intent3 = getPackageManager().getLaunchIntentForPackage("com.dhruv.drawDraw");
        if(intent3!=null)
            startActivity(intent3);
        else
            Toast.makeText(this,"Package not Available",Toast.LENGTH_LONG).show();
    }

    public void callIntent4(View view)
    {
        Intent intent4 = new Intent(this, FunMode.class);
        startActivity(intent4);
    }

}
