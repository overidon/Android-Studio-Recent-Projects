package com.overidon.sportsteams;

/*

    PROJECT: SportsTeams
    PURPOSE: Case Study and RecyclerView / JSON practice
    GOAL: Make app that extracts sports information from API / database and display information

 */

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "TAG: ";



    private ArrayList<String> mNames = new ArrayList<>();
    private ArrayList<String> mImageUrls = new ArrayList<>();

    // this will hold the team objects...
    private ArrayList<SportsTeam> teamList = new ArrayList<>();

    // This is the free API key... I would of course use a Patreon API Key if the users of this app scaled.
    final String APIKEY = "1";

    private String url ;

    private RequestQueue mRequestQueue;
    private StringRequest mStringRequest;



    private EditText dataEntry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dataEntry = findViewById(R.id.data_entry);


        Log.d(TAG, "Main Activity was Created... ");


        dataEntry.setImeOptions(EditorInfo.IME_ACTION_DONE);

        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(dataEntry.getWindowToken(), 0);

        dataEntry.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if(id == EditorInfo.IME_ACTION_DONE){

                    Log.d(TAG, "action one... " + dataEntry.getText());

                    url = String.format("https://www.thesportsdb.com/api/v1/json/1/searchteams.php?t=%s", dataEntry.getText() );


                    // attempt to acquire raw JSON data...
                    acquireTeams();



                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(dataEntry.getWindowToken(), 0);

                    // Your action on done
                    return true;
                }
                return false;
            }


        });






        //initImageBitmaps();


    // end of the onCreate method
    }


    private void acquireTeams() {



        //there must be a request Queue ...
        mRequestQueue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            teamList.clear();


                            // store the teams in a JSON array...
                            JSONArray teams = response.getJSONArray("teams");

                            // loop through the JSON Array to acquire the teams..
                            for (int i = 0; i < teams.length(); i++) {

                                String teamString = teams.get(i).toString();

                                Gson g = new Gson();
                                SportsTeam team0 = g.fromJson(teamString, SportsTeam.class);

                                teamList.add(team0);

                            // end of the for loop for the JSON Array
                            }


                            mImageUrls.clear();
                            mNames.clear();

                            // now that the for loop is completed inside the try
                            // it is safe to initialize the sportsTeams
                            // this is better than doing another request or ASync
                            initSportsTeams();

                        } catch (Exception e) {
                            Log.d(TAG, "### Error Extracting team data ...");
                            Log.d(TAG, "ERROR DETAILS: "+ e);
                        }


                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error

                    }
                });

        mRequestQueue.add(jsonObjectRequest);
    }

    private static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;
        DownloadImageTask(ImageView bmImage) {

            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    /*
    private void sendAndRequestResponse() {

        //RequestQueue initialized
        mRequestQueue = Volley.newRequestQueue(this);

        //String Request initialized
        mStringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                String data = response;

                Log.i(TAG,"Positive Response is: " + data);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.i(TAG,"Error Response is: " + error.toString());
            }
        });

        mRequestQueue.add(mStringRequest);
    }
    */



    private void initSportsTeams() {
        Log.d(TAG, "initImageBitmaps: preparing teams.");

        for (int i = 0; i < teamList.size(); i++) {

            SportsTeam S = teamList.get(i);

            mImageUrls.add(S.strTeamBadge);
            mNames.add(S.strAlternate);

        }

        Log.d(TAG, "the size of the teamList is: " + teamList.size());

        initRecyclerView();

    // end of the initSportsTeams method
    }




    private void initRecyclerView() {

        Log.d(TAG, "initRecyclerView: init recyclerview. ");
        RecyclerView recyclerView = findViewById(R.id.recycler_view);

        RecyclerViewAdapter adapter = new RecyclerViewAdapter(this, mNames, mImageUrls);

        recyclerView.setAdapter(adapter);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

// end of the MainActivity class
}
