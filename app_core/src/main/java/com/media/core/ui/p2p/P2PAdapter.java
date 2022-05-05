package com.media.core.ui.p2p;

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

public class P2PAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<String> clients;

    public P2PAdapter(Context context, List<String> clients) {
        this.context = context;
        this.clients = clients;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_p2p, parent, false);
        return new NormalHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        NormalHolder normalHolder = (NormalHolder) holder;
        normalHolder.tViewClient.setText(clients.get(position));
        normalHolder.tViewCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, P2PActivity.class);
                intent.putExtra("client", clients.get(position));
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return clients.size();
    }

    public static class NormalHolder extends RecyclerView.ViewHolder {

        public TextView tViewClient, tViewCall;

        public NormalHolder(View itemView) {
            super(itemView);
            tViewClient = itemView.findViewById(R.id.tViewClient);
            tViewCall = itemView.findViewById(R.id.tViewCall);
        }

    }
}