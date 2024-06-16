package eu.cartograph.cartograph_android_service_example;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.HashMap;
import java.util.Map;

import eu.cartograph.androidapi.ICartographMapTileService;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    // Map tile service
    // https://devarea.com/android-services-and-aidl/
    private Map<ComponentName, ICartographMapTileService> mMapTileServices = new HashMap<>();
    private Map<ComponentName, Boolean> mMapTileServiceBound = new HashMap<>();
    private final ServiceConnection mMapTileServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            mMapTileServiceBound.remove(name);
            mMapTileServices.remove(name);
            mMapTileServices.put(name, ICartographMapTileService.Stub.asInterface(binder));
            mMapTileServiceBound.put(name, true);

            Log.e("INFO", "Connected to map service: " + name);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mMapTileServiceBound.remove(name);
            mMapTileServices.remove(name);
            mMapTileServiceBound.put(name, false);

            Log.e("INFO", "Removed map service: " + name);
        }
    };

    @Override
    protected void onStart() {
        super.onStart();

        //   bindService(new Intent(this, ICartographMapTileService.class), mMapTileServiceConnection,
        //           Context.BIND_AUTO_CREATE);

        // eu.cartograph.cartograph_android_service_example.ExampleMapProviderService
        boolean res = bindService(new Intent("eu.cartograph.androidapi.ICartographMapTileService").setPackage("eu.cartograph.cartograph_android_service_example"),mMapTileServiceConnection,
                Context.BIND_AUTO_CREATE);

        Log.e("INFO", "Map service bind try result: " + res);
    }

    @Override
    protected void onStop() {
        super.onStop();


        for (Map.Entry<ComponentName, Boolean> entry : mMapTileServiceBound.entrySet()) {
            if (entry.getValue().booleanValue()) {
                unbindService(mMapTileServiceConnection);
                break;
            }
        }
        mMapTileServiceBound.clear();
    }
}