package com.example.finalwork.Fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import com.example.finalwork.adapter.UserDBAdapter;
import com.example.finalwork.db.MovieDatabaseHelper;
import com.example.finalwork.db.UserDatabaseHelper;
import com.example.finalwork.entity.Movie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class RecommendFragment extends Fragment {
    private RecyclerView recyclerView;
    private MovieAdapter movieAdapter;
    private MovieDatabaseHelper movieDb;
    private TextView emptyView;
    private long currentUserId;

    public RecommendFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        movieDb = new MovieDatabaseHelper(requireContext());
        // 获取当前用户ID
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recommend, container, false);

        SharedPreferences prefs = requireActivity().getSharedPreferences("user_info", Context.MODE_PRIVATE);
        currentUserId = prefs.getLong("user_id", -1);
        // 初始化视图
        recyclerView = view.findViewById(R.id.recommendRecyclerView);
        emptyView = view.findViewById(R.id.emptyView);

        // 设置RecyclerView
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        movieAdapter = new MovieAdapter(getContext());
        recyclerView.setAdapter(movieAdapter);

        // 加载推荐电影
        loadRecommendations();

        return view;
    }

    private void loadRecommendations() {
        List<Movie> recommendations = new ArrayList<>();

        // 获取用户评分数量
        int userRatingCount = getUserRatingCount();

        if (userRatingCount < 5) {
            // 冷启动荐：基于用户选择的偏好
            recommendations = getColdStartRecommendations();
        } else {
            // 基于用户评分历史的协同过滤推荐
            recommendations = getCollaborativeRecommendations();
        }

        updateUI(recommendations);
    }

    private List<Movie> getColdStartRecommendations() {
        List<Movie> recommendations = new ArrayList<>();
        Integer preferredTypeId = null;

        try {
            UserDatabaseHelper userDbHelper = new UserDatabaseHelper(requireContext());
            SQLiteDatabase userDb = userDbHelper.getReadableDatabase();

            // 使用 preferred_type_id 进行查询
            Cursor cursor = userDb.query("user_preferences",
                    new String[]{"preferred_type_id"},
                    "user_id = ?",
                    new String[]{String.valueOf(currentUserId)},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                preferredTypeId = cursor.getInt(cursor.getColumnIndexOrThrow("preferred_type_id"));
            }
            cursor.close();
            userDb.close();

            if (preferredTypeId != null) {
                // 使用 category_id 匹配电影，从 classified_movies 表查询
                String movieQuery =
                        "SELECT DISTINCT m.* FROM movies m " +
                                "INNER JOIN classified_movies c ON m.id = c.movie_id " +
                                "WHERE c.category_id = ? " +
                                "ORDER BY m.rating DESC LIMIT 10";
                recommendations = movieDb.getMoviesByQuery(movieQuery,
                        new String[]{String.valueOf(preferredTypeId)});
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // 如果推荐数量不足，补充高分电影
        if (recommendations.size() < 10) {
            int needed = 10 - recommendations.size();
            String topQuery =
                    "SELECT DISTINCT m.* FROM movies m " +
                            "INNER JOIN classified_movies c ON m.id = c.movie_id " +
                            "WHERE c.category_id != ? " +
                            "ORDER BY m.rating DESC LIMIT ?";
            List<Movie> topMovies = movieDb.getMoviesByQuery(topQuery,
                    new String[]{String.valueOf(preferredTypeId), String.valueOf(needed)});
            recommendations.addAll(topMovies);
        }

        return recommendations;
    }


    private int getUserRatingCount() {
        return movieDb.getRatingCount(currentUserId);
    }

    private List<Movie> getPopularMovies() {
        List<Movie> popularMovies = new ArrayList<>();

        // 获取评分最高的10部电影
        String query = "SELECT m.* FROM movies m " +
                "ORDER BY m.rating DESC LIMIT 10";

        popularMovies = movieDb.getMoviesByQuery(query, null);
        return popularMovies;
    }

    private List<Movie> getCollaborativeRecommendations() {
        // 1. 获取当前用户的评分数据
        Map<Long, Double> userRatings = getUserRatings(currentUserId);

        // 2. 计算用户相似度并获取相似用户
        List<UserSimilarity> similarities = getSimilarUsers(userRatings);

        // 3. 收集推荐候选电影
        Map<Long, Double> candidateScores = new HashMap<>();
        for (UserSimilarity simUser : similarities) {
            Map<Long, Double> otherRatings = getUserRatings(simUser.getUserId());
            for (Map.Entry<Long, Double> entry : otherRatings.entrySet()) {
                Long movieId = entry.getKey();
                if (!userRatings.containsKey(movieId)) {
                    double weightedScore = entry.getValue() * simUser.getSimilarity();
                    candidateScores.merge(movieId, weightedScore, Double::sum);
                }
            }
        }

        // 4. 获取并返回推荐电影列表
        return candidateScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(10)
                .map(e -> movieDb.getMovieById(e.getKey()))
                .filter(m -> m != null && !hasRatedOrFavorited(m.getId()))
                .collect(Collectors.toList());
    }

    private Map<Long, Double> getUserRatings(long userId) {
        String query = "SELECT movie_id, rating FROM ratings WHERE user_id = ?";
        return movieDb.getRatingsMap(query, new String[]{String.valueOf(userId)});
    }

    private List<UserSimilarity> getSimilarUsers(Map<Long, Double> userRatings) {
        List<UserSimilarity> similarities = new ArrayList<>();
        List<Long> otherUsers = movieDb.getOtherUsers(currentUserId);

        for (Long otherId : otherUsers) {
            Map<Long, Double> otherRatings = getUserRatings(otherId);
            double similarity = calculateCosineSimilarity(userRatings, otherRatings);
            if (similarity > 0) {
                similarities.add(new UserSimilarity(otherId, similarity));
            }
        }

        return similarities.stream()
                .sorted((a, b) -> Double.compare(b.getSimilarity(), a.getSimilarity()))
                .limit(20)
                .collect(Collectors.toList());
    }

    private double calculateCosineSimilarity(Map<Long, Double> user1, Map<Long, Double> user2) {
        Set<Long> commonMovies = new HashSet<>(user1.keySet());
        commonMovies.retainAll(user2.keySet());

        if (commonMovies.isEmpty()) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (Long movieId : commonMovies) {
            double score1 = user1.get(movieId);
            double score2 = user2.get(movieId);
            dotProduct += score1 * score2;
            norm1 += Math.pow(score1, 2);
            norm2 += Math.pow(score2, 2);
        }

        if (norm1 == 0 || norm2 == 0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    private boolean hasRatedOrFavorited(long movieId) {
        return movieDb.hasRated(currentUserId, movieId) ||
                movieDb.hasFavorited(currentUserId, movieId);
    }

    private static class UserSimilarity {
        private final long userId;
        private final double similarity;

        UserSimilarity(long userId, double similarity) {
            this.userId = userId;
            this.similarity = similarity;
        }

        public long getUserId() {
            return userId;
        }

        public double getSimilarity() {
            return similarity;
        }
    }

    private void updateUI(List<Movie> recommendations) {
        if (recommendations.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
            movieAdapter.setMovies(recommendations);
        }
    }

    private List<Long> getSimilarUsers() {
        List<Long> similarUsers = new ArrayList<>();

        // 找到与当前用户有相似评分行为的用户
        String query = "SELECT DISTINCT r2.user_id FROM ratings r1 " +
                "INNER JOIN ratings r2 ON r1.movie_id = r2.movie_id " +
                "WHERE TRIM(r1.user_id) = TRIM(?) COLLATE NOCASE AND r2.user_id != ? " +
                "GROUP BY r2.user_id " +
                "HAVING COUNT(*) >= 3 " +
                "ORDER BY AVG(ABS(r1.rating - r2.rating)) " +
                "LIMIT 5";

        String[] args = {String.valueOf(currentUserId), String.valueOf(currentUserId)};
        similarUsers = movieDb.getUsersByQuery(query, args);

        return similarUsers;
    }

    @Override
    public void onResume() {
        super.onResume();
        // 刷新推荐列表
        loadRecommendations();
    }
}