package com.example.finalwork.Fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalwork.R;
import com.example.finalwork.adapter.PostAdapter;
import com.example.finalwork.dao.PostDao;
import com.example.finalwork.entity.Post;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {
    private RecyclerView recyclerView;
    private PostAdapter adapter;
    private PostDao postDao;
    private List<Post> posts;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        posts = new ArrayList<>();
        postDao = PostDao.getInstance(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewPosts);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PostAdapter(requireContext(), posts);
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = view.findViewById(R.id.fabCreatePost);
        fab.setOnClickListener(v -> showCreatePostDialog());

        loadPosts();
        return view;
    }

    private void loadPosts() {
        try {
            posts.clear();
            posts.addAll(postDao.getAllPosts());
            adapter.notifyDataSetChanged();
            recyclerView.scrollToPosition(0);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "加载帖子失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void showCreatePostDialog() {
        try {
            Dialog dialog = new Dialog(requireContext());
            dialog.setContentView(R.layout.dialog_create_post);

            EditText titleEdit = dialog.findViewById(R.id.editTextTitle);
            EditText contentEdit = dialog.findViewById(R.id.editTextContent);
            Button cancelButton = dialog.findViewById(R.id.buttonCancel);
            Button postButton = dialog.findViewById(R.id.buttonPost);

            SharedPreferences sharedPreferences = requireContext().getSharedPreferences("user_info", Context.MODE_PRIVATE);
            String currentUser = sharedPreferences.getString("username", null);
            long userId = sharedPreferences.getLong("user_id", -1);

            if (currentUser == null || userId == -1) {
                Toast.makeText(requireContext(), "请先登录", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                return;
            }

            cancelButton.setOnClickListener(v -> dialog.dismiss());

            postButton.setOnClickListener(v -> {
                String title = titleEdit.getText().toString().trim();
                String content = contentEdit.getText().toString().trim();

                if (!title.isEmpty() && !content.isEmpty()) {
                    try {
                        Post newPost = new Post(title, content, currentUser, userId);
                        long id = postDao.insertPost(newPost);
                        if (id != -1) {
                            loadPosts();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(requireContext(), "保存失败", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(requireContext(), "发布失败", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(requireContext(), "标题和内容不能为空", Toast.LENGTH_SHORT).show();
                }
            });

            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "创建对话框失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPosts();
    }
}