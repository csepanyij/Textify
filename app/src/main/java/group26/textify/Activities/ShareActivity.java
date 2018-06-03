package group26.textify.Activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import group26.textify.R;
import group26.textify.Utils.DatabaseEntry;
import group26.textify.Utils.FileGridAdapter;
import group26.textify.Utils.Scanfile;


public class ShareActivity extends NavbarActivity{

    public ArrayList<Scanfile> files;
    int countSelected;

    FileGridAdapter gridAdapter;
    GridView gvFiles;
    TextView textCount;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = auth.getCurrentUser();

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        super.initNavbar(this,"share");

        files = new ArrayList<>();
        createFiles();

        countSelected=0;
        textCount = findViewById(R.id.textCount);
        textCount.setText(countSelected+" "+getResources().getString(R.string.selected));

        gridAdapter = new FileGridAdapter(this,files);
        gvFiles = findViewById(R.id.gridFiles);
        gvFiles.setAdapter(gridAdapter);

        gvFiles.setOnItemClickListener(new OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position, long arg)
            {
                Scanfile selectedFile = (Scanfile)adapter.getItemAtPosition(position);
                selectedFile.setSelected(!selectedFile.isSelected());
                if(selectedFile.isSelected()){
                    countSelected++;
                }else{
                    countSelected--;
                }
                notifyGridview();
                textCount.setText(countSelected+" "+getResources().getString(R.string.selected));
            }
        });

    }


    private void notifyGridview(){
        gridAdapter.notifyDataSetChanged();
    }

    private void createFiles(){

        if (ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            createFilesPermissionGranted();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 10);
        }
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 10: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    createFilesPermissionGranted();
                }
                else {
                    AlertDialog.Builder adb = new AlertDialog.Builder(this);
                    adb.setMessage("Permission Denied!");
                    adb.create().show();
                }
            }
        }
    }

    private void createFilesPermissionGranted() {
        files = new ArrayList<>();

        final StorageReference userBucket = storageRef.child(currentUser.getUid());

        final File folder = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES).getParent() + File.separator + "RecognisedImages");
        if (folder.exists()) {

            String userUID = currentUser.getUid();
            DatabaseReference userEntries = database.getReference(userUID);

            userEntries.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    final DatabaseEntry record = dataSnapshot.getValue(DatabaseEntry.class);
                    String filepath = folder.getPath() + File.separator + record.getFileName();
                    File file = new File(filepath);
                    if (file.exists()) {
                        DateTimeFormatter dateformatter = DateTimeFormat.forPattern("yyyy.MM.dd - HH:mm");
                        Scanfile scanfile = new Scanfile(record.getFileName(),record.getRecognisedText(), LocalDateTime.parse(record.getTime(),dateformatter),file);
                        files.add(scanfile);
                        notifyGridview();
                    } else {
                        StorageReference imageReference = userBucket.child(record.getFileName());
                        try {
                            file.createNewFile();
                            imageReference.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                    DateTimeFormatter dateformatter = DateTimeFormat.forPattern("yyyy.MM.dd - HH:mm");
                                    String filepath = folder.getPath() + File.separator + record.getFileName();
                                    File file = new File(filepath);
                                    Scanfile scanfile = new Scanfile(record.getFileName(), record.getRecognisedText(), LocalDateTime.parse(record.getTime(), dateformatter), file);
                                    files.add(scanfile);
                                    notifyGridview();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(ShareActivity.this, "Downloading failed, some items not shown", Toast.LENGTH_SHORT).show();
                                    Log.d("downloadImages: ", "ERROR: ",e);
                                }
                            });
                        } catch (IOException e) {
                            Toast.makeText(ShareActivity.this, "Creating new file failed, some items not shown", Toast.LENGTH_SHORT).show();
                            Log.d("newFileCreate: ", "ERROR: ",e);
                        }

                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        } else {
            AlertDialog.Builder adb = new AlertDialog.Builder(this);
            adb.setMessage("No pictures found");
        }


    }


    public void onClickShare(View view) {
        ArrayList<Uri> imageUris = new ArrayList<>();
        for(Scanfile file : files){
            if(file.isSelected()){
                imageUris.add(Uri.fromFile(file.getImage()));
            }
        }
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
        shareIntent.setType("image/*");
        startActivity(Intent.createChooser(shareIntent, getResources().getString(R.string.share_photos)));
    }
}


