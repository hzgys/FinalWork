package com.example.finalwork.Fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.finalwork.LoginActivity;
import com.example.finalwork.R;
import com.example.finalwork.dao.UserDao;
import com.example.finalwork.entity.User;

public class MineFragment extends Fragment {
    private TextView usernameTv;
    private TextView preferredTypeTv;
    private TextView ratingsTv;
    private Button logoutButton;
    private SharedPreferences sharedPreferences;
    private UserDao userDao;

    private Button changePasswordButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine, container, false);

        // 初始化视图
        usernameTv = view.findViewById(R.id.usernameTv);
        preferredTypeTv = view.findViewById(R.id.preferredTypeTv);

        logoutButton = view.findViewById(R.id.logoutButton);

        changePasswordButton = view.findViewById(R.id.changePasswordButton);
        changePasswordButton.setOnClickListener(v -> showChangePasswordDialog());

        // 获取SharedPreferences
        sharedPreferences = requireContext().getSharedPreferences("user_info", Context.MODE_PRIVATE);
        userDao = UserDao.getInstance(requireContext());
        // 显示用户名
        String username = sharedPreferences.getString("username", "未登录");
        usernameTv.setText("当前用户：" + username);
        long userId = sharedPreferences.getLong("user_id", -1);

        if (userId != -1) {
            User user = userDao.loginUser(username, sharedPreferences.getString("password", ""));
            if (user != null) {
                Log.d("MineFragment", "偏好类型: " + user.getPreferredType());
                Log.d("MineFragment", "剧情评分: " + user.getStoryRating());

                String preferredType = user.getPreferredType();
                if (preferredType != null && !preferredType.isEmpty()) {
                    preferredTypeTv.setText("偏好类型：" + preferredType);
                } else {
                    preferredTypeTv.setText("偏好类型：暂未设置");
                }


            }
        }

        // 设置退出登录按钮点击事件
        logoutButton.setOnClickListener(v -> logout());

        return view;
    }


    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_change_password, null);
        builder.setView(dialogView);

        EditText oldPasswordEt = dialogView.findViewById(R.id.oldPasswordEt);
        EditText newPasswordEt = dialogView.findViewById(R.id.newPasswordEt);
        EditText confirmPasswordEt = dialogView.findViewById(R.id.confirmPasswordEt);
        Button confirmBtn = dialogView.findViewById(R.id.confirmBtn);
        Button cancelBtn = dialogView.findViewById(R.id.cancelBtn);

        AlertDialog dialog = builder.create();

        confirmBtn.setOnClickListener(v -> {
            String oldPassword = oldPasswordEt.getText().toString();
            String newPassword = newPasswordEt.getText().toString();
            String confirmPassword = confirmPasswordEt.getText().toString();

            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(getContext(), "新密码两次输入不一致", Toast.LENGTH_SHORT).show();
                return;
            }

            long userId = sharedPreferences.getLong("user_id", -1);
            String currentPassword = sharedPreferences.getString("password", "");

            if (!oldPassword.equals(currentPassword)) {
                Toast.makeText(getContext(), "原密码错误", Toast.LENGTH_SHORT).show();
                return;
            }

            if (userDao.updatePassword(userId, newPassword)) {
                Toast.makeText(getContext(), "密码修改成功，请重新登录", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                logout();
            } else {
                Toast.makeText(getContext(), "密码修改失败", Toast.LENGTH_SHORT).show();
            }
        });

        cancelBtn.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
    private void logout() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("user_info", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();  // 清除所有数据
        editor.apply();

        // 跳转到登录界面
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        // 更新用户名显示
        String username = sharedPreferences.getString("username", "未登录");
        usernameTv.setText("当前用户：" + username);
    }
}