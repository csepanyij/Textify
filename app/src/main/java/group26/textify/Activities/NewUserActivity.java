package group26.textify.Activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import group26.textify.R;

import static android.content.ContentValues.TAG;

public class NewUserActivity extends AppCompatActivity {

    public static String passedEmail = "group26.textify";
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ProgressBar pb = findViewById(R.id.progressBarNewUser);
        pb.setVisibility(View.GONE);
        mAuth = FirebaseAuth.getInstance();

        Intent intent = getIntent();
        passedEmail = intent.getStringExtra(passedEmail);
        TextView emailTextView = findViewById(R.id.editEmail);
        emailTextView.setText(passedEmail);
    }


    public void onClickCreate(View view) {
        ProgressBar pb = findViewById(R.id.progressBarNewUser);
        pb.setVisibility(View.VISIBLE);

        TextView emailTextView = findViewById(R.id.editEmail);
        String email = emailTextView.getText().toString();

        TextView passwordTextView = findViewById(R.id.editPassword);
        String password = passwordTextView.getText().toString();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG,"createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            onAuthResponse(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            ProgressBar pb = findViewById(R.id.progressBarNewUser);
                            pb.setVisibility(View.GONE);
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(NewUserActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void onAuthResponse(FirebaseUser user) {
        if (user == null) {
            AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
            dlgAlert.setMessage("Authentication unsuccessful!");
            dlgAlert.setPositiveButton("OK", null);
            dlgAlert.setCancelable(true);
            dlgAlert.show();
        } else {
            Intent intent = new Intent(this, ScanActivity.class);
            String email = user.getEmail();
            intent.putExtra(Intent.EXTRA_TEXT, email);
            startActivity(intent);
        }
    }
}
