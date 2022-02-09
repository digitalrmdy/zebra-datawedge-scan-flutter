package be.rmdy.zebra_datawedge_scan_flutter

import org.json.JSONObject

class Scan(private val data: String, private val symbology: String)
{
    fun toJson(): String{
        return JSONObject(mapOf(
                "scanData" to this.data,
                "symbology" to this.symbology
        )).toString();
    }
}