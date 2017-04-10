package com.lawnscape;

import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

//Profile Activity
public class ViewMyProfileActivity extends AppCompatActivity {
    //Firebase global init
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseStorage storage;
    private TextView tvEmail;
    private TextView tvUserID;
    private TextView tvLocation;
    private TextView tvName;
    private ImageView ivProfilePhoto;

    private FirebaseDatabase database;
    /************** Begin LifeCycle Functions ****************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_my_profile);
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
                    startActivity(new Intent(ViewMyProfileActivity.this, LoginActivity.class));
                    finish();
                }else{
                    //user is logged in
                    storage = FirebaseStorage.getInstance();
                    database = FirebaseDatabase.getInstance ();
                    tvEmail = (TextView) findViewById(R.id.tvMyProfileUserEmail);
                    tvUserID = (TextView) findViewById(R.id.tvMyProfileUserID);
                    tvLocation = (TextView) findViewById(R.id.tvMyProfileLocation);
                    tvName = (TextView) findViewById(R.id.tvMyProfileName);
                    ivProfilePhoto = (ImageView) findViewById(R.id.ivMyProfileImage);
                    tvEmail.setText(currentUser.getEmail().toString());
                    tvUserID.setText(currentUser.getUid().toString());
                    ivProfilePhoto.setImageDrawable(null);
                    //This finds the photo data by the job id from firebase storage, nothing is passed around
                    StorageReference jobPhotoRef = storage.getReference().child("userprofilephotos").child(currentUser.getUid());
                    jobPhotoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            // Got the download URL for 'users/me/profile.png'
                            // Pass it to Picasso to download, show in ImageView and caching
                            Picasso.with(ViewMyProfileActivity.this).load(uri.toString()).into(ivProfilePhoto);
                        }
                    });
                    database.getReference("Users").child(currentUser.getUid())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // This method is called once with the initial value and again
                            // whenever data at this location is updated.
                            if(dataSnapshot.hasChild("name")) {
                                tvName.setText(dataSnapshot.child("name").getValue().toString());
                            }
                            if(dataSnapshot.hasChild("location")) {
                                tvLocation.setText(dataSnapshot.child("location").getValue().toString());
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError error) { }
                    });
                }
            }
        };
    }
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }
    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
             mAuth.removeAuthStateListener(mAuthListener);
        }
    }
    /************** End LifeCycle ****************/
    public void gotoProfileSettings(View v){
        startActivity(new Intent(this,EditProfileActivity.class));
        finish();
    }
    public void gotoPostNewJob(View v){
        startActivity( new Intent( this, PostJobActivity.class));
    }
    /******************* Menu Handling *******************/
    //make the menu show up
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_profile, menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.profileMenuSettings:
                startActivity(new Intent(this, EditProfileActivity.class));
                return true;
            case R.id.profileMenuChats:
                startActivity(new Intent(this, ViewActiveChatsActivity.class));
                return true;
            case R.id.profileMenuSearch:
                startActivity(new Intent(this, SearchActivity.class));
                return true;
            case R.id.profileMenuMyJobs:
                startActivity(new Intent(this, ViewMyPostsActivity.class));
                return true;
            case R.id.profileMenuAllJobs:
                Intent allJobsViewIntent = new Intent(this, ViewJobsListsActivity.class);
                allJobsViewIntent.putExtra("View", "all");
                startActivity(allJobsViewIntent);
                return true;
            case R.id.profileMenuAllJobsMap:
                Intent MapAllJobsViewIntent = new Intent(this, MapJobsActivity.class);
                startActivity(MapAllJobsViewIntent);
                return true;
            case R.id.profileMenuSignOut:
                mAuth.signOut();
                return true;
            default:
                finish();
                return super.onOptionsItemSelected(item);
        }
    }
    public void searchJobsButton(View v){
        startActivity(new Intent(this, SearchActivity.class));
    }
    public void listAllJobsButton(View v){
        Intent allJobsViewIntent = new Intent(this, ViewJobsListsActivity.class);
        allJobsViewIntent.putExtra("View", "all");
        startActivity(allJobsViewIntent);
    }
    public void mapJobsButton(View v){
        Intent MapAllJobsViewIntent = new Intent(this, MapJobsActivity.class);
        startActivity(MapAllJobsViewIntent);
    }
    public void myPostsButton(View v){
        startActivity(new Intent(this, ViewMyPostsActivity.class));
    }
}
