package com.example.barcodereader;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {


    private EditText suffixInput;
    private RecyclerView recyclerView;
    private BarcodeAdapter adapter;
    private ArrayList<String> barcodeList;
    private Uri selectedFileUri;
    private EditText manualBarcodeInput;
    private Button addButton;
    private int editingPosition = -1;
    private Button saveEditButton;


    private ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        selectedFileUri = data.getData();
                        try {
                            saveBarcodesToFile();
                            Toast.makeText(this, "Códigos de barras salvos com sucesso", Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            Log.e("MainActivity", "Erro ao salvar o arquivo", e);
                            Toast.makeText(this, "Erro ao salvar o arquivo", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        suffixInput = findViewById(R.id.suffixInput);
        recyclerView = findViewById(R.id.recyclerView);
        Button scanButton = findViewById(R.id.scanButton);
        Button saveButton = findViewById(R.id.saveButton);
        manualBarcodeInput = findViewById(R.id.manualBarcodeInput); // Inicializar
        addButton = findViewById(R.id.addButton); // Inicializar
        barcodeList = new ArrayList<>();
        adapter = new BarcodeAdapter(barcodeList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        saveEditButton = findViewById(R.id.saveEditButton); // Assuming you have this button in your layout
        saveEditButton.setVisibility(View.GONE); // Initially hide the button


        scanButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, BarcodeReaderActivity.class);
            startActivityForResult(intent, 1);
        });

        saveButton.setOnClickListener(v -> {
            if (barcodeList.isEmpty()) {
                Toast.makeText(this, "Nenhum código de barras para salvar", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TITLE, "codigos.txt");
            filePickerLauncher.launch(intent);
        });

        addButton.setOnClickListener(v -> {
            String manualBarcode = manualBarcodeInput.getText().toString();
            if (manualBarcode.isEmpty()) {
                Toast.makeText(MainActivity.this, "Digite um código de barras", Toast.LENGTH_SHORT).show();
            } else {
                String suffix = suffixInput.getText().toString();
                if (!suffix.isEmpty()) {
                    manualBarcode = manualBarcode + "-" + suffix;
                }
                barcodeList.add(manualBarcode);
                adapter.notifyDataSetChanged();
                manualBarcodeInput.setText(""); // Limpar o campo após adicionar
            }
        });

        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(
                this,
                recyclerView,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        editingPosition = position;
                        manualBarcodeInput.setText(barcodeList.get(position));
                        saveEditButton.setVisibility(View.VISIBLE);
                    }
                    @Override
                    public void onLongItemClick(View view, int position) {
                        barcodeList.remove(position);
                        adapter.notifyItemRemoved(position);
                    }
                }
        ));

        saveEditButton.setOnClickListener(v -> {
            if (editingPosition != -1) {
                String editedBarcode = manualBarcodeInput.getText().toString();
                barcodeList.set(editingPosition, editedBarcode);
                adapter.notifyItemChanged(editingPosition);
                manualBarcodeInput.setText("");
                saveEditButton.setVisibility(View.GONE);
                editingPosition = -1;
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            String barcode = data.getStringExtra("barcode");
            Log.d("MainActivity", "onActivityResult: barcode = " + barcode);
            String suffix = suffixInput.getText().toString(); // Mudança: prefix -> suffix
            if (!suffix.isEmpty()) {
                barcode = barcode + "-" + suffix; // Mudança: sufixo adicionado ao final
            }
            barcodeList.add(barcode);
            adapter.notifyDataSetChanged();
            Log.d("MainActivity", "onActivityResult: barcodeList size = " + barcodeList.size());
        }
    }


    private void saveBarcodesToFile() throws IOException {
        if (selectedFileUri == null) {
            return;
        }

        Log.d("MainActivity", "saveBarcodesToFile: barcodeList size = " + barcodeList.size());

        try (OutputStream outputStream = getContentResolver().openOutputStream(selectedFileUri);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream))) {
            for (String barcode : barcodeList) {
                writer.write(barcode + "\n");
            }
        }
    }
}