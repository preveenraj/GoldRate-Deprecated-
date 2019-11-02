package com.techrush.goldrate;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.StrictMode;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.facebook.ads.*;

public class MainActivity extends AppCompatActivity {

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

        //method to load the advertisment banner
        loadAd();

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
                    date_status.setText("TODAY");
                }else{
                    int dayFromLocal =Integer.parseInt(new SimpleDateFormat("dd").format(dateFromLocal));
                    int curDay =Integer.parseInt(new SimpleDateFormat("dd").format(curDate));
                    dStatusContainer.setBackgroundResource(R.drawable.rounded_red);
                    if((curDay-dayFromLocal)==1) {
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

    private void loadAd() {

        //Facebook Ads
        // Initialize the Audience Network SDK
        AudienceNetworkAds.initialize(this);

        adView = new AdView(this, "2524690977767175_2524692101100396", AdSize.BANNER_HEIGHT_50);
//      AdSettings.addTestDevice("7a098a31-9628-4bb1-aa77-013fcdc41a17");       //REMOVE THIS WHEN DEPLOYING TO PLAY STORE

        // Find the Ad Container
        LinearLayout adContainer = (LinearLayout) findViewById(R.id.banner_container);

        // Add the ad view to your activity layout
        adContainer.addView(adView);

        // Request an ad
        adView.loadAd();


    }

    public void OnClick(View view) {

        updatePage();

    }


    public void updatePage(){

        final String time = new SimpleDateFormat("HHmm").format(Calendar.getInstance().getTime());
        int timeint = Integer.parseInt(time);

        if(timeint<=1030) {
            Toast.makeText(this,"Try after 10:30 am", Toast.LENGTH_SHORT).show();
            mySwipeRefreshLayout.setRefreshing(false);
        } else {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final Document htmldata = Jsoup.connect("https://www.keralagold.com/kerala-gold-rate-per-gram.htm").get();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                datastring = htmldata.toString();
                                Elements tabledata = htmldata.select("table[width='280'][cellspacing='0']");
                                String reqData = tabledata.text();
//                                Log.i("reqData",reqData);
                                ratestring = reqData.substring(reqData.lastIndexOf("Rs. "),reqData.lastIndexOf("Rs. ")+8);
//                                Log.i("ratestring",ratestring);
                                String tempStringforDate = reqData.substring(reqData.lastIndexOf("Today")-16,reqData.lastIndexOf("Today")-1).trim();
                                tempStringforDate = tempStringforDate.substring(tempStringforDate.indexOf("-")-2,tempStringforDate.lastIndexOf("-")+3);
//                                Log.i("tempData","yoyo"+tempStringforDate+"oyoy");
                                goldrate_gram.setText(ratestring);
                                String ratePerGram = ratestring.substring(4);
//                                Log.i("ratePergram",ratePerGram );
                                int ratePerPavan = Integer.parseInt(ratePerGram)*8;
                                goldrate_pavan.setText("Rs. "+ currencyFormatter.format(ratePerPavan));
                                timeStamp = tempStringforDate;

                                SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yy");
                                try {
                                    Date tempDate = formatter.parse(timeStamp);
                                    String curStringDate = formatter.format(new Date());
                                    Date curdate = formatter.parse(curStringDate);
                                    timeStamp = new SimpleDateFormat("dd MMMM yyyy").format(tempDate);
                                    Log.d("timestamp",tempDate.toString());
                                    Log.d("curdate",tempDate.toString());

                                    if(tempDate.compareTo(curdate)==0)  {
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

                                mySwipeRefreshLayout.setRefreshing(false);
                            }
                        });
                    } catch (Throwable e) {
                        e.printStackTrace();
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
}