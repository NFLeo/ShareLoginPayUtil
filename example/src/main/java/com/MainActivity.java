package com;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import me.shaohui.shareutil.ShareConfig;
import me.shaohui.shareutil.ShareManager;

public class MainActivity extends AppCompatActivity {

    String APP_ID = "wx41592d9564208b37";
    String APP_SECRET = "XXXXXXX";

    private Button btnLogin;
    private Button btnShare;
    private Button btnPay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewBy();
        setListener();
        ShareConfig config = ShareConfig.instance().qqId("12345678")
                .weiboScope("XXX").weiboRedirectUrl("XXXX")
                .wxId(APP_ID).wxSecret(APP_SECRET);
        ShareManager.init(config);
    }

    private void findViewBy() {
        btnLogin = findViewById(R.id.btn_login);
        btnShare = findViewById(R.id.btn_share);
        btnPay = findViewById(R.id.btn_pay);
    }

    private void setListener() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginBottomDialog dialog = new LoginBottomDialog();
                dialog.show(getSupportFragmentManager());
            }
        });

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShareBottomDialog dialog = new ShareBottomDialog();
                dialog.show(getSupportFragmentManager());
            }
        });

        btnPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PayBottomDialog dialog = new PayBottomDialog();
                dialog.show(getSupportFragmentManager());
            }
        });
    }
}
