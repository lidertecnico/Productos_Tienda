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
    private final NetworkCallback networkCallback;
    private boolean networkAvailable;

    // Constructor para inicializar el monitor de red
    public NetworkMonitor(Context context) {
        this.context = context;
        this.connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        this.networkAvailable = false;

        // Inicializar la clase interna NetworkCallback
        this.networkCallback = new NetworkCallback();

        // Crear una solicitud de red para monitorizar cambios en la conexión
        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();

        // Registrar el callback para recibir actualizaciones sobre la red
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
    }

    // Método para verificar si la red está disponible
    public boolean isNetworkAvailable() {
        return networkAvailable;
    }

    // Método para obtener el callback de la red
    public NetworkCallback getNetworkCallback() {
        return networkCallback;
    }

    // Método para limpiar los recursos (desregistrar el callback)
    public void cleanup() {
        if (connectivityManager != null && networkCallback != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }
    }

    // Clase interna NetworkCallback para manejar eventos de la red
    public class NetworkCallback extends ConnectivityManager.NetworkCallback {
        @Override
        public void onAvailable(Network network) {
            // Establecer la red como disponible y mostrar un mensaje
            networkAvailable = true;
            Toast.makeText(context, "Conexión a internet disponible", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onLost(Network network) {
            // Establecer la red como no disponible y mostrar un mensaje
            networkAvailable = false;
            Toast.makeText(context, "Conexión a internet perdida", Toast.LENGTH_SHORT).show();
        }
    }
}