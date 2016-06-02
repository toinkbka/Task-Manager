package com.hust.activity;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ActionMode;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.hust.adapter.TaskAdapter;
import com.hust.common.ProcessThread;
import com.hust.model.MainListItem;

import java.util.ArrayList;
import java.util.List;

import main.hut.ShellUtils;

/**
 * Created by laidong on 20/09/2015.
 */
public abstract class BaseActivity extends Fragment implements TaskAdapter.TaskClickListener, RecyclerView.OnItemTouchListener, ActionMode.Callback {

    private ProcessThread runApp;
    private List<MainListItem> items;
    RecyclerView list;
    static TaskAdapter adapter;
    private final static int MENU_ITEM0 = 0, MENU_ITEM1 = 1;
    private ActivityManager activityManager;
    private boolean type = false;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    TextView tvTitle;
    TextView tv;
    GestureDetectorCompat gestureDetector;
    ActionMode actionMode;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main, container, false);

        activityManager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        list = (RecyclerView) view.findViewById(R.id.list);
        list.setHasFixedSize(true);

        tvTitle = (TextView) view.findViewById(R.id.tvTitle);
        tvTitle.setText(getTitelTask());

        items = new ArrayList<MainListItem>();
        adapter = new TaskAdapter(items);
        adapter.setListener(this);
        RecyclerView.LayoutManager layout = new LinearLayoutManager(getContext());
        list.setLayoutManager(layout);
        list.setAdapter(adapter);
        list.addOnItemTouchListener(this);
        gestureDetector =
                new GestureDetectorCompat(getContext(), new RecyclerViewDemoOnGestureListener());

        tv = new TextView(getContext());
        tv.setText("");
        tv.setTextColor(Color.WHITE);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextSize(14);
        tv.setBackgroundResource(R.drawable.textview_bg);
        tv.setText("Loading...");
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setCustomView(tv);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowCustomEnabled(true);

        runApp = new ProcessThread(getContext(), activityManager, tv, items, adapter
                , 0);
        runApp.start();
       // actionAfterInit();
        return view ;
    }

    @Override
    public void onItemClick(int position, View v, MainListItem item) {
        recyclerItemClick(position, v, item);
    }

    /*@Override
    public void onRestart() {
        super.onRestart();
        runApp = new ProcessThread(this, activityManager, tv, items, adapter, 0);
        runApp.start();
    }*/

    @Override
    public void onStop() {
        super.onStop();
        runApp.stopThread();
       /* if(type)
            finish();*/
    }

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        createActionMode(actionMode, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {

        actionItemClicked(actionMode, menuItem);
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
        this.actionMode = null;
        adapter.clearSelections();
        //fab.setVisibility(View.VISIBLE);
    }

    protected void myToggleSelection(int idx) {
        adapter.toggleSelection(idx);
        String title = getString(R.string.selected_count, adapter.getSelectedItemCount());
        actionMode.setTitle(title);
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        gestureDetector.onTouchEvent(e);
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean b) {

    }

    private class RecyclerViewDemoOnGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            View view = list.findChildViewUnder(e.getX(), e.getY());
            //onClick(view);
            return super.onSingleTapConfirmed(e);
        }

        public void onLongPress(MotionEvent e) {
            View view = list.findChildViewUnder(e.getX(), e.getY());
            if (actionMode != null) {
                return;
            }
// Start the CAB using the ActionMode.Callback defined above
            actionMode = ((AppCompatActivity)getActivity()).startActionMode(BaseActivity.this);
            int idx = list.getChildPosition(view);
            myToggleSelection(idx);
            super.onLongPress(e);
        }
    }



    public abstract int getTypeShow();

    public abstract void createActionMode(ActionMode actionMode, Menu menu);

    public abstract void actionItemClicked(ActionMode actionMode, MenuItem menuItem);

    public abstract void recyclerItemClick(int position, View v, MainListItem item);

    public abstract String getTitelTask();

}
