package com.example.imabarimapapp

import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {

    private lateinit var map: MapView
    private lateinit var searchBar: EditText

    // 読み込んだスポットを保持（検索用）
    private val spotsList = mutableListOf<Spot>()

    data class Spot(val name: String, val lat: Double, val lon: Double)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // OSMDroid の設定
        Configuration.getInstance().userAgentValue = packageName

        // 検索バー
        searchBar = findViewById(R.id.searchBar)

        // 地図の初期化
        map = findViewById(R.id.map)
        map.setTileSource(TileSourceFactory.MAPNIK)

        // 初期表示
        map.controller.setCenter(GeoPoint(33.5, 132.8))
        map.controller.setZoom(8.0)

        // ズーム制限
        map.setMinZoomLevel(9.0)
        map.setMaxZoomLevel(18.0)

        // マルチタッチ
        map.setMultiTouchControls(true)

        // 今治市を中心へ
        map.controller.setCenter(GeoPoint(34.0661, 132.9979))
        map.controller.setZoom(13.0)

        // 日本国内のみにスクロール制限
        val north = 45.8
        val south = 24.0
        val east = 153.0
        val west = 122.0
        map.setScrollableAreaLimitDouble(org.osmdroid.util.BoundingBox(north, east, south, west))

        // 観光スポットを読み込んで表示
        loadTouristSpots()

        // 🔍 検索バーが入力されたらマーカーを絞り込み
        searchBar.addTextChangedListener {
            val keyword = it.toString()
            updateMarkers(keyword)
        }
    }

    // JSON 読み込み
    private fun loadTouristSpots() {
        try {
            val inputStream = assets.open("spots.json")
            val reader = BufferedReader(InputStreamReader(inputStream))
            val jsonString = reader.use { it.readText() }

            val jsonArray = JSONArray(jsonString)
            spotsList.clear()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val name = obj.getString("name")
                val lat = obj.getDouble("lat")
                val lon = obj.getDouble("lon")

                spotsList.add(Spot(name, lat, lon))
            }

            updateMarkers("") // 初期表示（全件）
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 🔍 マーカー更新（検索対応）
    private fun updateMarkers(keyword: String) {
        map.overlays.clear() // 古いマーカー削除

        val filtered = if (keyword.isBlank()) {
            spotsList
        } else {
            spotsList.filter { it.name.contains(keyword, ignoreCase = true) }
        }

        for (spot in filtered) {
            val marker = Marker(map)
            marker.position = GeoPoint(spot.lat, spot.lon)
            marker.title = spot.name
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            map.overlays.add(marker)
        }

        map.invalidate()
    }
}
