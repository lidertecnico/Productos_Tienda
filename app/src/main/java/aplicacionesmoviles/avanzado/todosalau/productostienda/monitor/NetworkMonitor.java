package aplicacionesmoviles.avanzado.todosalau.productostienda.monitor;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.widget.Toast;

public class NetworkMonitor {
    private final Context context;
    private final ConnectivityManager connectivityManager;
    public NetworkCallback networkCallback;
    private boolean networkAvailable;

    public NetworkMonitor(Context context) {
        this.context = context;
        this.connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        this.networkAvailable = false;

        // Inicializa la clase interna NetworkCallback
        this.networkCallback = new NetworkCallback();

        // Registrar el callback para monitorear cambios en la red
        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
    }

    public boolean isNetworkAvailable() {
        return networkAvailable;
    }

    public NetworkCallback getNetworkCallback() {
        return networkCallback;
    }

    public void cleanup() {
        if (connectivityManager != null && networkCallback != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }
    }

    // Clase interna NetworkCallback
    public class NetworkCallback extends ConnectivityManager.NetworkCallback {
        @Override
        public void onAvailable(Network network) {
            networkAvailable = true;
            Toast.makeText(context, "Conexión a internet disponible", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onLost(Network network) {
            networkAvailable = false;
            Toast.makeText(context, "Conexión a internet perdida", Toast.LENGTH_SHORT).show();
        }
    }
}