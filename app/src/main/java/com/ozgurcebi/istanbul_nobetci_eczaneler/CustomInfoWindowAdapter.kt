package com.ozgurcebi.istanbul_nobetci_eczaneler

import android.content.Context
import android.view.LayoutInflater
import android.view.View

import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter
import com.google.android.gms.maps.model.Marker
import android.widget.TextView


class CustomInfoWindowAdapter(c : Context) : InfoWindowAdapter {
    private val mWindow: View = LayoutInflater.from(c).inflate(R.layout.custom_info_window, null)
    override fun getInfoContents(p0: Marker?): View {
        rendowWindowText(p0, mWindow)
        return mWindow
    }

    override fun getInfoWindow(p0: Marker?): View? {
        return null
    }

    private fun rendowWindowText(marker: Marker?, view: View?) {

        val title = marker?.title
        val tvTitle = view?.findViewById<View>(R.id.title) as TextView

        if (title != "") {
            tvTitle.text = title
        }

        val snippet = marker?.snippet
        val tvSnippet = view.findViewById(R.id.snippet) as TextView

        if (snippet != "") {
            tvSnippet.text = snippet
        }
        val tvRouteInfo = view.findViewById(R.id.routeInfo) as TextView

        if (snippet != "") {
            tvRouteInfo.text = "Rota oluşturmak için dokunun."
        }
    }
}