package com.lawnscape.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lawnscape.adapters.ChatMessageAdapter;
import com.lawnscape.VElisteners.ChatMessageListVEListener;
import com.lawnscape.classes.ChatMessage;
import com.lawnscape.R;
import com.lawnscape.VElisteners.ToggleAddIDVEListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ChatActivity extends AppCompatActivity {
    private FirebaseUser currentUser;
    private String otherUserid;

    private ArrayList<ChatMessage> allMessages;
    private ChatMessageAdapter messageAdapter;

    private ListView messagesWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        otherUserid = getIntent().getExtras().get("otherid").toString();

        setContentView(R.layout.fragment_chat);
        final FirebaseDatabase database = FirebaseDatabase.getInstance();

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        allMessages = new ArrayList<>();
        messagesWindow = (ListView) findViewById(R.id.lvChatMessageView);
        messageAdapter = new ChatMessageAdapter(this,allMessages, currentUser.getUid());
        messagesWindow.setAdapter(messageAdapter);
        /*
        If there is no chat ID shared by the two users then this listener will make one
        if there is a chat id shared by the two users, this listener will grab its ref
         */
        final DatabaseReference myChatRef = database.getReference("Users").child(currentUser.getUid()).child("chatids");
        myChatRef.orderByChild("date").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //chat id
                String idToFetch;
                // The chatid ref will have key-value pairs like "OtherUserID":"chatid"

                if(dataSnapshot.hasChild(otherUserid)){
                    //Found an existing chat
                    idToFetch = dataSnapshot.child(otherUserid).getValue().toString();
                }else{
                    //make new chat and give both users the id in their chat list
                    DatabaseReference allChatsRef = database.getReference("Chats");
                    DatabaseReference newChatid = allChatsRef.push();
                    String newChatID = newChatid.getKey();
                    newChatid.child("Members").addListenerForSingleValueEvent(
                            new ToggleAddIDVEListener(ChatActivity.this, currentUser.getUid(),"true"));
                    newChatid.child("Members").addListenerForSingleValueEvent(
                            new ToggleAddIDVEListener(ChatActivity.this, otherUserid,"true"));
                    DatabaseReference addChatIDRef = database.getReference("Users").child(currentUser.getUid()).child("chatids");
                    addChatIDRef.addListenerForSingleValueEvent(
                            new ToggleAddIDVEListener(ChatActivity.this,otherUserid,newChatID));
                    /***      now give the other user the chat id       ******/
                    addChatIDRef = database.getReference("Users").child(otherUserid).child("chatids");
                    addChatIDRef.addListenerForSingleValueEvent(
                            new ToggleAddIDVEListener(ChatActivity.this, currentUser.getUid(),newChatID));
                    idToFetch = newChatid.getKey();
                }

                //Set up the listview to grab all the messages continuously
                final DatabaseReference chatRef = database.getReference("Chats").child(idToFetch);
                chatRef.addValueEventListener(
                        new ChatMessageListVEListener(ChatActivity.this, messageAdapter, allMessages));
                //Currently selected last message
                messagesWindow.setSelection(messageAdapter.getCount() - 1);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        //puts a "send" button on the keyboard
        final EditText messageText = (EditText) findViewById(R.id.etChatMessage);
        messageText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    sendMessage(messageText);
                    handled = true;
                }
                return handled;
            }
        });

    }

    @SuppressWarnings("UnusedParameters")
    private void sendMessage(View v){
        //Find the chat
        super.onStart();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final ChatMessage chatMessage = new ChatMessage();
        EditText messageText = (EditText) findViewById(R.id.etChatMessage);
        DatabaseReference chatRef = database.getReference("Users").child(currentUser.getUid()).child("chatids").child(otherUserid);

        //set the message details below
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd mm:ss");
        chatMessage.setDate(sdf.format(new Date()));
        chatMessage.setTextMsg(messageText.getText().toString());
        chatMessage.setSentByUid(currentUser.getUid());
        messageText.setText("");
        // User listeners to push data to the database
        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String thechatid = dataSnapshot.getValue().toString();
                DatabaseReference ourChatRef = database.getReference("Chats").child(thechatid).getRef().push();
                chatMessage.setMsgId(ourChatRef.getKey());
                ourChatRef.getRef().setValue(chatMessage);
                messageAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {  }
        });
    }
    /********************* MENU STUFF ACTION BAR ********************/
    @Override
    public boolean onCreateOptionsMenu( Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_chat, menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent upIntent = NavUtils.getParentActivityIntent(this);
                upIntent.putExtra("View", "all");
                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    TaskStackBuilder builder = TaskStackBuilder.create(this);
                    builder.addNextIntentWithParentStack(upIntent);
                    builder.startActivities();
                } else {
                    upIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    this.startActivity(upIntent);
                    this.finish();
                }
                return true;
        }
        return false;
    }
}
