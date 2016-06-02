package firewall;

import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;

/**
 * Created by Lai Dong on 4/20/2016.
 */
public class AppItem {
    /** linux user uid */
    int uid = 0;
    /** names of applications belong to this uid */
    String label = "";
    /** application is selected for wifi */
    public boolean selected_Wifi;
    /** application is selected for 3g */
    public boolean selected_3G;
    /** toString cache */
    String toStr = "";
    /** Application 's Info */
    ApplicationInfo appInfo;
    /** cached app icon */
    Drawable cached_icon = null;
    /** icon is loaded already */
    boolean icon_loaded = false;
    /** first item seem ? */
    boolean firstSeem = false;

    public int getId() {
        return uid;
    }

    public AppItem() {

    }

    public AppItem(int uid, String label, boolean s_wifi, boolean s_3G, Drawable icon) {
        this.uid = uid;
        this.label = label;
        this.selected_3G = s_3G;
        this.selected_Wifi = s_wifi;
        this.cached_icon=icon;
    }

}
