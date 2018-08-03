package android.cs.sprint18.meowfest;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

/**
 * A simple cat image upload and scroll feed
 **/
public class MainActivity extends AppCompatActivity {

    private class ImageAsyncTask extends AsyncTask<String, Void, ArrayList<Drawable>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        //Gets images from the two urls, then adds to an array and returns
        @Override
        protected ArrayList<Drawable> doInBackground (String... urls)  {
            ArrayList<Drawable> images=new ArrayList<Drawable>();
            images.add(getURLImage(urls[0]));
            images.add(getURLImage(urls[1]));
            return images;
        }

        private Drawable getURLImage(String url){
            try {
                String[] splitUrl=url.split("/");
                Log.d("BAKA", "1");
                String name=splitUrl[splitUrl.length-1];
                Log.d("BAKA", "2");
                InputStream is =  (InputStream)new URL(url).getContent();
                Log.d("Cont type: ",""+is.getClass());
                Log.d("BAKA", "3");
                //Must be name of drawable
                Drawable d = Drawable.createFromStream(is, name);
                Log.d("BAKA", "4");
                return d;
            } catch (Exception e) {
                Log.d("Couldn't get url im:", "true");
                return null;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<Drawable> result) {
            MainActivity.this.onImagesObtained(result);
            hideProgressDialog();
        }
    }

    public class JSONAsyncTask extends AsyncTask<String, Void, String> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground (String... urls)  {
            Log.d("In load stream method", "true");
            try {
                Log.d("In try block", "true");
                return getJSONData();
            }
            catch (IOException e) {
                Log.d("IO Exception", "true");
                e.printStackTrace();
            }
            return "Try block failed";
        }

        @Override
        protected void onPostExecute(String result) {
            showProgressDialog();
            MainActivity.this.onBackgroundTaskDataObtained(result);
        }

        private String getJSONData() throws IOException {
            //Object jsonURLContent = new URL(feedUrl).getContent();

            URL JSONUrl = new URL("https://chex-triplebyte.herokuapp.com/api/cats?page=0");
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            JSONUrl.openStream()));

            String finalJSON="";
            String inputLine;

            while ((inputLine = in.readLine()) != null){
                //In this case, we get all JSON in just one line
                finalJSON=inputLine;
            }


            in.close();
            return finalJSON;
        }

    }

    //Null until instantiated
    JSONArray mJSON;

    //The order of the top cat in the JSON Array:
    int topCatIndex=0;


    TextView dateV1;
    TextView dateV2;

    TextView titleV1;
    TextView titleV2;

    TextView descripView1;
    TextView descripView2;

    ImageView imV1;
    ImageView imV2;

    float y1,y2;

    boolean onFirst=true;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("In main method", "True");
        new JSONAsyncTask().execute();


        dateV1=findViewById(R.id.date1View);
        dateV2=findViewById(R.id.date2View);

        titleV1=findViewById(R.id.title1View);
        titleV2=findViewById(R.id.title2View);

        descripView1=findViewById(R.id.descrip1View);
        descripView2=findViewById(R.id.descrip2View);

        imV1=findViewById(R.id.im1View);
        imV2=findViewById(R.id.im2View);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event){

        int action = MotionEventCompat.getActionMasked(event);


        String DEBUG_TAG="Motion:";
        switch(action) {
            case (MotionEvent.ACTION_DOWN):
                Log.d(DEBUG_TAG, "Action was DOWN");
                y1 = event.getY();
                return true;

            case (MotionEvent.ACTION_UP) :
                Log.d(DEBUG_TAG,"Action was UP");
                try {
                    y2=event.getY();
                    if(topCatIndex+2>=mJSON.length()&&y2<y1){
                        hideProgressDialog();
                    }
                    else {
                        if (y2 < y1 && (topCatIndex < mJSON.length())) {
                            scrollDown();
                        } else if (y1 < y2 && topCatIndex > 1) {
                            scrollUp();
                        } else if (onFirst) {
                            onFirst = false;
                        }
                        //Reached bottom

                        else {
                            Log.d("Top ind:", "" + topCatIndex);
                            Log.d("JSON L:", "" + mJSON.length());
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return true;

            default :
                return super.onTouchEvent(event);
        }
    }

    protected void onImagesObtained(ArrayList<Drawable> images){
        imV1.setImageDrawable(resize(images.get(0)));
        imV2.setImageDrawable(resize(images.get(1)));
    }
    protected void onBackgroundTaskDataObtained(String results) {
        //do stuff with the results here..
        try{
            mJSON=new JSONArray(results);
            setCurrentViews();

        }
        catch(JSONException E){
            Log.d("JSON Exception", "L 191 main");
        }
    }
    //Adds the progress dialog to the center of the blue bar
    private void showProgressDialog(){
        ImageView progressView=(ImageView)findViewById(R.id.loadView);
        progressView.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.load_indicator));
    }
    private void hideProgressDialog(){
        ImageView progressView=(ImageView)findViewById(R.id.loadView);
        progressView.setImageDrawable(null);
    }
    private JSONObject getCatX(int catIndex) throws JSONException {
        return (JSONObject)mJSON.get(catIndex);
    }
    private String formatDate(String oldTimestamp){
        String dateAftYear=oldTimestamp.substring(4);
        String year=oldTimestamp.substring(0,4);
        return year+dateAftYear.substring(0,dateAftYear.indexOf('T')).replace('-','/');
    }
    private Drawable resize(Drawable image) {
        int newWidth;
        int newHeight;
        //Keeps image width and height in the 180dp width, 100dp height box
        Bitmap b = ((BitmapDrawable)image).getBitmap();
        if(b.getWidth()>150){
            newWidth=150;
        }
        else{
            newWidth=b.getWidth();
        }
        if(b.getHeight()>100){
            newHeight=100;
        }
        else{
            newHeight=b.getHeight();
        }
        Bitmap bitmapResized = Bitmap.createScaledBitmap(b, newWidth, newHeight, false);
        return new BitmapDrawable(getResources(), bitmapResized);
    }

    //Sets current ccat views on the page:
    private void setCurrentViews() throws JSONException {
        JSONObject catObj1=getCatX(topCatIndex);
        JSONObject catObj2=getCatX(topCatIndex+1);

        String date1=catObj1.getString("timestamp");
        String date2=catObj2.getString("timestamp");

        dateV1.setText(formatDate(date1));
        dateV2.setText(formatDate(date2));

        titleV1.setText(catObj1.getString("title"));
        titleV2.setText(catObj2.getString("title"));

        descripView1.setText(catObj1.getString("description"));
        descripView2.setText(catObj2.getString("description"));

        String im1URL=catObj1.getString("image_url");
        String im2URL=catObj2.getString("image_url");

        String[] urlHolder=new String[]{im1URL,im2URL};

        new ImageAsyncTask().execute(urlHolder);
        showProgressDialog();
    }

    private void scrollUp() throws JSONException {
        if(topCatIndex>=2) {
            topCatIndex -= 2;
            setCurrentViews();
        }
    }

    private void scrollDown() throws JSONException {
        if(mJSON.length()-1>=topCatIndex+2) {
            topCatIndex += 2;
            setCurrentViews();
        }
    }
}













