package com.example.ideveloper.sunshine;

/**
 * Created by iDeveloper on 2/28/15.
 */

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public  class ForecastFragment extends Fragment {

    private ArrayAdapter<String> mForecastAdapter;
    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id==R.id.action_refresh){
            FetchWeatherTask weatherTask= new FetchWeatherTask();
            weatherTask.execute("20903");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        String[] foreCastArray= {
                "Today - Sunny - 88/63",
                "Tomorrow - Foggy - 70/46",
                "Weds - Cloudy - 72/63",
                "Thurs - Asteroids - 75/65",
                "Fri - Heavy Rain -65/66",
                "Sat - HELP TRAPPED IN WEATHERSTATION - 60/51",
                "Sun - Sunny - 80/68"
        };

        List<String> weekForeCast=new ArrayList<String>(Arrays.asList(foreCastArray));



        mForecastAdapter=new ArrayAdapter<String>(
                getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                weekForeCast);

        ListView listView=(ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mForecastAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String forecast= mForecastAdapter.getItem(position);
                //Toast.makeText(getActivity(),forecast,Toast.LENGTH_SHORT).show();
                Intent intent= new Intent(getActivity(),DetailActivity.class).putExtra(Intent.EXTRA_TEXT,forecast);
                startActivity(intent);
            }
        });

        return rootView;
    }

    private class FetchWeatherTask extends AsyncTask<String,Void,String[]>
    {
        private final String LOG_TAG=FetchWeatherTask.class.getSimpleName();

        protected String[] doInBackground(String... params) {
            if(params.length==0){
                return null;
            }
            HttpURLConnection urlConnection=null;
            BufferedReader reader= null;
            String[] strArray={};
            //will contain the raw json response as a string.
            String forecastJsonStr=null;
            String format="json";
            String units="metric";
            int numDays=7;

            try
            {
                //Construct the URL for thr OpenWeatherMap query
                //possible parameters are available at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast

                final String FORECAST_BASE_URL="http://api.openweathermap.org/data/2.5/forecast/daily";
                final String QUERY_PARAM="q";
                final String FORMAT_PARAM="mode";
                final String UNITS_PARAM="units";
                final String DAYS_PARAM="cnt";

                Uri builtUri= Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM,params[0])
                        .appendQueryParameter(FORMAT_PARAM,format)
                        .appendQueryParameter(UNITS_PARAM,units)
                        .appendQueryParameter(DAYS_PARAM,Integer.toString(numDays)).build();
                URL url= new URL(builtUri.toString());
                Log.v(LOG_TAG,"Built URI "+builtUri.toString());
                urlConnection=(HttpURLConnection)url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();




                //read the input stream into a string
                InputStream inputStream= urlConnection.getInputStream();
                StringBuffer buffer= new StringBuffer();

                if(inputStream==null){
                    //nothing to do.
                    forecastJsonStr=null;

                }

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;

                while((line=reader.readLine())!=null )
                {
                    //Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    buffer.append(line +"\n");
                }

                if(buffer.length()==0)
                {
                    forecastJsonStr=null;
                }

                forecastJsonStr=buffer.toString();
                Log.e(LOG_TAG, forecastJsonStr);

                try{
                    WeatherDataParser d= new WeatherDataParser();
                    strArray =d.getWeatherDataFromJson( forecastJsonStr,numDays);
                }catch (JSONException e){
                    Log.e(LOG_TAG,e.getMessage(),e);
                    e.printStackTrace();
                }
            }
            catch (IOException e)
            {
                Log.e(LOG_TAG, "Error", e);
                forecastJsonStr=null;
            }
            finally {

                if(urlConnection !=null){
                    urlConnection.disconnect();
                }

                if(reader !=null){
                    try{
                        reader.close();

                    }catch (final IOException e){
                        Log.e(LOG_TAG,"Error CLosing stream",e);
                    }
                }

            }
return strArray;
        }

        @Override
        protected void onPostExecute(String[] result) {
           if(result !=null){
               mForecastAdapter.clear();
               for (String dayForecastStr:result){
                   mForecastAdapter.add(dayForecastStr);
               }
           }
        }
    }
}
