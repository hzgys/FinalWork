package com.example.finalwork.Fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalwork.R;
import com.example.finalwork.adapter.MovieAdapter;
import com.example.finalwork.db.MovieDatabaseHelper;
import com.example.finalwork.entity.Movie;
import java.util.List;
import android.content.Context;

public class CollectionFragment extends Fragment {
    private MovieAdapter movieAdapter;
    private MovieDatabaseHelper dbHelper;
    private TextView emptyView;
    private long currentUserId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_collection, container, false);

        // 修改这里：使用正确的SharedPreferences名称
        SharedPreferences sharedPreferences =
                requireActivity().getSharedPreferences("user_info", Context.MODE_PRIVATE);
        currentUserId = sharedPreferences.getLong("user_id", -1);

        emptyView = view.findViewById(R.id.empty_view);
        RecyclerView recyclerView = view.findViewById(R.id.collection_recycler_view);

        setupRecyclerView(recyclerView);
        loadFavoriteMovies();

        return view;
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        movieAdapter = new MovieAdapter(requireContext());
        movieAdapter.setFromCollection(true);
        recyclerView.setAdapter(movieAdapter);
        dbHelper = new MovieDatabaseHelper(requireContext());
    }

    private void loadFavoriteMovies() {
        List<Movie> favorites = dbHelper.getFavoriteMovies(currentUserId);
        movieAdapter.setMovies(favorites);

        if (favorites.isEmpty()) {
            emptyView.setText("暂无收藏电影");
            emptyView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFavoriteMovies();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}