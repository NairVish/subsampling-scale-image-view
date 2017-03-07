package com.davemorrissey.labs.subscaleview;

import android.view.MotionEvent;

/**
 * Class that detects two finger rotation gestures and returns the rotation angle to a listener
 */
public class RotationGestureDetector {

    private OnRotationListener mListener;

    /**
     * Coordinates for (f)irst and (s)econd pointers
     */
    private float mFirstX, mFirstY, mSecondX, mSecondY;
    private int mPointer1 = MotionEvent.INVALID_POINTER_ID,
            mPointer2 = MotionEvent.INVALID_POINTER_ID;
    private float mAngle = 0;

    public RotationGestureDetector(OnRotationListener listener) {
        mListener = listener;
    }

    public interface OnRotationListener {
        public boolean onRotationBegin();

        public void onRotationEnd();

        /**
         * @param angle Rotation angle of last rotation event, radians clockwise
         * @return True if the event was consumed, false to keep accumulating
         */
        public boolean onRotation(float angle);
    }

    public boolean onTouchEvent(MotionEvent e) {
        final int index = e.getActionIndex();
        final int action = e.getActionMasked();
        final int id = e.getPointerId(index);
        switch (action) {
            // First pointer down
            case MotionEvent.ACTION_DOWN:
                mPointer1 = id;
                mFirstX = e.getX();
                mFirstY = e.getY();
                return true;

            // Further pointers down
            case MotionEvent.ACTION_POINTER_DOWN:
                if (mPointer2 == MotionEvent.INVALID_POINTER_ID) {
                    mPointer2 = id;
                    mSecondX = e.getX(index);
                    mSecondY = e.getY(index);
                    return true;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                // Nothing valid to be done
                if (e.getPointerCount() < 2) {
                    break;
                } else if (e.findPointerIndex(mPointer1) >= 0 &&
                        e.findPointerIndex(mPointer2) >= 0) {
                    // View coordinates for [f]irst and [s]econd pointers
                    final float vfX = e.getX(e.findPointerIndex(mPointer1));
                    final float vfY = e.getY(e.findPointerIndex(mPointer1));
                    final float vsX = e.getX(e.findPointerIndex(mPointer2));
                    final float vsY = e.getY(e.findPointerIndex(mPointer2));

                    // Rotation angle
                    mAngle += angleBetweenLines(mFirstX, mFirstY, mSecondX, mSecondY,
                            vfX, vfY, vsX, vsY);
                    updateListener();

                    // Update persistent variables
                    mFirstX = vfX;
                    mFirstY = vfY;
                    mSecondX = vsX;
                    mSecondY = vsY;
                    return true;
                }

                break;

            // One pointer up
            case MotionEvent.ACTION_POINTER_UP:
                if (id == mPointer1) {
                    mPointer1 = mPointer2;
                    mPointer2 = MotionEvent.INVALID_POINTER_ID;
                    mFirstX = mSecondX;
                    mFirstY = mSecondY;
                } else if (id == mPointer2) {
                    mPointer2 = MotionEvent.INVALID_POINTER_ID;
                } else {
                    // Do nothing if pointer is unknown
                    break;
                }

                // Reassign mPointer2 (if any pointers apply)
                for (int i = 0; i < e.getPointerCount(); i++) {
                    // Do not use the pointer for this event or mPointer1
                    if (i != index && i != e.findPointerIndex(mPointer1)) {
                        mPointer2 = e.getPointerId(i);
                        mSecondX = e.getX(i);
                        mSecondY = e.getY(i);
                        break;
                    }
                }

                if (mPointer2 == MotionEvent.INVALID_POINTER_ID) {
                    mListener.onRotationEnd();
                }


                return true;


            // Last pointer up
            case MotionEvent.ACTION_UP:
                mPointer1 = MotionEvent.INVALID_POINTER_ID;
                mPointer2 = MotionEvent.INVALID_POINTER_ID;
                return true;

        }

        return false;
    }

    private void updateListener() {
        mAngle = mListener.onRotation(mAngle) ? 0 : mAngle;
    }

    public static final float RAD_TO_DEG = 180f / (float) Math.PI;

    private float angleBetweenLines(float x1, float y1, float x2, float y2, float x3, float y3,
                                    float x4, float y4) {
        float angle1 = (float) Math.atan2( (y1 - y2), (x1 - x2) );
        float angle2 = (float) Math.atan2( (y3 - y4), (x3 - x4) );

        return (angle2 - angle1);

    }
}

