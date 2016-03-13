package chen.xiaoyu.helloworld;

import android.content.Context;
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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

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
    private DotView devicePin;
    private Queue<DotView> apPins = new LinkedList<>();
    private Queue<RectView> grids = new LinkedList<>();
    private int topOffset;

    private String username;
    private int session;

    private Context self = this;


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

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                username = "unknown";
            } else {
                username = extras.getString("username");
                session = Integer.parseInt(extras.getString("session"));
            }
        } else {
            username = (String) savedInstanceState.getSerializable("username");
        }
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
        if (devicePin == null) return;
        new SpeedTest(this, username, session, devicePin.xGrid, devicePin.yGrid).execute();
    }

    public void generateHeatmap(View view) {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://tony.recg.rice.edu/helloworld?user="+username+"&session="+session;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String[] rows = response.split("\\n");
                        for (RectView grid: grids) {
                            layout.removeView(grid);
                        }
                        grids = new LinkedList<>();
                        for (int i = 0; i < rows.length; i++) {
                            String[] vals = rows[i].split(" ");
                            for (int j = 0; j < vals.length; j++) {
                                RectView grid = new RectView(self, j, i, (int)(Color.RED * Double.parseDouble(vals[j])), myRect);
                                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                                layout.addView(grid, params);
                                grids.add(grid);
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
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
        layout.removeView(devicePin);
        while (apPins.size() > 0) {
            layout.removeView(apPins.poll());
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
                int xGridNum = myRect.getGridNumX(x);
                int yGridNum = myRect.getGridNumY(y);
                System.out.println(xGridNum);
                System.out.println(yGridNum);
                //do something with grid upload to db
                DotView newPin = new DotView(this, xGridNum, yGridNum, Color.RED, myRect);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                layout.addView(newPin, params);
                layout.removeView(devicePin);
                devicePin = newPin;
                pinDev = false;
            }
        }
        else if (pinAP) {
            int x = (int)e.getX();
            int y = (int)e.getY() - topOffset;
            if (myRect.getBoundingRect().contains(x, y)) {
                int xGridNum = myRect.getGridNumX(x);
                int yGridNum = myRect.getGridNumY(y);
                DotView newPin = new DotView(this, xGridNum, yGridNum, Color.GREEN, myRect);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                layout.addView(newPin, params);
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
            devicePin.update();
            for (DotView apPin: apPins) {
                apPin.update();
            }
            for (RectView grid: grids) {
                grid.update();
            }
            return true;
        }
    }
}
