package com.senai.get_in.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.senai.get_in.R;

public class ToastUtils {

    public static void showSuccess(Context context, String message) {
        showCustomToast(context, message, R.drawable.ic_launcher_foreground, true);
    }

    public static void showError(Context context, String message) {
        showCustomToast(context, message, R.drawable.ic_launcher_foreground, true);
    }

    public static void showInfo(Context context, String message) {
        showCustomToast(context, message, 0, false);
    }

    private static void showCustomToast(Context context, String message, int iconRes, boolean showIcon) {
        if (context == null) return;

        LayoutInflater inflater = LayoutInflater.from(context);
        View layout = inflater.inflate(R.layout.layout_custom_toast, null);

        TextView text = layout.findViewById(R.id.toast_text);
        text.setText(message);

        ImageView icon = layout.findViewById(R.id.toast_icon);
        if (showIcon && iconRes != 0) {
            icon.setImageResource(iconRes);
            icon.setVisibility(View.VISIBLE);
        } else {
            icon.setVisibility(View.GONE);
        }

        Toast toast = new Toast(context.getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }
}
