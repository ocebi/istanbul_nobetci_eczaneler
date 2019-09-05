package com.ozgurcebi.istanbul_nobetci_eczaneler

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jsoup.Jsoup

class MainViewModel : ViewModel() {
    var PharmacyList : MutableList<Pharmacy> = mutableListOf() //maybe val
    var finished : (data : MutableList<Pharmacy>)->Unit = { data->}
    var dataPulled = false
    var finishedPulling : (data : Boolean)->Unit = {data->}
    init {
        CoroutineScope(Dispatchers.IO).launch {
            getData()
        }
        //finished.invoke(PharmacyList)
    }
    private fun getData()
    {

        Log.d("deneme","Inside getData")

        val doc = Jsoup.connect("http://www.istanbulnobetcieczaneler.com/bolge/").get()
        val doc2 = doc.getElementsByClass("col-md-12")
        val pharmacies = doc2.get(4)
        var latitude : Double = 0.0
        var longtitude : Double = 0.0
        var name : String = ""
        var address: String = ""
        for(x in pharmacies.children())
        {
            try {
                //Log.d("deneme","checking")
                if(x.hasAttr("name") and x.attr("name").equals("enlem"))
                {
                    latitude = x.attr("value").toDouble()
                }
                else if(x.hasAttr("name") and x.attr("name").equals("boylam"))
                {
                    longtitude = x.attr("value").toDouble()
                }
                else if(x.hasAttr("name") and x.attr("name").equals("ad"))
                {
                    name = x.attr("value") + " ECZANESİ"

                    //Log.d("deneme","$name $latitude : $longtitude $address")
                }
                else if(x.hasAttr("name") and x.attr("name").equals("adres"))
                {
                    address = x.attr("value")
                    PharmacyList.add(
                        Pharmacy(
                            name,
                            latitude,
                            longtitude,
                            address
                        )
                    )
                }
            }
            catch (e: Exception)
            {
                Log.d("deneme",e.message)
            }

        }
        Log.d("deneme","getData finished. check listeners")
        finished.invoke(PharmacyList)
    }
    /*
    fun pushData() : MutableList<Pharmacy>
    {
        if(PharmacyList.isEmpty())
            getData()
        return PharmacyList
    }

     */

}