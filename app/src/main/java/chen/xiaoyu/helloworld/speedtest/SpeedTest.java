package chen.xiaoyu.helloworld.speedtest;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import chen.xiaoyu.helloworld.MainActivity;
import chen.xiaoyu.helloworld.R;

/**
 * Speed test module
 * Created by chenxiaoyu on 10/26/15.
 */
public class SpeedTest extends AsyncTask<Void, Void, List<Double>> {

    private final String pingAddr = "tony.recg.rice.edu";

    private final String HTTPURL = "http://tony.recg.rice.edu/97397";

    private final long overhead;

    private final String username;
    private final int session;
    private final int x;
    private final int y;

    private int serverResponseCode = 0;
    private final String upLoadServerUri = "http://tony.recg.rice.edu/UploadToServerDiscard.php";
    private final String dbRecordUri = "http://tony.recg.rice.edu/recordSpeed.php";

    private ProgressDialog progressDialog;
    private Context context;

    public SpeedTest(Context context, String username, int session, int x, int y) {
        this.context = context;
        overhead = -(System.nanoTime() - System.nanoTime());
        Log.i("Nano call overhead ", String.valueOf(overhead));
        this.username = username;
        this.session = session;
        this.x = x;
        this.y = y;
    }

    @Override
    protected void onPreExecute()
    {
        progressDialog= ProgressDialog.show(context, "Measuring network performance","Testing ping", true);
    }

    protected List<Double> doInBackground(Void... params) {
        final List<Double> ret = new ArrayList<>();
        List<Double> res = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            double current = ping();
            if (current > 0) {
                res.add(current);
            }
        }

        double sum = 0.0;
        for (Double d: res) {
            sum += d;
        }

        ret.add(sum / res.size());
        sum = 0;
        res = new ArrayList<>();

        ((Activity)context).runOnUiThread(new Runnable() {

            @Override
            public void run() {
                progressDialog.setMessage("Testing uploads");
                Toast.makeText(context, String.valueOf(ret.get(ret.size() - 1)) + "ms", Toast.LENGTH_SHORT).show();
            }
        });

        for (int i = 0; i < 1; i++) {
            double current = upload(R.raw.f97397, "f97397");
            if (current > 0) {
                res.add(current);
            }
        }

        for (Double d: res) {
            sum += d;
        }

        ret.add(sum / res.size());
        sum = 0;
        res = new ArrayList<>();

        ((Activity)context).runOnUiThread(new Runnable() {

            @Override
            public void run() {
                progressDialog.setMessage("Testing downloads");
                Toast.makeText(context, String.valueOf(ret.get(ret.size() - 1)) + "kb/s", Toast.LENGTH_SHORT).show();
            }
        });

        for (int i = 0; i < 1; i++) {
            double current = download(HTTPURL);
            if (current > 0) {
                res.add(current);
            }
        }

        for (Double d: res) {
            sum += d;
        }

        ret.add(sum / res.size());

        writeToDB(ret);

        return ret;
    }

    private Double ping() {
        try {
            Process p1 = Runtime.getRuntime().exec("/system/bin/ping -c 1 " + pingAddr);
            int extValue = p1.waitFor();
            if (extValue != 0) {
                System.out.println("Exit value: " + extValue);
                return -1.0;
            }
            InputStream is = p1.getInputStream();
            StringBuilder sb = new StringBuilder();
            int i;
            while ((i = is.read()) != -1) {
                sb.append((char)i);
            }
            return Double.parseDouble(sb.toString().split("time=")[1].split(" ms")[0]);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return -1.0;
    }

    private Double download(String urlInString) {
        long totalTime = 0L;
        try {
            URL myURL = new URL(urlInString);
            InputStream is = new BufferedInputStream(myURL.openStream());

            byte[] buffer = new byte[4096]; //change this to see if it affects results
            int byteCnt = 1;
            double read = 0;

            double cnt = 0L;

            while (byteCnt > 0) {
                long start = System.nanoTime();
                try {
                    byteCnt = is.read(buffer);
                }catch (Exception e) {
                    break;
                }
                long current = System.nanoTime();
                totalTime += current - start - overhead;
                read += (double)byteCnt / 131072;

                cnt += (double)byteCnt / 1024;

                if (cnt > 1024) {
                    final double speed = read * 1000000000L / totalTime;
                    ((Activity) context).runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            progressDialog.setMessage("Testing downloads\nSpeed: " + speed + " Mbps");
                        }
                    });
                    cnt = 0;
                }
            }
            is.close();

            //estimation for too short time interval
            if (totalTime == 0) totalTime = 1;

            return read / (double)totalTime * 1000000000L;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1.0;
    }

    private double upload(int id, String name) {
        InputStream is = context.getResources().openRawResource(id);

        HttpURLConnection conn;
        DataOutputStream dos;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1024 * 1024;

        try {

            // open a URL connection to the Servlet
            URL url = new URL(upLoadServerUri);

            // Open a HTTP  connection to  the URL
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true); // Allow Inputs
            conn.setDoOutput(true); // Allow Outputs
            conn.setUseCaches(false); // Don't use a Cached Copy
            conn.setChunkedStreamingMode(1024);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("ENCTYPE", "multipart/form-data");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            conn.setRequestProperty("uploaded_file", name);

            dos = new DataOutputStream(conn.getOutputStream());

            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" + name + "\"" + lineEnd);

            dos.writeBytes(lineEnd);

            // create a buffer of  maximum size
            bytesAvailable = is.available();

            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            long totalTime = 0L;

            // read file and write it into form...
            bytesRead = is.read(buffer, 0, bufferSize);

            double written = 0;
            double cnt = 0;

            while (bytesRead > 0) {
                long start = System.nanoTime();
                try {
                    dos.write(buffer, 0, bufferSize);
                }catch (Exception e) {
                    break;
                }
                long current = System.nanoTime();
                totalTime += current - start - overhead;
                written += (double)bytesRead / 131072;

                cnt += (double)bytesRead / 1024;

                if (cnt > 1024) {
                    final double speed = written * 1000000000L / totalTime;
                    ((Activity) context).runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            progressDialog.setMessage("Testing uploads\nSpeed: " + speed + " Mbps");
                        }
                    });
                    cnt = 0;
                }

                dos.write(buffer, 0, bufferSize);
                bytesAvailable = is.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = is.read(buffer, 0, bufferSize);

            }

            // send multipart form data necesssary after file data...
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            // Responses from the server (code and message)
            serverResponseCode = conn.getResponseCode();
            String serverResponseMessage = conn.getResponseMessage();

            Log.i("uploadFile", "HTTP Response is : "
                    + serverResponseMessage + ": " + serverResponseCode);

            if(serverResponseCode == 200){
                return written / totalTime * 1000000000L;
            }

            //close the streams //
            is.close();
            dos.flush();
            dos.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1.0;
    }

    @Override
    protected void onPostExecute(List<Double> result)
    {
        super.onPostExecute(result);
        progressDialog.dismiss();

        Toast.makeText(context, String.valueOf(result.get(2)) + " Mbps", Toast.LENGTH_SHORT).show();
    }

    private void writeToDB(List<Double> result) {
        HttpURLConnection conn;
        DataOutputStream dos;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";

        try {

            // open a URL connection to the Servlet
            URL url = new URL(dbRecordUri);

            // Open a HTTP  connection to  the URL
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true); // Allow Inputs
            conn.setDoOutput(true); // Allow Outputs
            conn.setUseCaches(false); // Don't use a Cached Copy
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            dos = new DataOutputStream(conn.getOutputStream());

            dos.writeBytes("x="+x+"&y="+y+"&ping=" + result.get(0) + "&upload=" + result.get(1) + "&download="
                    + result.get(2) + "&user="+username+"&session="+session);

            // Responses from the server (code and message)
            serverResponseCode = conn.getResponseCode();
            String serverResponseMessage = conn.getResponseMessage();

            Log.i("record to database", "HTTP Response is : "
                    + serverResponseMessage + ": " + serverResponseCode);

            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line, res = "";
            while ((line = rd.readLine()) != null) {
                res += line;
            }
            Log.i("record to database", res);
            //close the streams //
            dos.flush();
            dos.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
