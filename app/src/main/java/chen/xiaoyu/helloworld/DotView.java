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
    public DotView (Context context, int x, int y, int color) {
        super(context);
        mDrawable = new ShapeDrawable(new OvalShape());
        mDrawable.getPaint().setColor(color);
        mDrawable.setBounds(x - 10, y - 10, x + 10, y + 10);
    }

    protected void onDraw(Canvas canvas) {
        mDrawable.draw(canvas);
    }
}
