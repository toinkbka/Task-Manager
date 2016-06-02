package com.hust.activity;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.hust.adapter.TaskAdapter;
import com.hust.common.KillProcessUtils;
import com.hust.dialog.DialogMenu;
import com.hust.model.MainListItem;

import java.util.ArrayList;
import java.util.List;

import detect.contextuser.RecentTaskHelper;
import detect.contextuser.Task;
import main.hut.ShellUtils;

/**
 * Created by laidong on 20/09/2015.
 */
public class AllListAppProcess extends BaseActivity{

    SharedPreferences sharedPreferences;
    private List<ApplicationInfo> appList = null;
    private String txtDialog;
    public static AllListAppProcess instance;

    public static AllListAppProcess getInstance(){
        if(instance==null){
            instance = new AllListAppProcess();
        }
        return instance;
    }


    @Override
    public int getTypeShow() {
        return 0;
    }

    @Override
    public void createActionMode(ActionMode actionMode, Menu menu) {
        MenuInflater inflater = actionMode.getMenuInflater();
        inflater.inflate(R.menu.menu_action_long_press, menu);
    }

    @Override
    public void actionItemClicked(ActionMode actionMode, MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.killProcess:
                List<MainListItem> listMainItem = adapter.getListMainItemListSelected();

                KillProcessUtils.getInstance(getActivity()).killAll(listMainItem);
                Toast.makeText(getContext(), listMainItem.size() + " process have added to Auto Kill", Toast.LENGTH_LONG).show();

                actionMode.finish();
                adapter.clearSelections();
                break;
            case R.id.freeze:
                List<MainListItem> listMainItem1 = adapter.getListMainItemListSelected();
                for (MainListItem item : listMainItem1) {
                    if (ShellUtils.runCmd("am force-stop " + item.processName)) {
                        Toast.makeText(getContext(), "Hibernated " + item.label, Toast.LENGTH_SHORT)
                                .show();
                    } else {
                        Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                    }
                }
                actionMode.finish();
                adapter.clearSelections();
                break;
            case R.id.disable:
                List<MainListItem> listDisabled = adapter.getListMainItemListSelected();
                for (MainListItem item : listDisabled) {
                    if (ShellUtils.runCmd("pm block " + item.processName + ";pm disable "
                            + item.processName)) {

                        Toast.makeText(getContext(), "Disable " + item.label, Toast.LENGTH_SHORT)
                                .show();
                        Task task=new Task();
                        task.packApp=item.processName;
                        task.nameApp=item.label;
                        RecentTaskHelper.getInstance(getContext()).insertDisableTask(task);
                    } else {
                        Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                    }
                }
                actionMode.finish();
                adapter.clearSelections();
                break;
        }
    }

    @Override
    public void recyclerItemClick(final int position, View v, final MainListItem item) {
        int idx = position;
        if (actionMode != null) {
            myToggleSelection(idx);
            return;
        } else {
            DialogMenu menu = new DialogMenu(getContext(), item.label, new String[]{"Open","Kill this process", "Disable", "Hibernate", "Uninstall"}) {
                @Override
                public void onClickDialog(int position) {
                    switch (position) {
                        case 0:
                            if (item.mainClassName.equals("")) {
                            } else {
                                Intent intent = new Intent();
                                intent.setComponent(new ComponentName(item.processName,
                                        item.mainClassName));
                                getActivity().startActivity(intent);
                                Toast.makeText(getContext(), item.label + " have been start", Toast.LENGTH_SHORT).show();
                            }

                            break;
                        case 1:
                            KillProcessUtils.getInstance(getContext()).killTask(item);
                            Toast.makeText(getContext(), item.label + " have been killed", Toast.LENGTH_SHORT).show();
                            break;

                        case 2:
                            txtDialog = "Disable";
                            showYesNoDialog(getContext(), item,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialog,
                                                int which) {
                                            if (ShellUtils.runCmd("pm block " + item.processName + ";pm disable "
                                                    + item.processName)) {

                                                Toast.makeText(getContext(), "Disable " + item.label, Toast.LENGTH_SHORT)
                                                        .show();
                                                Task task = new Task();
                                                task.packApp = item.processName;
                                                task.nameApp = item.label;
                                                RecentTaskHelper.getInstance(getContext()).insertDisableTask(task);
                                            } else {
                                                Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                            break;

                        case 3:
                            txtDialog = "hibernate";
                            showYesNoDialog(getContext(),item,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialog,
                                                int which) {
                                            if (ShellUtils.runCmd("am force-stop " + item.processName)) {
                                                Toast.makeText(getContext(), "Hibernated " + item.label, Toast.LENGTH_SHORT)
                                                        .show();
                                            } else {
                                                Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                            break;

                        case 4:
                            Uri packageURI = Uri.parse("package:" + item.processName);
                            Toast.makeText(getContext(), item.processName, Toast.LENGTH_SHORT).show();
                            Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
                            getContext().startActivity(uninstallIntent);
                            break;

                       /* case 1:
                            AppSave app = new AppSave();
                            app.processName = item.processName;
                            app.save();
                            break;*/
                    }
                }
            };
        }
    }

    @Override
    public String getTitelTask() {
        return "List All Process";
    }


    private void showYesNoDialog(Context context,MainListItem item,
                                 DialogInterface.OnClickListener listener) {
        AlertDialog.Builder ad = new AlertDialog.Builder(context);
        ad.setTitle("Application will be " + txtDialog + "?");
        ad.setMessage(item.label + " will be " + txtDialog + "?");
        ad.setPositiveButton("OK", listener);
        ad.setNegativeButton("Cancel", null);
        ad.show();
    }





}
