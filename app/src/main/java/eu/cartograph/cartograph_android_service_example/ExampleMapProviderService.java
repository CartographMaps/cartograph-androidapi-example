package eu.cartograph.cartograph_android_service_example;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import eu.cartograph.androidapi.ICartographMapTileService;
import eu.cartograph.androidapi.MapListResponse;
import eu.cartograph.androidapi.MapTileRequest;
import eu.cartograph.androidapi.MapTileResponse;

public class ExampleMapProviderService extends Service {

    // The service provides two different types of maps:
    private final static String MAP_KEY_1 = "osm1";
    private final static String MAP_KEY_2 = "osm2";

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final ICartographMapTileService.Stub mBinder = new ICartographMapTileService.Stub() {
        @Override
        public String getServiceUid() throws RemoteException {
            return "eu.cartograph.cartograph_android_service_example";
        }

        @Override
        public String getServiceName() throws RemoteException {
            return "Example map service";
        }

        @Override
        public String getServiceOpenUrl() throws RemoteException {
            return "eu.cartograph.cartograph_android_service_example";
        }

        @Override
        public MapListResponse getMapList() throws RemoteException {
            MapListResponse obj = new MapListResponse();
            try {
                obj.addEntry(new MapListResponse.MapEntry(MAP_KEY_1, "OpenStreetMap tile server", ""));
                obj.addEntry(new MapListResponse.MapEntry(MAP_KEY_2, "OpenStreetMap tile server 2", ""));
            }
            catch (NullPointerException ex) {
                ex.printStackTrace();
            }
            return obj;
        }

        @Override
        public MapTileResponse getMapTile(String mapKey, MapTileRequest tile) throws
                RemoteException {
            String path;
            MapTileResponse response = null;
            if (mapKey.equals(MAP_KEY_1)) {
                path = "https://tile.openstreetmap.org/{z}/{x}/{y}.png";
            }
            else if (mapKey.equals(MAP_KEY_2)) {
                path = "https://a.tile.openstreetmap.fr/hot/{z}/{x}/{y}.png";
            }
            else {
                response = new MapTileResponse(-1, "Invalid tile URL");
                return response;
            }

            try {
                path = path.replace("{z}", String.valueOf(tile.getZoom()))
                        .replace("{x}", String.valueOf(tile.getX()))
                        .replace("{y}", String.valueOf(tile.getY()));

                Log.d("C INFO", ">>>> Requesting tile: " + path);

                URL url = new URL(path);
                HttpsURLConnection con = (HttpsURLConnection)url.openConnection();
                con.setRequestProperty("User-Agent", "Cartograph Maps Android plugin example");
                con.setReadTimeout(2500);
                con.setDoOutput(true);
                con.setDoInput(true);
                con.connect();
                if(con.getResponseCode() == HttpsURLConnection.HTTP_OK)
                {
                    ByteArrayOutputStream bs = new ByteArrayOutputStream();
                    InputStream is = con.getInputStream();

                    // Read data chunks
                    byte[] byteChunk = new byte[4096];
                    int n;

                    while ((n = is.read(byteChunk)) > 0) {
                        bs.write(byteChunk, 0, n);
                    }

                    response = new MapTileResponse(bs.toByteArray(), MapTileResponse.DATA_FORMAT_RASTER);

                    bs.close();
                    is.close();
                }
                else {
                    throw new Exception();
                }
            }
            catch (Exception ex) {
                ex.printStackTrace();
                response = new MapTileResponse(-1, "Error while downloading data");
            }
            return response;
        }

        @Override
        public boolean openMap(String mapKey) throws RemoteException {

            return mapKey.equals(MAP_KEY_1) || mapKey.equals(MAP_KEY_2);
        }

        @Override
        public void closeMap(String mapKey) throws RemoteException {
            // no op
        }

        @Override
        public IBinder asBinder() {
            return this;
        }
    };
}
