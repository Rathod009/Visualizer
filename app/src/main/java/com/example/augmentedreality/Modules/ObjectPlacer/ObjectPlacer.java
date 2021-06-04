package com.example.augmentedreality.Modules.ObjectPlacer;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Gravity;
import android.view.PixelCopy;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.augmentedreality.MainActivity;
import com.example.augmentedreality.R;
import com.google.ar.core.Anchor;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.assets.RenderableSource;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.PlaneRenderer;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ObjectPlacer extends AppCompatActivity
{
    /* Note : It Depends on Files like
        RecyclerViewAdapterTop (Java)
        RecyclerViewAdapterBottom (Java)
        layout_list (XML)
        layout_listitem (XML)
     */
    public static RecyclerView recyclerViewTop,recyclerViewBottom;
    public static RecyclerViewAdapterBottom adapterBottom;
    public static RecyclerViewAdapterTop adapterTop;

    // animation and media player
    MediaPlayer mp,rec1,rec2;
    static Animation myAnim,recordinganim;

    // Bottom layout
    RelativeLayout objmenulayout;
    // menu option Button
    Button menu;
    private static int counterformenu=0;

    // Camera Click Button
    Button cameraclick;

    // for Video Recording
    Boolean islongpressed=false;
    VideoRecorder videoRecorder;
    CardView recordercardview;
    Thread thread;

    int px;

    private ArFragment customfragment;
    private AnchorNode anchorNode;

    public static Context context;

    private DatabaseReference databaseReference;

    // array list for captured links from firebase
    // this is for main menu images
    public ArrayList<String> itemimages = new ArrayList<>();
    // this is for main menu text
    public ArrayList<String> itemtext = new ArrayList<>();
    // this is for submenu item images
    public static ArrayList<ArrayList<String>> models_images = new ArrayList<ArrayList<String>>();
    // this is for submenu item model links
    public ArrayList<ArrayList<String>> models_links = new ArrayList<ArrayList<String>>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_object_placer);


        // getting context
        context = getApplicationContext();

        // init recyclerview Top and Bottom
        recyclerViewTop = findViewById(R.id.recyclerViewTop);
        recyclerViewBottom = findViewById(R.id.recyclerViewBottom);

        // initialize animation and media player
        recordinganim = AnimationUtils.loadAnimation(this,R.anim.zoomout);
        rec1 = MediaPlayer.create(this,R.raw.beep2);
        rec2 = MediaPlayer.create(this,R.raw.beep1);

        myAnim = AnimationUtils.loadAnimation(this, R.anim.bounce);
        mp = MediaPlayer.create(this, R.raw.camera);

        final float scale = this.getResources().getDisplayMetrics().density;
        px = (int) (90 * scale + 0.5f);

        objmenulayout = (RelativeLayout)findViewById(R.id.relativelayoutobjsubmenu);

        menu = (Button)findViewById(R.id.objmenuoption);

        // initialize ArFragment
        customfragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.arfragmentobjectplacer1);
        // changing plan finding dots color
        customfragment.getArSceneView().getPlaneRenderer().getMaterial().thenAccept(material -> material.setFloat3(PlaneRenderer.MATERIAL_COLOR, new Color(0.0f, 0.0f, 1.0f, 1.0f)));
        // setting up ontaplistener
        customfragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {

            placeModel(hitResult.createAnchor());

        });

        // Cardview for Video Timer GUI
        recordercardview = (CardView)findViewById(R.id.recordercardviewobjplacer);

        // initializing and setting up long click listener for Video Recording
        cameraclick = (Button) findViewById(R.id.imagebuttoncameraobjplacer);
        cameraclick.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                if(!islongpressed)
                {
                    setrecvisibility();
                    cameraclick.setBackgroundResource(R.drawable.recordig);
                    cameraclick.setAnimation(recordinganim);
                    rec1.start();
                    Toast toast=Toast.makeText(getApplicationContext()," Recording Started !",Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER_HORIZONTAL,0,0);
                    toast.getView().setBackgroundColor(0xFFFFFF);
                    toast.show();
                    islongpressed=true;

                    startrecording();
                    startTimer();
                }
                return true;
            }

        });

        //getData();
        getDatafromFirebase();

    }

    public void initRecyclerViewTop()
    {

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerViewTop.setLayoutManager(layoutManager);
        // creating class object
        adapterTop = new RecyclerViewAdapterTop(this,itemimages,itemtext);
        recyclerViewTop.setAdapter(adapterTop);
        // recyclerView.removeAllViewsInLayout();

    }

    public static void initRecyclerViewBottom()
    {
        LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        recyclerViewBottom.setLayoutManager(layoutManager);
        // creating class object
        adapterBottom = new RecyclerViewAdapterBottom(context,models_images.get(RecyclerViewAdapterTop.counter));
        recyclerViewBottom.setAdapter(adapterBottom);
    }

    private void placeModel(Anchor anchor)
    {
        String temp=models_links.get(RecyclerViewAdapterTop.counter).get(RecyclerViewAdapterBottom.counter);

        RenderableSource renderableSource = RenderableSource
                .builder()
                .setSource(this, Uri.parse(temp), RenderableSource.SourceType.GLB)
                .setScale(0.3f)
                .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                .build();


        ModelRenderable.builder()
                .setSource(this,renderableSource)
                .build()
                .thenAccept(modelRenderable -> addModelToScene(anchor,modelRenderable))
                .exceptionally(throwable -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(throwable.getMessage())
                            .show();
                    return null;
                });

        Toast.makeText(this,"Downloading...",Toast.LENGTH_SHORT).show();

    }

    private void addModelToScene(Anchor anchor, ModelRenderable modelRenderable)
    {
        // we cant increse or decrese size of anchornode
        anchorNode = new AnchorNode(anchor);
        // for zoom in zoom out
        TransformableNode transformableNode = new TransformableNode(customfragment.getTransformationSystem());
        transformableNode.setParent(anchorNode);
        // tranformableNode is child of anchorNode
        transformableNode.setRenderable(modelRenderable);
        customfragment.getArSceneView().getScene().addChild(anchorNode); // here place parent node not child(transformableNode)
        transformableNode.select();  // we select it
    }

    private void getDatafromFirebase()
    {
        databaseReference = FirebaseDatabase.getInstance().getReference().child("objplacer");

        ValueEventListener eventListenerobjplacer = new ValueEventListener()
        {
            // array list for captured links from firebase
            // this is for main menu images
            ArrayList<String> itemimages = new ArrayList<>();
            // this is for main menu text
            ArrayList<String> itemtext = new ArrayList<>();
            // this is for submenu items
            ArrayList<ArrayList<String>> models_images = new ArrayList<ArrayList<String>>();

            ArrayList<ArrayList<String>> models_links = new ArrayList<ArrayList<String>>();

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    //Toast.makeText(getApplicationContext(),""+ds.getKey(),Toast.LENGTH_SHORT).show();

                    itemimages.add(ds.child("image").getValue().toString());
                    itemtext.add(ds.child("text").getValue().toString());

                    ArrayList<String> temp1 = new ArrayList<>();
                    ArrayList<String> temp2 = new ArrayList<>();

                    for(DataSnapshot ds1 : ds.child("models").getChildren())
                    {
                        temp1.add(ds1.child("image").getValue().toString());
                        temp2.add(ds1.child("model").getValue().toString());
                    }

                    models_images.add(temp1);
                    models_links.add(temp2);

                }
                sendDatatoApp(itemimages,itemtext,models_images,models_links);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("TAG", databaseError.getMessage()); //Don't ignore potential errors!
            }
        };
        databaseReference.addListenerForSingleValueEvent(eventListenerobjplacer);

        //Toast.makeText(this,"after",Toast.LENGTH_SHORT).show();

    }

    public void sendDatatoApp(ArrayList<String> it_images,ArrayList<String> it_text,ArrayList<ArrayList<String>> mod_images,ArrayList<ArrayList<String>> mod_links)
    {
        itemimages = it_images;
        itemtext = it_text;
        models_images = mod_images;
        models_links=mod_links;

        // creating  Top recyclerview
        initRecyclerViewTop();
    }

    private String generateFilename() {
        String date =
                new SimpleDateFormat("yyyyMMddHHmmss", java.util.Locale.getDefault()).format(new Date());
        return Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES) + File.separator + "Sceneform/" + date + "_screenshot.jpg";
    }

    private void saveBitmapToDisk(Bitmap bitmap, String filename) throws IOException {

        File out = new File(filename);
        if (!out.getParentFile().exists()) {
            out.getParentFile().mkdirs();
        }
        try (FileOutputStream outputStream = new FileOutputStream(filename);
             ByteArrayOutputStream outputData = new ByteArrayOutputStream()) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputData);
            outputData.writeTo(outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (IOException ex) {
            throw new IOException("Failed to save bitmap to disk", ex);
        }
    }

    private void takePhoto() {
        final String filename = generateFilename();
        /*ArSceneView view = fragment.getArSceneView();*/
        //mSurfaceView = findViewById(R.id.arfragmentfacefilter);
        // Create a bitmap the size of the scene view.
        final Bitmap bitmap = Bitmap.createBitmap(customfragment.getArSceneView().getWidth(), customfragment.getArSceneView().getHeight(),
                Bitmap.Config.ARGB_8888);

        // Create a handler thread to offload the processing of the image.
        final HandlerThread handlerThread = new HandlerThread("PixelCopier");
        handlerThread.start();
        // Make the request to copy.
        PixelCopy.request(customfragment.getArSceneView(), bitmap, (copyResult) -> {
            if (copyResult == PixelCopy.SUCCESS) {
                try {
                    saveBitmapToDisk(bitmap, filename);

                } catch (IOException e) {
                    Toast toast = Toast.makeText(this, e.toString(),
                            Toast.LENGTH_LONG);
                    toast.show();
                    return;
                }

            } else {
                Log.d("DrawAR", "Failed to copyPixels: " + copyResult);
                Toast toast = Toast.makeText(this,
                        "Failed to copyPixels: " + copyResult, Toast.LENGTH_LONG);
                toast.show();
            }
            handlerThread.quitSafely();
        }, new Handler(handlerThread.getLooper()));
    }

    private void startTimer() {

        TextView timer = findViewById(R.id.reccountobjplacer);

        thread = new Thread(() -> {
            int second = 0;

            while(islongpressed){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                second++;
                int minutesPassed = second/60;
                int secondsPassed = second %60;

                this.runOnUiThread(
                        ()-> timer.setText(String.format("%02d:%02d",minutesPassed,secondsPassed)));
            }
        });
        thread.start();
    }

    public void startrecording()
    {
        if(videoRecorder == null)
        {
            videoRecorder = new VideoRecorder();
            videoRecorder.setSceneView(customfragment.getArSceneView());

            int orientation = getResources().getConfiguration().orientation;

            videoRecorder.setVideoQuality(CamcorderProfile.QUALITY_HIGH, orientation);
        }

        boolean isrec = videoRecorder.onToggleRecord();

    }


    public void getmenubar(View view)
    {
        counterformenu++;
        if(counterformenu%2==1)
        {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) objmenulayout.getLayoutParams();
// Changes the height and width to the specified *pixels*
            params.height = px;
            objmenulayout.setLayoutParams(params);

            menu.setBackgroundResource(R.drawable.menu2);
        }
        else
        {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) objmenulayout.getLayoutParams();
// Changes the height and width to the specified *pixels*
            params.height = 1;
            objmenulayout.setLayoutParams(params);

            menu.setBackgroundResource(R.drawable.menu1);
        }
    }

    public void savephoto(View view)
    {
        // no long press detected
        if(!islongpressed)
        {
            cameraclick.startAnimation(myAnim);
            mp.start();
            takePhoto();
            Toast toast=Toast.makeText(this,"Image Captured!",Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER_HORIZONTAL,0,0);
            toast.getView().setBackgroundColor(0xFFFFFF);
            toast.show();
        }
        else
        {
            rec2.start();
            cameraclick.setBackgroundResource(R.drawable.captureimage);
            Toast toast=Toast.makeText(this," Recording Saved !",Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER_HORIZONTAL,0,0);
            toast.getView().setBackgroundColor(0xFFFFFF);
            toast.show();
            islongpressed=false;
            setrecinvisibility();
            startrecording();
        }

    }

    private void setrecinvisibility()
    {
        recordercardview.setVisibility(View.INVISIBLE);
    }
    private void setrecvisibility()
    {
        recordercardview.setVisibility(View.VISIBLE);
    }

    public void goBack(View view)
    {
        Intent intent1 = new Intent(this, MainActivity.class);
        startActivity(intent1);
    }
}
