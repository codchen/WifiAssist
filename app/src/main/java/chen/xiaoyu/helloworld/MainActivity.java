package chen.xiaoyu.helloworld;

import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.GestureDetector;
import android.support.v4.view.GestureDetectorCompat;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.LinkedList;
import java.util.Queue;

import chen.xiaoyu.helloworld.speedtest.SpeedTest;

public class MainActivity extends AppCompatActivity implements GestureDetector.OnGestureListener{
    public final static String EXTRA_MESSAGE = "chen.xiaoyu.helloworld.Message";

    private GestureDetectorCompat gestureDetector;
    private ScaleGestureDetector scaleGestureDetector;

    private boolean pinDev = false;
    private boolean pinAP = false;
    private CustomDrawableView myRect;
    private ViewGroup layout;
    private Queue<View> devicePins = new LinkedList<View>();
    private Queue<View> apPins = new LinkedList<View>();
    private int topOffset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        myRect = (CustomDrawableView)findViewById(R.id.rect);
        layout = (ViewGroup)findViewById(R.id.main_layout);
        gestureDetector = new GestureDetectorCompat(this, this);
        scaleGestureDetector = new ScaleGestureDetector(this,new ScaleListener());
        setSupportActionBar(toolbar);

        getTopOffset();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Changed Action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    private void getTopOffset() {
        final TypedArray styledAttributes = getTheme().obtainStyledAttributes(
                new int[] { android.R.attr.actionBarSize});
        topOffset = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();

        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            topOffset += getResources().getDimensionPixelSize(resourceId);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void executeTest (View view) {
        new SpeedTest(this).execute();
    }

    public void pinDevice (View view) {
        pinDev = true;
        pinAP = false;
    }

    public void pinAP (View view) {
        pinAP = true;
        pinDev = false;
    }

    public void clearAll (View view) {
        while (devicePins.size() > 0) {
            ((RelativeLayout) layout).removeView(devicePins.poll());
        }
        while (apPins.size() > 0) {
            ((RelativeLayout) layout).removeView(apPins.poll());
        }
    }

    public AppCompatActivity getActivity() {
        return this;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        if (pinDev) {
            int x = (int)e.getX();
            int y = (int)e.getY() - topOffset;
            if (myRect.getBoundingRect().contains(x, y)) {
                View newPin = new DotView(this, x, y, Color.RED);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                ((RelativeLayout) layout).addView(newPin, params);
                devicePins.add(newPin);
                pinDev = false;
            }
        }
        else if (pinAP) {
            int x = (int)e.getX();
            int y = (int)e.getY() - topOffset;
            if (myRect.getBoundingRect().contains(x, y)) {
                View newPin = new DotView(this, x, y, Color.GREEN);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                ((RelativeLayout) layout).addView(newPin, params);
                apPins.add(newPin);
                pinAP = false;
            }
        }
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    public boolean onTouchEvent (MotionEvent event) {
        this.gestureDetector.onTouchEvent(event);
        this.scaleGestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    private class ScaleListener extends ScaleGestureDetector.
            SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactorX = detector.getPreviousSpanX()>0?detector.getCurrentSpanX()/detector.getPreviousSpanX():1;
            scaleFactorX = Math.max(0.1f, Math.min(scaleFactorX, 5.0f));
            float scaleFactorY = detector.getPreviousSpanY()>0?detector.getCurrentSpanY()/detector.getPreviousSpanY():1;
            scaleFactorY = Math.max(0.1f, Math.min(scaleFactorY, 5.0f));
            myRect.scale(scaleFactorX, scaleFactorY);
            return true;
        }
    }
}
