package com.pic.browserapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.TlsVersion;

public class MainActivity extends AppCompatActivity {

    private final static String MAIN_TAG = "MainActivity";

    private ProgressBar m_prgBar;
    public static TextView m_txtUpdateCheck;

    public String m_sAppName;
    public String m_sPath;
    public String m_sVersionCode;
    public String m_sVersionName;
    public String m_sPackageName;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_txtUpdateCheck = (TextView) findViewById(R.id.ID_UPDATE_CHECK_TXT);
        m_prgBar		 = (ProgressBar) findViewById(R.id.progressBar);

        if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        UpdateCheck();
    }

    public static boolean isExternalStorageRemovable() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            return isExternalStorageRemovable();
        }
        return true;
    }

    public static File getExternalCacheDir(Context context) {
        if (context.getExternalCacheDir() != null) {
            return context.getExternalCacheDir();
        }

        // Before Froyo we need to construct the external cache dir ourselves
        final String cacheDir = "/Android/data/" + context.getPackageName() + "/cache/";
        return new File(Environment.getExternalStorageDirectory().getPath() + cacheDir);
    }

    public static String getDiskCacheDir(Context context) {

        // Check if media is mounted or storage is built-in, if so, try and use external cache dir
        // otherwise use internal cache dir
        final String cachePath =
                Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                        !isExternalStorageRemovable() ?
                        getExternalCacheDir(context).getPath() :
                        context.getCacheDir().getPath();

        File cacheDir = new File(cachePath);
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }

        return cacheDir.getAbsolutePath();
    }

    private void removeDownloadApk() {
        String strDownPATH = getDiskCacheDir(MainActivity.this);

        File file = new File(strDownPATH);

        if (!file.exists())	return;

        File outputFile = new File(file, "browserapp.apk");

        if(outputFile.exists()) {
            outputFile.delete();
        }
    }

    public static void checkUpdate(final ResultCallback resultCallback, String tag) {
        //String url = Constant.UPDDATE_URL + deviceId + "&ver=" + Constant.m_pckInfo.versionCode;
        String url = "http://3.87.225.224/apk/1TribeRadio.apk";
//        String url = "http://192.168.10.97/download/1TribeRadio.apk";

        HttpRequest.getInstance().getStringResponsePost(url, resultCallback, false, tag);
    }

    private void UpdateCheck(){
        removeDownloadApk();

        checkUpdate(new ResultCallback(){
            @Override
            public void onSuccess() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        XMLParser parser = new XMLParser();
//                        ArrayList<HashMap<String, String>> arrMapChannel = parser.getUpdateData(result);
//                        if(arrMapChannel.size() > 0) {
//                            HashMap<String, String> a_mapData = arrMapChannel.get(0);
//                            m_sAppName = a_mapData.get("appName");
//                            m_sPath = a_mapData.get("path");
//                            m_sVersionCode = a_mapData.get("versionCode");
//                            m_sVersionName = a_mapData.get("versionName");
//                            m_sPackageName = a_mapData.get("packageName");
//                        }
                        UpdateDialog dialog = new UpdateDialog(MainActivity.this);
                        dialog.setMessage("Press OK to download APK");
                        dialog.setCancelable(false);
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.setOnClickListenerPositive(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                new DownloadNewVersion().execute();
                            }
                        });

                        dialog.show();
                    }
                });
            }

            @Override
            public void onError(Object error) {
                //
            }
        }, MAIN_TAG);
    }

    class DownloadNewVersion extends AsyncTask<String, Integer, File> {

        protected void onPreExecute() {
            super.onPreExecute();
            try {
                m_txtUpdateCheck.setVisibility(View.VISIBLE);
                m_prgBar.setVisibility(View.VISIBLE);
                m_txtUpdateCheck.setText("Downloading...(0%)");
            } catch (Exception e){}
        }

        @Override
        protected  void  onProgressUpdate(Integer...progress) {
            super.onProgressUpdate();
            try {
                m_prgBar.animate();
                m_prgBar.setIndeterminate(false);
                m_prgBar.setMax(100);
                m_prgBar.setProgress(progress[0]);
                if (progress[0] > 99) {
                    m_txtUpdateCheck.setText("Finish...");
                } else {
                    m_txtUpdateCheck.setText("Downloading...(" + progress[0] + "%)");
                }
            } catch (Exception e){}
        }

        @Override
        protected void onPostExecute(File results) {
            super.onPostExecute(results);

            try {
                if (results != null) {
                    SilentInstallApplication(results);
                } else {
                    Toast.makeText(MainActivity.this, "Download Error: Try Again", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e){}
        }

        @SuppressLint("WrongThread")
        @Override
        protected File doInBackground(String... strings) {
            try{
                OkHttpClient client = HttpRequest.getInstance().getOkHttpClient();

                m_sPath = "http://3.87.225.224/apk/1TribeRadio.apk";
//                m_sPath = "http://192.168.10.97/download/1TribeRadio.apk";
                Request request = new Request.Builder().url(m_sPath).build();
                Response response = client.newCall(request).execute();

                String strDownPATH = getDiskCacheDir(MainActivity.this);

                File file = new File(strDownPATH); // PATH = /mnt/sdcard/download/
                if (!file.exists()) {
                    file.mkdirs();
                }
                File outputFile = new File(file, "browserapp.apk");

                if(outputFile.exists()) {
                    outputFile.delete();
                }
                FileOutputStream fos = new FileOutputStream(outputFile);
                InputStream is = response.body().byteStream(); // Get from Server and Catch In Input Stream Object.
                long total_size = response.body().contentLength();
                byte[] buffer = new byte[1024];
                int len1 = 0;
                int per = 0;
                int downloaded = 0;
                while ((len1 = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len1); // Write In FileOutputStream.
                    downloaded += len1;
                    per = (int) (downloaded * 100  / total_size);
                    publishProgress(per);
                }
                fos.close();
                is.close();

                return outputFile;
            } catch (IOException e) {
                Log.d("Exception", "Error");
            }

            return null;
        }
    }

    public void InstallApplication(File file) {
        finish();

        if (Build.VERSION.SDK_INT >= 24) {
            try {
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            } catch(Exception e) {
                e.printStackTrace();
            }

            Uri packageURI = Uri.parse(getApplicationContext().getPackageName());
            Intent intent = new Intent(android.content.Intent.ACTION_VIEW, packageURI);
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } else {
            Uri packageURI = Uri.parse(m_sPackageName.toString());
            Intent intent = new Intent(android.content.Intent.ACTION_VIEW, packageURI);
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        }
    }

    private boolean SilentInstallApplication(File updateFile) {
        String outPath = updateFile.getAbsolutePath();

        ArrayList<String > cmdlist = new ArrayList<String>();
        cmdlist.add( "pm install -r " + outPath);
        cmdlist.add( "rm -f " + outPath );

        int res = suExecCommandList( cmdlist.toArray( new String[ cmdlist.size()]) );

        if (res != 0) {
            InstallApplication(updateFile);
        }
        else {
            finish();
        }

        return true;
    }

    public int suExecCommandList(String[] commands ) {
        int result = -1;

        Process process = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            for (String command : commands) {
                if (command == null) {
                    continue;
                }

                // donnot use os.writeBytes(commmand), avoid chinese charset
                // error
                os.write(command.getBytes());
                os.writeBytes( "\n" );
                os.flush();
            }
            os.writeBytes( "exit\n" );
            os.flush();

            result = process.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (process != null) {
                process.destroy();
            }
        }

        return result;
    }

    public static class HttpRequest {
        private static HttpRequest instance;
        private OkHttpClient mOkHttpClient;

        public static HttpRequest getInstance() {
            if (instance == null) {
                instance = new HttpRequest();
            }
            return instance;
        }

        public HttpRequest() {
            OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient().newBuilder();

            if (Build.VERSION.SDK_INT >= 16 && Build.VERSION.SDK_INT < 22) {
                ConnectionSpec cs = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                        .tlsVersions(TlsVersion.TLS_1_2, TlsVersion.TLS_1_1, TlsVersion.TLS_1_0)
                        .build();
                List<ConnectionSpec> specs = new ArrayList<>();
                specs.add(cs);
                specs.add(ConnectionSpec.COMPATIBLE_TLS);
                specs.add(ConnectionSpec.CLEARTEXT);
                okHttpClientBuilder.connectionSpecs(specs);
            }

            mOkHttpClient = okHttpClientBuilder
                    //test
                    //.dns(new EasyDns())
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    //.protocols(Arrays.asList(Protocol.HTTP_1_1))
                    .build();
        }

        public OkHttpClient getOkHttpClient() {
            return mOkHttpClient;
        }

        public void getStringResponsePost(String url, final ResultCallback resultCallback, boolean dnsjsonHeader, String tag) {
            Request request = null;
            if (dnsjsonHeader) {
                request = new Request.Builder()
                        .addHeader("Accept", "application/dns-json")
                        .url(url)
                        .get()
                        .tag(tag)
                        .build();
            } else {
                request = new Request.Builder()
                        .url(url)
                        .get()
                        .tag(tag)
                        .build();
            }

            mOkHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    if (resultCallback != null) {
                        resultCallback.onError(e);
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (resultCallback != null) {
                        resultCallback.onSuccess();
                    }
                }
            });
        }
    }

    public interface ResultCallback {
        void onSuccess();
        void onError(Object error);
    }
}
