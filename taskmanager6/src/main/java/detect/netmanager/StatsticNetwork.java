package detect.netmanager;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hust.activity.R;

import main.hut.MainActivity;

/**
 * Created by Lai Dong on 4/12/2016.
 */
public class StatsticNetwork extends AppCompatActivity {
    TextView tx, rx, tp, rp;
    private Handler mHandler = new Handler();
    long mTotalRxBytes = 0;
    long mTotalTxBytes = 0;
    long mTotalRxPackets = 0;
    long mTotalTxPackets = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acivity_statstic_network);
        tx = (TextView)findViewById(R.id.tv_TX);
        rx = (TextView)findViewById(R.id.tv_RX);
        tp = (TextView)findViewById(R.id.tv_TP);
        rp = (TextView)findViewById(R.id.tv_RP);

        if(mTotalTxBytes == TrafficStats.UNSUPPORTED || mTotalRxBytes == TrafficStats.UNSUPPORTED
                || mTotalTxPackets == TrafficStats.UNSUPPORTED || mTotalRxPackets == TrafficStats.UNSUPPORTED ){
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Uh oh!");
            alert.setMessage("Your device does not support traffic stat monitoring");
            alert.show();
        }else{
            mHandler.postDelayed(mRunnable, 1000);
        }

    }
    private final Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            mTotalTxBytes = TrafficStats.getTotalTxBytes();
            mTotalRxBytes = TrafficStats.getTotalRxBytes();
            mTotalTxPackets = TrafficStats.getTotalTxPackets();
            mTotalRxPackets = TrafficStats.getTotalRxPackets();
            tx.setText(mTotalTxBytes + " bytes");
            rx.setText(mTotalRxBytes + " bytes");
            tp.setText(mTotalTxPackets + " Pcks");
            rp.setText(mTotalRxPackets + " Pcks");
            mHandler.postDelayed(mRunnable, 1000);
        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }
}
