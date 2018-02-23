/*
 * Copyright 2017, The Android Open Source Project
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

package com.example.androidthings.candle;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.BounceInterpolator;

import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.Pwm;

import java.io.IOException;

/**
 * Implement a flickering candle using LEDs connected to PWM
 * and a simple ObjectAnimator.
 */
public class HomeActivity extends Activity {
    private static final String TAG = "EdisonCandle";

    // PWM output pin names
    private static final String YELLOW_PWM = BoardDefaults.getYellowPwmPort();
    private static final String ORANGE_PWM = BoardDefaults.getOrangePwmPort();
    // Brightness values
    private static final float BRIGHTNESS_START = 25f;
    private static final float BRIGHTNESS_END = 100f;
    // Animation duration
    private static final int DURATION_MS = 350;
    // Offset between animations to give the candle more life
    private static final int DELAY_MS = 150;

    private Pwm mYellowLed, mOrangeLed;
    private ObjectAnimator mYellowFlame, mOrangeFlame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            mYellowLed = openLed(YELLOW_PWM);
            mOrangeLed = openLed(ORANGE_PWM);
        } catch (IOException e) {
            Log.w(TAG, "Unable to open LED connections", e);
        }

        // Create an run the flicker animations
        mYellowFlame = animateFlicker(mYellowLed, 0);
        mOrangeFlame = animateFlicker(mOrangeLed, DELAY_MS);
        mYellowFlame.start();
        mOrangeFlame.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mYellowFlame.cancel();
        mOrangeFlame.cancel();

        try {
            closeLed(mYellowLed);
            closeLed(mOrangeLed);
        } catch (IOException e) {
            Log.w(TAG, "Unable to close LED connections", e);
        }
    }

    /**
     * Open a new LED connection to the provided port name
     * @throws IOException
     */
    private Pwm openLed(String name) throws IOException {
        Pwm led = PeripheralManager.getInstance().openPwm(name);
        led.setPwmFrequencyHz(60.0f);
        led.setPwmDutyCycle(BRIGHTNESS_START);
        led.setEnabled(true);

        return led;
    }

    /**
     * Close the provided PWM connection
     * @throws IOException
     */
    private void closeLed(Pwm pwm) throws IOException {
        if (pwm != null) {
            pwm.setEnabled(false);
            pwm.close();
        }
    }

    /**
     * Create an infinite animation to modify the LED brightness.
     */
    private ObjectAnimator animateFlicker(Pwm led, long delay) {
        ObjectAnimator animator = ObjectAnimator
                .ofFloat(led, new BrightnessProperty(), BRIGHTNESS_START, BRIGHTNESS_END)
                .setDuration(DURATION_MS + delay);

        // "Bounce" at each end to create a flicker effect
        animator.setInterpolator(new BounceInterpolator());
        // Cycle through the animation forever
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setRepeatCount(ValueAnimator.INFINITE);

        return animator;
    }

}
