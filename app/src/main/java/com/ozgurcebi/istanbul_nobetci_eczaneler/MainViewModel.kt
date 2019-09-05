package com.ozgurcebi.istanbul_nobetci_eczaneler

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jsoup.Jsoup

class MainViewModel : ViewModel() {
    var PharmacyList : MutableList<Pharmacy> = mutableListOf()
    var finished : (data : MutableList<Pharmacy>)->Unit = { data->}
    var dataPulled = false

    fun requestData()
    {
        Log.d("hata","data requested")
        getData()
    }

    private fun getData()
    {
        CoroutineScope(Dispatchers.IO).launch {
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
            dataPulled = true
            finished.invoke(PharmacyList)
            Log.d("hata","getData finished.")
        }

    }

}