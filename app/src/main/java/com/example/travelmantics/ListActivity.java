package com.example.travelmantics;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.travelmantics.databinding.ActivityMainBinding;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Arrays;
import java.util.List;

public class ListActivity extends AppCompatActivity implements RecyclerViewAdapter.OnItemClickListener {
    private RecyclerViewAdapter recyclerViewAdapter;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Context context;
    private TravelDeal clickedItem;
    private FirestoreRecyclerOptions<TravelDeal> options;
    private CollectionReference dbRef;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser user = mAuth.getCurrentUser();
    private MenuItem next;
    private DocumentSnapshot document;
    public FirebaseAuth mFirebaseAuth;
    public FirebaseAuth.AuthStateListener mAuthListener;
    private final int RC_SIGN_IN = 123;
    private ActivityMainBinding mainBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        mFirebaseAuth = FirebaseAuth.getInstance();

        mFirebaseAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    signIn();
                }

            }

        };

        setupAdapter();

        if (user != null) {
            String userId = user.getUid();
            checkAdmin(userId);
        }

    }

    private void signIn() {

        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());

        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setTheme(R.style.AppTheme)
                        .setLogo(R.drawable.beach_holiday)
                        .setIsSmartLockEnabled(false)
                        .build(),
                RC_SIGN_IN);
        setResult(RESULT_OK);
        mFirebaseAuth.removeAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (user != null) {
            String userId = user.getUid();
            checkAdmin(userId);
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        mFirebaseAuth.removeAuthStateListener(mAuthListener);
        recyclerViewAdapter.stopListening();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFirebaseAuth.removeAuthStateListener(mAuthListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_activity_menu, menu);

        next = menu.findItem(R.id.insert_menu).setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.insert_menu) {
            Intent intent = new Intent(ListActivity.this, DealActivity.class);
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.logout_menu) {
            AuthUI.getInstance().signOut(this).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    mFirebaseAuth.addAuthStateListener(mAuthListener);
                }
            });
            mFirebaseAuth.removeAuthStateListener(mAuthListener);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (user != null) {
            String userId = user.getUid();
            checkAdmin(userId);
        }
        setupAdapter();
    }

    private void setupAdapter() {

        dbRef = db.collection("Deals");
        Query query = dbRef.orderBy("price", Query.Direction.DESCENDING);

        options = new FirestoreRecyclerOptions.Builder<TravelDeal>()
                .setQuery(query, TravelDeal.class)
                .build();
        recyclerViewAdapter = new RecyclerViewAdapter(options, ListActivity.this);
        recyclerViewAdapter.startListening();
        recyclerViewAdapter.notifyDataSetChanged();
        mainBinding.recyclerView.setHasFixedSize(false);
        mainBinding.recyclerView.setLayoutManager(new LinearLayoutManager(context));
        mainBinding.recyclerView.setAdapter(recyclerViewAdapter);
        recyclerViewAdapter.setOnItemClickListener(ListActivity.this);
        mFirebaseAuth.addAuthStateListener(mAuthListener);
        if (user != null) {
            String userId = user.getUid();
            checkAdmin(userId);
        }


    }


    @Override
    public void onItemClick(DocumentSnapshot documentSnapshot, int position) {

        clickedItem = documentSnapshot.toObject(TravelDeal.class);
        clickedItem.setId((documentSnapshot.getId()));

        if (document != null) {
            if (document.exists() && document.getData() != null) {

                Intent intent = new Intent(ListActivity.this, DealActivity.class);

                intent.putExtra("title", clickedItem.getTitle());
                intent.putExtra("desc", clickedItem.getDescription());
                intent.putExtra("price", clickedItem.getPrice());
                intent.putExtra("image", clickedItem.getImageUrl());
                intent.putExtra("id", clickedItem.getId());

                startActivity(intent);
            }
        }
    }

    public void checkAdmin(final String userId) {
        dbRef = db.collection("users");
        dbRef.document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    document = task.getResult();
                    if (document != null) {
                        if (document.exists() && next != null) {
                            next.setVisible(true);
                            Log.d("FireBaseUtil", "Document exists!");
                        } else {
                            Log.d("FireBaseUtil", "Document does not exist!");
                        }
                    }
                } else {
                    Log.d("FireBaseUtil", "Failed with: ", task.getException());
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN && resultCode == RESULT_OK) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (user != null) {
                setupAdapter();

            }
        }
    }

}
