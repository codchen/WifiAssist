package chen.xiaoyu.helloworld;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.view.View;

/**
 * Created by chenxiaoyu on 10/25/15.
 */
public class RectView extends View{
    private ShapeDrawable mDrawable;
    public int xGrid;
    public int yGrid;
    private int color;
    private CustomDrawableView rect;

    public RectView (Context context, int xGrid, int yGrid, int color, CustomDrawableView rect) {
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
        mDrawable = new ShapeDrawable(new RectShape());
        mDrawable.getPaint().setColor(color);
        mDrawable.setBounds(x - rect.getGridSizeX() / 2, y - rect.getGridSizeY() / 2, x + rect.getGridSizeX() / 2, y + rect.getGridSizeY() / 2);
        invalidate();
    }

    public void updateColor(int color) {
        this.color = color;
        mDrawable.getPaint().setColor(color);
        invalidate();
    }

    protected void onDraw(Canvas canvas) {
        mDrawable.draw(canvas);
    }
}

