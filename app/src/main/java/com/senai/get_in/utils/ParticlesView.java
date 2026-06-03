package com.senai.get_in.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ParticlesView extends View {
    private static final int PARTICLE_COUNT = 60;
    private static final float MAX_DISTANCE = 250f;
    private final List<Particle> particles = new ArrayList<>();
    private final Paint particlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Random random = new Random();
    private boolean isInitialized = false;

    public ParticlesView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // Esta linha deteta se o modo escuro está ativo
        int nightModeFlags = getContext().getResources().getConfiguration().uiMode &
                android.content.res.Configuration.UI_MODE_NIGHT_MASK;

        int color;
        if (nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
            color = Color.WHITE; // Cor branca no modo escuro
        } else {
            color = Color.parseColor("#4DA8EA"); // Azul original no modo claro
        }

        particlePaint.setColor(color);
        particlePaint.setStyle(Paint.Style.FILL);
        particlePaint.setAlpha(100); // Mantendo a transparência que sugerimos antes

        linePaint.setColor(color);
        linePaint.setStrokeWidth(1.5f);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!isInitialized) {
            for (int i = 0; i < PARTICLE_COUNT; i++) {
                particles.add(new Particle(getWidth(), getHeight()));
            }
            isInitialized = true;
        }

        // Desenhar e atualizar partículas
        for (int i = 0; i < particles.size(); i++) {
            Particle p1 = particles.get(i);
            p1.update(getWidth(), getHeight());

            // Desenhar partícula
            canvas.drawCircle(p1.x, p1.y, p1.radius, particlePaint);

            // Desenhar linhas entre partículas próximas
            for (int j = i + 1; j < particles.size(); j++) {
                Particle p2 = particles.get(j);
                float dx = p1.x - p2.x;
                float dy = p1.y - p2.y;
                float distance = (float) Math.sqrt(dx * dx + dy * dy);

                if (distance < MAX_DISTANCE) {
                    // Efeito de opacidade baseado na distância
                    int alpha = (int) (255 * (1 - distance / MAX_DISTANCE) * 1);
                    linePaint.setAlpha(alpha);
                    canvas.drawLine(p1.x, p1.y, p2.x, p2.y, linePaint);
                }
            }
        }

        invalidate(); // Força o redesenho para criar a animação
    }

    private class Particle {
        float x, y, vx, vy, radius;

        Particle(int width, int height) {
            x = random.nextFloat() * width;
            y = random.nextFloat() * height;
            vx = (random.nextFloat() - 0.5f) * 2f;
            vy = (random.nextFloat() - 0.5f) * 2f;
            radius = random.nextFloat() * 5f + 6f;
        }

        void update(int width, int height) {
            x += vx;
            y += vy;

            if (x < 0 || x > width) vx *= -1;
            if (y < 0 || y > height) vy *= -1;
        }
    }
}
