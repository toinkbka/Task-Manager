package firewall;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.hust.activity.R;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Lai Dong on 4/20/2016.
 */
public class MainFirewall extends Fragment {
    ListView listAppsView;
    Button btn_enable, btn_saveRule;
    ArrayList<AppItem> listApps;
    ListAppAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_firewall, container, false);
        listAppsView = (ListView) view.findViewById(R.id.list_apps);
        listApps = new ArrayList<AppItem>();
        btn_enable = (Button) view.findViewById(R.id.btn_toggleFW);
        btn_saveRule = (Button) view.findViewById(R.id.btn_saveRule);
        Log.i("status: ", "Oncreate");
        Api.assertBinaries(getContext(), true);
        checkPreferences();
        btn_saveRule.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Api.saveRules(getContext());
            }
        });
        btn_enable.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                boolean x = false;
                Api.saveRules(getContext());
                Log.i("State CUR: ", Api.MODE_CUR + "");
                if (Api.MODE_CUR) {
                    Api.cleanIptablesRules(getContext());
                } else {

                    // x = Api.applyIptablesRules(getApplicationContext(),
                    // Api.list_3G, Api.list_Wifi, false, true);
                    x = Api.applyIptablesRules(getContext(),
                            Api.list_3G, Api.list_Wifi, false, true);
                }

                checkPreferences();

            }
        });
        return view;
    }

    /**
     * Check if the stored preferences are OK
     */
    private void checkPreferences() {
        final SharedPreferences prefs = getActivity().getSharedPreferences(Api.PREF_NAME, 0);
        Api.MODE_CUR = prefs.getBoolean(Api.BLOCK_MODE, Api.MODE_OFF);
        updateShow();
    }

    public void updateShow() {
        Resources s = getResources();
        if (Api.MODE_CUR)
            btn_enable.setText("Disable FireWall");
        else
            btn_enable.setText("Enable FireWall");
    }

    @Override
    public void onResume() {
        super.onResume();
        requireRootAccess();
        loadAppsList();
        adapter = new ListAppAdapter(getContext(), R.layout.item_firewall,
                Api.listapps);
        listAppsView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        Log.i("status: ", "OnResume");
    }


    public void requireRootAccess() {
        try {
            Runtime.getRuntime().exec("su");
        } catch (IOException e) {
        }
    }

    public void loadAppsList() {
        Resources res = getResources();
        final ProgressDialog progress = ProgressDialog.show(getContext(),
                res.getString(R.string.loading), res.getString(R.string.wait),
                true);
        Api.loadInstalledApps(getContext());
        progress.dismiss();
    }
}
