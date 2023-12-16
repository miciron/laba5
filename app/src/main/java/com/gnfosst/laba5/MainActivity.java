package com.gnfosst.laba5;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import android.app.DownloadManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputLayout;
import java.io.File;

public class MainActivity extends AppCompatActivity {

    public static final String APP_PREFERENCES = "setts";
    private SharedPreferences mSetts;
    private SharedPreferences.Editor editor;
    private boolean popupState;
    private Button bDownload;
    private Button bDelete;
    private EditText journal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSetts = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        bDelete = findViewById(R.id.bDelete);
        bDownload = findViewById(R.id.bDownload);
        journal = findViewById(R.id.journal);
        bDelete.setActivated(false);

        popupState = mSetts.getBoolean("statePopup", false);
        if(!popupState) {
            showPopupWindow();
        }

        journal.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "journal" + journal.getText().toString() + ".pdf");

                if(journal.getText().toString().length() > 0 && file.exists()) {
                    bDownload.setText("Посмотреть");
                    bDownload.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            viewPdf(file);
                        }
                    });

                    bDelete.setEnabled(true);
                    bDelete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setMessage("Вы уверены, что хотите удалить этот файл?");

                            builder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    file.delete();
                                    Toast.makeText(MainActivity.this, "Файл удалён", Toast.LENGTH_SHORT).show();

                                    bDownload.setText("Скачать");
                                    bDelete.setEnabled(false);
                                    journal.setText(null);
                                }
                            });

                            builder.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });

                            builder.show();
                        }
                    });
                } else {
                    bDelete.setEnabled(false);

                    bDownload.setText("Скачать");
                    bDownload.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                            if (networkInfo != null && networkInfo.isConnected()) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setMessage("Подтвердите!");

                                builder.setNegativeButton("Скачать на устройство", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        downloadPdf(journal.getText().toString());
                                        journal.setText(null);
                                    }
                                });
                                builder.show();
                            } else {
                                journal.setText("Нет подключения к интернету");
                            }
                        }
                    });
                }
            }
        });

    }

    private void downloadPdf(String pdfId) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse("http://ntv.ifmo.ru/file/journal/" + pdfId + ".pdf"));
        request.setTitle("PDF Download");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "journal" + pdfId + ".pdf");
        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
    }

    private void viewPdf(File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", file);
        intent.setDataAndType(uri, "application/pdf");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }

    private void showPopupWindow(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        dialog.setTitle("Инструкиця");

        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);

        View addStudentWindow = inflater.inflate(R.layout.popup, null);

        dialog.setView(addStudentWindow);

        CheckBox popupState = addStudentWindow.findViewById(R.id.popupState);

        dialog.setNegativeButton("Закрыть", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        dialog.show();
    }
}