package group26.textify.Activities;

import android.net.Uri;
import android.os.Bundle;
import android.content.Intent;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import group26.textify.Utils.ImageUtils;
import group26.textify.R;
import group26.textify.Utils.Scanfile;


public class DetailActivity extends NavbarActivity {

    Scanfile file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        super.initNavbar(this,"files");

        Intent i = getIntent();
        file = (Scanfile) i.getSerializableExtra("selectedFile");

        System.out.println(((Scanfile) i.getSerializableExtra("selectedFile")).getName());

        TextView nameTextView = findViewById(R.id.textName);
        TextView dateTextView = findViewById(R.id.textDate);
        TextView fileTextView = findViewById(R.id.textFile);
        ImageView fileImageView = findViewById(R.id.imageFile);

        nameTextView.setText(file.getName());
        dateTextView.setText(file.getDateTaken().toString("dd/MM/yyyy HH:mm"));
        fileTextView.setText(file.getText());
        fileTextView.setMovementMethod(new ScrollingMovementMethod());
        fileImageView.setImageBitmap( ImageUtils.decodeSampledBitmap(file.getImage(),500, 500) );

    }

    public void onClickExit(View view){
        finish();
    }

    public void onClickShare(View view){
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, file.getText());
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file.getImage()));
        shareIntent.setType("image/jpeg");
        startActivity(Intent.createChooser(shareIntent, getResources().getString(R.string.share_photos)));
    }
}
