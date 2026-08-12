/*
 * Copyright 2026 Synced Synapse. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.xbmc.kore.ui.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.xbmc.kore.utils.UIUtils;

/**
 * Full-area touch surface that translates swipes/taps into remote navigation gestures,
 * mirroring the trackpad mode found in apps like Yatse. Reuses {@link GestureDetector}
 * from the platform, no new dependency.
 */
public class GestureTrackpad extends FrameLayout {

    private static final int SWIPE_MIN_DISTANCE = 60; // dp
    private static final int SWIPE_MIN_VELOCITY = 100; // dp/s

    public interface OnTrackpadGestureListener {
        void onSwipeUp();
        void onSwipeDown();
        void onSwipeLeft();
        void onSwipeRight();
        void onTap();
        void onDoubleTap();
        void onLongPress();
    }

    private OnTrackpadGestureListener onTrackpadGestureListener;
    private GestureDetector gestureDetector;

    public GestureTrackpad(@NonNull Context context) {
        super(context);
        initializeView(context);
    }

    public GestureTrackpad(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initializeView(context);
    }

    public GestureTrackpad(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initializeView(context);
    }

    public void setOnTrackpadGestureListener(OnTrackpadGestureListener listener) {
        this.onTrackpadGestureListener = listener;
    }

    private void initializeView(Context context) {
        setClickable(true);
        setFocusable(true);
        gestureDetector = new GestureDetector(context, new GestureListener(context));
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        onTrackpadGestureListener = null;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private final float density;

        GestureListener(Context context) {
            this.density = context.getResources().getDisplayMetrics().density;
        }

        @Override
        public boolean onDown(@NonNull MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
            if (onTrackpadGestureListener == null) return false;
            UIUtils.handleVibration(getContext(), GestureTrackpad.this);
            onTrackpadGestureListener.onTap();
            return true;
        }

        @Override
        public boolean onDoubleTap(@NonNull MotionEvent e) {
            if (onTrackpadGestureListener == null) return false;
            UIUtils.handleVibration(getContext(), GestureTrackpad.this);
            onTrackpadGestureListener.onDoubleTap();
            return true;
        }

        @Override
        public void onLongPress(@NonNull MotionEvent e) {
            if (onTrackpadGestureListener == null) return;
            UIUtils.handleVibration(getContext(), GestureTrackpad.this);
            onTrackpadGestureListener.onLongPress();
        }

        @Override
        public boolean onFling(MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
            if (e1 == null || onTrackpadGestureListener == null) return false;

            float minDistance = SWIPE_MIN_DISTANCE * density;
            float minVelocity = SWIPE_MIN_VELOCITY * density;

            float diffX = e2.getX() - e1.getX();
            float diffY = e2.getY() - e1.getY();

            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (Math.abs(diffX) < minDistance || Math.abs(velocityX) < minVelocity) return false;
                UIUtils.handleVibration(getContext(), GestureTrackpad.this);
                if (diffX > 0) {
                    onTrackpadGestureListener.onSwipeRight();
                } else {
                    onTrackpadGestureListener.onSwipeLeft();
                }
            } else {
                if (Math.abs(diffY) < minDistance || Math.abs(velocityY) < minVelocity) return false;
                UIUtils.handleVibration(getContext(), GestureTrackpad.this);
                if (diffY > 0) {
                    onTrackpadGestureListener.onSwipeDown();
                } else {
                    onTrackpadGestureListener.onSwipeUp();
                }
            }
            return true;
        }
    }
}
