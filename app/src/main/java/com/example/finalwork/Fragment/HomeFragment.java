package com.example.finalwork.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.finalwork.R;
import com.example.finalwork.adapter.MovieAdapter;
import com.example.finalwork.db.MovieDatabaseHelper;

public class HomeFragment extends Fragment {
    private RecyclerView recyclerView;
    private MovieAdapter movieAdapter;
    private SearchView searchView;
    private MovieDatabaseHelper dbHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // 初始化数据库
        dbHelper = new MovieDatabaseHelper(getContext());

        // 初始化RecyclerView
        recyclerView = view.findViewById(R.id.movie_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        movieAdapter = new MovieAdapter(getContext());
        recyclerView.setAdapter(movieAdapter);

        // 初始化SearchView
        searchView = view.findViewById(R.id.search_view);
        setupSearchView();

        // 加载电影数据
        loadMovies();

        return view;
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                movieAdapter.setMovies(dbHelper.searchMovies(query));
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    loadMovies();
                } else {
                    movieAdapter.setMovies(dbHelper.searchMovies(newText));
                }
                return true;
            }
        });
    }

    private void loadMovies() {
        movieAdapter.setMovies(dbHelper.getAllMovies());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}