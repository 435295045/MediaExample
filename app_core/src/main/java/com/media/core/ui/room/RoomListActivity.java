package com.media.core.ui.room;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.media.core.R;
import com.media.core.ui.utils.SPUtils;
import com.media.rtc.MediaSDK;

import java.util.ArrayList;
import java.util.List;

public class RoomListActivity extends AppCompatActivity implements View.OnClickListener {
    private String SP_ROOMS_KEY = "SP_ROOMS_KEY";
    private RoomsAdapter roomsAdapter;
    private final List<String> rooms = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_list);
        findViewById(R.id.btnAdd).setOnClickListener(this);
        findViewById(R.id.btnJoin).setOnClickListener(this);

        RecyclerView rView = findViewById(R.id.rView);

        //线性布局
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rView.setLayoutManager(linearLayoutManager);
        roomsAdapter = new RoomsAdapter(this, rooms);
        rView.setAdapter(roomsAdapter);
        //获取房间
        getRooms();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnAdd) {
            addRoom();
        } else if (id == R.id.btnJoin) {
            joinRoom();
        }
    }

    private void getRooms() {
        List<String> strings = SPUtils.getClazz(RoomListActivity.this, SP_ROOMS_KEY, List.class);
        if (strings != null) {
            rooms.clear();
            rooms.addAll(strings);
            roomsAdapter.notifyDataSetChanged();
        }
    }

    private void addRoom() {
        EditText editText = new EditText(this);
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("请输入一个房间id")
                .setIcon(R.mipmap.ic_launcher)
                .setView(editText)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {//添加"Yes"按钮
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String roomId = editText.getText().toString();
                        List<String> strings = SPUtils.getClazz(RoomListActivity.this, SP_ROOMS_KEY, List.class);
                        if (strings == null) {
                            strings = new ArrayList<>();
                        }
                        strings.add(roomId);
                        SPUtils.putClazz(RoomListActivity.this, SP_ROOMS_KEY, strings);
                        getRooms();
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

    private void joinRoom() {
        EditText editText = new EditText(this);
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("请输入一个房间id")
                .setIcon(R.mipmap.ic_launcher)
                .setView(editText)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {//添加"Yes"按钮
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String roomId = editText.getText().toString();
                        try {
                            Intent intent = new Intent(RoomListActivity.this, RoomActivity.class);
                            intent.putExtra("roomId", roomId);
                            startActivity(intent);
                            dialogInterface.dismiss();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

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
}
