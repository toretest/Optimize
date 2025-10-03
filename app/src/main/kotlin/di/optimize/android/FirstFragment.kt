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
        map.setBuiltInZoomControls(true)

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

            // 1) Try Google Maps explicitly
            try {
                val mapsIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
                    setPackage("com.google.android.apps.maps")
                }
                startActivity(mapsIntent)
                return@setOnClickListener
            } catch (e: Exception) {
                // Ignore and try implicit
            }

            // 2) Try any app that can handle geo: VIEW
            try {
                val anyMapIntent = Intent(Intent.ACTION_VIEW, geoUri)
                startActivity(anyMapIntent)
                return@setOnClickListener
            } catch (e: Exception) {
                // Ignore and try HTTP fallback
            }

            // 3) Final fallback: open in a browser
            try {
                val httpUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(address))
                startActivity(Intent(Intent.ACTION_VIEW, httpUri))
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Kunne ikke åpne kartapp eller nettleser", Toast.LENGTH_SHORT).show()
            }
        }
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