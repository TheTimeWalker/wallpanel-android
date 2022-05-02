
/*
 * Copyright (c) 2022 WallPanel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jjoe64.motiondetection.motiondetection;

//import android.util.Log;

public class AggregateLumaMotionDetection implements IMotionDetection {

    // private static final String TAG = "AggregateLumaMotionDetection";

    // Specific settings
    private int mLeniency = 20; // Difference of aggregate map of
                                             // luma values
    private static final int mDebugMode = 2; // State based debug
    private static final int mXBoxes = 10; // State based debug
    private static final int mYBoxes = 10; // State based debug

    private static int[] mPrevious = null;
    private static int mPreviousWidth;
    private static int mPreviousHeight;
    private static State mPreviousState = null;

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] getPrevious() {
        return ((mPrevious != null) ? mPrevious.clone() : null);
    }

    protected boolean isDifferent(int[] first, int width, int height) {
        if (first == null) throw new NullPointerException();

        if (mPrevious == null) return false;
        if (first.length != mPrevious.length) return true;
        if (mPreviousWidth != width || mPreviousHeight != height) return true;

        if (mPreviousState == null) {
            mPreviousState = new State(mPrevious, mPreviousWidth, mPreviousHeight);
            return false;
        }

        State state = new State(first, width, height);
        Comparer comparer = new Comparer(state, mPreviousState, mXBoxes, mYBoxes, mLeniency, mDebugMode);

        boolean different = comparer.isDifferent();
        // String output = "isDifferent="+different;

        mPreviousState = state;

        return different;
    }

    /**
     * Detect motion using aggregate luma values. {@inheritDoc}
     */
    @Override
    public boolean detect(int[] luma, int width, int height) {
        if (luma == null) throw new NullPointerException();

        int[] original = luma.clone();

        // Create the "mPrevious" picture, the one that will be used to check
        // the next frame against.
        if (mPrevious == null) {
            mPrevious = original;
            mPreviousWidth = width;
            mPreviousHeight = height;
            // Log.i(TAG, "Creating background image");
        }

        // long bDetection = System.currentTimeMillis();
        boolean motionDetected = isDifferent(luma, width, height);
        // long aDetection = System.currentTimeMillis();
        // Log.d(TAG, "Detection "+(aDetection-bDetection));

        // Replace the current image with the previous.
        mPrevious = original;
        mPreviousWidth = width;
        mPreviousHeight = height;

        return motionDetected;
    }

    public void setLeniency(int l) {
        mLeniency = l;
    }
}
