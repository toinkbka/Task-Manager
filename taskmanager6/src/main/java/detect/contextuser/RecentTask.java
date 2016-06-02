package detect.contextuser;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.hust.activity.R;

import java.util.List;

/**
 * Created by Lai Dong on 4/13/2016.
 */
public class RecentTask extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.gettask_fragment, container, false);

        ListView lv = (ListView) view.findViewById(R.id.lv);
        List<Task> tasks = RecentTaskHelper.getInstance(getContext()).getAllTask();
        TaskAdapter adapter = new TaskAdapter(tasks, getActivity());
        lv.setAdapter(adapter);

        return view;
    }

    private class TaskAdapter extends BaseAdapter{
        List<Task> tasks;
        Context mContext;

        public TaskAdapter(List<Task> tasks, Context mContext) {
            super();
            this.tasks = tasks;
            this.mContext = mContext;
        }

        @Override
        public int getCount() {
            return tasks.size();
        }

        @Override
        public Object getItem(int position) {
            return tasks.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Task task = tasks.get(position);
            ViewHolder holder = null;

            if(convertView == null){
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_task_uses, null);
                holder = new ViewHolder();
                holder.img = (ImageView) convertView.findViewById(R.id.imgView_task);
                holder.txt = (TextView) convertView.findViewById(R.id.tv_task);
                holder.tvCount = (TextView) convertView
                        .findViewById(R.id.tvCount);

                convertView.setTag(holder);
            }else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.txt.setText(task.nameApp);
            try {
                holder.img.setImageDrawable(getActivity().getPackageManager()
                        .getApplicationIcon(
                                getActivity().getPackageManager().getApplicationInfo(
                                        task.packApp,
                                        PackageManager.GET_META_DATA)));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            holder.tvCount.setText(task.count + " used");
            return convertView;
        }
    }
    class ViewHolder {
        ImageView img;
        TextView txt;
        TextView tvCount;
    }
}
