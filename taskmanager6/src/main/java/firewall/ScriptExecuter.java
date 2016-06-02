package firewall;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

/**
 * Created by Lai Dong on 4/20/2016.
 */
public class ScriptExecuter extends Thread {
    final File file;
    final String script;
    final StringBuilder res;
    final boolean root;
    public int exitCode = -1;
    private Process exec;

    /**
     * Create a script Executer
     *
     * @param file
     *            temporary script file
     * @param script
     *            script to execute
     * @param res
     *            response output
     * @param root
     *            root Access or not
     */
    public ScriptExecuter(File file, String script, StringBuilder res,
                          boolean root) {
        this.file = file;
        this.script = script;
        this.res = res;
        this.root = root;
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub

        try {
            file.createNewFile();
            final String path = file.getAbsolutePath();
            Log.i("Path: ", path);
            // access excute permission for script
            Runtime.getRuntime().exec("chmod 755 " + path).waitFor();

            // Write the scripted to be executed
            final OutputStreamWriter out = new OutputStreamWriter(
                    new FileOutputStream(file));
            if (new File("/system/bin/sh").exists()) {
                out.write("#!/system/bin/sh\n");
            }

            out.write(script);
            if (!script.endsWith("\n"))
                out.write("\n");

            out.write("exit\n");
            out.flush();
            out.close();

            if (this.root) {
                // Request su for the script
                exec = Runtime.getRuntime().exec("su -c " + path);
                exec.waitFor();
                Log.i("su permission:", "ok");

            } else {
                // Create sh request to run the script
                exec = Runtime.getRuntime().exec("sh " + path);
                exec.waitFor();
            }

        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            if (res != null)
                res.append("\nOperation timed-out");
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            if (res != null)
                res.append("\n" + e);
            e.printStackTrace();
        } finally {
            destroy();
        }
    }

    /**
     * Destroy this script executer
     */
    public synchronized void destroy() {
        if (exec != null)
            exec.destroy();
        exec = null;
    }
}
