package com.media.core;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.media.core.ui.door.DoorActivity;
import com.media.core.ui.door.DoorListActivity;
import com.media.core.ui.group.GroupActivity;
import com.media.core.ui.group.GroupListActivity;
import com.media.core.ui.p2p.P2PActivity;
import com.media.core.ui.p2p.P2PListActivity;
import com.media.core.ui.room.RoomListActivity;
import com.media.core.ui.utils.SPUtils;
import com.media.rtc.MediaSDK;
import com.media.rtc.listener.RegisterListener;
import com.media.rtc.media.door.listener.DoorListener;
import com.media.rtc.media.door.listener.state.DoorState;
import com.media.rtc.media.group.listener.GroupListener;
import com.media.rtc.media.group.listener.state.GroupState;
import com.media.rtc.media.p2p.listener.P2PListener;
import com.media.rtc.media.p2p.listener.state.P2PState;
import com.web.socket.utils.StringUtils;


public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        RegisterListener,
        GroupListener,
        P2PListener,
        DoorListener {

    private TextView tViewClient, tViewState;
    private Button btnGroupCall, btnRoom;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tViewClient = findViewById(R.id.tViewClient);
        tViewState = findViewById(R.id.tViewState);

        findViewById(R.id.btnP2PCall).setOnClickListener(this);

        btnGroupCall = findViewById(R.id.btnGroupCall);
        btnGroupCall.setOnClickListener(this);

        btnRoom = findViewById(R.id.btnRoom);
        btnRoom.setOnClickListener(this);

        findViewById(R.id.btnDoor).setOnClickListener(this);

        //注册状态监听
        MediaSDK.addRegisterListener(this);
        //注册P2P
        MediaSDK.p2p().addListener(this);
        //注册群呼监听
        MediaSDK.group().addListener(this);
        //注册门禁监听
        MediaSDK.door().addListener(this);
        String clientId = SPUtils.getString(this, MainConstant.SP_KEY_CLIENT, "");
        if (StringUtils.isEmpty(clientId)) {
            clientDialog();
        } else {
            MediaSDK.register(clientId);
            tViewClient.setText(clientId);
        }

        //开启摄像头
        //CameraHelper.instance().init(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnP2PCall) {
            startActivity(new Intent(this, P2PListActivity.class));
        } else if (id == R.id.btnGroupCall) {
            startActivity(new Intent(this, GroupListActivity.class));
        } else if (id == R.id.btnRoom) {
            startActivity(new Intent(this, RoomListActivity.class));
        } else if (id == R.id.btnDoor) {
            startActivity(new Intent(this, DoorListActivity.class));
        }
    }

    @Override
    public void onRegister(int code) {
        tViewState.setText(String.valueOf(code));
    }

    private void clientDialog() {
        EditText editText = new EditText(this);
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("请输入一个账号")
                .setIcon(R.mipmap.ic_launcher)
                .setView(editText)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {//添加"Yes"按钮
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String clientId = editText.getText().toString();
                        if (StringUtils.isEmpty(clientId)) {
                            Toast.makeText(MainActivity.this, "账号不能为null！", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        SPUtils.putString(MainActivity.this, MainConstant.SP_KEY_CLIENT, clientId);
                        MediaSDK.register(clientId);
                        tViewClient.setText(clientId);
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
    public void onGroupListener(GroupState groupState) {
        if (groupState instanceof GroupState.Offer) {
            //DoorState.Offer offer = (DoorState.Offer) doorState;
            Intent intent = new Intent(MainActivity.this, GroupActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onP2PListener(P2PState p2pState) {
        if (p2pState instanceof P2PState.Offer) {
            Intent intent = new Intent(MainActivity.this, P2PActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onDoorListener(DoorState doorState) {
        if (doorState instanceof DoorState.Offer) {
            //DoorState.Offer offer = (DoorState.Offer) doorState;
            Intent intent = new Intent(MainActivity.this, DoorActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MediaSDK.removeRegisterListener(this);
        MediaSDK.p2p().removeListener(this);
        MediaSDK.group().removeListener(this);
        MediaSDK.door().removeListener(this);
        //关闭摄像头
        //CameraHelper.instance().stop();
    }
}