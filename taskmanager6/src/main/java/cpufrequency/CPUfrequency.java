package cpufrequency;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.hust.activity.R;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.CommandCapture;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

import main.hut.MainActivity;

/**
 * Created by Lai Dong on 5/24/2016.
 */
public class CPUfrequency extends Fragment {
    static Context context;
    String[] availableFrequenciesArray;
    String selectedGovernor, selectedMaximumFrequency, selectedMinimumFrequency;
    int selectedMaxFreq, selectedMinFreq;
    Button applySelectedCPUFrequencyButton,applygovernorButton, selectMaximumCPUFrequencyButton, selectMinimumCPUFrequencyButton;
    Spinner maxFreqSpinner, minFreqSpinner, governorSpinner;
    ArrayList<String> availableFrequencies, availableFrequenciesForSpinner,
            availableGovernorsForSpinner;
    int frequency;
    String selectedRealMaximumFrequency, selectedRealMinimumFrequency, governor, currentCPUFrequency;
    TextView currentGovernorTV;
    TextView currentCPUFrequencyTV,currentCPUMaxFrequencyTV, currentCPUMinFrequencyTV;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
       final View view = inflater.inflate(R.layout.activity_cpufrequency, container, false);
        _context();
        _declareBaseMaximumFrequencyVariable();
        _declareBaseMinimumFrequencyVariable();

        currentGovernorTV = (TextView) view.findViewById(R.id.currentGovernor);
        governorSpinner = (Spinner) view.findViewById(R.id.governorSpinner);
        applySelectedCPUFrequencyButton = (Button) view.findViewById(
                R.id.applySelectedCPUFrequenciesButton);
        applygovernorButton = (Button) view.findViewById(R.id.applyGovernor);

        _currentCPUFrequencyTextViewUpdate(view);
        _currentCPUMaxFrequencyTextViewUpdate(view);
        _currentCPUMinFrequencyTextViewUpdate(view);

        try {
            _readAvailableFrequencies();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            _readAvailableFrequenciesForSpinner();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            _readAvailableGovernorsForSpinner();
        } catch (Exception e) {
            e.printStackTrace();
        }

        _readCurrentGovernor();
        selectMinimumCPUFrequencyButton = (Button) view.findViewById(
                R.id.selectMinimumFrequencyButton);
        selectMaximumCPUFrequencyButton = (Button) view.findViewById(
                R.id.selectMaximumFrequencyButton);
        selectMinimumCPUFrequencyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    __selectMinimumCPUFrequencyDialog();
            }
        });

        selectMaximumCPUFrequencyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _selectMaximumCPUFrequencyDialog();
            }
        });

        applySelectedCPUFrequencyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _applySelectedFrequencies(view);
            }
        });

        applygovernorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _applySelectedGovernor(view);
            }
        });

        return view;
    }


    private String _declareBaseMaximumFrequencyVariable() {
        return selectedMaximumFrequency = "";
    }

    private String _declareBaseMinimumFrequencyVariable() {
        return selectedMinimumFrequency = "";
    }

    public Context _context() {
        context = getContext();

        return context;
    }

    public String[] _readAvailableFrequencies() throws Exception {
        File scalingAvailableFrequenciesFile = new File(
                "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies");
        availableFrequencies = new ArrayList<>();
        Scanner scanner = new Scanner(scalingAvailableFrequenciesFile);
        while (scanner.hasNext()) {
            availableFrequencies.add(scanner.next());
        }

        availableFrequenciesArray = new String[availableFrequencies.size()];
        availableFrequenciesArray = availableFrequencies.toArray(availableFrequenciesArray);

        return availableFrequenciesArray;
    }

    public ArrayList<String> _readAvailableFrequenciesForSpinner() throws Exception {
        File scalingAvailableFrequenciesFile = new File(
                "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies");
        availableFrequenciesForSpinner = new ArrayList<>();
        Scanner scanner = new Scanner(scalingAvailableFrequenciesFile);
        while (scanner.hasNext()) {
            frequency = scanner.nextInt();
            switch (String.valueOf(frequency).length()) {
                case 6:
                    int frequencyInMHz = frequency / 1000;
                    String frequencyInMHzString = String.valueOf(frequencyInMHz);
                    availableFrequenciesForSpinner.add(frequencyInMHzString + " MHz");
                    break;
                case 7:
                    String frequencyInGHzString = String.valueOf(frequency);
                    double frequencyInGHzDouble = Double.valueOf(frequencyInGHzString);
                    double frequencyToGHz = frequencyInGHzDouble / 1000000;
                    DecimalFormat df = new DecimalFormat();
                    df.setMaximumFractionDigits(2);
                    String frequencyToGHzString = String.valueOf(df.format(frequencyToGHz));
                    availableFrequenciesForSpinner.add(frequencyToGHzString + " GHz");
            }
        }

        _selectMaximumFrequencySpinner(availableFrequenciesForSpinner);
        _selectMinimumFrequencySpinner(availableFrequenciesForSpinner);

        return availableFrequenciesForSpinner;
    }

    public void _readAvailableGovernorsForSpinner() throws Exception {
        File scalingAvailableGovernorsFile = new File(
                "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_governors");
        availableGovernorsForSpinner = new ArrayList<>();
        Scanner scanner4 = new Scanner(scalingAvailableGovernorsFile);
        while (scanner4.hasNext()) {
            governor = scanner4.next();
            availableGovernorsForSpinner.add(governor);
        }
        _selectGovernorSpinner(availableGovernorsForSpinner);
    }

    private String _selectGovernorSpinner(final List<String> availableGovernorsForSpinner) {
        ArrayAdapter<String> adapter4 = new ArrayAdapter<String>(context, R.layout.row_layout_frequency,
                availableGovernorsForSpinner);
        governorSpinner.setAdapter(adapter4);
        governorSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position,
                                               long id) {
                        selectedGovernor = availableGovernorsForSpinner.get(position);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                }
        );

        return selectedGovernor;
    }

    public int _selectMaximumFrequencySpinner(final List<String> availableFrequenciesForSpinner) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                R.layout.row_layout_frequency, availableFrequenciesForSpinner);
        maxFreqSpinner.setAdapter(adapter);
        maxFreqSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> arg0, View arg1,
                                               int arg2, long arg3) {
                        selectedRealMaximumFrequency = availableFrequenciesArray[arg2];
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {
//                        onVisibleBehindCanceled();
                    }
                });

        return selectedMaxFreq;
    }

    public int _selectMinimumFrequencySpinner(final List<String> availableFrequenciesForSpinner) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                R.layout.row_layout_frequency, availableFrequenciesForSpinner);
        minFreqSpinner.setAdapter(adapter);
        minFreqSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> arg0, View arg1,
                                               int arg2, long arg3) {
                        selectedRealMinimumFrequency = availableFrequenciesArray[arg2];
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {
//                        onVisibleBehindCanceled();
                    }
                });

        return selectedMinFreq;
    }

    public void _applySelectedFrequencies(View view) {
        _checkForSelectedFrequencies();
    }

    public String _readCurrentCPUFrequency() {
        ProcessBuilder readOutCurrentCPUFrequency;
        String currentCPUFrequency = "";

        try {
            String[] currentCPUFrequencyFile = {"/system/bin/cat",
                    "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq"};
            readOutCurrentCPUFrequency = new ProcessBuilder(currentCPUFrequencyFile);
            Process readProcess = readOutCurrentCPUFrequency.start();
            InputStream readInCurrentCPUFrequency = readProcess.getInputStream();
            byte[] read = new byte[1024];
            while (readInCurrentCPUFrequency.read(read) != -1) {
                currentCPUFrequency = currentCPUFrequency + new String(read);
            }
            readInCurrentCPUFrequency.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return currentCPUFrequency;
    }

    public String _readCurrentMaxCPUFrequency() {
        ProcessBuilder readOutCurrentMaxCPUFrequency;
        String currentMaxCPUFrequency = "";

        try {
            String[] currentMaxCPUFrequencyFile = {"/system/bin/cat",
                    "/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq"};
            readOutCurrentMaxCPUFrequency = new ProcessBuilder(currentMaxCPUFrequencyFile);
            Process readProcess2 = readOutCurrentMaxCPUFrequency.start();
            InputStream readInCurrentMaxCPUFrequency = readProcess2.getInputStream();
            byte[] read2 = new byte[1024];
            while (readInCurrentMaxCPUFrequency.read(read2) != -1) {
                currentMaxCPUFrequency = currentMaxCPUFrequency + new String(read2);
            }
            readInCurrentMaxCPUFrequency.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return currentMaxCPUFrequency;
    }

    public String _readCurrentMinCPUFrequency() {
        ProcessBuilder readOutCurrentMinCPUFrequency;
        String currentMinCPUFrequency = "";

        try {
            String[] currentMinCPUFrequencyFile = {"/system/bin/cat",
                    "/sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq"};
            readOutCurrentMinCPUFrequency = new ProcessBuilder(currentMinCPUFrequencyFile);
            Process process3 = readOutCurrentMinCPUFrequency.start();
            InputStream readInCurrentMinCPUFrequency = process3.getInputStream();
            byte[] read3 = new byte[1024];
            while (readInCurrentMinCPUFrequency.read(read3) != -1) {
                currentMinCPUFrequency = currentMinCPUFrequency + new String(read3);
            }
            readInCurrentMinCPUFrequency.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return currentMinCPUFrequency;
    }

    public void _currentCPUFrequencyTextViewUpdate(final View view) {

        Thread currentCPUFrequencyUpdate = new Thread() {
            @Override
            public void run() {
                try {
                    //noinspection InfiniteLoopStatement
                    while (true) {
                        Thread.sleep(600);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String currentCPUFrequencyString = _readCurrentCPUFrequency();
                                String finalCurrentCPUFreq = currentCPUFrequencyString.trim();
                                currentCPUFrequencyTV = (TextView) view.findViewById(R.id.currentCPUFreq);
                                currentCPUFrequencyTV.setText(finalCurrentCPUFreq + " KHz");
                            }
                        });
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        currentCPUFrequencyUpdate.start();
    }

    public void _currentCPUMaxFrequencyTextViewUpdate(final View view) {

        try {
            Process proc = Runtime.getRuntime().exec("cat /sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq");
            InputStream input = proc.getInputStream();
            StringBuilder builder = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(input));
            String line = null;
            while((line = br.readLine()) != null ){
                builder.append(line);
//                builder.append("\n");
            }
            br.close();
            currentCPUMaxFrequencyTV = (TextView) view.findViewById(R.id.currentMaxCPUFreq);
            currentCPUMaxFrequencyTV.setText(builder.toString()+" KHZ");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void _currentCPUMinFrequencyTextViewUpdate(final View view) {

        Thread currentCPUMinFrequencyUpdate = new Thread() {
            @Override
            public void run() {
                try {
                    while (true) {
                        Thread.sleep(600);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String currentCPUMinFrequencyString = _readCurrentMinCPUFrequency();
                                String finalCurrentMinCPUFreq = currentCPUMinFrequencyString.trim();
                                currentCPUMinFrequencyTV = (TextView) view.findViewById(R.id.currentMinCPUFreq);
                                currentCPUMinFrequencyTV.setText(finalCurrentMinCPUFreq + " KHz");
                            }
                        });
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        currentCPUMinFrequencyUpdate.start();
    }

    public void _applySelectedGovernor(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Are you sure?");
        builder.setMessage("Save change!");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    CommandCapture setGovernor = new CommandCapture(0,
                            "echo \"" + selectedGovernor +
                                    "\" > /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor");
                    RootTools.getShell(true).add(setGovernor);
                } catch (RootDeniedException | IOException rde) {
                    rde.printStackTrace();
                    Toast.makeText(context, rde.getMessage(), Toast.LENGTH_LONG).show();
                } catch (TimeoutException te) {
                    te.printStackTrace();
                    Toast.makeText(context, te.getMessage(), Toast.LENGTH_SHORT).show();
                }
                Toast.makeText(context, "Set " + selectedGovernor + " governor",
                        Toast.LENGTH_LONG).show();
                _readCurrentGovernor();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();

        _readCurrentGovernor();
    }

    public String _readCurrentGovernor() {
        ProcessBuilder readOutCurrentGovernor;
        String currentGovernor = "";

        try {
            String[] currentGovernorFile = {"/system/bin/cat",
                    "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor"};
            readOutCurrentGovernor = new ProcessBuilder(currentGovernorFile);
            Process readProcess4 = readOutCurrentGovernor.start();
            InputStream readInCurrentGovernor = readProcess4.getInputStream();
            byte[] read4 = new byte[1024];
            while (readInCurrentGovernor.read(read4) != -1) {
                currentGovernor = currentGovernor + new String(read4);
            }
            readInCurrentGovernor.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
//        _setCurrentGovernorTextLabel(currentGovernor);
        currentGovernorTV.setText(currentGovernor + "");
        return currentGovernor;
    }

    public void __selectMinimumCPUFrequencyDialog(){
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(context);
        builderSingle.setTitle("Select minimum CPU frequency:");
        final ArrayAdapter<String> arrayAdapter;
        arrayAdapter = new ArrayAdapter<String>(context, R.layout.row_layout_frequency,
                availableFrequenciesForSpinner);
        builderSingle.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builderSingle.setAdapter(arrayAdapter,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedMinimumFrequency = availableFrequenciesArray[which];

                        String selectedMinimumValieWhich = availableFrequenciesForSpinner.get(
                                which);
                        selectMinimumCPUFrequencyButton.setText(selectedMinimumValieWhich);
                    }
                });
        builderSingle.show();
    }

    public void _selectMaximumCPUFrequencyDialog(){
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(context);
        builderSingle.setTitle("Select maximum CPU frequency:");
        final ArrayAdapter<String> arrayAdapter;
        arrayAdapter = new ArrayAdapter<String>(context, R.layout.row_layout_frequency,
                availableFrequenciesForSpinner);
        builderSingle.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builderSingle.setAdapter(arrayAdapter,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedMaximumFrequency = availableFrequenciesArray[which];
                        String selectedMaximumValueWhich = availableFrequenciesForSpinner.get(which);
                        selectMaximumCPUFrequencyButton.setText(selectedMaximumValueWhich);
                    }
                });
        builderSingle.show();
    }

    public void _checkForSelectedFrequencies() {
        if (selectedMaximumFrequency.equals("")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Loser");
            builder.setMessage("Select max?");
            builder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            builder.show();
        } else {
            _checkMinimumVariable();
        }
    }

    private void _checkMinimumVariable() {
        if (selectedMinimumFrequency.equals("")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Loser");
            builder.setMessage("Select min?");
            builder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            builder.show();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Are you sure?");
            builder.setMessage("Save change");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    try {
                        CommandCapture setMaximumCPUFrequency = new CommandCapture(0,
                                "echo \"" + selectedMaximumFrequency +
                                        "\" > /sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq");
                        RootTools.getShell(true).add(setMaximumCPUFrequency);
                    } catch (RootDeniedException | IOException rde) {
                        rde.printStackTrace();
                        Toast.makeText(context, rde.getMessage(), Toast.LENGTH_LONG).show();
                    } catch (TimeoutException te) {
                        te.printStackTrace();
                        Toast.makeText(context, te.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    try {
                        CommandCapture setMinimumCPUFrequency = new CommandCapture(0,
                                "echo \"" + selectedMinimumFrequency +
                                        "\" > /sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq");
                        RootTools.getShell(true).add(setMinimumCPUFrequency);
                    } catch (RootDeniedException | IOException rde) {
                        rde.printStackTrace();
                        Toast.makeText(context, rde.getMessage(), Toast.LENGTH_LONG).show();
                    } catch (TimeoutException te) {
                        te.printStackTrace();
                        Toast.makeText(context, te.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    Toast.makeText(context, "Set " + selectedMaximumFrequency +
                            " as maximum frequency\nSet " + selectedMinimumFrequency +
                            " as minimum frequency", Toast.LENGTH_LONG).show();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
        }
    }


}
