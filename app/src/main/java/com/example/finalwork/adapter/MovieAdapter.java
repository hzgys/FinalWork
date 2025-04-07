package com.example.finalwork.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.finalwork.R;
import com.example.finalwork.MovieDetailActivity;
import com.example.finalwork.entity.Movie;
import java.util.ArrayList;
import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {
    private static final String TAG = "MovieAdapter";
    private List<Movie> movieList;
    private List<Movie> filteredList;
    private Context context;
    private boolean fromCollection = false;

    public MovieAdapter(Context context) {
        this.context = context;
        this.movieList = new ArrayList<>();
        this.filteredList = new ArrayList<>();
    }

    public void setFromCollection(boolean fromCollection) {
        this.fromCollection = fromCollection;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_movie, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = filteredList.get(position);
        holder.titleText.setText(movie.getTitle());
        holder.ratingText.setText(String.format("评分: %.1f", movie.getRating()));
        holder.directorText.setText("导演: " + movie.getDirector());

        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.drawable.placeholder_movie)
                .error(R.drawable.placeholder_movie)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop();

        try {
            String imageUrl = movie.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                if (!imageUrl.startsWith("http://") && !imageUrl.startsWith("https://")) {
                    imageUrl = "https:" + imageUrl;
                }

                Glide.with(context)
                        .load(imageUrl)
                        .apply(requestOptions)
                        .into(holder.posterImage);
            } else {
                holder.posterImage.setImageResource(R.drawable.placeholder_movie);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading image for movie: " + movie.getTitle(), e);
            holder.posterImage.setImageResource(R.drawable.placeholder_movie);
        }

        holder.itemView.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(context, MovieDetailActivity.class);
                intent.putExtra("movie", movie);
                intent.putExtra("from_collection", fromCollection);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Error starting MovieDetailActivity", e);
                Toast.makeText(context, "无法打开电影详情", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void setMovies(List<Movie> movies) {
        this.movieList = movies;
        this.filteredList = new ArrayList<>(movies);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(movieList);
        } else {
            for (Movie movie : movieList) {
                if (movie.getTitle().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(movie);
                }
            }
        }
        notifyDataSetChanged();
    }

    static class MovieViewHolder extends RecyclerView.ViewHolder {
        ImageView posterImage;
        TextView titleText;
        TextView ratingText;
        TextView directorText;

        MovieViewHolder(View itemView) {
            super(itemView);
            posterImage = itemView.findViewById(R.id.movie_poster);
            titleText = itemView.findViewById(R.id.movie_title);
            ratingText = itemView.findViewById(R.id.movie_rating);
            directorText = itemView.findViewById(R.id.movie_director);
        }
    }
}