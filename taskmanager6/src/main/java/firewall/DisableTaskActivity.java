package firewall;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.hust.activity.R;

import java.util.ArrayList;
import java.util.List;

import detect.contextuser.RecentTask;
import detect.contextuser.RecentTaskHelper;
import detect.contextuser.Task;
import main.hut.MainActivity;
import main.hut.ShellUtils;

public class DisableTaskActivity extends AppCompatActivity {
ListView lv;
    static int resId=R.layout.item_disabled;
    ListDisabledAdapter adapter;
    List<Task> list;
    RecentTaskHelper database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disable_task);
        list=new ArrayList<Task>();
        lv=(ListView)findViewById(R.id.list_disabled);
        database=new RecentTaskHelper(getApplicationContext());
        adapter=new ListDisabledAdapter(getApplicationContext(),list,resId);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DisableTaskActivity.this);
                builder.setTitle("UnDisable App")
                        .setMessage("Are you sure to UnDisable "+list.get(position).nameApp +" ?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (ShellUtils.runCmd("pm unblock " + list.get(position).packApp + ";pm enable "
                                        + list.get(position).packApp)) {
                                    Toast.makeText(getApplicationContext(),
                                            "Mở lại " + list.get(position).nameApp, Toast.LENGTH_SHORT)
                                            .show();
                                    RecentTaskHelper.getInstance(getApplicationContext()).delete("disableTask",list.get(position).packApp);
                                    getList();
                                }
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();

                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getList();
        System.out.println("RESUME");
    }
    public void getList(){
        list=database.getDisabledTask();
        adapter=new ListDisabledAdapter(getApplicationContext(),list,resId);
        lv.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i1 = new Intent(DisableTaskActivity.this, MainActivity.class);
        startActivity(i1);
    }


}
