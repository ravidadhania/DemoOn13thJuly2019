package com.demo.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.demo.R;
import com.demo.api.ApiClient;
import com.demo.api.ApiInterface;
import com.demo.databinding.ActivityMainBinding;
import com.demo.databinding.RowDataBinding;
import com.demo.model.HitsModel;
import com.demo.model.MainModel;
import com.demo.utils.CommonUtils;

import org.json.JSONObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    public ActivityMainBinding mBinder;
    private Context context;
    private ArrayList<HitsModel> mMainArrayList = new ArrayList<>();
    private ArrayList<HitsModel> mSubArrayList;
    private Adapter adapter;
    private boolean isPullToRefresh = false;
    private LinearLayoutManager linearLayoutManager;

    //for pagination
    int perPageItem = 20;
    int page = 0;
    int loaderCounter = 0;
    int load_counter = 1;
    int count = 0;
    boolean loadingMore = true;

    @Override
    protected void onResume() {
        super.onResume();
        if (CommonUtils.isNetworkAvailable(context)) {
            apiCallForFirstTime();
        } else {
            CommonUtils.showAlertDialog(context, "No Internet");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = MainActivity.this;
        mBinder = DataBindingUtil.setContentView(MainActivity.this, R.layout.activity_main);
        linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        mBinder.swipeContainer.setOnRefreshListener(MainActivity.this);

        mBinder.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int lastInScreen = linearLayoutManager.findLastVisibleItemPosition();
                int totalItemCount = linearLayoutManager.getItemCount();

                if (lastInScreen == (totalItemCount - 1)
                        && (loadingMore) && (loaderCounter / load_counter) >= perPageItem) {
                    loadingMore = false;
                    count = totalItemCount;
                    apiCallForAllTime();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void setAdapter() {
        adapter = new Adapter(mMainArrayList);
        mBinder.recyclerView.setLayoutManager(linearLayoutManager);
        mBinder.recyclerView.setAdapter(adapter);
        mBinder.recyclerView.getLayoutManager().scrollToPosition(count - 1);
        loaderCounter = mMainArrayList.size();
        if (!loadingMore) {
            loadingMore = true;
            load_counter++;
        }
        calculateSelectedHits();
    }

    @Override
    public void onRefresh() {
        isPullToRefresh = true;
        if (CommonUtils.isNetworkAvailable(context)) {
            apiCallForFirstTime();
        } else {
            mBinder.swipeContainer.setRefreshing(false);
            CommonUtils.showAlertDialog(context, "No Internet");
        }
    }

    private void apiCallForFirstTime() {
        if (CommonUtils.isNetworkAvailable(context)) {
            perPageItem = 20;
            page = 0;
            loaderCounter = 0;
            load_counter = 1;
            count = 0;
            loadingMore = true;
            mMainArrayList = new ArrayList<>();
            apiCallForGetData();
        } else {
            Toast.makeText(context, "No Internet", Toast.LENGTH_SHORT).show();
        }
    }

    private void apiCallForAllTime() {
        if (CommonUtils.isNetworkAvailable(context)) {
            page = page + 1;
            apiCallForGetData();
        } else {
            Toast.makeText(context, "No Internet", Toast.LENGTH_SHORT).show();
        }
    }

    private void calculateSelectedHits() {
        int count = 0;

        for (int i = 0; i < mMainArrayList.size(); i++) {
            if (mMainArrayList.get(i).getSelected())
                count++;
        }

        mBinder.tvTitle.setText("Selected Hits : " + count);
    }

    public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

        private ArrayList<HitsModel> mArrayList;

        public Adapter(ArrayList<HitsModel> list) {
            this.mArrayList = list;
        }

        @Override
        public int getItemCount() {
            return mArrayList.size();
        }

        @Override
        public int getItemViewType(int position) {
            return super.getItemViewType(position);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            final HitsModel model = mArrayList.get(position);

            holder.rowBinding.tvTitle.setText(model.getTitle());
            // here we got time format wrong : yyyy-MM-dd'T'HH:mm:ss.SSSZ so just put date
            holder.rowBinding.tvTime.setText(model.getCreatedAt().substring(0,19));

            if (model.getSelected()) {
                holder.rowBinding.switchBtn.setChecked(true);
            } else {
                holder.rowBinding.switchBtn.setChecked(false);
            }

            holder.rowBinding.switchBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    mArrayList.get(position).setSelected(b);
                    notifyDataSetChanged();
                    calculateSelectedHits();
                }
            });
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            RowDataBinding rowBinding;

            private ViewHolder(RowDataBinding rowBinding) {
                super(rowBinding.getRoot());
                this.rowBinding = rowBinding;
                this.setIsRecyclable(false);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            RowDataBinding rowBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.row_data, parent, false);
            return new ViewHolder(rowBinding);
        }
    }

    private void apiCallForGetData() {

        if (page != 50) {
            if (!isPullToRefresh)
                CommonUtils.showProgressDialog(context);

            ApiClient.getClient().create(ApiInterface.class).getData("story", "" + page)
                    .enqueue(new Callback<MainModel>() {
                        @Override
                        public void onResponse(Call<MainModel> call, Response<MainModel> response) {
                            CommonUtils.hideProgressDialog();
                            if (isPullToRefresh) {
                                isPullToRefresh = false;
                                mBinder.swipeContainer.setRefreshing(false);
                            }

                            if (response.isSuccessful()) {

                                mSubArrayList = new ArrayList<>();
                                mSubArrayList = response.body().getHits();
                                if (mSubArrayList.size() > 0) {
                                    mBinder.swipeContainer.setVisibility(View.VISIBLE);
                                    mBinder.recyclerView.setVisibility(View.VISIBLE);
                                    mMainArrayList.addAll(mSubArrayList);
                                    setAdapter();
                                } else {
                                    if (page == 50) {
                                        mBinder.swipeContainer.setVisibility(View.VISIBLE);
                                        mBinder.recyclerView.setVisibility(View.GONE);
                                    }
                                }

                            } else {
                                Toast.makeText(context, "Error", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<MainModel> call, Throwable t) {
                            if (!isPullToRefresh)
                                CommonUtils.hideProgressDialog();
                            else {
                                isPullToRefresh = false;
                                mBinder.swipeContainer.setRefreshing(false);
                            }
                        }
                    });
        }
    }
}
