package imagelistview.task.internal.com.txlistviewimage.ui.activity;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import imagelistview.task.internal.com.txlistviewimage.R;
import imagelistview.task.internal.com.txlistviewimage.ui.adapter.TxListAdapter;
import manager.request_queue_manager.TxNetworkManager;
import manager.response.SingleUserInfo;
import manager.response.UserContentMain;
import manager.utils.TxStringUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class TxMainActivity extends AppCompatActivity {

    public static final String HTTPS_DL_DROPBOXUSERCONTENT_COM_U_746330_FACTS_JSON = "https://dl.dropboxusercontent.com/u/746330/facts.json";
    private final String TAG = TxMainActivity.class.getSimpleName();
    private final String TAG_VOLLY_REQUEST = "my_own_tax";
    private SwipeRefreshLayout mSwipeRefreshLayout = null;

    private ProgressBar progressBar;
    private List<SingleUserInfo> userList = new ArrayList<SingleUserInfo>();
    private ListView listView;
    private TxListAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tx_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeJsonObjReq();

            }
        });

        listView = (ListView) findViewById(R.id.list);

        progressBar=(ProgressBar)findViewById(R.id.progressView);

        //Initialize swipe to refresh view
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //Refreshing data on server
                makeJsonObjReq();
            }
        });

    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        makeJsonObjReq();
    }

    private void makeJsonObjReq() {

        if (!mSwipeRefreshLayout.isRefreshing()) {
        progressBar.setVisibility(View.VISIBLE);}


        Snackbar.make(listView, "Refreshing data", Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                HTTPS_DL_DROPBOXUSERCONTENT_COM_U_746330_FACTS_JSON, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Gson gson = new Gson();
                        UserContentMain userContentMain = null;

                        if (response != null) {
                            userContentMain = gson.fromJson(response.toString(), UserContentMain.class);
                        }

                        if (userContentMain != null) {
                            progressBar.setVisibility(View.GONE);
                            updateTitleAndListData(userContentMain);
                        }
                        if (mSwipeRefreshLayout.isRefreshing()) {
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                        progressBar.setVisibility(View.GONE);
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                progressBar.setVisibility(View.GONE);
                if (mSwipeRefreshLayout.isRefreshing()) {
                    mSwipeRefreshLayout.setRefreshing(false);
                }

            }
        });

        Log.d(TAG, jsonObjReq.toString());

        // Adding request to request queue
        TxNetworkManager.getInstance().addToRequestQueue(jsonObjReq,
                TAG_VOLLY_REQUEST);

    }

    private void updateTitleAndListData(UserContentMain userContentMain) {

        if (new TxStringUtils().isStringDataAValid(userContentMain.getTitle())) {
            getSupportActionBar().setTitle(userContentMain.getTitle());
        }

        userList = userContentMain.getRows();

        adapter = new TxListAdapter(this, userContentMain.getRows());
        listView.setAdapter(adapter);
        //adapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(TxMainActivity.this.isFinishing()){
            // Cancelling request
             TxNetworkManager.getInstance().getRequestQueue().cancelAll(TAG_VOLLY_REQUEST);
        }

    }
}
