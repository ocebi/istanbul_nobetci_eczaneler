package com.ozgurcebi.istanbul_nobetci_eczaneler

data class Pharmacy(val name : String, val latitude : Double, val longtitude: Double, val address : String)
{
    override fun toString(): String {
        return "$name"
    }
}