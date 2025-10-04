package di.optimize.android

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import di.optimize.android.databinding.FragmentFirstBinding
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import org.osmdroid.views.CustomZoomButtonsController

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var myLocationOverlay: MyLocationNewOverlay? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) ||
                (permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true)
        if (granted) {
            enableMyLocation()
        } else {
            Toast.makeText(requireContext(), "Posisjonstillatelse er nødvendig for å hente koordinater", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configure osmdroid to use internal cache and set a proper user agent
        val ctx = requireContext().applicationContext
        Configuration.getInstance().userAgentValue = ctx.packageName
        val basePath = java.io.File(ctx.cacheDir, "osmdroid").apply { mkdirs() }
        val tileCache = java.io.File(basePath, "tiles").apply { mkdirs() }
        Configuration.getInstance().osmdroidBasePath = basePath
        Configuration.getInstance().osmdroidTileCache = tileCache

        val map = binding.osmMapView
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setTilesScaledToDpi(true)
        map.setMultiTouchControls(true)
        map.zoomController.setVisibility(CustomZoomButtonsController.Visibility.ALWAYS)

        val controller = map.controller
        controller.setZoom(16.0)
        val defaultLat = 59.87
        val defaultLon = 10.66
        val defaultPoint = GeoPoint(defaultLat, defaultLon)
        controller.setCenter(defaultPoint)
        // No fixed marker; we'll center on user location when permission is granted.

        // Request or enable location to get device coordinates
        if (hasLocationPermission()) {
            enableMyLocation()
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }

        // Button to open the address in Google Maps (or any maps app / browser as fallback)
        binding.fabOpenGoogleMaps.setOnClickListener {
            val address = "Øvre Movei 23, 1450 Nesodden"
            val geoUri = Uri.parse("geo:0,0?q=" + Uri.encode(address))

            // 1) Try Google Maps explicitly (component)
            try {
                val mapsIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
                    setPackage("com.google.android.apps.maps")
                }
                Toast.makeText(requireContext(), "Åpner Google Maps-appen", Toast.LENGTH_SHORT).show()
                startActivity(mapsIntent)
                return@setOnClickListener
            } catch (e: Exception) {
                // Ignore and try implicit
            }

            // 2) Try any app that can handle geo: VIEW (component)
            try {
                val anyMapIntent = Intent(Intent.ACTION_VIEW, geoUri)
                Toast.makeText(requireContext(), "Åpner kartapp", Toast.LENGTH_SHORT).show()
                startActivity(anyMapIntent)
                return@setOnClickListener
            } catch (e: Exception) {
                // Ignore and try HTTP fallback
            }

            // 3) Final fallback: open in a browser (web)
            try {
                val httpUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(address))
                Toast.makeText(requireContext(), "Åpner i nettleser", Toast.LENGTH_SHORT).show()
                startActivity(Intent(Intent.ACTION_VIEW, httpUri))
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Kunne ikke åpne kartapp eller nettleser", Toast.LENGTH_SHORT).show()
            }
        }

        // --- THIS IS THE NEW CODE TO ADD ---
        binding.fabAddItem.setOnClickListener {    // Get the user's current location from the map overlay
            val currentLocation = myLocationOverlay?.myLocation
            if (currentLocation != null) {
                // 1. Create a message with the coordinates
                val text = "Nytt element lagt til på: ${String.format("%.5f, %.5f", currentLocation.latitude, currentLocation.longitude)}"
                Toast.makeText(requireContext(), text, Toast.LENGTH_LONG).show()

                // 2. Create a new marker for the map
                val newMarker = Marker(binding.osmMapView).apply {
                    position = GeoPoint(currentLocation.latitude, currentLocation.longitude)
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = "Nytt element"
                }

                // 3. Add the new marker to the map and redraw it
                binding.osmMapView.overlays.add(newMarker)
                binding.osmMapView.invalidate()

            } else {
                // Show a message if location is not yet available
                Toast.makeText(requireContext(), "Finner ikke nåværende posisjon", Toast.LENGTH_SHORT).show()
            }
        }
// --- END OF NEW CODE ---

    }

    private fun hasLocationPermission(): Boolean {
        val ctx = requireContext()
        val fine = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    private fun enableMyLocation() {
        val map = binding.osmMapView
        if (myLocationOverlay == null) {
            myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(requireContext()), map).apply {
                enableMyLocation()
                enableFollowLocation()
            }
            map.overlays.add(myLocationOverlay)
        } else {
            myLocationOverlay?.enableMyLocation()
            myLocationOverlay?.enableFollowLocation()
        }
        myLocationOverlay?.runOnFirstFix {
            val loc = myLocationOverlay?.myLocation
            if (loc != null) {
                val point = GeoPoint(loc.latitude, loc.longitude)
                requireActivity().runOnUiThread {
                    map.controller.setZoom(18.0)
                    map.controller.setCenter(point)
                    Toast.makeText(requireContext(), "Din posisjon: ${String.format("%.5f, %.5f", point.latitude, point.longitude)}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.osmMapView.onResume()
        activity?.findViewById<View>(R.id.fab)?.visibility = View.GONE
        if (hasLocationPermission()) {
            myLocationOverlay?.enableMyLocation()
            myLocationOverlay?.enableFollowLocation()
        }
    }

    override fun onPause() {
        binding.osmMapView.onPause()
        activity?.findViewById<View>(R.id.fab)?.visibility = View.VISIBLE
        myLocationOverlay?.disableFollowLocation()
        myLocationOverlay?.disableMyLocation()
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}