package com.example.finalwork.adapter;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalwork.R;
import com.example.finalwork.entity.MovieComment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MovieCommentAdapter extends RecyclerView.Adapter<MovieCommentAdapter.CommentViewHolder> {
    private List<MovieComment> comments = new ArrayList<>();

    public void setComments(List<MovieComment> comments) {
        this.comments = comments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_movie_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        MovieComment comment = comments.get(position);
        holder.bind(comment);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        private final TextView usernameText;
        private final RatingBar ratingBar;
        private final TextView contentText;
        private final TextView timeText;

        CommentViewHolder(View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.comment_username);
            ratingBar = itemView.findViewById(R.id.comment_rating);
            contentText = itemView.findViewById(R.id.comment_content);
            timeText = itemView.findViewById(R.id.comment_time);

            // 设置RatingBar属性
            ratingBar.setNumStars(5);
            ratingBar.setStepSize(0.5f);
            ratingBar.setIsIndicator(true);
        }

        void bind(MovieComment comment) {
            // 设置用户名
            usernameText.setText(comment.getUsername());

            // 将0-100的评分转换为0-5星
            float starRating = comment.getRating() / 20.0f;
            ratingBar.setRating(starRating);

            // 设置评论内容
            contentText.setText(comment.getContent());

            // 设置时间
            try {
                long timestamp = Long.parseLong(comment.getCreateTime());
                timeText.setText(formatTime(timestamp));
            } catch (NumberFormatException e) {
                timeText.setText(comment.getCreateTime());
            }
        }

        private String formatTime(long timestamp) {
            long now = System.currentTimeMillis();
            long diff = now - timestamp;

            if (diff < DateUtils.MINUTE_IN_MILLIS) {
                return "刚刚";
            } else if (diff < DateUtils.HOUR_IN_MILLIS) {
                return diff / DateUtils.MINUTE_IN_MILLIS + "分钟前";
            } else if (diff < DateUtils.DAY_IN_MILLIS) {
                return diff / DateUtils.HOUR_IN_MILLIS + "小时前";
            } else if (diff < 7 * DateUtils.DAY_IN_MILLIS) {
                return diff / DateUtils.DAY_IN_MILLIS + "天前";
            } else {
                return new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                        .format(new Date(timestamp));
            }
        }
    }
}