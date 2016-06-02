package firewall;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import com.hust.activity.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Lai Dong on 4/20/2016.
 */
public class Api {
    public static final String BLOCK_MODE = "block_mode";
    public static final boolean MODE_ON = true;
    public static final boolean MODE_OFF = false;
    public static boolean MODE_CUR = false;

    public static final String PREF_BLOCKED_APPS = "BlockList";
    public static final String KEY_3G = "block_3G";
    public static final String KEY_WIFI = "block_wifi";

    public static List<AppItem> listapps = new ArrayList<AppItem>();
    public static List<Integer> listuids = new ArrayList<Integer>();

    // list of blocked apps */
    public static List<Integer> list_3G = new ArrayList<Integer>();
    public static List<Integer> list_Wifi = new ArrayList<Integer>();

    /** special application UID used to indicate "any application" */
    public static final int SPECIAL_UID_ANY = -10;
    /** special application UID used to indicate the Linux Kernel */
    public static final int SPECIAL_UID_KERNEL = -11;

    // Preferences
    public static final String PREF_NAME = "FireWallPrefs";
    public static final String PREF_3G_UIDS = "Allowed3G";
    public static final String PREF_WIFI_UIDS = "AllowedWifi";
    public static final String PREF_ENABLE = "Enabled";

    // root access
    public static boolean isRoot = false;

    // State of FireWall
    boolean fireOn = false;

    // script file name
    public final static String SCRTPT_NAME = "firewall.sh";

    // State of Special Uid
    public static boolean any_3g = false;
    public static boolean any_wifi = false;

    /** Load list of Installed Apps */
    public static void loadInstalledApps(Context ctx) {
        getBlockedApps(ctx);

        listapps = new ArrayList<AppItem>();
        PackageManager pm = ctx.getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(0);

        HashMap<Integer, AppItem> map = new HashMap<Integer, AppItem>();

        AppItem item = null;
        listuids = new ArrayList<Integer>();

        int id = 0;
        // Add special UID
        AppItem special = new AppItem(SPECIAL_UID_ANY,
                "Any App - Same as select all application", MODE_OFF, MODE_OFF,
                ctx.getResources().getDrawable(R.mipmap.ic_launcher));
        id = listuids.indexOf(SPECIAL_UID_ANY);
        if (id >= 0) {
            listapps.get(id).label += ", " + special.label;
            return;
        }
        listapps.add(special);
        listuids.add(SPECIAL_UID_ANY);

        for (ApplicationInfo app : apps) {
            // check if app have internet permission
            if (pm.checkPermission(Manifest.permission.INTERNET,
                    app.packageName) == PackageManager.PERMISSION_GRANTED) {

                item = new AppItem(app.uid, pm.getApplicationLabel(app)
                        .toString(), false, false, pm.getApplicationIcon(app));

                int index = listuids.indexOf(item.getId());

                if (index >= 0) {
                    listapps.get(index).label += ", " + item.label;
                    continue;
                }
                // check app's uid is blocked or not
                int index_3g = list_3G.indexOf(item.getId());
                if (index_3g >= 0)
                    item.selected_3G = true;

                int index_wifi = list_Wifi.indexOf(item.getId());
                if (index_wifi >= 0)
                    item.selected_Wifi = true;

                listapps.add(item);
                listuids.add(item.getId());
                // Log.i("uid: ", item.getId() + " " + item.label);

            }
        }

    }

    /** Save block list */
    public static void saveRules(Context ctx) {
        SharedPreferences pre = ctx.getSharedPreferences(PREF_NAME, 0);
        SharedPreferences.Editor editor = pre.edit();
        // remove previous values
        editor.remove(KEY_3G);
        editor.remove(KEY_WIFI);
        editor.apply();
        // list block 3g
        StringBuilder block_3g = new StringBuilder();
        for (int i : list_3G) {
            block_3g.append(String.valueOf(i));
            block_3g.append("|");
        }
        editor.putString(KEY_3G, block_3g.toString());

        // list block wifi
        StringBuilder block_wifi = new StringBuilder();
        for (int i : list_Wifi) {
            block_wifi.append(String.valueOf(i));
            block_wifi.append("|");
        }
        editor.putString(KEY_WIFI, block_wifi.toString());
        editor.commit();

        /** Update firewall after changing rules */
        applyIptablesRules(ctx, list_3G, list_Wifi, MODE_OFF, MODE_CUR);

        Toast.makeText(ctx, "Rules Saved!", Toast.LENGTH_SHORT).show();
    }

    /**
     * get list of uid with is blocked from 3G or Wifi
     *
     * */
    public static void getBlockedApps(Context ctx) {
        SharedPreferences pre = ctx.getSharedPreferences(PREF_NAME, 0);
        String list_3g = pre.getString(KEY_3G, "");
        String list_wifi = pre.getString(KEY_WIFI, "");

        if (!list_3g.equals("")) {
            String[] tmp_3g = list_3g.split("\\|");
            Log.d("length_3g: ", list_3g + "");
            list_3G = new ArrayList<Integer>();
            for (String i : tmp_3g) {
                Log.i("blocked: ", i);
                if (i != "")
                    list_3G.add(Integer.parseInt(i));
            }
        }

        if (!list_wifi.equals("")) {
            list_Wifi = new ArrayList<Integer>();
            String[] tmp_wifi = list_wifi.split("\\|");
            for (String i : tmp_wifi) {
                if (i != "")
                    list_Wifi.add(Integer.parseInt(i));
            }
        }

    }

    /**
     * Runs a script, wither as root or as a regular user (multiple commands
     * separated by "\n").
     *
     * @param ctx
     *            mandatory context
     * @param script
     *            the script to be executed
     * @param res
     *            the script output response (stdout + stderr)
     * @param timeout
     *            timeout in milliseconds (-1 for none)
     * @return the script exit code
     */
    public static int runScript(Context ctx, String script, StringBuilder res,
                                long timeout, boolean root) {
        final File file = new File(ctx.getDir("bin", 0), SCRTPT_NAME);
        final ScriptExecuter executer = new ScriptExecuter(file, script, res,
                root);
        executer.start();

        //
        try {
            if (timeout > 0) {
                executer.join(timeout);
            } else {
                executer.join();
            }

            if (executer.isAlive()) {
                // TImed out
                executer.interrupt();
                executer.join(150);
                executer.destroy();
                executer.join(50);
            }
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return executer.exitCode;
    }

    public static String scriptHeader(Context ctx) {
        final String dir = ctx.getDir("bin", 0).getAbsolutePath();
        Log.i("DIR:", dir);
        final String myiptables = dir + "/iptables_armv5";
        return "" + "IPTABLES=iptables\n" + "BUSYBOX=busybox\n" + "GREP=grep\n"
                + "ECHO=echo\n" + "# Try to find busybox\n" + "if "
                + dir
                + "/busybox_g1 --help >/dev/null 2>/dev/null ; then\n"
                + "	BUSYBOX="
                + dir
                + "/busybox_g1\n"
                + "	GREP=\"$BUSYBOX grep\"\n"
                + "	ECHO=\"$BUSYBOX echo\"\n"
                + "elif busybox --help >/dev/null 2>/dev/null ; then\n"
                + "	BUSYBOX=busybox\n"
                + "elif /system/xbin/busybox --help >/dev/null 2>/dev/null ; then\n"
                + "	BUSYBOX=/system/xbin/busybox\n"
                + "elif /system/bin/busybox --help >/dev/null 2>/dev/null ; then\n"
                + "	BUSYBOX=/system/bin/busybox\n"
                + "fi\n"
                + "# Try to find grep\n"
                + "if ! $ECHO 1 | $GREP -q 1 >/dev/null 2>/dev/null ; then\n"
                + "	if $ECHO 1 | $BUSYBOX grep -q 1 >/dev/null 2>/dev/null ; then\n"
                + "		GREP=\"$BUSYBOX grep\"\n"
                + "	fi\n"
                + "	# Grep is absolutely required\n"
                + "	if ! $ECHO 1 | $GREP -q 1 >/dev/null 2>/dev/null ; then\n"
                + "		$ECHO The grep command is required. FireWall will not work.\n"
                + "		exit 1\n"
                + "	fi\n"
                + "fi\n"
                + "# Try to find iptables\n"
                + "if "
                + myiptables
                + " --version >/dev/null 2>/dev/null ; then\n"
                + "	IPTABLES="
                + myiptables + "\n" + "fi\n" + "";
    }

    /**
     * Copies a raw resource file, given its ID to the given location
     *
     * @param ctx
     *            context
     * @param
     * @param file
     *            destination file
     * @param mode
     *            file permissions (E.g.: "755")
     * @throws IOException
     *             on error
     * @throws InterruptedException
     *             when interrupted
     */
    public static void copyRawFile(Context ctx, int resId, File file,
                                   String mode) {
        final String path = file.getAbsolutePath();

        // Write the Iptables binary

        try {
            FileOutputStream out = new FileOutputStream(file);
            final InputStream is = ctx.getResources().openRawResource(resId);
            byte buf[] = new byte[1024];
            int len;
            while ((len = is.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.close();
            is.close();

            // ContextAware the Permission
            Runtime.getRuntime().exec("chmod " + mode + " " + path).waitFor();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * Asserts that the binary files are installed in the cache directory.
     *
     * @param ctx
     *            context
     * @param showErrors
     *            indicates if errors should be alerted
     * @return false if the binary files could not be installed
     */
    public static boolean assertBinaries(Context ctx, boolean showErrors) {
        boolean changed = false;
        try {
            // Check iptables_armv5
            File file = new File(ctx.getDir("bin", 0), "iptables_armv5");
            if (!file.exists() || file.length() != 198652) {
                copyRawFile(ctx, R.raw.iptables_armv5, file, "755");
                changed = true;
            }
            // Check busybox
            file = new File(ctx.getDir("bin", 0), "busybox_g1");
            if (!file.exists()) {
                copyRawFile(ctx, R.raw.busybox_g1, file, "755");
                changed = true;
            }
            if (changed) {
                Toast.makeText(ctx, R.string.bin_installed, Toast.LENGTH_LONG)
                        .show();
            }
        } catch (Exception e) {
            if (showErrors)
                Log.e("error: ", "Error installing binary files: " + e);
            return false;
        }
        return true;
    }

    //
    public static boolean applyIptablesRules(Context ctx,
                                             List<Integer> list_3g, List<Integer> list_wifi, boolean Error,
                                             boolean enableFW) {

        assertBinaries(ctx, Error);
        final String ITFS_WIFI[] = { "tiwlan+", "wlan+", "eth+", "ra+" };
        final String ITFS_3G[] = { "rmnet+", "pdp+", "ppp+", "uwbr+", "wimax+",
                "vsnet+", "ccmni+", "usb+" };
        final StringBuilder script = new StringBuilder();
        try {
            int code;

            script.append(scriptHeader(ctx));
            script.append(""
                    + "$IPTABLES --version || exit 1\n"
                    + "# Create the firewall chains if necessary\n"
                    + "$IPTABLES -L wall411 >/dev/null 2>/dev/null || $IPTABLES --new wall411 || exit 2\n"
                    + "$IPTABLES -L wall411-3g >/dev/null 2>/dev/null || $IPTABLES --new wall411-3g || exit 3\n"
                    + "$IPTABLES -L wall411-wifi >/dev/null 2>/dev/null || $IPTABLES --new wall411-wifi || exit 4\n"
                    + "$IPTABLES -L wall411-reject >/dev/null 2>/dev/null || $IPTABLES --new wall411-reject || exit 5\n"
                    + "# Add wall411 chain to OUTPUT chain if necessary\n"
                    + "$IPTABLES -L OUTPUT | $GREP -q wall411 || $IPTABLES -A OUTPUT -j wall411 || exit 6\n"
                    + "# Flush existing rules\n"
                    + "$IPTABLES -F wall411 || exit 7\n"
                    + "$IPTABLES -F wall411-3g || exit 8\n"
                    + "$IPTABLES -F wall411-wifi || exit 9\n"
                    + "$IPTABLES -F wall411-reject || exit 10\n" + "");

            script.append("" + "# Create the reject rule (log disabled)\n"
                    + "$IPTABLES -A wall411-reject -j REJECT || exit 11\n" + "");

            script.append("# Main rules (per interface)\n");

            for (final String itf : ITFS_3G) {
                script.append("$IPTABLES -A wall411 -o ").append(itf)
                        .append(" -j wall411-3g || exit\n");
            }
            for (final String itf : ITFS_WIFI) {
                script.append("$IPTABLES -A wall411 -o ").append(itf)
                        .append(" -j wall411-wifi || exit\n");
            }

            final String targetRule = "wall411-reject";

            if (any_3g) {
				/* block all applications from using 3G */
                script.append("$IPTABLES -A wall411-3g -j ").append(targetRule)
                        .append(" || exit\n");
            } else {
				/* release/block individual applications from using 3g */
                for (final Integer uid : list_3g) {
                    if (uid >= 0)
                        script.append(
                                "$IPTABLES -A wall411-3g -m owner --uid-owner ")
                                .append(uid).append(" -j ").append(targetRule)
                                .append(" || exit\n");
                }
            }

            if (any_wifi) {
				/* block all applications from using Wifi */
                script.append("$IPTABLES -A wall411-wifi -j ")
                        .append(targetRule).append(" || exit\n");
            } else {
				/* release/block individual applications from using wifi */
                for (final Integer uid : list_wifi) {
                    if (uid >= 0)
                        script.append(
                                "$IPTABLES -A wall411-wifi -m owner --uid-owner ")
                                .append(uid).append(" -j ").append(targetRule)
                                .append(" || exit\n");
                }
            }

            final StringBuilder res = new StringBuilder();
            code = runScriptAsRoot(ctx, script.toString(), res);

            Api.MODE_CUR=enableFW;
            if (Error && code != 0) {
                String msg = res.toString();
                Log.e("FireWall", msg);
                // Remove unnecessary help message from output
                if (msg.indexOf("\nTry `iptables -h' or 'iptables --help' for more information.") != -1) {
                    msg = msg
                            .replace(
                                    "\nTry `iptables -h' or 'iptables --help' for more information.",
                                    "");
                }
                Log.e("error: ", "Error applying iptables rules. Exit code: "
                        + code + "\n\n" + msg.trim());
            } else {
                updateModeStatePref(ctx, enableFW);
                return true;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            if (Error)
                Log.e("ERROR: ", "error refreshing iptables: " + e);
        }
        return false;

    }

    public static void updateModeStatePref(Context ctx, boolean mode) {
        final SharedPreferences prefs = ctx.getSharedPreferences(Api.PREF_NAME,
                0);
        final SharedPreferences.Editor editor = prefs.edit();
        editor.remove(BLOCK_MODE);
        editor.apply();
        editor.putBoolean(BLOCK_MODE, mode);

        editor.commit();
    }

    /**
     * Runs a script as root (multiple commands separated by "\n") with a
     * default timeout of 20 seconds.
     *
     * @param ctx
     *            mandatory context
     * @param script
     *            the script to be executed
     * @param res
     *            the script output response (stdout + stderr)
     /** @param timeout
     *            timeout in milliseconds (-1 for none)
     * @return the script exit code
     * @throws IOException
     *             on any error executing the script, or writing it to disk
     */
    public static int runScriptAsRoot(Context ctx, String script,
                                      StringBuilder res) throws IOException {
        return runScriptAsRoot(ctx, script, res, 40000);
    }

    /**
     * Runs a script as root (multiple commands separated by "\n").
     *
     * @param ctx
     *            mandatory context
     * @param script
     *            the script to be executed
     * @param res
     *            the script output response (stdout + stderr)
     * @param timeout
     *            timeout in milliseconds (-1 for none)
     * @return the script exit code
     */
    public static int runScriptAsRoot(Context ctx, String script,
                                      StringBuilder res, long timeout) {
        return runScript(ctx, script, res, timeout, true);
    }

    /**
     * Check if we have root access
     *
     * @param ctx
     *            mandatory context
     * @param showErrors
     *            indicates if errors should be alerted
     * @return boolean true if we have root
     */
    public static boolean hasRootAccess(final Context ctx, boolean showErrors) {
        if (isRoot)
            return true;
        final StringBuilder res = new StringBuilder();
        try {
            // Run an empty script just to check root access
            if (runScriptAsRoot(ctx, "exit 0", res) == 0) {
                isRoot = true;
                return true;
            }
        } catch (Exception e) {
            Log.e("PL:", e.toString());
        }
        if (showErrors) {
            Log.e("ERROR: ",
                    "Could not acquire root access.\n"
                            + "You need a rooted phone to run wall411.\n\n"
                            + "If this phone is already rooted, please make sure DroidWall has enough permissions to execute the \"su\" command.\n"
                            + "Error message: " + res.toString());
        }
        return false;
    }

    // Clean Iptables Rules
    public static boolean cleanIptablesRules(Context ctx) {
        final StringBuilder res = new StringBuilder();

        try {
            assertBinaries(ctx, false);
            final StringBuilder script = new StringBuilder();
            script.append(scriptHeader(ctx));
            script.append("" + "$IPTABLES -F wall411\n"
                    + "$IPTABLES -F wall411-reject\n"
                    + "$IPTABLES -F wall411-3g\n"
                    + "$IPTABLES -F wall411-wifi\n" + "");
            updateModeStatePref(ctx, false);
            int code = runScriptAsRoot(ctx, script.toString(), res);
            if (code == -1) {
                Log.e("Error: ", "Error purging iptables. exit code: " + code
                        + "\n" + res);
                return false;
            }

            return true;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }

    }

}
