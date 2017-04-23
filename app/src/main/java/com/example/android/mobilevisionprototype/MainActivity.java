package com.example.android.mobilevisionprototype;

import android.app.Activity;
import android.support.v4.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Scanner;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.*;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
                                    AdapterView.OnItemSelectedListener,
                                    CreateEventDialogFragment.CreateEventDialogListener{

    ImageView mImageView;
    Button mProcessButton;
    ArrayList<String> drawablePicList;
    Spinner spinner;

    String currentDrawableResource = null;
    int currentDrawableResourceID = 0;

    final String TAG = "MV";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = (ImageView) findViewById(R.id.image);
        mProcessButton = (Button) findViewById(R.id.button);
        spinner = (Spinner) findViewById(R.id.spinner);

        mProcessButton.setOnClickListener(this);

        drawablePicList = getPics();
        for(String picName: drawablePicList)    {
            Log.v(TAG, "Picture Resource Name: " + picName);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),
                                        android.R.layout.simple_spinner_item,
                                        drawablePicList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        currentDrawableResource = drawablePicList.get(1);
        currentDrawableResourceID = getResources()
                                    .getIdentifier(currentDrawableResource, "id", getPackageName());
        Glide.with(this)
                .load(currentDrawableResourceID)
                .centerCrop()
                .into(mImageView);
    }

    private ArrayList<String> getPics() {
        Field[] drawables = R.drawable.class.getFields();
        ArrayList<String> drawableResources = new ArrayList<>();

        for (Field drawableResourceField: drawables)    {
            try {
                String drawableResourceName = drawableResourceField.getName();

                if (drawableResourceName.startsWith("se_pic"))   {
                    drawableResources.add(drawableResourceName);
                }
            } catch (Exception E)   {
                Log.v(TAG, "getPics() EXCEPTION: " + E);
            }
        }
        return drawableResources;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId())  {
            case R.id.button:
                Toast.makeText(this, "Process button clicked!", Toast.LENGTH_SHORT).show();
                detectText();
//                showCreateEventDialog(detectText());
                break;
        }
    }

    void showCreateEventDialog(String message)  {
        DialogFragment newFragment = new CreateEventDialogFragment(message);
        newFragment.show(getSupportFragmentManager(), "CEDF");
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        Log.v(TAG, "Positive click in MainActivity!");
        Toast.makeText(getApplicationContext(), "Create Event!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        Log.v(TAG, "Negative click in MainActivity!");
        Toast.makeText(getApplicationContext(), "Cancelled!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        currentDrawableResource = drawablePicList.get((int) id);
        currentDrawableResourceID = getResources()
                .getIdentifier(currentDrawableResource, "drawable", getPackageName());

        Log.v(TAG, " onItemSelected: ID: " + id);
        Log.v(TAG, " onItemSelected: Pos: " + position);
        Log.v(TAG, " onItemSelected: currentDrawableResource: " + currentDrawableResource);

        Glide.with(this)
                .load(currentDrawableResourceID)
                .centerCrop()
                .into(mImageView);

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public String detectText() {
        TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();

        if(!textRecognizer.isOperational()) {
            Log.w(TAG, "Detector dependencies are not yet available!");

            IntentFilter lowStorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowStorageFilter) != null;

            if (hasLowStorage)  {
                Toast.makeText(this, "Low storage! ", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Low storage!");
            }
        }

        Bitmap bitmap = ((GlideBitmapDrawable)mImageView.getDrawable().getCurrent()).getBitmap();
        Frame frame = new Frame.Builder().setBitmap(bitmap).build();

        SparseArray<TextBlock> textBlockSparseArray = textRecognizer.detect(frame);

        String detectedText = "";
        for (int i = 0; i < textBlockSparseArray.size(); i++) {
            TextBlock textBlock = textBlockSparseArray.valueAt(i);
            detectedText += textBlock.getValue();
            Log.v(TAG, " Text! " + textBlock.getValue());
        }

        // getDate method looks for months and extract the line that contains the month
        getDate(detectedText);

//        Toast.makeText(this, detectedText, Toast.LENGTH_LONG).show();
        return detectedText;
    }

    // getDate method looks for months and extract the line that contains the month
    public String getDate(String lineText) {
        Scanner scanner = new Scanner(lineText);
        String line = "";
        while (scanner.hasNext()) {
            line = scanner.nextLine();
            if (line.contains("Jan")) {
                Log.v(TAG, " Date: " + line);
                setUpEvent(line, 1);
            }
            else if (line.contains("Feb")) {
                Log.v(TAG, " Date: " + line);
                setUpEvent(line, 2);
            }
            else if (line.contains("Mar")) {
                Log.v(TAG, " Date: " + line);
                setUpEvent(line, 3);
            }
            else if (line.contains("Apr")) {
                Log.v(TAG, " Date: " + line);
                setUpEvent(line, 4);
            }
            else if (line.contains("May")) {
                Log.v(TAG, " Date: " + line);
                setUpEvent(line, 5);
            }
            else if (line.contains("Jun")) {
                Log.v(TAG, " Date: " + line);
                setUpEvent(line, 6);
            }
            else if (line.contains("Jul")) {
                Log.v(TAG, " Date: " + line);
                setUpEvent(line, 7);
            }
            else if (line.contains("Aug")) {
                Log.v(TAG, " Date: " + line);
                setUpEvent(line, 8);
            }
            else if (line.contains("Sept")) {
                Log.v(TAG, " Date: " + line);
                setUpEvent(line, 9);
            }
            else if (line.contains("Oct")) {
                Log.v(TAG, " Date: " + line);
                setUpEvent(line, 10);
            }
            else if (line.contains("Nov")) {
                Log.v(TAG, " Date: " + line);
                setUpEvent(line, 11);
            }
            else if (line.contains("Dec")) {
                Log.v(TAG, " Date: " + line);
                setUpEvent(line, 12);
            }

        }
        return line;
    }

    public void setUpEvent(String date, int y) {
        String splitWords = date;
        String[] strArray = splitWords.split(" ");

        int x = Integer.parseInt(strArray[0]);
        //int y = Integer.parseInt(strArray[1]);
        int z = Integer.parseInt(strArray[2]);

        Log.v(TAG, x + "\n");
        Log.v(TAG, y + "\n");
        Log.v(TAG, z + "\n");

        Calendar startTime = Calendar.getInstance();
        startTime.set(z, y, x, 8, 0);

        Calendar endTime = Calendar.getInstance();
        endTime.set(z, y, x, 9, 0);

        long eventTime1 = startTime.getTimeInMillis();
        long eventTime2 = endTime.getTimeInMillis();

        String description = "Event Description";
        Intent intent = new Intent(Intent.ACTION_EDIT);
        intent.setType("vnd.android.cursor.item/event");
        intent.putExtra("beginTime", eventTime1);
        intent.putExtra("endTime", eventTime2);//+60*60*1000);
        intent.putExtra("title", "A Test Event from android app");
        intent.putExtra("description", description);
        intent.putExtra("eventLocation", "ECE Building");
        startActivity(intent);
    }

}

