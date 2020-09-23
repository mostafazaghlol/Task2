package com.mostafazaghloul.task2;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.mostafazaghloul.task2.Volley.githubRepo;
import com.mostafazaghloul.task2.adapter.MyAdapter;
import com.mostafazaghloul.task2.model.Repo;
import com.mostafazaghloul.task2.model.repodata.data;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity {
    List<data> output;
    private RecyclerView recyclerView;
    private MyAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private int i = 1;
    ProgressDialog progressDialog;
    SharedPreferences mSharedPreferences;
    static SharedPreferences.Editor editor;
    SwipeRefreshLayout mSwipeRefreshLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        showprogress();
        prepareSharedPref();
        prepareSwipRefresh();
        prepareRecyler();
        prepareData(false);
        ReachTheEndOfRecycler();
        NotificationAndSchedualed();
        SearchKeyWord();
    }

    private void SearchKeyWord() {
        EditText editText = findViewById(R.id.edittext);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString());
            }
        });
    }
    private void filter(String text) {
        ArrayList<data> filteredList = new ArrayList<>();
        for (data item : output) {
            if (item.getTitle().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }
        mAdapter.filterList(filteredList);
    }
    private void NotificationAndSchedualed() {
        ScheduledExecutorService scheduler =
                Executors.newSingleThreadScheduledExecutor();
        final NotificationManager mNotificationManager;
        final NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getApplicationContext(), "notify_001");
        Intent ii = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, ii, 0);
        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        bigText.bigText("New Data Here !");
        bigText.setBigContentTitle("Task2");
        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher_round);
        mBuilder.setPriority(Notification.PRIORITY_MAX);
        mBuilder.setStyle(bigText);
        mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        scheduler.scheduleAtFixedRate
                (new Runnable() {
                    public void run() {
                        Log.e("hi mostafa","hh");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                i=1;
                                output.clear();
                                editor.clear();
                                editor.commit();
                                prepareData(true);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                                {
                                    String channelId = "Your_channel_id";
                                    NotificationChannel channel = new NotificationChannel(
                                            channelId,
                                            "Channel human readable title",
                                            NotificationManager.IMPORTANCE_HIGH);
                                    mNotificationManager.createNotificationChannel(channel);
                                    mBuilder.setChannelId(channelId);
                                }

                                mNotificationManager.notify(0, mBuilder.build());

                            }
                        });

                    }
                }, 1, 1, TimeUnit.HOURS);
    }
    private void ReachTheEndOfRecycler() {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(1) && newState==RecyclerView.SCROLL_STATE_IDLE) {
                    Log.d("-----","end");
                    if(isconected()){
                        i++;
                        prepareData(true);
                        progressDialog.show();   
                    }else{
                        Toast.makeText(MainActivity.this, "No Internet Connection !", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
    private void prepareSharedPref() {
        mSharedPreferences = getSharedPreferences("acc",MODE_PRIVATE);
        editor = mSharedPreferences.edit();
    }
    private void prepareSwipRefresh() {
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(isconected()) {
                    progressDialog.show();
                    i = 1;
                    output.clear();
                    editor.clear();
                    editor.commit();
                    prepareData(true);
                    mSwipeRefreshLayout.setRefreshing(false);
                }else{
                    mSwipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(MainActivity.this, "No Internet Connection !", Toast.LENGTH_SHORT).show();                    
                }
            }
        });
    }
    private void prepareData(boolean isToRefresh) {
        if(mSharedPreferences.contains("response") && !isToRefresh){
            String response = mSharedPreferences.getString("response","0");
            try {
                JSONArray jsonResponse = new JSONArray(response);
                getData(jsonResponse);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            if(isconected()){

                // Response received from the server
                Response.Listener<String> responseListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonResponse = new JSONArray(response);
                            addjsonTogether(response);
                            getData(jsonResponse);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                };

                githubRepo RigsterRequest = new githubRepo("https://api.github.com/orgs/square/repos?per_page=10&page="+String.valueOf(i), responseListener);
                RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
                queue.add(RigsterRequest);
                queue.addRequestFinishedListener(new RequestQueue.RequestFinishedListener<Object>() {
                    @Override
                    public void onRequestFinished(Request<Object> request) {
                        mAdapter.notifyDataSetChanged();
                    }
                });   
            }else{
                progressDialog.dismiss();
                Toast.makeText(this, "No Internet Connection !", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void addjsonTogether(String response) {
        //To save full list
        String res="";
        if(mSharedPreferences.contains("response")){
            String oldres = mSharedPreferences.getString("response","0");
            //solve bad issue
            int length = oldres.length();
            oldres = charRemoveAt(oldres,length-1);
            oldres = oldres + ",";
            response = charRemoveAt(response,0);
            res = oldres + response ;
        }else{
            res = response;
        }
        editor.putString("response",res);
        editor.apply();
        editor.commit();
    }
    private void getData(JSONArray jsonResponse) {
        Repo mRepo = null;
        try {
            for(int i = 0;i<jsonResponse.length();i++){
                mRepo = new Gson().fromJson(jsonResponse.getJSONObject(i).toString(), Repo.class);
                //Log.e("Data",mRepo.getName());
                if(mRepo.getDescription() == null){
                    output.add(new data(mRepo.getName(),
                            mRepo.getOwner().getLogin(),
                            "No Descroption here",
                            mRepo.getFork(),
                            mRepo.getHtmlUrl()));
                }else{
                    output.add(new data(mRepo.getName(),mRepo.getOwner().getLogin()
                            ,mRepo.getDescription().toString(),mRepo.getFork(),mRepo.getHtmlUrl()));
                }
                Log.e("Data",String.valueOf(output.size()));
                progressDialog.dismiss();

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public void showprogress(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.app_name));
        progressDialog.setMessage("Loading");
        progressDialog.show();
    }
    public void prepareRecyler(){
        output = new ArrayList<>();
        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new MyAdapter(this,output);
        recyclerView.setAdapter(mAdapter);
    }
    public String charRemoveAt(String str, int p) {
        return str.substring(0, p) + str.substring(p + 1);
    }
    public boolean isconected(){
        ConnectivityManager cm = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo nInfo = cm.getActiveNetworkInfo();
        boolean connected = nInfo != null && nInfo.isAvailable() && nInfo.isConnected();
        return  connected;
    }

}