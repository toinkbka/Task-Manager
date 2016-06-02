package main.hut;

import android.util.Log;

import java.io.DataOutputStream;
import java.io.PrintWriter;

/**
 * Created by Lai Dong on 4/12/2016.
 */
public class ShellUtils {
    public static boolean runCmd(String cmd) {
        try {
            boolean root = true;
            Process process = null;
            if (root) {
                process = Runtime.getRuntime().exec("su");
                PrintWriter pw = new PrintWriter(process.getOutputStream());
                pw.println(cmd);
                pw.flush();
                pw.close();
                process.waitFor();
                Log.v(cmd, "CMD");
                Log.d("Authorities", "root");
            } else {
                process = Runtime.getRuntime().exec(cmd);
                process.waitFor();
                Log.d("Authorities", "no root");
            }
            if (process!=null) {
                return process.exitValue()!=0 ? false : true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void runAsRoot(String[] mCommands){

        try {
            Process mProcess = Runtime.getRuntime().exec("su");
            DataOutputStream mOS = new DataOutputStream(mProcess.getOutputStream());
            for (String mCommand : mCommands) {
                mOS.writeBytes(mCommand + "\n");
            }
            mOS.writeBytes("exit\n");
            mOS.flush();

        }catch (Exception o){
            Log.e("Exception: ", o.toString());
        }

    }
    public static String sudo(String... command) throws Exception {
        return sudo(command);
    }
}
