package com.lawnscape.activities;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.lawnscape.R;
import com.lawnscape.VElisteners.ToggleAddIDVEListener;
import com.lawnscape.adapters.PhotoAdapter;
import com.lawnscape.classes.Job;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ViewSingleJobActivity extends AppCompatActivity {

    // Create a storage reference from our app
    private FirebaseStorage storage;
    private FirebaseDatabase database;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser currentUser;
    private Job jobPost;
    private ImageView ivPhoto;
    private PhotoAdapter photoAdapter;
    private ArrayList<Uri> otherPhotoList;
    private BootstrapButton deleteButton;
    private BootstrapButton requestButton;
    private BootstrapButton editButton;
    private Boolean isFavorite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_single_job);
        deleteButton = (BootstrapButton) findViewById(R.id.buttonDeletePost);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deletePost();
            }
        });
        requestButton = (BootstrapButton) findViewById(R.id.buttonRequestJob);
        requestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestJob();
            }
        });
        editButton = (BootstrapButton) findViewById(R.id.buttonEditPostDetails);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Edit Post
                Toast.makeText(ViewSingleJobActivity.this,"Need To Implement", Toast.LENGTH_SHORT ).show();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tvSingleJobTitle);
        TextView tvLoc = (TextView) findViewById(R.id.tvSingleJobLocation);
        TextView tvDesc = (TextView) findViewById(R.id.tvSingleJobDescription);
        TextView tvDate = (TextView) findViewById(R.id.tvSingleJobDate);
        ivPhoto = (ImageView) findViewById(R.id.ivSingleJobPhoto);
        GridView gvOtherPhotos = (GridView) findViewById(R.id.gvSingleJob);
        otherPhotoList = new ArrayList<>();
        photoAdapter = new PhotoAdapter(this, otherPhotoList);
        gvOtherPhotos.setAdapter(photoAdapter);
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();
        Intent jobIntent = getIntent();
        jobPost = jobIntent.getParcelableExtra("Job");
        tvTitle.setText(jobPost.getTitle());
        tvLoc.setText(jobPost.getLocation());
        tvDesc.setText(jobPost.getDescription());
        tvDate.setText(jobPost.getDate());
        //This finds the photo data by the job id from firebase storage, nothing is passed around
        final StorageReference jobPhotoRef = storage.getReference().child("jobphotos").child(jobPost.getPostid());
        DatabaseReference otherPhotoStorageRef = database.getReference("Jobs").child(jobPost.getPostid()).child("photoids");
        jobPhotoRef.child("mainphoto").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Pass it to Picasso to download, show in ImageView and caching
                Picasso.with(ViewSingleJobActivity.this).load(uri.toString()).into(ivPhoto);
            }
        });
        //get the list of extra photos
        //DatabaseReference otherPhotosRef = database.getReference("Jobs").child(jobPost.getPostid());
        otherPhotoStorageRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot node : dataSnapshot.getChildren()) {
                    if (!node.getKey().equals("mainphoto")) {
                        jobPhotoRef.child("otherphotos").child(node.getKey()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                // Got the download URL for 'users/me/profile.png'
                                // Pass it to Picasso to download, show in ImageView and caching
                                otherPhotoList.add(uri);
                                photoAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        //get firebase auth instance
        mAuth = FirebaseAuth.getInstance();
        //make sure user is logged in and has an account
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
                currentUser = firebaseAuth.getCurrentUser();
                if (currentUser == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(ViewSingleJobActivity.this, LoginActivity.class));
                    finish();
                } else {



                    if ((currentUser.getUid().equals(jobPost.getUserid()))) {
                        requestButton.setVisibility(View.GONE);
                    }
                    DatabaseReference adminRef = FirebaseDatabase.getInstance().getReference("Admins");
                    adminRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(currentUser.getUid())) {
                                deleteButton.setText(R.string.admin_delete);
                                editButton.setText(R.string.admin_edit);
                                deleteButton.setVisibility(View.VISIBLE);
                                editButton.setVisibility(View.VISIBLE);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                }
            }
        };
    }


    // Boiler Plate Authentication
    @Override
    public void onStart() {
        super.onStart();
        // Boiler Plate Authentication
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        // Boiler Plate Authentication
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
    /************* End LifeCycle ****************/
    /******************* Menu Handling *******************/
    //make the menu show up
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_single_post_view, menu);

        final MenuItem favoritePostMenuItem = menu.findItem(R.id.viewSinglePostMenuFavorite);
        //Depends on lifecycle activities so currentUser objct isnt null
        if (!currentUser.getUid().equals(jobPost.getUserid())) {
            MenuItem editPostMenuItem = menu.findItem(R.id.viewSinglePostMenuEditPost);
            MenuItem deletePostMenuItem = menu.findItem(R.id.viewSinglePostMenuDelete);
            editPostMenuItem.setVisible(false);
            deletePostMenuItem.setVisible(false);

            DatabaseReference postFavoritedRef;
            postFavoritedRef = database.getReference("Users").child(currentUser.getUid()).child("savedjobs").getRef();
            postFavoritedRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //Find each job made saved by the user
                    if (dataSnapshot.hasChild(jobPost.getPostid())) {
                        favoritePostMenuItem.setIcon(R.drawable.unfavorite_icon);
                        isFavorite = true;
                    } else {
                        isFavorite = false;
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {/* idk what we would do*/ }
            });
        } else {
            //Current user's own post
            MenuItem chatPostMenuItem = menu.findItem(R.id.viewSinglePostMenuChat);
            MenuItem reportPostMenuItem = menu.findItem(R.id.viewSinglePostMenuReport);
            chatPostMenuItem.setVisible(false);
            reportPostMenuItem.setVisible(false);
            favoritePostMenuItem.setVisible(false);
        }
        //Creates a back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.viewSinglePostMenuFavorite:
                if (isFavorite) {
                    item.setIcon(R.drawable.favorite_icon);
                    saveJob();
                } else {
                    item.setIcon(R.drawable.unfavorite_icon);
                    saveJob();
                }
                isFavorite = !isFavorite;
                return true;
            case R.id.viewSinglePostMenuEditPost:
                Intent editJobIntent = new Intent(this,EditJobActivity.class);
                editJobIntent.putExtra("Job",jobPost);
                startActivity(editJobIntent);
                finish();
                return true;
            case R.id.viewSinglePostMenuDelete:
                deletePost();
                return true;
            case R.id.viewSinglePostMenuReport:
                reportPost();
                return true;
            case R.id.viewSinglePostMenuMyProfile:
                startActivity(new Intent(ViewSingleJobActivity.this, ViewProfileActivity.class));
                return true;
            case R.id.viewSinglePostMenuChat:
                if (!currentUser.getUid().equals(jobPost.getUserid())) {
                    Intent chatIntent = new Intent(ViewSingleJobActivity.this, ChatActivity.class);
                    chatIntent.putExtra("otherid", jobPost.getUserid());
                    startActivity(chatIntent);
                }
                return true;
            case R.id.viewSinglePostMenuMyJobs:
                startActivity(new Intent(ViewSingleJobActivity.this, ViewJobsListsActivity.class).putExtra("View","myjobs"));
                return true;
            case R.id.viewSinglePostMenuSearch:
                startActivity(new Intent(ViewSingleJobActivity.this, SearchActivity.class));
                return true;
            case R.id.viewSinglePostMenuSignOut:
                mAuth.signOut();
                finish();
                return true;
            default:
                finish();
                return super.onOptionsItemSelected(item);
        }
    }

    private void reportPost(){
        DatabaseReference ref = database.getReference("Jobs").child(jobPost.getPostid()).child("reporters");
        ref.addListenerForSingleValueEvent(
                new ToggleAddIDVEListener(ViewSingleJobActivity.this, currentUser.getUid(),"true"));
        ref = database.getReference("ReportedJobs");
        ref.addListenerForSingleValueEvent(
                new ToggleAddIDVEListener(ViewSingleJobActivity.this, currentUser.getUid(),jobPost.getPostid()));
    }

    private void deletePost() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        //remove the job from the list of all jobs with a listener
        DatabaseReference myJobRef = database.getReference("Jobs");
        //remove the job from the users job list with a listener*
        DatabaseReference myUserJobsRef = database.getReference("Users").child(jobPost.getUserid()).child("jobs");
        final StorageReference jobPhotoStorageRef = storage.getReference().child("jobphotos").child(jobPost.getPostid());
        DatabaseReference jobPhotoDatabaseRef = database.getReference("Jobs").child(jobPost.getPostid()).child("photoids");
        jobPhotoDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                jobPhotoStorageRef.child("mainphoto").delete();
                for (DataSnapshot node : dataSnapshot.getChildren()) {
                    if (!node.getKey().equals("mainphoto")) {
                        jobPhotoStorageRef.child("otherphotos").child(node.getKey()).delete();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        myJobRef.addListenerForSingleValueEvent(new ToggleAddIDVEListener(ViewSingleJobActivity.this, jobPost.getPostid()));
        myUserJobsRef.addListenerForSingleValueEvent(new ToggleAddIDVEListener(ViewSingleJobActivity.this, jobPost.getPostid()));
        finish();
    }

    @SuppressWarnings("UnusedParameters")
    private void saveJob() {
        DatabaseReference mySavedJobsRef = database.getReference("Users").child(currentUser.getUid()).child("savedjobs");
        mySavedJobsRef.addListenerForSingleValueEvent(
                new ToggleAddIDVEListener(ViewSingleJobActivity.this, jobPost.getPostid(), jobPost.getPostid()));
    }

    @SuppressWarnings("UnusedParameters")
    public void requestJob() {
        DatabaseReference ref = database.getReference("Jobs").child(jobPost.getPostid()).child("requesters");
        ref.addListenerForSingleValueEvent(
                new ToggleAddIDVEListener(ViewSingleJobActivity.this, currentUser.getUid()));
        ref = database.getReference("Users").child(currentUser.getUid()).child("requestedjobs");
        ref.addListenerForSingleValueEvent(
                new ToggleAddIDVEListener(ViewSingleJobActivity.this, jobPost.getPostid()));
        ref = database.getReference("Jobs").child(jobPost.getPostid()).child("requesters");
        ref.addListenerForSingleValueEvent(
                new ToggleAddIDVEListener(ViewSingleJobActivity.this, currentUser.getUid(), currentUser.getUid()));
    }

    @SuppressWarnings("UnusedParameters")
    public void openChat(View v) {
        Intent chatIntent = new Intent(ViewSingleJobActivity.this, ChatActivity.class);
        chatIntent.putExtra("otherid", jobPost.getUserid());
        startActivity(chatIntent);
        finish();
    }


}
