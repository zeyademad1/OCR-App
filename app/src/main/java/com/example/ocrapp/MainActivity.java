package com.example.ocrapp;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    final static int CAMERA_REQ_CODE = 1;
    Button copy, capture;
    TextView data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        copy = findViewById(R.id.btn_copy);
        capture = findViewById(R.id.btn_capture);
        data = findViewById(R.id.text_data);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_REQ_CODE);
        }

        copy.setOnClickListener(c -> {
            CopyToClipboard(data.getText().toString());
        });

        capture.setOnClickListener(camera -> {
            CropImage.activity().setGuidelines(CropImageView.Guidelines.ON)
                    .start(MainActivity.this);
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                assert result != null;
                Uri uri = result.getUri();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                    GetTextFromImage(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }


    private void GetTextFromImage(Bitmap b) {
        TextRecognizer recognizer = new TextRecognizer.Builder(this).build();
        if (!recognizer.isOperational()) {
            Toast.makeText(MainActivity.this, "Error Occurred", Toast.LENGTH_SHORT).show();
        } else {
            Frame frame = new Frame.Builder().setBitmap(b).build();
            SparseArray<TextBlock> textBlockSparseArray = recognizer.detect(frame);
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < textBlockSparseArray.size(); i++) {
                builder.append(textBlockSparseArray.valueAt(i).getValue());
                builder.append("\n");
            }
            data.setText(builder);
            capture.setText(R.string.retake);
            copy.setVisibility(View.VISIBLE);

        }
    }




    public void CopyToClipboard(String Text) {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData data = ClipData.newPlainText("copies text", Text);
        clipboardManager.setPrimaryClip(data);
        Toast.makeText(MainActivity.this,"Copied To ClipBoard",Toast.LENGTH_LONG).show();

    }
}