package android.c.maproutetrackerapplication.model.google.nearbySearch

import android.c.maproutetrackerapplication.model.google.nearbySearch.Northeast
import android.c.maproutetrackerapplication.model.google.nearbySearch.Southwest
import com.google.gson.annotations.SerializedName

data class Viewport(@SerializedName("southwest")
                    val southwest: Southwest,
                    @SerializedName("northeast")
                    val northeast: Northeast
)