package chen.xiaoyu.helloworld;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.view.View;

/**
 * Created by chenxiaoyu on 10/25/15.
 */
public class DotView extends View{
    private ShapeDrawable mDrawable;
    public int xGrid;
    public int yGrid;
    private int color;
    private CustomDrawableView rect;

    public DotView (Context context, int xGrid, int yGrid, int color, CustomDrawableView rect) {
        super(context);
        this.xGrid = xGrid;
        this.yGrid = yGrid;
        this.color = color;
        this.rect = rect;
        update();
    }

    public void update(){
        int x = rect.getBoundingRect().left + xGrid * rect.getGridSizeX() + (int)(0.5 * rect.getGridSizeX());
        int y = rect.getBoundingRect().top + yGrid * rect.getGridSizeY() + (int)(0.5 * rect.getGridSizeY());
        mDrawable = new ShapeDrawable(new OvalShape());
        mDrawable.getPaint().setColor(color);
        mDrawable.setBounds(x - 10, y - 10, x + 10, y + 10);
        invalidate();
    }
//
//    public void scale(float scaleFactorX, float scaleFactorY) {
//        double xRatio = 1.0 * (x - left) / width;
//        double yRatio = 1.0 * (y - top) / height;
//        left -= width * (scaleFactorX - 1) / 2;
//        top -= height * (scaleFactorY - 1) / 2;
//        width *= scaleFactorX;
//        height *= scaleFactorY;
//        x = left + (int)(xRatio * width);
//        y = top + (int)(yRatio * height);
//        mDrawable = new ShapeDrawable(new OvalShape());
//        mDrawable.getPaint().setColor(color);
//        mDrawable.setBounds(x - 10, y - 10, x + 10, y + 10);
//        invalidate();
//    }

    protected void onDraw(Canvas canvas) {
        mDrawable.draw(canvas);
    }
}
