package com.techrush.goldrate;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;

import android.graphics.drawable.Drawable;
import android.os.StrictMode;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/*import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;*/

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.facebook.ads.*;

public class MainActivity extends AppCompatActivity {


//    private AdView mAdView;

    TextView goldrate_gram,goldrate_pavan,date,date_status;
    String datastring,datestring,timeStamp,ratestring;
    SwipeRefreshLayout mySwipeRefreshLayout;
    RelativeLayout dStatusContainer;
    DecimalFormat currencyFormatter = new DecimalFormat("##,###");


    private AdView adView;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Facebook Ads
        // Initialize the Audience Network SDK
        AudienceNetworkAds.initialize(this);

        adView = new AdView(this, "2524690977767175_2524692101100396", AdSize.BANNER_HEIGHT_50);
//        AdSettings.addTestDevice("7a098a31-9628-4bb1-aa77-013fcdc41a17");       //REMOVE THIS WHEN DEPLOYING TO PLAY STORE

        // Find the Ad Container
        LinearLayout adContainer = (LinearLayout) findViewById(R.id.banner_container);

        // Add the ad view to your activity layout
        adContainer.addView(adView);

        // Request an ad
        adView.loadAd();




//        mAdView = (AdView) findViewById(R.id.adView);
//        AdRequest adRequest = new AdRequest.Builder()
//                .setRequestAgent("android_studio:ad_template").build();
//        mAdView.loadAd(adRequest);




        goldrate_gram = (TextView) findViewById(R.id.goldrate_gram);
        goldrate_pavan = (TextView) findViewById(R.id.goldrate_pavan);

        date = (TextView) findViewById(R.id.date);
        date_status = (TextView) findViewById(R.id.date_status);
        dStatusContainer = (RelativeLayout) findViewById(R.id.dStatus_container);


        SharedPreferences sharedPreferences = getSharedPreferences("GOLD",MODE_PRIVATE);
        ratestring = sharedPreferences.getString("goldrate","");
        datestring = sharedPreferences.getString("date"," ");
        String ratePerGram;
        if(ratestring!="") {
            goldrate_gram.setText(ratestring);
            try {
                ratePerGram = ratestring.substring(4); //,ratestring.length()-4);
                int ratePerPavan = Integer.parseInt(ratePerGram) * 8;
                goldrate_pavan.setText("Rs. " + currencyFormatter.format(ratePerPavan));
                date.setTextColor(Color.parseColor("#2c3e50"));
                date.setText(datestring);
            } catch (Exception e) {
                ratePerGram = ratestring.substring(4, ratestring.length() - 4);
                int ratePerPavan = Integer.parseInt(ratePerGram) * 8;
                goldrate_pavan.setText("Rs. " + currencyFormatter.format(ratePerPavan));
                date.setTextColor(Color.parseColor("#2c3e50"));
                date.setText(datestring);
            }
        }else{
            date_status.setText("Please Refresh");
            dStatusContainer.setBackgroundResource(R.drawable.rounded_red);
        }

        if(!datestring.equals(" ")) {
            try {
                SimpleDateFormat localFormatter = new SimpleDateFormat("dd MMMM yyyy");
                Date dateFromLocal = localFormatter.parse(datestring);
                String curLocalStringDate = localFormatter.format(new Date());
                Date curDate = localFormatter.parse(curLocalStringDate);
                if (dateFromLocal.compareTo(curDate) == 0) {
//                    date.setTextColor(Color.parseColor("#2ed573"));
//                    date.setTypeface(date.getTypeface(),Typeface.BOLD);
                    date_status.setText("TODAY");
                }else{
                    int dayFromLocal =Integer.parseInt(new SimpleDateFormat("dd").format(dateFromLocal));
                    int curDay =Integer.parseInt(new SimpleDateFormat("dd").format(curDate));
                    dStatusContainer.setBackgroundResource(R.drawable.rounded_red);
                    if((curDay-dayFromLocal)==1) {
//                        date.setTextColor(Color.parseColor("#ff6348"));
                        date_status.setText("YESTERDAY");
                        }else{
                        date_status.setText("LONG AGO");
                    }
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }


        mySwipeRefreshLayout = findViewById(R.id.swiperefresh);
        int color = getResources().getColor(R.color.colorPrimary);
        mySwipeRefreshLayout.setColorSchemeColors(color);
        mySwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        updatePage();
                    }
                }
        );



    }

    public void OnClick(View view) {

        updatePage();

    }


    public void updatePage(){

        String time = new SimpleDateFormat("HHmm").format(Calendar.getInstance().getTime());
        // Log.i("time: ",time);

        int timeint = Integer.parseInt(time);


        if(timeint<=1030)
        {
            Toast.makeText(this,"Try after 10:30 am", Toast.LENGTH_SHORT).show();
            mySwipeRefreshLayout.setRefreshing(false);

        }

        else {


            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

            StrictMode.setThreadPolicy(policy);


//            final ProgressDialog progress = new ProgressDialog(this);
//            progress.setMessage("Wait... ");
//            progress.setCancelable(false);


            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {

                        final Document htmldata = Jsoup.connect("https://www.keralagold.com/kerala-gold-rate-per-gram.htm").get();


                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                datastring = htmldata.toString();


                                ratestring = datastring.substring(datastring.indexOf("Today")+7);
                                Log.i("rate ",ratestring);

                                ratestring = ratestring.substring(ratestring.indexOf("Today"));
                                Log.i("rate ",ratestring);

                                ratestring = ratestring.substring(ratestring.indexOf("Rs. ")+5);
                                Log.i("rate ",ratestring);

                                String tempStringforDate = ratestring;
                                Log.i("rate ",ratestring);

                                ratestring = ratestring.substring(ratestring.indexOf("Rs. "));
                                Log.i("rate ",ratestring);

                                ratestring = ratestring.substring(ratestring.indexOf("Rs. "),ratestring.indexOf("<")); //+ " /gm";
                                Log.i("rate ",ratestring);

                                //    ratestring = datastring.substring(datastring.indexOf("<font color=\"#C00000\">Today ") + 95, datastring.indexOf("<font color=\"#C00000\">Today ") + 103) + " /gm";
//                                Log.i("datastring: ",datastring);


                                goldrate_gram.setText(ratestring);

                                String ratePerGram = ratestring.substring(4); //,ratestring.length()-4);
                                int ratePerPavan = Integer.parseInt(ratePerGram)*8;
                                goldrate_pavan.setText("Rs. "+ currencyFormatter.format(ratePerPavan));

                                timeStamp = tempStringforDate.substring(tempStringforDate.indexOf("kg2b") +6, tempStringforDate.indexOf("<br><font color=\"#C00000\">Today "));


                                //timeStamp = new SimpleDateFormat("dd-MM-yyyy").format(Calendar.getInstance().getTime());
                                SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yy");
                                try {
                                    Date tempDate = formatter.parse(timeStamp);
                                    String curStringDate = formatter.format(new Date());
                                    Date curdate = formatter.parse(curStringDate);
                                    timeStamp = new SimpleDateFormat("dd MMMM yyyy").format(tempDate);
                                    Log.d("timestamp",tempDate.toString());
                                    Log.d("curdate",tempDate.toString());


                                    if(tempDate.compareTo(curdate)==0)  {
//                                        dStatusContainer.setBackgroundColor(Color.parseColor("#2ED573"));
                                          dStatusContainer.setBackgroundResource(R.drawable.rounded_green);
                                        date_status.setText("TODAY");
                                    }
                                    date.setText(timeStamp);



                                } catch (ParseException e) {
                                    Toast.makeText(MainActivity.this, "Unparsable Date", Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();

                                }




                                SharedPreferences sharedPreferences = getSharedPreferences("GOLD", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("goldrate", ratestring);
                                editor.putString("date", timeStamp);
                                editor.commit();


//                                progress.dismiss();
                                mySwipeRefreshLayout.setRefreshing(false);


                            }
                        });


                    } catch (Throwable e) {

                        e.printStackTrace();

//                        progress.dismiss();
                        mySwipeRefreshLayout.setRefreshing(false);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "NO INTERNET", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                }
            });
            t.start();
//            progress.show();
            mySwipeRefreshLayout.setRefreshing(true);



        }

    }


    @Override
    protected void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
    }


/*RELATED TO GOOGLE ADS
    @Override
    public void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }

    */
}