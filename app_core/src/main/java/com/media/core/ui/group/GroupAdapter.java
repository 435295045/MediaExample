package com.media.core.ui.group;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.media.core.R;

import java.util.ArrayList;
import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<String> clients;
    private static final List<String> selectClients = new ArrayList<>();

    public GroupAdapter(Context context, List<String> clients) {
        this.context = context;
        this.clients = clients;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_group, parent, false);
        return new NormalHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        NormalHolder normalHolder = (NormalHolder) holder;
        normalHolder.tViewClient.setText(clients.get(position));
        normalHolder.cBoxClient.setChecked(selectClients.contains(clients.get(position)));
        normalHolder.cBoxClient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectClients.contains(clients.get(position))) {
                    selectClients.remove(clients.get(position));
                } else {
                    selectClients.add(clients.get(position));
                }
                notifyDataSetChanged();
            }
        });
    }

    public String[] getClients() {
        return selectClients.toArray(new String[selectClients.size()]);
    }

    @Override
    public int getItemCount() {
        return clients.size();
    }

    public static class NormalHolder extends RecyclerView.ViewHolder {
        public CheckBox cBoxClient;
        public TextView tViewClient;

        public NormalHolder(View itemView) {
            super(itemView);
            tViewClient = itemView.findViewById(R.id.tViewClient);
            cBoxClient = itemView.findViewById(R.id.cBoxClient);
        }

    }
}