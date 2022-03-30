package com.media.core.ui.p2p;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.media.core.R;
import com.media.core.ui.utils.SPUtils;

import java.util.ArrayList;
import java.util.List;

public class P2PListActivity extends AppCompatActivity implements View.OnClickListener {
    private final String SP_ROOMS_KEY = "SP_CLIENTS_KEY";
    private P2PAdapter p2PAdapter;
    private final List<String> clients = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_p2p_list);
        findViewById(R.id.btnAdd).setOnClickListener(this);
        RecyclerView rView = findViewById(R.id.rView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rView.setLayoutManager(linearLayoutManager);
        p2PAdapter = new P2PAdapter(this, clients);
        rView.setAdapter(p2PAdapter);
        //获取用户
        getClients();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnAdd) {
            addClient();
        }
    }

    private void getClients() {
        List<String> strings = SPUtils.getClazz(this, SP_ROOMS_KEY, List.class);
        if (strings != null) {
            clients.clear();
            clients.addAll(strings);
            p2PAdapter.notifyDataSetChanged();
        }
    }

    private void addClient() {
        EditText editText = new EditText(this);
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("请输入一个用户")
                .setIcon(R.mipmap.ic_launcher)
                .setView(editText)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {//添加"Yes"按钮
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String roomId = editText.getText().toString();
                        List<String> strings = SPUtils.getClazz(P2PListActivity.this, SP_ROOMS_KEY, List.class);
                        if (strings == null) {
                            strings = new ArrayList<>();
                        }
                        if (!strings.contains(roomId)) {
                            strings.add(roomId);
                            SPUtils.putClazz(P2PListActivity.this, SP_ROOMS_KEY, strings);
                            getClients();
                        }
                        dialogInterface.dismiss();
                    }
                })

                .setNegativeButton("取消", new DialogInterface.OnClickListener() {//添加取消
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create();
        alertDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
