package group26.textify.Utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import group26.textify.R;


public class FileGridAdapter extends BaseAdapter{

    private LayoutInflater inflater;
    private ArrayList<Scanfile> files;

    public FileGridAdapter(Context context, ArrayList<Scanfile> files) {
        this.files = files;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return files.size();
    }

    @Override
    public Object getItem(int position) {
        return files.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View itemView = inflater.inflate(R.layout.grid_item_file, parent, false);

        TextView nameTextView = itemView.findViewById(R.id.itemName);
        ImageView thumbnailImageView = itemView.findViewById(R.id.itemThumbnail);
        ImageView selectedImageView = itemView.findViewById(R.id.iconSelected);

        Scanfile file = (Scanfile) getItem(position);
        nameTextView.setText(file.getName());
        thumbnailImageView.setImageBitmap( ImageUtils.decodeSampledBitmap(file.getImage(),150,100 ));
        if(file.isSelected()){
            selectedImageView.setVisibility(View.VISIBLE);
        }else{
            selectedImageView.setVisibility(View.INVISIBLE);
        }

        return itemView;
    }


}


