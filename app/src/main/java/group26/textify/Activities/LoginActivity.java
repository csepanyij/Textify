package group26.textify.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
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


public class LoginActivity extends Activity {
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ProgressBar pb = findViewById(R.id.progressBarLoginPage);
        pb.setVisibility(View.GONE);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        try {
            String email = currentUser.getEmail();
            if (email != null) {
                Intent intent = new Intent(this, ScanActivity.class);
                intent.setType("text/plain");
                intent.putExtra(ScanActivity.userEmail, email);
                startActivity(intent);
            }
        } catch (NullPointerException npe) {
            Log.i(TAG, "Not logged in");
        }
    }

    public void onClickLogin(View view) {

        ProgressBar pb = findViewById(R.id.progressBarLoginPage);
        pb.setVisibility(View.VISIBLE);

        TextView emailTextView = findViewById(R.id.emailTextView);
        String email = emailTextView.getText().toString();

        TextView passwordTextView = findViewById(R.id.passwordTextView);
        String password = passwordTextView.getText().toString();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        ProgressBar pb = findViewById(R.id.progressBarLoginPage);
                        pb.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            onAuthResponse(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            AlertDialog.Builder adb = new AlertDialog.Builder(LoginActivity.this);
                            adb.setMessage("Login failed, email or password is incorrect!");
                            adb.setPositiveButton("OK",null);
                            adb.setCancelable(false);
                            adb.create().show();
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        /*String title = getResources().getString(R.string.chooser_title);
        Intent chooser = Intent.createChooser(intent, title);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(chooser);*/
    }

    public  void onClickNewAccount(View view) {
        ProgressBar pb = findViewById(R.id.progressBarLoginPage);
        pb.setVisibility(View.VISIBLE);
        TextView emailTextView = findViewById(R.id.emailTextView);
        String email = emailTextView.getText().toString();
        Intent intent = new Intent(this, NewUserActivity.class);
        intent.putExtra(NewUserActivity.passedEmail, email);
        startActivity(intent);
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
            intent.putExtra(ScanActivity.userEmail, email);
            startActivity(intent);
        }
    }
}
