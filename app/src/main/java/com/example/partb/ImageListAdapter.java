package com.example.partb;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import java.util.List;
import com.example.partb.ImageListAdapter.OnItemClickListener;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;


public class ImageListAdapter extends RecyclerView.Adapter<ImageListAdapter.ViewHolder> {

    private List<String> imageUrls;
    private List<Image> images;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(String imageUrl);
    }

    public ImageListAdapter(List<? extends Object> images, OnItemClickListener listener) {
        if (images.size() > 0 && images.get(0) instanceof Image) {
            this.images = (List<Image>) images;
        } else if (images.size() > 0 && images.get(0) instanceof String) {
            this.imageUrls = (List<String>) images;
        }
        this.listener = listener;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String imageUrl = imageUrls != null ? imageUrls.get(position) : images.get(position).getUrl();

        Picasso.get()
                .load(imageUrl)
                .fit()
                .centerCrop()
                .into(holder.imageView);

        holder.itemView.setOnClickListener(v -> listener.onItemClick(imageUrl));
    }

    @Override
    public int getItemCount() {
        return imageUrls != null ? imageUrls.size() : images.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
        }
    }
}
