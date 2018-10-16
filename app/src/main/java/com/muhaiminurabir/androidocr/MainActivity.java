package com.muhaiminurabir.androidocr;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.digitalbithub.magnifize.MagnifizeView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.karan.churi.PermissionManager.PermissionManager;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.image_pickup)
    Button imagePickup;
    @BindView(R.id.image_view)
    CircularImageView imageView;
    @BindView(R.id.imageView2)
    ImageView imageView2;
    @BindView(R.id.image_text)
    TextView imageText;


    PermissionManager permissionManager;
    @BindView(R.id.image_view3)
    MagnifizeView imageView3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        try {

            permissionManager = new PermissionManager() {
            };
            permissionManager.checkAndRequestPermissions(MainActivity.this);
        } catch (Exception e) {
            Log.d("Error Line Number", Log.getStackTraceString(e));
        }
    }

    @OnClick(R.id.image_pickup)
    public void onViewClicked() {
        EasyImage.openChooserWithGallery(MainActivity.this, "CHOOSE IMAGE", 0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        permissionManager.checkResult(requestCode, permissions, grantResults);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        EasyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                //Some error handling
            }

            @Override
            public void onImagesPicked(List<File> imagesFiles, EasyImage.ImageSource source, int type) {
                Log.d("image file", imagesFiles.size() + "");
                if (imagesFiles != null) {
                    File final_image = imagesFiles.get(0);
                    final_image=saveBitmapToFile(final_image);
                    Log.d("CAMERA", final_image.getAbsolutePath());
                    Bitmap myBitmap = BitmapFactory.decodeFile(final_image.getAbsolutePath());
                    imageView.setImageBitmap(myBitmap);
                    imageView2.setImageBitmap(myBitmap);
                    imageView3.setBitmap(myBitmap);
                    try {
                        Log.d("Vision", "OCR");
                        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(myBitmap);
                        /*FirebaseVisionTextRecognizer textRecognizer = FirebaseVision.getInstance()
                                .getOnDeviceTextRecognizer();*/
                        FirebaseVisionTextRecognizer textRecognizer = FirebaseVision.getInstance()
                                .getCloudTextRecognizer();
                        textRecognizer.processImage(image)
                                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                                    @Override
                                    public void onSuccess(FirebaseVisionText result) {
                                        // Task completed successfully
                                        // ...
                                        //processTxt(result);
                                        processTextRecognitionResult(result);
                                    }
                                })
                                .addOnFailureListener(
                                        new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // Task failed with an exception
                                                // ...
                                            }
                                        });
                    } catch (Exception e) {
                        Log.d("Error Line Number", Log.getStackTraceString(e));
                    }
                }

            }
        });
    }

    private void processTxt(FirebaseVisionText result) {
        /*List<FirebaseVisionText.TextBlock> blocks = text.getTextBlocks();
        if (blocks.size() == 0) {
            Toast.makeText(MainActivity.this, "No Text :(", Toast.LENGTH_LONG).show();
            return;
        }
        for (FirebaseVisionText.TextBlock block : text.getTextBlocks()) {
            String txt = block.getText();
            imageText.setTextSize(24);
            imageText.setText(txt);
        }*/
        String r = "";
        /*List<FirebaseVisionText.TextBlock> blocks = result.getTextBlocks();
        for (int i = 0; i < blocks.size(); i++) {
            List<FirebaseVisionText.Line> lines = blocks.get(i).getLines();
            for (int j = 0; j < lines.size(); j++) {
                List<FirebaseVisionText.Element> elements = lines.get(j).getElements();
                for (int k = 0; k < elements.size(); k++) {
                    r=r+"\n"+elements.get(k).getText().toString()+" ";
                }
            }
        }*/
        imageText.setText(r);
    }

    private void processTextRecognitionResult(FirebaseVisionText texts) {
        List<FirebaseVisionText.TextBlock> blocks = texts.getTextBlocks();
        if (blocks.size() == 0) {
            imageText.setText("No text found");
            return;
        }
        String r = "";
        /*for (int i = 0; i < blocks.size(); i++) {
            List<FirebaseVisionText.Line> lines = blocks.get(i).getLines();
            for (int j = 0; j < lines.size(); j++) {
                List<FirebaseVisionText.Element> elements = lines.get(j).getElements();
                for (int k = 0; k < elements.size(); k++) {
                    r=r+elements.get(k).getText().toString()+" ";

                }
            }
        }*/
        for (FirebaseVisionText.TextBlock block : texts.getTextBlocks()) {
            String text = block.getText();
            r = r + " " + text;
        }
        imageText.setText(r);

    }

    public File saveBitmapToFile(File file){
        try {

            // BitmapFactory options to downsize the image
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            o.inSampleSize = 6;
            // factor of downsizing the image

            FileInputStream inputStream = new FileInputStream(file);
            //Bitmap selectedBitmap = null;
            BitmapFactory.decodeStream(inputStream, null, o);
            inputStream.close();

            // The new size we want to scale to
            final int REQUIRED_SIZE=75;

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while(o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                    o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2;
            }

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            inputStream = new FileInputStream(file);

            Bitmap selectedBitmap = BitmapFactory.decodeStream(inputStream, null, o2);
            inputStream.close();

            // here i override the original image file
            /*file.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(file);

            selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 100 , outputStream);

            return file;*/


            File folder = new File(Environment.getExternalStorageDirectory() + "/Ocr");
            boolean success = true;
            if (!folder.exists()) {
                success = folder.mkdir();
            }
            if (success) {
                File newFile = new File(new File(folder.getAbsolutePath()), file.getName());
                if (newFile.exists()) {
                    newFile.delete();
                }
                FileOutputStream outputStream = new FileOutputStream(newFile);

                if (getFileExt(file.getName()).equals("png") || getFileExt(file.getName()).equals("PNG")) {
                    selectedBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                } else {
                    selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                }

                return newFile;
            } else {
                return null;
            }

        } catch (Exception e) {
            return null;
        }
    }
    public static String getFileExt(String fileName) {
        try {
            return fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
        }catch (Exception e){
            Log.d("Error Line Number",Log.getStackTraceString(e));
        }
        return null;
    }

}
