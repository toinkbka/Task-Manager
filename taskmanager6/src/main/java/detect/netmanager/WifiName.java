package detect.netmanager;

/**
 * Created by Lai Dong on 4/5/2016.
 */
public class WifiName {
    private String wifiname;
    private int level;

    public WifiName(String wifiname, int level) {
        this.wifiname = wifiname;
        this.level = level;
    }

    public String getWifiname() {
        return wifiname;
    }

    public void setWifiname(String wifiname) {
        this.wifiname = wifiname;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
