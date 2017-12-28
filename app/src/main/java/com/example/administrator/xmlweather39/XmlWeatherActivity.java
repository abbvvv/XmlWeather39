package com.example.administrator.xmlweather39;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Xml;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import android.widget.LinearLayout.LayoutParams;

public class XmlWeatherActivity extends AppCompatActivity{
    HttpURLConnection httpURLConnection = null;
    ArrayList<WeatherInf> weatherInfs = new ArrayList<>();
    String cityname = "广州";
    private EditText mCityname;
    private Button mFind;
    private LinearLayout mShow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xml_weather);
        setTitle("天气查询XML");
        mCityname = (EditText) findViewById(R.id.cityname);
        mFind = (Button) findViewById(R.id.search);
        mShow = (LinearLayout) findViewById(R.id.show_weather);
        mFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mShow.removeAllViews();
                cityname = mCityname.getText().toString();
                Toast.makeText(XmlWeatherActivity.this,"正在查询天气信息...",Toast.LENGTH_LONG).show();
                GetXml gx = new GetXml(cityname);
                gx.start();
            }
        });
    }
    private final Handler handler = new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case 1:
                    show();
                    break;
            }
            super.handleMessage(msg);
        }
    };
    class GetXml extends Thread{
        private String urlstr =  "http://wthrcdn.etouch.cn/WeatherApi?city=";
        public GetXml(String cityname){
            try{
                urlstr = urlstr+ URLEncoder.encode(cityname,"UTF-8");
            }catch (Exception ee){
                ee.printStackTrace();
            }
        }

        @Override
        public void run() {
            for(int i = 0; i<weatherInfs.size();i++){
                weatherInfs.clear();
            }
            InputStream din = null;
            try{
                URL url = new URL(urlstr);
                httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                din = httpURLConnection.getInputStream();
                XmlPullParser xmlPullParser = Xml.newPullParser();
                xmlPullParser.setInput(din,"UTF-8");
                WeatherInf pw = null;
                M m = null;
                int eveType = xmlPullParser.getEventType();
                while(eveType != XmlPullParser.END_DOCUMENT){
                    if(eveType == XmlPullParser.START_TAG){
                        String tag = xmlPullParser.getName();
                        if(tag.equalsIgnoreCase("weather")){
                            pw = new WeatherInf();
                        }
                        if(tag.equalsIgnoreCase("date")){
                            if(pw != null){
                                pw.date = xmlPullParser.nextText();
                            }
                        }
                        if(tag.equalsIgnoreCase("high")){
                            if(pw != null){
                                pw.high = xmlPullParser.nextText();
                            }
                        }
                        if(tag.equalsIgnoreCase("low")){
                            if(pw != null){
                                pw.low = xmlPullParser.nextText();
                            }
                        }
                        if(tag.equalsIgnoreCase("day")){
                            m = new M();
                        }
                        if(tag.equalsIgnoreCase("night")){
                            m = new M();
                        }
                        if(tag.equalsIgnoreCase("type")){
                            if(m != null){
                                m.type = xmlPullParser.nextText();
                            }
                        }
                        if(tag.equalsIgnoreCase("fengxiang")){
                            if(m != null){
                                m.fengxiang = xmlPullParser.nextText();
                            }
                        }
                        if(tag.equalsIgnoreCase("fengli")){
                            if(m != null){
                                m.fengli = xmlPullParser.nextText();
                            }
                        }
                    }
                    else if(eveType == XmlPullParser.END_TAG){
                        String tag = xmlPullParser.getName();
                        if (tag.equalsIgnoreCase("weather")){
                            weatherInfs.add(pw);
                            pw = null;
                        }
                        if(tag.equalsIgnoreCase("day")){
                            pw.day = m;
                            m = null;
                        }
                        if(tag.equalsIgnoreCase("night")){
                            pw.night = m;
                            m = null;
                        }
                    }
                    eveType = xmlPullParser.next();
                }
                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
    public void show(){
        mShow.removeAllViews();
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
        for(int i = 0; i<weatherInfs.size();i++){
            TextView dateView = new TextView(this);
            dateView.setGravity(Gravity.CENTER_HORIZONTAL);
            dateView.setLayoutParams(params);
            dateView.setBackgroundColor(getResources().getColor(R.color.primary));
            dateView.setText("日期："+weatherInfs.get(i).date);
            mShow.addView(dateView);
            TextView mView = new TextView(this);
            mView.setLayoutParams(params);
            String str = "高温：" + weatherInfs.get(i).high+",低温：" + weatherInfs.get(i).low + "\n";
            str = str + "白天：" + weatherInfs.get(i).day.inf() + "\n";
            str = str + "夜间：" +weatherInfs.get(i).night.inf();
            mView.setText(str);
            mShow.addView(mView);
        }

    }

}
