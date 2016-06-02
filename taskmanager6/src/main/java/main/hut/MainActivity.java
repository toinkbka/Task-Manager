package main.hut;

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.hust.activity.AllListAppProcess;
import com.hust.activity.R;

import cpufrequency.CPUfrequency;
import detect.contextuser.ContextAware;
import detect.contextuser.RecentTask;
import detect.netmanager.StatsticNetwork;
import detect.screen.SettingActivity;
import detect.service.ServiceDetect;
import detect.service.UpdateService;
import firewall.DisableTaskActivity;
import firewall.MainFirewall;

public class MainActivity extends AppCompatActivity {
    private final String serviceWifiDetect = "detect.service.ServiceDetect";
    private final String serviceScreenDetect = "detect.service.UpdateService";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        PagerAdapter perAdapter = new PagerAdapter(getSupportFragmentManager(), MainActivity.this);
        viewPager.setAdapter(perAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);

        // Iterate over all tabs and set the custom view
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            tab.setCustomView(perAdapter.getTabView(i));
        }
      /* if(!isMyServiceRunning(serviceWifiDetect)){
            Intent i = new Intent(MainActivity.this, ServiceDetect.class);
            startService(i);
        }
        if(!isMyServiceRunning(serviceScreenDetect)){
            Intent i = new Intent(MainActivity.this, UpdateService.class);
            startService(i);
        }*/
    }

    class PagerAdapter extends FragmentPagerAdapter {
  //      String tabTitles[] = new String[] { "TaskMgr", "NetMgr", "FireWall", "History"};
          String tabTitles[] = new String[] { "TaskMgr", "FireWall", "CPUFrequency"};
        Context context;

        public PagerAdapter(FragmentManager fm, Context context) {
            super(fm);
            this.context = context;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new AllListAppProcess();
                case 1:
                    return new MainFirewall();
                case 2:
                    return new CPUfrequency();
               /* case 3:
                    return new RecentTask();*/
            }
            return null;
        }

        @Override
        public int getCount() {
            return tabTitles.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }
        public View getTabView(int position) {
            View tab = LayoutInflater.from(MainActivity.this).inflate(R.layout.custom_tab, null);
            TextView tv = (TextView) tab.findViewById(R.id.custom_text);
            tv.setText(tabTitles[position]);
            return tab;
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        MenuItem item = menu.getItem(0);
        if(isMyServiceRunning(serviceWifiDetect)|| isMyServiceRunning(serviceScreenDetect))
            item.setIcon(R.mipmap.stop_hand);
        else
            item.setIcon(R.mipmap.hand_ol);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
           /* case R.id.menuitem_search:
                Toast.makeText(this, getString(R.string.ui_menu_search),
                        Toast.LENGTH_SHORT).show();
                SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
                SearchView searchView = (SearchView) item.getActionView();
                searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
                searchView.setIconifiedByDefault(false);
                return true;*/

            case R.id.menuitem_service:
                new ContextAware(getApplicationContext()).start();
                return true;

            case R.id.menuitem_setting:
                Toast.makeText(this, getString(R.string.ui_menu_setting),
                        Toast.LENGTH_SHORT).show();
                Intent i = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(i);
                return true;
            case R.id.menuitem_list_disable:

                Intent i1 = new Intent(MainActivity.this, DisableTaskActivity.class);
                startActivity(i1);
                return true;
            case R.id.menuitem_traffic:
                Intent intent = new Intent(MainActivity.this, StatsticNetwork.class);
                startActivity(intent);
                return true;

            /*case R.id.menuitem_add:
                Toast.makeText(this, getString(R.string.ui_menu_add),
                        Toast.LENGTH_SHORT).show();
                return true;
            case R.id.menuitem_share:
                Toast.makeText(this, getString(R.string.ui_menu_share),
                        Toast.LENGTH_SHORT).show();
                return true;
            case R.id.menuitem_feedback:
                Toast.makeText(this, getString(R.string.ui_menu_feedback),
                        Toast.LENGTH_SHORT).show();
                return true;*/
            case R.id.menuitem_about:
                Toast.makeText(this, getString(R.string.ui_menu_about),
                        Toast.LENGTH_SHORT).show();
                return true;
            case R.id.menuitem_quit:
                Toast.makeText(this, getString(R.string.ui_menu_quit),
                        Toast.LENGTH_SHORT).show();
                finish(); // close the activity
                return true;
        }
        return false;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            Log.e("query", intent.getStringExtra(SearchManager.QUERY));
        }
    }

    private boolean isMyServiceRunning(String serviceName) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager
                .getRunningServices(Integer.MAX_VALUE)) {
            if (serviceName.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
