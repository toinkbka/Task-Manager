package firewall;

import android.content.Context;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hust.activity.R;

import java.util.List;

import detect.contextuser.Task;

/**
 * Created by Hitagi_007 on 03/05/2016.
 */


public class ListDisabledAdapter extends ArrayAdapter<Task> {
    Context context;
    int res;
    List<Task> list;
    LayoutInflater inflater;
    public ListDisabledAdapter(Context ctx, List<Task> list, int resId){
        super(ctx,resId,list);
        this.context=ctx;
        this.list=list;
        this.res=resId;
inflater=(LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder=new Holder();
        View view;
        view=inflater.inflate(res,null);
        Task task=list.get(position);
        final Task item=list.get(position);
        holder.img=(ImageView)view.findViewById(R.id.icon_disabled);
        holder.label=(TextView)view.findViewById(R.id.txt_name);
        holder.packageName=(TextView)view.findViewById(R.id.txt_pkg);
        try {
            holder.img.setImageDrawable(context.getPackageManager()
                    .getApplicationIcon(
                            context.getPackageManager().getApplicationInfo(
                                    task.packApp,
                                    PackageManager.GET_META_DATA)));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        holder.label.setText(task.nameApp);
        holder.packageName.setText(task.packApp);
        return view;
    }

    public class Holder {
        ImageView img;

        TextView label, packageName;
    }
}
