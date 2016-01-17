package chen.xiaoyu.helloworld;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by chenxiaoyu on 10/25/15.
 */
public class CustomDrawableView extends View {
    private ShapeDrawable mDrawable;
    private ImageView test;
    private int centralX, centralY, width, height;
    private int maxWidth, maxHeight;

    public CustomDrawableView(Context context, AttributeSet as) {
        super(context, as);

        Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
        final Point size = new Point();
        display.getSize(size);
        centralX = size.x / 2;
        centralY = size.y / 2;
        maxWidth = size.x;
        maxHeight = size.x;

        width = 600;
        height = 600;
        int x = centralX - width / 2;
        int y = centralY - height / 2;

        mDrawable = new ShapeDrawable(new RectShape());
        mDrawable.getPaint().setColor(Color.WHITE);
        mDrawable.setBounds(x, y, x + width, y + height);
    }

    protected void onDraw(Canvas canvas) {
        mDrawable.draw(canvas);
    }

    public void scale(float scaleFactorX, float scaleFactorY) {
        width *= scaleFactorX;
        height *= scaleFactorY;
        int x = centralX - width / 2;
        int y = centralY - height / 2;
        mDrawable = new ShapeDrawable(new RectShape());
        mDrawable.getPaint().setColor(Color.WHITE);
        mDrawable.setBounds(x, y, x + width, y + height);
        invalidate();
    }

    public Rect getBoundingRect() {
        System.out.println(mDrawable.getBounds());
        return mDrawable.getBounds();
    }
}