package com.lawnscape;

import android.content.Context;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by Mellis on 2/23/2017.
 *
 * This class will accept an arraylist of <Job> objects, a listview adaptor, and context
 * and it basically just posts job posts to listview widgets using the JobListAdapter
 * Context - Just for error toast message
 * ArrayList<Job> allPostDetails will hold the list of jobs that the user wants in the list
 * ArrayList<String> jobsToGet is optional, if a string arraylist is given it will only add those jobIDs to the list
 * Adapter jobsAdapter - This thing just acts as a middleman for the list view. If you want to put something
 *      edit the Job type arraylist and call NotifyDataSetChanged on the adapter
 *
 */

public class JobListVEListener implements ValueEventListener {

    ArrayList<Job> allPostDetailsList;
    //Leave this alone to get all jobs
    ArrayList<String> jobsToGet;
    JobListAdapter jobsAdaptor;
    Context thisContext;
    boolean justJobs;

    public JobListVEListener(Context context, ArrayList<Job> jobsList){
        thisContext = context;
        allPostDetailsList = jobsList;
        jobsToGet = null;
        justJobs = true;
    }
    public JobListVEListener(Context context, ArrayList<Job> jobsList, JobListAdapter jobPostAdaptor){
        thisContext = context;
        allPostDetailsList = jobsList;
        jobsAdaptor = jobPostAdaptor;
        jobsToGet = null;
        justJobs = false;
    }
    //used to get specific jobs
    public JobListVEListener(Context context, ArrayList<Job> jobsList, JobListAdapter jobPostAdaptor, ArrayList<String>JobIDsToGet){
        thisContext = context;
        allPostDetailsList = jobsList;
        jobsAdaptor = jobPostAdaptor;
        jobsToGet = JobIDsToGet;
        justJobs = false;
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        allPostDetailsList.clear();
        if(!justJobs) {
            //Add all the jobs to the array list
            for (DataSnapshot jobNode : dataSnapshot.getChildren()) {
                //Either grabs all jobs, or the list of jobs passed via constructor
                if (jobsToGet == null || jobsToGet.contains(jobNode.getKey().toString())) {
                    String title = (String) jobNode.child("title").getValue();
                    String location = (String) jobNode.child("location").getValue();
                    String description = (String) jobNode.child("description").getValue();
                    String date = (String) jobNode.child("date").getValue();
                    String lat = (String) jobNode.child("latitude").getValue();
                    String lng = (String) jobNode.child("longitude").getValue();
                    String userid = (String) jobNode.child("userid").getValue();
                    String postid = (String) jobNode.getKey().toString();

                    allPostDetailsList.add(new Job(date, title, location, description, userid, postid, lat, lng));
                    //Tell the listview adaptor to update the listview based on the ArrayList updates
                }
            }
            jobsAdaptor.notifyDataSetChanged();
        }else{
            for (DataSnapshot jobNode : dataSnapshot.getChildren()) {

                System.out.println("JOBJOBJOB");
                String title = (String) jobNode.child("title").getValue();
                String location = (String) jobNode.child("location").getValue();
                String description = (String) jobNode.child("description").getValue();
                String date = (String) jobNode.child("date").getValue();
                String lat = (String) jobNode.child("latitude").getValue();
                String lng = (String) jobNode.child("longitude").getValue();
                String userid = (String) jobNode.child("userid").getValue();
                String postid = (String) jobNode.getKey().toString();
                Job newJob = new Job(date, title, location, description, userid, postid, lat, lng);
                allPostDetailsList.add(newJob);

                System.out.println(allPostDetailsList.contains(newJob));
                //Tell the listview adaptor to update the listview based on the ArrayList updates
            }
        }
    }

    @Override
    public void onCancelled(DatabaseError firebaseError) {
        Toast.makeText(thisContext ,"DB ERROR: Could not get jobs",Toast.LENGTH_SHORT).show();
    }
}