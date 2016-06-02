package firewall;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.hust.activity.R;

import java.util.List;

/**
 * Created by Lai Dong on 4/20/2016.
 */
public class ListAppAdapter extends ArrayAdapter<AppItem> {
    int layoutId;
    List<AppItem> list = null;
    Context context;
    LayoutInflater inflater;

    public ListAppAdapter(Context context, int resource, List<AppItem> listapps) {
        super(context, resource, listapps);
        this.context = context;
        this.layoutId = resource;
        this.list = listapps;
        inflater = (LayoutInflater) this.context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public String getItems(String[] i) {
        StringBuilder b = new StringBuilder();
        int c = 0;
        for (String j : i) {
            c++;
            if (c == i.length)
                break;
            b.append(j + ", ");

        }
        return b.toString();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        Holder holder = new Holder();
        View view;
        view = inflater.inflate(layoutId, null);
        final AppItem item = list.get(position);
        holder.img = (ImageView) view.findViewById(R.id.itemicon);
        holder.check_wifi = (CheckBox) view.findViewById(R.id.itemcheck_wifi);
        holder.check_3g = (CheckBox) view.findViewById(R.id.itemcheck_3g);
        holder.label = (TextView) view.findViewById(R.id.itemtext);

        holder.img.setImageDrawable(item.cached_icon);
        holder.check_3g.setChecked(item.selected_3G);
        holder.check_wifi.setChecked(item.selected_Wifi);
        holder.label.setText(String.valueOf(item.getId()) + ": " + item.label);
        holder.check_3g
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,
                                                 boolean isChecked) {
                        // TODO Auto-generated method stub
                        item.selected_3G = isChecked;
                        if (item.selected_3G) {
                            if (Api.list_3G.indexOf(item.getId()) ==-1)
                                Api.list_3G.add(item.getId());

                            if(item.getId()==Api.SPECIAL_UID_ANY){
                                Api.any_3g=true;
                            }
                        } else {
                            int id = Api.list_3G.indexOf(item.getId());
                            if (id >= 0)
                                Api.list_3G.remove(id);

                            if(item.getId()==Api.SPECIAL_UID_ANY){
                                Api.any_3g=false;
                            }
                        }
                    }
                });

        holder.check_wifi
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,
                                                 boolean isChecked) {
                        // TODO Auto-generated method stub
                        item.selected_Wifi = isChecked;
                        if (item.selected_Wifi) {
                            if (Api.list_Wifi.indexOf(item.getId()) == -1){
                                Api.list_Wifi.add(item.getId());
                                Log.i("Enabled: ", item.getId() + "");
                            }
                            if(item.getId()==Api.SPECIAL_UID_ANY){
                                Api.any_wifi=true;
                            }
                        } else {
                            int id = Api.list_Wifi.indexOf(item.getId());
                            if (id >= 0)
                                Api.list_Wifi.remove(id);
                            if(item.getId()==Api.SPECIAL_UID_ANY){
                                Api.any_wifi=false;
                            }
                        }
                    }
                });
        return view;
    }

    public class Holder {
        ImageView img;
        CheckBox check_wifi, check_3g;
        TextView label;
    }

}
