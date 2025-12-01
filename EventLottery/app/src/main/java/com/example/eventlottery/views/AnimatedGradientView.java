package com.example.eventlottery.views;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * This class functions as a custom Android view that displays a rectangle filled with a dynamically animated linear gradient
 * The gradient cycles through a set of initial colors, continuously changing the color mix over time.
 */
public class AnimatedGradientView extends View {

    private Paint paint;
    private int[] colors;
    private ValueAnimator animator;

    /**
     * Constructs a new AnimatedGradientView instance.
     * @param context The context for the View.
     */

    public AnimatedGradientView(Context context) {
        super(context);
        init();
    }

    /**
     * Constructs a new AnimatedGradientView instance with attributes from an XML layout.
     * @param context The context for the View.
     * @param attrs The attributes passed from the XML layout file.
     */

    public AnimatedGradientView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Constructs a new AnimatedGradientView instance with attributes and a default style.
     * @param context The context for the View.
     * @param attrs The attributes passed from the XML layout file.
     * @param defStyleAttr The default style to apply to this view.
     */

    public AnimatedGradientView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * Initializes the view and sets up the gradient animation.
     */
    private void init() {
        paint = new Paint();

        // Initial colors
        colors = new int[]{0xFFEE7752, 0xFFE73C7E, 0xFF23A6D5, 0xFF23D5AB};

        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(3000);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.REVERSE);

        animator.addUpdateListener(animation -> {
            float fraction = (float) animation.getAnimatedValue();

            // Animate each color slightly to simulate RGB cycling
            int c0 = blendColors(colors[0], colors[1], fraction);
            int c1 = blendColors(colors[1], colors[2], fraction);
            int c2 = blendColors(colors[2], colors[3], fraction);
            int c3 = blendColors(colors[3], colors[0], fraction);

            LinearGradient gradient = new LinearGradient(
                    0, 0, getWidth(), getHeight(),
                    new int[]{c0, c1, c2, c3},
                    null,
                    Shader.TileMode.MIRROR
            );
            paint.setShader(gradient);

            invalidate();
        });

        animator.start();
    }

    /**
     * Called when the view should render its content.
     * @param canvas the canvas on which the background will be drawn
     */

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Draw a filled rectangle covering the entire view bounds using the animated paint
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
    }

    /**
     * Linearly interpolates (blends) two ARGB colors based on a ratio.
     * @param color1 The starting ARGB color
     * @param color2 The ending ARGB color
     * @param ratio The interpolation fraction
     * @return The resulting blended ARGB color.
     */

    private int blendColors(int color1, int color2, float ratio) {
        return (Integer) new ArgbEvaluator().evaluate(ratio, color1, color2);
    }
}