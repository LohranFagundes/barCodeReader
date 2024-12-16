package com.example.barcodereader;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BarcodeAdapter extends RecyclerView.Adapter<BarcodeAdapter.BarcodeViewHolder> {

    private List<String> listaCodigos;

    public BarcodeAdapter(List<String> listaCodigos) {
        this.listaCodigos = listaCodigos;
    }

    @NonNull
    @Override
    public BarcodeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_barcode, parent, false);
        return new BarcodeViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull BarcodeViewHolder holder, int position) {
        holder.textViewCodigo.setText(listaCodigos.get(position));
    }

    @Override
    public int getItemCount() {
        return listaCodigos.size();
    }

    public void atualizarLista(List<String> novaLista) {
        this.listaCodigos = novaLista;
        notifyDataSetChanged();
    }

    public static class BarcodeViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewCodigo;

        public BarcodeViewHolder(View itemView) {
            super(itemView);
            textViewCodigo = itemView.findViewById(R.id.textViewCodigo);
        }
    }
}