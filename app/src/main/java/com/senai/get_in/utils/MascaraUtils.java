package com.senai.get_in.utils;

import android.text.Editable;
import android.text.TextWatcher;
import com.google.android.material.textfield.TextInputEditText;

public class MascaraUtils {

    public static TextWatcher aplicar(TextInputEditText campo, String mascara) {
        return new TextWatcher() {
            private boolean editando = false;

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (editando) return;
                editando = true;
                
                String digits = s.toString().replaceAll("[^\\d]", "");
                StringBuilder sb = new StringBuilder();
                int di = 0;
                
                for (int i = 0; i < mascara.length() && di < digits.length(); i++) {
                    char mc = mascara.charAt(i);
                    if (mc == '#') sb.append(digits.charAt(di++));
                    else sb.append(mc);
                }
                
                campo.setText(sb.toString());
                if (sb.length() > 0) campo.setSelection(sb.length());
                
                editando = false;
            }
        };
    }
}
