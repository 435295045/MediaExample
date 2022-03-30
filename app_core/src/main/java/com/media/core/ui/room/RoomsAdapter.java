package com.media.core.ui.room;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.media.core.R;
import com.media.rtc.MediaSDK;

import java.util.List;

public class RoomsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<String> rooms;

    public RoomsAdapter(Context context, List<String> rooms) {
        this.context = context;
        this.rooms = rooms;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_room, parent, false);
        return new NormalHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        NormalHolder normalHolder = (NormalHolder) holder;
        normalHolder.tViewRoom.setText(rooms.get(position));
        normalHolder.tViewJoni.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(context, RoomActivity.class);
                    intent.putExtra("roomId", rooms.get(position));
                    context.startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }

    public static class NormalHolder extends RecyclerView.ViewHolder {

        public TextView tViewRoom, tViewJoni;

        public NormalHolder(View itemView) {
            super(itemView);
            tViewRoom = itemView.findViewById(R.id.tViewRoom);
            tViewJoni = itemView.findViewById(R.id.tViewJoni);
        }
    }
}