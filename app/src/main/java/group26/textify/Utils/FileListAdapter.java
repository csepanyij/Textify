package group26.textify.Utils;

import android.widget.*;
import android.content.Context;
import java.util.ArrayList;
import android.view.*;

import group26.textify.R;


public class FileListAdapter extends BaseAdapter{

    private LayoutInflater inflater;
    private ArrayList<Scanfile> files;

    public FileListAdapter(Context context, ArrayList<Scanfile> files) {
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
        View rowView = inflater.inflate(R.layout.list_item_file, parent, false);

        TextView nameTextView = rowView.findViewById(R.id.itemName);
        TextView summaryTextView = rowView.findViewById(R.id.itemSummary);
        ImageView thumbnailImageView = rowView.findViewById(R.id.itemThumbnail);

        Scanfile file = (Scanfile) getItem(position);
        nameTextView.setText(file.getName());
        summaryTextView.setText(file.getSummary());
        thumbnailImageView.setImageBitmap( ImageUtils.decodeSampledBitmap(file.getImage(),150,100 ));
        return rowView;
    }

}


