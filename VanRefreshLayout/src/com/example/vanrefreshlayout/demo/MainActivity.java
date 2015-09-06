package com.example.vanrefreshlayout.demo;

import java.util.ArrayList;
import java.util.List;

import com.example.vanrefreshlayout.R;
import com.example.vanrefreshlayout.view.PullToRefreshLayout;

import android.support.v4.widget.SwipeRefreshLayout;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.ListView;


public class MainActivity extends Activity {
	private PullToRefreshLayout refreshLayout;
	private ListView listView;
	private int count = 5;
    private SwipeRefreshLayout.OnRefreshListener swipeRefreshListener = new  SwipeRefreshLayout.OnRefreshListener(){
        @Override
		public void onRefresh() {
            new Handler()  
            {  
                @Override  
                public void handleMessage(Message msg)  
                {  
                	count++;
                    listView.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_expandable_list_item_1,getData()));
                    refreshLayout.refreshFinish(PullToRefreshLayout.REFRESH_SUCCEED);  
                }  
            }.sendEmptyMessageDelayed(0, 3000);
		}
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_expandable_list_item_1,getData()));
        refreshLayout = (PullToRefreshLayout) findViewById(R.id.refresh_view);
        refreshLayout.setOnRefreshListener(swipeRefreshListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private List<String> getData(){
        
       List<String> data = new ArrayList<String>();
       for(int i=0; i < count; i++){
           data.add("²âÊÔÊý¾Ý "+i+1);
       }
       return data;
   }
}
