package com.media.core.ui.door;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.media.core.R;
import com.media.core.ui.utils.SPUtils;
import com.media.rtc.MediaSDK;
import com.web.socket.utils.LoggerUtils;

import java.util.ArrayList;
import java.util.List;

public class DoorListActivity extends AppCompatActivity implements
        View.OnClickListener{
    private final String SP_CLIENTS_KEY = "SP_CLIENTS_KEY";
    private DoorAdapter doorAdapter;
    private final List<String> clients = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_list);
        findViewById(R.id.btnAdd).setOnClickListener(this);
        findViewById(R.id.btnCall).setOnClickListener(this);
        RecyclerView rView = findViewById(R.id.rView);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rView.setLayoutManager(linearLayoutManager);
        doorAdapter = new DoorAdapter(this, clients);
        rView.setAdapter(doorAdapter);
        //获取用户
        getClients();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnAdd) {
            addClient();
        } else if (id == R.id.btnCall) {//callDialog();
            String[] cs = doorAdapter.getClients();
            if (cs.length == 0) {
                Toast.makeText(this, "请先选择呼叫用户！", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, DoorActivity.class);
            intent.putExtra("clients", cs);
            startActivity(intent);
        }
    }

    private void getClients() {
        List<String> strings = SPUtils.getClazz(this, SP_CLIENTS_KEY, List.class);
        LoggerUtils.e("---------------------: " + strings);
        if (strings != null) {
            clients.clear();
            clients.addAll(strings);
            doorAdapter.notifyDataSetChanged();
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
                        List<String> strings = SPUtils.getClazz(DoorListActivity.this, SP_CLIENTS_KEY, List.class);
                        if (strings == null) {
                            strings = new ArrayList<>();
                        }
                        if (!strings.contains(roomId)) {
                            strings.add(roomId);
                            SPUtils.putClazz(DoorListActivity.this, SP_CLIENTS_KEY, strings);
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
