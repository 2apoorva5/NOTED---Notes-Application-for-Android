package com.developerdepository.noted;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import maes.tech.intentanim.CustomIntent;

public class AppInfoActivity extends AppCompatActivity {

    private ImageButton close;
    private TextView versionName, privacyPolicy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_info);

        close = findViewById(R.id.app_info_close_btn);
        versionName = findViewById(R.id.app_info_version_name);
        privacyPolicy = findViewById(R.id.app_info_privacy_policy);

        close.setOnClickListener(view -> onBackPressed());

        try {
            PackageInfo packageInfo = getApplicationContext().getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = packageInfo.versionName;
            versionName.setText(String.format("Version %s", version));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        privacyPolicy.setOnClickListener(v -> {
            String privacyPolicyUrl = "https://developerdepository.wixsite.com/noted-policies";
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyUrl));
            startActivity(browserIntent);
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void finish() {
        super.finish();
        CustomIntent.customType(AppInfoActivity.this, "up-to-bottom");
    }
}