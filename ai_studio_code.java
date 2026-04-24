package com.focusguard.pro;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.net.VpnService;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private int selectedLimit = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView limitText = findViewById(R.id.limitText);
        SeekBar seekBar = findViewById(R.id.minutesSeekBar);
        Button startBtn = findViewById(R.id.startBtn);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                selectedLimit = Math.max(1, progress);
                limitText.setText("Daily Limit: " + selectedLimit + " Minutes");
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        startBtn.setOnClickListener(v -> {
            if (!checkUsageStatsPermission()) {
                Toast.makeText(this, "Please allow Usage Access", Toast.LENGTH_LONG).show();
                startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
                return;
            }

            Intent vpnIntent = VpnService.prepare(this);
            if (vpnIntent != null) {
                startActivityForResult(vpnIntent, 101);
            } else {
                onActivityResult(101, RESULT_OK, null);
            }
        });
    }

    private boolean checkUsageStatsPermission() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, 
                android.os.Process.myUid(), getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK) {
            Intent serviceIntent = new Intent(this, FocusVpnService.class);
            serviceIntent.putExtra("limit", selectedLimit);
            startService(serviceIntent);
            Toast.makeText(this, "Monitoring Started!", Toast.LENGTH_SHORT).show();
        }
    }
}