package group26.textify.Activities;

import android.app.Activity;
import android.content.Intent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;

import com.google.firebase.auth.FirebaseAuth;

import group26.textify.R;

public class NavbarActivity extends Activity {

    public void initNavbar(Activity child, String currentTab){
        ImageButton ib;
        switch(currentTab){
            case "camera" :
                ib = child.findViewById(R.id.buttonCamera);
                ib.setEnabled(false);
                ib.setImageResource(R.mipmap.camera_selected);
                break;
            case "files" :
                ib = child.findViewById(R.id.buttonFiles);
                ib.setEnabled(false);
                ib.setImageResource(R.mipmap.file_selected);
                break;
            case "share" :
                ib = child.findViewById(R.id.buttonShare);
                ib.setEnabled(false);
                ib.setImageResource(R.mipmap.share_selected);
                break;
            case "params" :
                ib = child.findViewById(R.id.buttonParams);
                ib.setEnabled(false);
                ib.setImageResource(R.mipmap.gear_selected);
                break;
        }
    }

    public void onClickCamera(View view) {
        Intent intent = new Intent(view.getContext(), ScanActivity.class);
        view.getContext().startActivity(intent);
    }

    public void onClickFiles(View view) {
        Intent intent = new Intent(view.getContext(), ListActivity.class);
        view.getContext().startActivity(intent);
    }

    public void onClickShare(View view) {
        Intent intent = new Intent(view.getContext(), ShareActivity.class);
        view.getContext().startActivity(intent);
    }

    public void onClickParams(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        MenuInflater inflater = popup.getMenuInflater();
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.log_out:
                        logout();
                        return true;
                    case R.id.user_guide:
                        guide();
                        return true;
                    default:
                        return false;
                }
            }
        });
        inflater.inflate(R.menu.params, popup.getMenu());
        popup.show();
    }


    public void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    public void guide() {
        Intent intent = new Intent(this, UserGuideActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

}
