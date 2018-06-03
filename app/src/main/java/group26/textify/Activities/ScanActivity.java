package group26.textify.Activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

import group26.textify.Utils.AsyncLangDetectTask;
import group26.textify.Utils.AsyncTranslateTask;
import group26.textify.Utils.DatabaseEntry;
import group26.textify.R;
import group26.textify.Utils.Language;
import group26.textify.Utils.VisionHttpRequest;
import io.grpc.Context;

public class ScanActivity extends NavbarActivity {

    String currentPhotoPath;
    String stringImage;
    String recognizedText;
    String sourceLang;
    String targetLang;

    private DatabaseReference database = FirebaseDatabase.getInstance().getReference();
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser user = auth.getCurrentUser();

    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReference();

    private int imageType = -1;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_IMAGE_FROM_GALLERY = 2;
    public static String userEmail = "group26.textify.Activities.ScanActivity";

    ProgressBar pb;
    ImageView selectedImage;
    TextView responseTextView;
    EditText nameEditText;
    Button saveButton;
    Button cancelButton;
    Spinner languageSpinner;
    TextView translateText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        super.initNavbar(this,"camera");

        findLayoutViews();
        initLanguagesSpinner();
        setVisibilityCapture(View.GONE);

        Intent intent = getIntent();
        String email = intent.getStringExtra(userEmail);
        if (email != null)
        {
            Toast.makeText(ScanActivity.this, "Logged in as: " + email,
                    Toast.LENGTH_SHORT).show();
        }
        pb.setVisibility(View.GONE);
    }

    private void findLayoutViews(){
        pb = findViewById(R.id.progressBarRecognisePage);
        selectedImage = findViewById(R.id.selectedImage);
        responseTextView = findViewById(R.id.responseTextView);
        nameEditText = findViewById(R.id.editFileName);
        saveButton = findViewById(R.id.buttonSave);
        cancelButton = findViewById(R.id.buttonCancel);
        languageSpinner = findViewById(R.id.languageSpinner);
        translateText = findViewById(R.id.translateText);
    }

    private void initLanguagesSpinner(){
        ArrayAdapter<Language> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                Language.languageArray);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(spinnerAdapter);

        languageSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                targetLang = Language.languageArray[position].getID();
                if(targetLang == null){
                    responseTextView.setText(recognizedText);
                }else {
                    translateText();
                }
            }
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    public void translateText() {
        pb.setVisibility(View.VISIBLE);
        try {
            if(sourceLang == null){
                AsyncLangDetectTask asyncLangDetectTask = new AsyncLangDetectTask(this);
                asyncLangDetectTask.execute(recognizedText);
            }else{
                onCallbackLangDetect(sourceLang);
            }
        } catch (Exception e) {
            Log.d("translateText: ", "ERROR: ", e);
            Toast.makeText(ScanActivity.this, "Error in translation", Toast.LENGTH_LONG).show();
            pb.setVisibility(View.GONE);
        }
    }

    public void onCallbackLangDetect(String response){
        sourceLang = response;
        AsyncTranslateTask asyncTranslateTask = new AsyncTranslateTask(this);
        asyncTranslateTask.execute(recognizedText,sourceLang,targetLang);
    }

    public void onCallbackTranslate(String response){
        responseTextView.setText(response);
        pb.setVisibility(View.GONE);
    }

    // Library button pressed
    public void onClickLibrary(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select picture"),REQUEST_IMAGE_FROM_GALLERY);
    }

    // Camera button pressed
    public void onClickCamera(View view) {
        // Check if there's permission for camera, and ask for permission if not
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            dispatchTakePictureIntent();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        }
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    dispatchTakePictureIntent();
                }
                else {
                    createAlertDialog("Camera Access permission Denied!");
                }
            }
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                imageType = -1;
                System.out.println("Error while creating file");
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "group26.textify.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        currentPhotoPath = image.getAbsolutePath();
        imageType = 0;
        return image;
    }

    @Override
    // This is called when the startActivityForResult() activity finishes
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE){
                // If the image was taken by camera
                onCaptureImageResult();
            } else if (requestCode == REQUEST_IMAGE_FROM_GALLERY) {
                // If the image was selected from gallery
                onSelectFromGalleryResult(data);
            }
        } else {
            createAlertDialog("Error: " + resultCode);
        }
    }

    private void onCaptureImageResult () {
        File imgFile = new File(currentPhotoPath);
        try {
            if(imgFile.exists()){
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                selectedImage.setImageBitmap(myBitmap);
                imageType = 0;
            } else {
                createAlertDialog("File not Found").create().show();
            }
        } catch(Exception e) {
            createAlertDialog(e.toString()).create().show();
            imageType = -1;
        }
    }

    private void onSelectFromGalleryResult(Intent data) {

        Bitmap bitmap;
        if (data != null) {
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                selectedImage.setImageBitmap(bitmap);
                stringImage = convertBitmapToBase64(bitmap);
                imageType = 1;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // Log.v(TAG,"base64"+img);
    }

    private AlertDialog.Builder createAlertDialog(String message) {
        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
        dlgAlert.setMessage(message);
        dlgAlert.setPositiveButton("OK", null);
        dlgAlert.setCancelable(true);
        return dlgAlert;
    }

    private String convertBitmapToBase64(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 90, baos);
        byte[] imageBytes = baos.toByteArray();

        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    // Converting bitmap files to base64 strings for Vision API
    private String convertFileToBase64(String photoPath) {
        String encodedImage = "";
        File imgFile = new File(photoPath);
        if (imgFile.exists()) {
            Bitmap bm = BitmapFactory.decodeFile(photoPath);

            encodedImage = convertBitmapToBase64(bm);
        } else {
            createAlertDialog("File not Found").create().show();
        }
        return encodedImage;
    }

    private String getImageFileName(){
        String imageFileName = nameEditText.getText().toString();
        if(imageFileName.equals("")) {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            imageFileName = "JPEG_" + timeStamp;
        }
        return imageFileName;
    }

    private void uploadToFirebaseStorage(StorageReference userBucketRef, FileInputStream fit) {
        UploadTask uploadTask = userBucketRef.putStream(fit);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ScanActivity.this, "File upload Unsuccessful", Toast.LENGTH_SHORT).show();
                Log.d("UploadFaliure: ", "ERROR: ", e);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(ScanActivity.this, "File upload successful, download URL: " + taskSnapshot.getDownloadUrl(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String copyFileToFile(File copyFrom, File copyTo) throws IOException {
        String imageFileName = getImageFileName();
        File historyImageFile = new File(copyTo.getPath() + File.separator + imageFileName + ".jpg");
        StorageReference userBucketRef = storageRef.child(user.getUid() + File.separator + imageFileName + ".jpg");
        boolean created = historyImageFile.createNewFile();
        if (created) {
            FileInputStream fit = new FileInputStream(copyFrom);
            FileOutputStream fot = new FileOutputStream(historyImageFile);
            Log.d("copyFileToFile", copyTo.toString());
            FileChannel inChannel = fit.getChannel();
            FileChannel outChannel = fot.getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);

            uploadToFirebaseStorage(userBucketRef, fit);

            fit.close();
            fot.close();
            return imageFileName;
        }
        else {
            createAlertDialog("copyFileToFile: Image File cannot be created");
            throw new IOException("ERROR: File cannot be created");
        }
    }

    private String copyBase64ToFile(String stringImage, File copyTo) throws IOException {
        String imageFileName = getImageFileName();
        File historyImageFile = new File(copyTo.getPath() + File.separator + imageFileName+".jpg");
        StorageReference userBucketRef = storageRef.child(user.getUid() + File.separator + imageFileName + ".jpg");
        boolean created = historyImageFile.createNewFile();
        if (created) {
            FileOutputStream fos = new FileOutputStream(new File(historyImageFile.getAbsolutePath()),true);
            byte[] decodedString = android.util.Base64.decode(stringImage, Base64.DEFAULT);
            fos.write(decodedString);
            fos.close();
            UploadTask uploadTask = userBucketRef.putBytes(decodedString);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ScanActivity.this, "File upload Unsuccessful", Toast.LENGTH_SHORT).show();
                    Log.d("UploadFaliure: ", "ERROR: ", e);
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(ScanActivity.this, "File upload successful, download URL: " + taskSnapshot.getDownloadUrl(), Toast.LENGTH_SHORT).show();
                }
            });
            return imageFileName;
        }
        else {
            createAlertDialog("Failed to create History image file");
            throw new IOException("ERROR: File cannot be created");
        }

    }

    public File getWorkingDirectory() {
        File folder = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES).getParent() + File.separator + "RecognisedImages");
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdirs();
        }
        if (success) {
            return folder;
        }
        else {
            return null;
        }
    }

    public void writeFirebaseDatabase(String fileName, String recognisedText, Date date) {
        DatabaseEntry dbEntry = new DatabaseEntry(fileName,recognisedText, new SimpleDateFormat("yyyy.MM.dd - HH:mm").format(date));

        database.child(user.getUid()).child(new SimpleDateFormat("yyyyMMdd_HHmmss").format(date)).setValue(dbEntry);
    }

    public void recogniseImage(View view) {
        responseTextView.setVisibility(View.INVISIBLE);
        responseTextView.setMovementMethod(new ScrollingMovementMethod());
        pb.setVisibility(View.VISIBLE);

        try {

            // Creating the JSON object for request to be sent
            JSONObject urlParameters = new JSONObject();
            JSONArray requests = new JSONArray();
            JSONObject request = new JSONObject();
            JSONObject image = new JSONObject();

            // Check for the origin of the picture:
            // If it was captured, then the fileUri to the temporary image is stored in currentPhotoPath
            // If it was selected from Gallery, then it was already converted from contentUri to base64 and stored in stringImage
            switch (imageType) {
                case 0: image.put("content", convertFileToBase64(currentPhotoPath));
                        break;
                case 1: image.put("content", stringImage);
                        break;
                case -1: createAlertDialog("Picture not selected!");
                         throw new Exception();
                default: createAlertDialog("Invalid argument for imageType!");
                         throw new Exception();
            }

            JSONArray features  = new JSONArray();
            JSONObject feature = new JSONObject();
            feature.put("type","TEXT_DETECTION");
            features.put(feature);
            request.put("image", image);
            request.put("features", features);
            requests.put(request);
            urlParameters.put("requests", requests);

            // An AsyncTask for network-related activities
            VisionHttpRequest cloudVisionRequest = new VisionHttpRequest(this);
            cloudVisionRequest.execute(urlParameters.toString());

        }catch (Exception e){
            e.printStackTrace();
            responseTextView.setVisibility(View.VISIBLE);
            pb.setVisibility(View.GONE);
            createAlertDialog("Error!").create().show();
        }
    }

    public void onCallbackVision(String response){
        recognizedText = response;
        responseTextView.setText(recognizedText);
        responseTextView.setVisibility(View.VISIBLE);
        setVisibilityCapture(View.VISIBLE);
        pb.setVisibility(View.GONE);
    }



    public void onClickSaveImage(View view){
        try {
            File defaultFolder = getWorkingDirectory();
            if(defaultFolder == null) {
                createAlertDialog("Directory not found, image will not be saved in History");
            }

            String imageFileName;
            switch (imageType) {
                case 0:
                    imageFileName = copyFileToFile(new File(currentPhotoPath), defaultFolder) + ".jpg";
                    break;
                case 1:
                    imageFileName = copyBase64ToFile(stringImage, defaultFolder) + ".jpg";
                    break;
                default: createAlertDialog("Invalid argument for imageType!");
                    throw new Exception();
            }
            writeFirebaseDatabase(imageFileName, responseTextView.getText().toString(), new Date());
            Toast.makeText(ScanActivity.this, "Saved file " + imageFileName,Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            e.printStackTrace();
            responseTextView.setVisibility(View.VISIBLE);
            pb.setVisibility(View.GONE);
            createAlertDialog("Error!").create().show();
        }
        setVisibilityCapture(View.GONE);
    }

    public void onClickCancelImage(View view){
        responseTextView.setText(R.string.text_default);

        setVisibilityCapture(View.GONE);
        imageType = -1;
        selectedImage.setImageResource(android.R.color.darker_gray);
        stringImage = null;
    }

    private void setVisibilityCapture(int state){
        nameEditText.setVisibility(state);
        cancelButton.setVisibility(state);
        saveButton.setVisibility(state);
        translateText.setVisibility(state);
        languageSpinner.setVisibility(state);
    }
}
