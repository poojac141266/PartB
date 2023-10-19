package com.example.partb;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.JsonSyntaxException;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private RecyclerView recyclerView;
    private ProgressDialog progressDialog;

    //Connection to Firestore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Images");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler_view);

        findViewById(R.id.single_column_view).setOnClickListener(v -> displayImages(1));
        findViewById(R.id.double_column_view).setOnClickListener(v -> displayImages(2));
    }

    private void displayImages(int columns) {
        String query = getIntent().getStringExtra("QUERY");
        if (TextUtils.isEmpty(query)) {
            Toast.makeText(MainActivity.this, "Please enter a query", Toast.LENGTH_LONG).show();
            return;
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading images...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        try {
            String url = "https://pixabay.com/api/?key=40130339-5a311ed3cd56be44c4eab5b7e&q="
                    + URLEncoder.encode(query, "UTF-8")
                    + "&per_page=200";
            OkHttpHelper.getInstance().get(url, new Callback() {
                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    runOnUiThread(() -> progressDialog.dismiss());
                    List<String> imageUrls = new ArrayList<>();

                    try {
                        ImagesResponse imagesResponse = JsonHelper.fromJson(response.body().string(), ImagesResponse.class);
                        if (imagesResponse != null && imagesResponse.getHits() != null) {
                            for (Image image : imagesResponse.getHits()) {
                                imageUrls.add(image.getUrl());
                            }
                        } else {
                            runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error loading images", Toast.LENGTH_LONG).show());
                            return;
                        }
                    } catch (JsonSyntaxException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error in JSON syntax", Toast.LENGTH_LONG).show());
                        return;
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error loading images", Toast.LENGTH_LONG).show());
                        return;
                    }

                    runOnUiThread(() -> {
                        GridLayoutManager layoutManager = new GridLayoutManager(MainActivity.this, columns);
                        recyclerView.setLayoutManager(layoutManager);

                        ImageListAdapter adapter = new ImageListAdapter(imageUrls, imageUrl -> {
                            progressDialog.setMessage("Uploading image...");
                            progressDialog.show();
                            uploadImage(imageUrl);
                        });

                        recyclerView.setAdapter(adapter);
                        recyclerView.setVisibility(View.VISIBLE);
                    });
                }

                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    e.printStackTrace();
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(MainActivity.this, "Error loading images", Toast.LENGTH_LONG).show();
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            progressDialog.dismiss();
            Toast.makeText(MainActivity.this, "Error loading images", Toast.LENGTH_LONG).show();
        }
    }

    private void uploadImage(String imageUrl) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imagesRef = storageRef.child("images/" + UUID.randomUUID().toString() + ".jpg");

        Picasso.get()
                .load(imageUrl)
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] data = baos.toByteArray();

                        imagesRef.putBytes(data)
                                .addOnSuccessListener(taskSnapshot -> {
                                    imagesRef.getDownloadUrl()
                                            .addOnSuccessListener(uri -> {
                                                String imageUrl = uri.toString();
                                                addImage(imageUrl);
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    progressDialog.dismiss();
                                    Toast.makeText(MainActivity.this, "Error uploading image", Toast.LENGTH_LONG).show();
                                });
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                        progressDialog.dismiss();
                        Toast.makeText(MainActivity.this, "Error loading image", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                        // Handle loading placeholder
                    }
                });
    }

    private void addImage(String imageUrl) {
        Image image = new Image();
        image.setUrl(imageUrl);
        collectionReference.add(image)
                .addOnSuccessListener(documentReference -> {
                    progressDialog.dismiss();
                    Toast.makeText(MainActivity.this, "Image uploaded successfully", Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(MainActivity.this, "Error uploading image", Toast.LENGTH_LONG).show();
                });
    }

    @Override
    protected void onStart() {
        super.onStart();

        collectionReference.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Image> images = queryDocumentSnapshots.toObjects(Image.class);

                    LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
                    recyclerView.setLayoutManager(layoutManager);

                    ImageListAdapter adapter = new ImageListAdapter(images, imageUrl -> {
                        progressDialog.setMessage("Uploading image...");
                        progressDialog.show();
                        uploadImage(imageUrl);
                    });
                    recyclerView.setAdapter(adapter);
                    recyclerView.setVisibility(View.VISIBLE);
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "Error loading images", Toast.LENGTH_LONG).show();
                });
    }
}