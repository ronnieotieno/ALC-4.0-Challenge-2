package com.example.travelmantics;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.travelmantics.databinding.ActivityTravelDealBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import static android.widget.Toast.LENGTH_SHORT;
import static java.lang.System.currentTimeMillis;

public class DealActivity extends AppCompatActivity implements ChooseImageFragment.OnInputListener {

    private byte[] mUploadBytes;
    private String profileImageUrl;
    private TravelDeal deal;
    private Bundle bundle;
    private String id;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser user = mAuth.getCurrentUser();
    private CollectionReference dbRef;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ActivityTravelDealBinding travelDealBinding;
    private MenuItem menuItem;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        travelDealBinding = DataBindingUtil.setContentView(this, R.layout.activity_travel_deal);

        bundle = getIntent().getExtras();

        if (bundle != null) {

            String Title = bundle.getString("title");
            String Desc = bundle.getString("desc");
            String Price = bundle.getString("price");
            id = bundle.getString("id");
            String image = bundle.getString("image");


            TravelDeal travelDeal = new TravelDeal(Title, Desc, Price, image);
            travelDealBinding.setTravels(travelDeal);


        }
    }

    public void showLoader(View view) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("MyDialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        ChooseImageFragment dialog = new ChooseImageFragment();
        dialog.show(ft, "MyDialog");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_menu, menu);

        if (bundle == null) {
            menu.findItem(R.id.delete_menu).setVisible(false);
        }
        if (bundle != null) {
            menu.findItem(R.id.save_menu).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        if (item.getItemId() == R.id.delete_menu) {
            dbRef = db.collection("Deals");
            dbRef.document(id).delete();
            finish();
        }

        if (item.getItemId() == R.id.save_menu) {
            insertData();
        }
        return super.onOptionsItemSelected(item);
    }

    public void insertData() {
        String title = travelDealBinding.txtTitle.getText().toString().trim();
        String price = travelDealBinding.txtPrice.getText().toString().trim();
        String description = travelDealBinding.txtDescription.getText().toString().trim();

        if (title.isEmpty()) {
            travelDealBinding.txtTitle.setError("Title Required");
            return;
        }
        if (price.isEmpty()) {
            travelDealBinding.txtPrice.setError("Price Required");
            return;
        }
        if (description.isEmpty()) {
            travelDealBinding.txtDescription.setError("Description Required");
            return;
        }
        if (profileImageUrl == null) {
            Toast.makeText(this, "Image Required", LENGTH_SHORT).show();
            return;
        }

        dbRef = db.collection("Deals");
        deal = new TravelDeal(title, description, price, profileImageUrl);
        travelDealBinding.progressBar.setVisibility(View.VISIBLE);
        dbRef.add(deal).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Toast.makeText(DealActivity.this, "Saved", LENGTH_SHORT).show();
                travelDealBinding.progressBar.setVisibility(View.GONE);
                clearViews();
                Intent intent = new Intent(DealActivity.this, ListActivity.class);
                startActivity(intent);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(DealActivity.this, e.getMessage(), LENGTH_SHORT).show();
                travelDealBinding.progressBar.setVisibility(View.GONE);
            }
        });
    }


    @Override
    public void sendInput(byte[] bytes) {
        mUploadBytes = bytes;
        Bitmap bitmap = BitmapFactory.decodeByteArray(mUploadBytes, 0, mUploadBytes.length);
        travelDealBinding.image.setImageBitmap(bitmap);
        uploadImageToFireBaseStorage();
    }

    private void uploadImageToFireBaseStorage() {
        if (user != null) {
            user.getUid();

            StorageReference profileImageRef =
                    FirebaseStorage.getInstance().getReference("dealpics/" + currentTimeMillis() + ".jpg");
            if (mUploadBytes != null) {
                travelDealBinding.progressBar.setVisibility(View.VISIBLE);
                profileImageRef.putBytes(mUploadBytes)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                Toast.makeText(DealActivity.this, "Image uploaded", LENGTH_SHORT).show();

                                Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                                while
                                (!urlTask.isSuccessful()) ;

                                Uri downloadUrl = urlTask.getResult();
                                profileImageUrl = downloadUrl.toString().trim();
                                travelDealBinding.progressBar.setVisibility(View.GONE);

                            }

                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(DealActivity.this, e.getMessage(), LENGTH_SHORT).show();
                                Log.d("DealActivity", "onFailure: " + e.getMessage());
                                travelDealBinding.progressBar.setVisibility(View.GONE);
                            }
                        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        travelDealBinding.progressBar.getProgress();
                        travelDealBinding.btnImage.setEnabled(false);

                    }
                });
            }
        }
    }

    public void clearViews() {
        travelDealBinding.txtDescription.getText().clear();
        travelDealBinding.txtPrice.getText().clear();
        travelDealBinding.txtTitle.getText().clear();

    }
}

