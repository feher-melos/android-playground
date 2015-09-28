package net.feketga.cropstudy1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import net.feketga.cropstudy1.ImageCropper;

public class CropImageView extends SubsamplingScaleImageView implements View.OnTouchListener {

    private static final String TAG = CropImageView.class.getSimpleName();

    private int mOffset = 100; // pixels
    private int mMinimumCropArea = 200 * 200; // pixels
    private int mCropBorderWidth = 2; // pixels
    private int mCropCornerWidth = 6; // pixels
    private int mCropBorderTouchRadius = 50; // pixels

    private RectF mCropRectangle;

    public CropImageView(Context context) {
        this(context, null);
    }

    public CropImageView(Context context, AttributeSet attr) {
        super(context, attr);
    }

    public void setImageBitmap(Bitmap bitmap) {
        setImage(ImageSource.bitmap(bitmap));
    }

    public RectF getCropRectangle() {
        PointF topLeft = viewToSourceCoord(mCropRectangle.left, mCropRectangle.top);
        PointF bottomRight = viewToSourceCoord(mCropRectangle.right, mCropRectangle.bottom);
        return new RectF(topLeft.x, topLeft.y, bottomRight.x, bottomRight.y);
    }

    public boolean saveCropToFile(String filePath) {
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Don't draw before image is ready so it doesn't move around during setup.
        if (!isReady()) {
            return;
        }

        if (mCropRectangle == null) {
            mCropRectangle = new RectF();
            PointF imageTopLeft = sourceToViewCoord(0, 0);
            PointF imageBottomRight = sourceToViewCoord(getSWidth() - 1, getSHeight() - 1);
            mCropRectangle.set(imageTopLeft.x, imageTopLeft.y, imageBottomRight.x, imageBottomRight.y);
            updateAfterAdjustments();
        }

        // Cover the image oustide the crop area with a dark shade.
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK);
        paint.setAlpha(mIsAdjustingCropArea ? 160 : 190);

        // Cover the top of the image view with a dark shade.
        RectF rectangle = new RectF(0, 0, getWidth(), mCropRectangle.top);
        canvas.drawRect(rectangle, paint);

        // Cover the bottom of the image view with a dark shade.
        rectangle.set(0, mCropRectangle.bottom, getWidth(), getHeight());
        canvas.drawRect(rectangle, paint);

        // Cover the left of the image view with a dark shade.
        rectangle.set(0, mCropRectangle.top, mCropRectangle.left, mCropRectangle.bottom);
        canvas.drawRect(rectangle, paint);

        // Cover the right of the image view with a dark shade.
        rectangle.set(mCropRectangle.right, mCropRectangle.top, getWidth(), mCropRectangle.bottom);
        canvas.drawRect(rectangle, paint);

        // Draw a bounding box around the crop area to make it stand out more.
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(mCropBorderWidth);
        paint.setColor(Color.GRAY);
        paint.setAlpha(255);
        canvas.drawRect(mCropRectangle, paint);

        // Left/top corner of crop area.
        paint.setStrokeWidth(mCropCornerWidth);
        paint.setColor(mIsAdjustingCropArea ? Color.WHITE : Color.GRAY);

        canvas.drawLine(
                mCropRectangle.left, mCropRectangle.top,
                mCropRectangle.left, mCropRectangle.top + mCropBorderTouchRadius,
                paint);
        canvas.drawLine(
                mCropRectangle.left, mCropRectangle.top,
                mCropRectangle.left + mCropBorderTouchRadius, mCropRectangle.top,
                paint);

        // Right/top corner of crop area.
        canvas.drawLine(
                mCropRectangle.right, mCropRectangle.top,
                mCropRectangle.right, mCropRectangle.top + mCropBorderTouchRadius,
                paint);
        canvas.drawLine(
                mCropRectangle.right, mCropRectangle.top,
                mCropRectangle.right - mCropBorderTouchRadius, mCropRectangle.top,
                paint);

        // Left/bottom corner of crop area.
        canvas.drawLine(
                mCropRectangle.left, mCropRectangle.bottom,
                mCropRectangle.left, mCropRectangle.bottom - mCropBorderTouchRadius,
                paint);
        canvas.drawLine(
                mCropRectangle.left, mCropRectangle.bottom,
                mCropRectangle.left + mCropBorderTouchRadius, mCropRectangle.bottom,
                paint);

        // Right/bottom corner of crop area.
        canvas.drawLine(
                mCropRectangle.right, mCropRectangle.bottom,
                mCropRectangle.right, mCropRectangle.bottom - mCropBorderTouchRadius,
                paint);
        canvas.drawLine(
                mCropRectangle.right, mCropRectangle.bottom,
                mCropRectangle.right - mCropBorderTouchRadius, mCropRectangle.bottom,
                paint);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }

    private Handler mHandler = new Handler();
    private boolean mIsAdjustingCropArea = false;
    private int mAdjustedCropBorder = BORDER_NONE;
    private PointF mFingerPos = new PointF();

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean consumed = false;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                consumed = handleActionDown(event);
                break;
            case MotionEvent.ACTION_UP:
                consumed = handleActionUp(event);
                break;
            case MotionEvent.ACTION_MOVE:
                consumed = handleActionMove(event);
                break;
            case MotionEvent.ACTION_CANCEL:
                consumed = handleActionCancel(event);
                break;
        }

        if (consumed) {
            Log.d(TAG, "CONSUMED: US");
            invalidate();
        }
        if (!consumed) {
            consumed = super.onTouchEvent(event);
            if (consumed) {
                Log.d(TAG, "CONSUMED: LIB");
            }
        }
        return consumed;
    }

    private boolean handleActionDown(MotionEvent event) {
        boolean isEventConsumed = false;
        int pointerIndex = event.getActionIndex();
        Log.d(TAG, "ACTION DOWN: " + pointerIndex);

        if (mIsAdjustingCropArea) {
            mIsAdjustingCropArea = false;
            isEventConsumed = true;
        } else {
            mFingerPos.set(event.getX(), event.getY());
            mAdjustedCropBorder = determineTouchedCorner(mFingerPos);
            if (mAdjustedCropBorder != BORDER_NONE) {
                mIsAdjustingCropArea = true;
                Log.d(TAG, "FINGER DOWN ON CROP BORDER: " + mAdjustedCropBorder);
                isEventConsumed = true;
            }
        }

        return isEventConsumed;
    }

    private static final int BORDER_NONE = 0;
    private static final int BORDER_TOP = 1;
    private static final int BORDER_BOTTOM = 1 << 1;
    private static final int BORDER_LEFT = 1 << 2;
    private static final int BORDER_RIGHT = 1 << 3;

    private int determineTouchedBorder(PointF touchPoint) {
        int touchedBorder = BORDER_NONE;

        // Top border of crop rectangle.
        RectF border = new RectF(
                mCropRectangle.left - mCropBorderTouchRadius,
                mCropRectangle.top - mCropBorderTouchRadius,
                mCropRectangle.right + mCropBorderTouchRadius,
                mCropRectangle.top + mCropBorderTouchRadius);
        if (border.contains(touchPoint.x, touchPoint.y)) {
            touchedBorder |= BORDER_TOP;
        }

        // Bottom border of crop rectangle.
        border.set(
                mCropRectangle.left - mCropBorderTouchRadius,
                mCropRectangle.bottom - mCropBorderTouchRadius,
                mCropRectangle.right + mCropBorderTouchRadius,
                mCropRectangle.bottom + mCropBorderTouchRadius);
        if (border.contains(touchPoint.x, touchPoint.y)) {
            touchedBorder |= BORDER_BOTTOM;
        }

        // Left border of crop rectangle.
        border.set(
                mCropRectangle.left - mCropBorderTouchRadius,
                mCropRectangle.top - mCropBorderTouchRadius,
                mCropRectangle.left + mCropBorderTouchRadius,
                mCropRectangle.bottom + mCropBorderTouchRadius);
        if (border.contains(touchPoint.x, touchPoint.y)) {
            touchedBorder |= BORDER_LEFT;
        }

        // Right border of crop rectangle.
        border.set(
                mCropRectangle.right - mCropBorderTouchRadius,
                mCropRectangle.top - mCropBorderTouchRadius,
                mCropRectangle.right + mCropBorderTouchRadius,
                mCropRectangle.bottom + mCropBorderTouchRadius);
        if (border.contains(touchPoint.x, touchPoint.y)) {
            touchedBorder |= BORDER_RIGHT;
        }

        return touchedBorder;
    }

    private int determineTouchedCorner(PointF touchPoint) {
        int touchedBorder = BORDER_NONE;

        // Left/top corner of crop rectangle.
        RectF border = new RectF(
                mCropRectangle.left - mCropBorderTouchRadius,
                mCropRectangle.top - mCropBorderTouchRadius,
                mCropRectangle.left + mCropBorderTouchRadius,
                mCropRectangle.top + mCropBorderTouchRadius);
        if (border.contains(touchPoint.x, touchPoint.y)) {
            touchedBorder |= BORDER_LEFT | BORDER_TOP;
        }

        // Right/top corner of crop rectangle.
        border.set(
                mCropRectangle.right - mCropBorderTouchRadius,
                mCropRectangle.top - mCropBorderTouchRadius,
                mCropRectangle.right + mCropBorderTouchRadius,
                mCropRectangle.top + mCropBorderTouchRadius);
        if (border.contains(touchPoint.x, touchPoint.y)) {
            touchedBorder |= BORDER_RIGHT | BORDER_TOP;
        }

        // Left/bottom corner of crop rectangle.
        border.set(
                mCropRectangle.left - mCropBorderTouchRadius,
                mCropRectangle.bottom - mCropBorderTouchRadius,
                mCropRectangle.left + mCropBorderTouchRadius,
                mCropRectangle.bottom + mCropBorderTouchRadius);
        if (border.contains(touchPoint.x, touchPoint.y)) {
            touchedBorder |= BORDER_LEFT | BORDER_BOTTOM;
        }

        // Right/bottom corner of crop rectangle.
        border.set(
                mCropRectangle.right - mCropBorderTouchRadius,
                mCropRectangle.bottom - mCropBorderTouchRadius,
                mCropRectangle.right + mCropBorderTouchRadius,
                mCropRectangle.bottom + mCropBorderTouchRadius);
        if (border.contains(touchPoint.x, touchPoint.y)) {
            touchedBorder |= BORDER_RIGHT | BORDER_BOTTOM;
        }

        return touchedBorder;
    }

    private boolean handleActionMove(MotionEvent event) {
        boolean isEventConsumed = false;
        int pointerIndex = event.getActionIndex();
        Log.d(TAG, "ACTION MOVE: " + pointerIndex);

        if (mIsAdjustingCropArea) {
            RectF newCropRectangle = new RectF(mCropRectangle);

            if ((mAdjustedCropBorder & BORDER_LEFT) != BORDER_NONE) {
                newCropRectangle.left += event.getX() - mFingerPos.x;
            }
            if ((mAdjustedCropBorder & BORDER_RIGHT) != BORDER_NONE) {
                newCropRectangle.right += event.getX() - mFingerPos.x;
            }
            if ((mAdjustedCropBorder & BORDER_TOP) != BORDER_NONE) {
                newCropRectangle.top += event.getY() - mFingerPos.y;
            }
            if ((mAdjustedCropBorder & BORDER_BOTTOM) != BORDER_NONE) {
                newCropRectangle.bottom += event.getY() - mFingerPos.y;
            }

            final float oldCropArea = mCropRectangle.width() * mCropRectangle.height();
            final float newCropArea = newCropRectangle.width() * newCropRectangle.height();
            if (newCropArea >= mMinimumCropArea || newCropArea >= oldCropArea) {
                mCropRectangle.set(newCropRectangle);
                invalidate();
            }

            mFingerPos.set(event.getX(), event.getY());
            isEventConsumed = true;
        }

        return isEventConsumed;
    }

    private boolean handleActionUp(MotionEvent event) {
        boolean isEventConsumed = false;
        int pointerIndex = event.getActionIndex();
        Log.d(TAG, "ACTION UP: " + pointerIndex);

        if (mIsAdjustingCropArea) {
            Log.d(TAG, "FINGER UP FROM CROP BORDER");
            mIsAdjustingCropArea = false;
            isEventConsumed = true;
        }

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                updateAfterAdjustments();
            }
        });

        return isEventConsumed;
    }

    private boolean handleActionCancel(MotionEvent event) {
        boolean isEventConsumed = false;

        if (mIsAdjustingCropArea) {
            mIsAdjustingCropArea = false;
            isEventConsumed = true;

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    updateAfterAdjustments();
                }
            });
        }

        return isEventConsumed;
    }

    private void updateAfterAdjustments() {
//        Log.d(TAG, "ORIENTATION: " + getAppliedOrientation());

        final int rawImageWidth = getSWidth();
        final int rawImageHeight = getSHeight();

        final PointF imageTopLeft = sourceToViewCoord(0, 0);
        final PointF imageBottomRight = sourceToViewCoord(rawImageWidth, rawImageHeight);
        final RectF imageRectangle = new RectF(imageTopLeft.x, imageTopLeft.y, imageBottomRight.x, imageBottomRight.y);

        if (imageRectangle.contains(mCropRectangle)) {
//            Log.d(TAG, "CROP INSIDE IMAGE. OK!");
            MaximizingParameters mp = calculateMaximizingParameters(mCropRectangle);
            updateImageAndCropRectangle(mCropRectangle, mp);
            return;
        }

        RectF intersection = new RectF();
        boolean isIntersecting = intersection.setIntersect(mCropRectangle, imageRectangle);
        if (isIntersecting) {
//            Log.d(TAG, "INTERSECTION! CORRECTING");
            // Scale up the intersection to use the max available/allowed room.
            MaximizingParameters mp = calculateMaximizingParameters(intersection);
            updateImageAndCropRectangle(intersection, mp);
        } else {
            // TODO
//            Log.d(TAG, "NO INTERSECTION");
        }
    }

    private class MaximizingParameters {
        public boolean isValid;
        public float rectangleWidth;
        public float rectangleHeight;
        public float rectangleScale;
        public float imageScale;
    }

    private MaximizingParameters calculateMaximizingParameters(RectF rectangle) {
        MaximizingParameters mp;

        if (rectangle.width() > rectangle.height()) {
            mp = calculateHorizontalMaximizingParameters(rectangle);
            if (!mp.isValid) {
                mp = calculateVerticalMaximizingParameters(rectangle);
            }
        } else {
            mp = calculateVerticalMaximizingParameters(rectangle);
            if (!mp.isValid) {
                mp = calculateHorizontalMaximizingParameters(rectangle);
            }
        }

        return mp;
    }

    private MaximizingParameters calculateHorizontalMaximizingParameters(RectF rectangle) {
        MaximizingParameters mp = new MaximizingParameters();

//        Log.d(TAG, "MAX HORIZ/WIDTH RECT: " + rectangle.toShortString() + ", " + getWidth());

        final float rawImageWidth = getSWidth();
        final float imageWidth = rawImageWidth * getScale();
        final int maxAllowedWidth = getWidth() - (mOffset * 2);

        mp.rectangleWidth = maxAllowedWidth;
        mp.rectangleScale = mp.rectangleWidth / rectangle.width();
        mp.rectangleHeight = mp.rectangleScale * rectangle.height();
        float newImageWidth = mp.rectangleScale * imageWidth;
        mp.imageScale = newImageWidth / rawImageWidth;

        sanitizeMaximizingParameters(mp, rectangle);

        return mp;
    }

    private MaximizingParameters calculateVerticalMaximizingParameters(RectF rectangle) {
        MaximizingParameters mp = new MaximizingParameters();

//        Log.d(TAG, "MAX VERT/HEIGHT RECT: " + rectangle.toShortString() + ", " + getHeight());

        final float rawImageHeight = getSHeight();
        final float imageHeight = getSHeight() * getScale();
        final int maxAllowedHeight = getHeight() - (mOffset * 2);

        mp.rectangleHeight = maxAllowedHeight;
        mp.rectangleScale = mp.rectangleHeight / rectangle.height();
        mp.rectangleWidth = mp.rectangleScale * rectangle.width();
        float newImageHeight = mp.rectangleScale * imageHeight;
        mp.imageScale = newImageHeight / rawImageHeight;

        sanitizeMaximizingParameters(mp, rectangle);

        return mp;
    }

    private void sanitizeMaximizingParameters(MaximizingParameters mp, RectF rectangle) {
        final float rawImageWidth = getSWidth();
        final float imageWidth = rawImageWidth * getScale();
        final int maxAllowedWidth = getWidth() - (mOffset * 2);
        final int maxAllowedHeight = getHeight() - (mOffset * 2);

        if (mp.imageScale > getMaxScale()) {
            mp.imageScale = getMaxScale();
            float newImageWidth = mp.imageScale * rawImageWidth;
            mp.rectangleScale = newImageWidth / imageWidth;
            mp.rectangleWidth = mp.rectangleScale * rectangle.width();
            mp.rectangleHeight = mp.rectangleScale * rectangle.height();
        }

        mp.isValid = mp.rectangleWidth <= maxAllowedWidth
                && mp.rectangleHeight <= maxAllowedHeight;
    }

    private void updateImageAndCropRectangle(RectF maximizedRectangle, MaximizingParameters mp) {
        PointF oldRawCenter = getCenter();
        PointF newRawCenter = viewToSourceCoord(maximizedRectangle.centerX(), maximizedRectangle.centerY());
        float oldScale = getScale();
        boolean isImageChanged = true;
//                Math.round(oldScale * 10000) != Math.round(mp.imageScale * 10000)
//                        || Math.round(oldRawCenter.x * 100) != Math.round(newRawCenter.x * 100)
//                        || Math.round(oldRawCenter.y * 100) != Math.round(newRawCenter.y * 100);
        if (isImageChanged) {
            Log.d(TAG, "CHANGED: " + oldScale + " != " + mp.imageScale + "    "
                    + oldRawCenter.x +" != "+ newRawCenter.x + "   " + oldRawCenter.y +" != "+ newRawCenter.y);
            animateScaleAndCenter(mp.imageScale, newRawCenter)
                    .withDuration(300)
                    .withEasing(SubsamplingScaleImageView.EASE_OUT_QUAD)
                    .withInterruptible(false)
                    .start();
        }

        float viewCenterX = getWidth() / 2.0f;
        float viewCenterY = getHeight() / 2.0f;
        mCropRectangle.set(
                viewCenterX - (mp.rectangleWidth / 2),
                viewCenterY - (mp.rectangleHeight / 2),
                viewCenterX + (mp.rectangleWidth / 2),
                viewCenterY + (mp.rectangleHeight / 2));

//        Log.d(TAG, "1 CROP RECT: " + mCropRectangle.toShortString());
    }

}
