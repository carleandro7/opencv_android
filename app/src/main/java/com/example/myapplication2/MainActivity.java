package com.example.myapplication2;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class MainActivity extends AppCompatActivity {

    // initializing variables
    Button select,camera;
    ImageView imageView;
    Bitmap bitmap;
    int select_code=100,camera_code=102;
    Mat mat_original;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // to check if openCV is loaded
        getPermission();
        if(OpenCVLoader.initDebug())
            Log.d("Loaded","success");
        else
            Log.d("Loaded","error");

        camera=findViewById(R.id.Camera);
        select=findViewById(R.id.select);
        imageView=findViewById(R.id.imageView);

        // for select button
        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,select_code);
            }
        });

        // for the camera button
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent,camera_code);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==select_code && data!=null)
        {
            try {
                bitmap= MediaStore.Images.Media.getBitmap(this.getContentResolver(),data.getData());
                imageView.setImageBitmap(bitmap);
                bitmap = Bitmap.createScaledBitmap(bitmap, 900, 467, false);
                // using mat class
                Mat mat_original=new Mat();
                Utils.bitmapToMat(bitmap,mat_original);
                Mat mat2= new Mat();
                Utils.bitmapToMat(bitmap,mat2);

                // converting RGB to gray
                Imgproc.cvtColor(mat_original,mat_original,Imgproc.COLOR_BGR2GRAY);
                Mat imgTh = new Mat();
                Imgproc.adaptiveThreshold(mat_original, imgTh,255,Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY_INV, 11, 12);
                Mat kernel = Mat.ones(2,2, CvType.CV_8U);
                Mat imgDil = new Mat();
                Imgproc.dilate(imgTh, imgDil, kernel);
                List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
                Mat hierarcy = new Mat();
                Imgproc.findContours(imgDil,contours,hierarcy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);


                double maxVal = 0;
                int maxValIdx = 0;
                for (int contourIdx = 0; contourIdx < contours.size(); contourIdx++)
                {
                    double contourArea = Imgproc.contourArea(contours.get(contourIdx));
                    if (maxVal < contourArea)
                    {
                        maxVal = contourArea;
                        maxValIdx = contourIdx;
                    }
                }

                Rect rect = Imgproc.boundingRect(contours.get(maxValIdx));
                Imgproc.rectangle(mat2,new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 0, 255, 255),3);
                Mat gabarito = new Mat(mat2, rect);
                Mat imgTh_gabarito = new Mat(mat2, rect);

                Bitmap Cropedimage = Bitmap.createBitmap(gabarito.cols(), gabarito.rows(), Bitmap.Config.ARGB_8888);

                Imgproc.cvtColor(gabarito,gabarito,Imgproc.COLOR_BGR2GRAY);
                Imgproc.threshold(gabarito,imgTh_gabarito,70,255, Imgproc.THRESH_BINARY_INV);
                //Utils.matToBitmap(m, Cropedimage);
                //imageView.setImageBitmap(Cropedimage);
                List<Retangulo> listCampos = new CriarObjetos().listCampos();
                Rect rectaux = new Rect(65, 6, 55, 45);
                Imgproc.rectangle(gabarito, rectaux, new Scalar(0, 0, 255, 255), 2);
                Rect rectaux1 = new Rect(120, 6, 55, 45);
                Imgproc.rectangle(gabarito, rectaux1, new Scalar(0, 255, 255, 255),2);

                Rect rectaux2 = new Rect(65, 54, 55, 45);
                Imgproc.rectangle(gabarito, rectaux2, new Scalar(0, 0, 255, 255), 2);
                Rect rectaux3 = new Rect(120, 54, 55, 45);
                Imgproc.rectangle(gabarito, rectaux3, new Scalar(0, 255, 255, 255),2);




                Utils.matToBitmap(gabarito, Cropedimage);
                imageView.setImageBitmap(Cropedimage);




            } catch (IOException e) {
                Log.d("tamanho erro",  e.getMessage());
                e.printStackTrace();
            }
        }

        if(requestCode==camera_code && data!=null)
        {
            bitmap=(Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(bitmap);

            // using mat class
            mat_original=new Mat();
            Utils.bitmapToMat(bitmap,mat_original);
            Imgproc.cvtColor(mat_original,mat_original,Imgproc.COLOR_RGB2GRAY);

            Utils.matToBitmap(mat_original,bitmap);
            imageView.setImageBitmap(bitmap);
        }
    }

    // get camera permission
    void getPermission()
    {
        if(checkSelfPermission(Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.CAMERA},101);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode==101 && grantResults.length>0)
        {
            if(grantResults[0]!=PackageManager.PERMISSION_GRANTED)
            {
                getPermission();
            }
        }
    }
}
