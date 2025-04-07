package com.example.finalwork.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalwork.R;
import com.example.finalwork.dao.PostDao;
import com.example.finalwork.entity.Post;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private List<Post> posts;
    private OnPostClickListener listener;
    private Context context;
    private PostDao postDao;

    public interface OnPostClickListener {
        void onPostClick(Post post);
    }

    public PostAdapter(Context context, List<Post> posts) {
        this.context = context;
        this.posts = posts;
        this.postDao = PostDao.getInstance(context);
    }

    public void setOnPostClickListener(OnPostClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.titleTextView.setText(post.getTitle());
        holder.contentTextView.setText(post.getContent());
        holder.authorTextView.setText("作者：" + post.getAuthor());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String dateStr = sdf.format(new Date(post.getTimestamp()));
        holder.timestampTextView.setText(dateStr);

        // 检查当前用户是否是帖子作者
        SharedPreferences sharedPreferences = context.getSharedPreferences("user_info", Context.MODE_PRIVATE);
        long currentUserId = sharedPreferences.getLong("user_id", -1);

        if (currentUserId == post.getUserId()) {
            holder.deleteButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setOnClickListener(v -> showDeleteConfirmDialog(position));
        } else {
            holder.deleteButton.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPostClick(post);
            }
        });
    }

    private void showDeleteConfirmDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("删除帖子")
                .setMessage("确定要删除这个帖子吗？");

        // 创建按钮并设置颜色
        builder.setPositiveButton("确定", (dialog, which) -> deletePost(position))
                .setNegativeButton("取消", null);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

            // 设置按钮文字颜色
            positiveButton.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
            negativeButton.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
        });

        dialog.show();
    }

    private void deletePost(int position) {
        Post post = posts.get(position);
        if (postDao.deletePost(post.getId())) {
            posts.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, posts.size());
            Toast.makeText(context, "删除成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "删除失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView contentTextView;
        TextView authorTextView;
        TextView timestampTextView;
        ImageButton deleteButton;

        PostViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.textViewTitle);
            contentTextView = itemView.findViewById(R.id.textViewContent);
            authorTextView = itemView.findViewById(R.id.textViewAuthor);
            timestampTextView = itemView.findViewById(R.id.textViewTimestamp);
            deleteButton = itemView.findViewById(R.id.buttonDelete);
        }
    }
}