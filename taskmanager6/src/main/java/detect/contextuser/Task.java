package detect.contextuser;

/**
 * Created by Lai Dong on 4/13/2016.
 */
public class Task {
    public int id;
    public String nameApp;
    public int count = 0;
    public String packApp;

    public Task(int id, String nameApp, int count, String packApp) {
        this.id = id;
        this.nameApp = nameApp;
        this.count = count;
        this.packApp = packApp;
    }

    public Task(String nameApp, int count, String packApp) {
        this.nameApp = nameApp;
        this.count = count;
        this.packApp = packApp;
    }

    public Task(int count, String nameApp) {
        this.count = count;
        this.nameApp = nameApp;
    }

    public Task(String nameApp) {
        this.nameApp = nameApp;
    }

    public Task() {
        super();
    }
}
