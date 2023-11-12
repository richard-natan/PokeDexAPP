package br.com.richardnatan.teste.util

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.palette.graphics.Palette
import br.com.richardnatan.teste.R
import br.com.richardnatan.teste.model.Pokemon
import com.squareup.picasso.Picasso
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.util.concurrent.Executors
import javax.net.ssl.HttpsURLConnection

class PokemonTask(private val callback: Callback){

    private val handler = Handler(Looper.getMainLooper())

    interface Callback {
        fun onResult(pokemons: List<Pokemon>)
    }

    fun execute() {
        val executor = Executors.newSingleThreadExecutor()

        //https://pokeapi.co/api/v2/pokemon?limit=100000&offset=0

        executor.execute {
            var urlConnection: HttpsURLConnection? = null
            var stream: InputStream? = null

            try {
                val requestUrl = URL("https://pokeapi.co/api/v2/pokemon?limit=1016&offset=0")
                urlConnection = requestUrl.openConnection() as HttpsURLConnection
                urlConnection.readTimeout = 5000
                urlConnection.connectTimeout = 5000
                val statusCode = urlConnection.responseCode

                if (statusCode > 400) {
                    throw IOException("Não foi possivel conectar ao servidor")
                }

                stream = urlConnection.inputStream
                val jsonAsString = stream.bufferedReader().use { it.readText() }

                val pokemonList = toPokemon(jsonAsString)

                handler.post {
                    callback.onResult(pokemonList)
                }


            } catch (e: IOException) {
                Log.e("RESULT", "execute: ${e.message}")
            }
        }
    }

    private fun toPokemon(jsonAsString: String): List<Pokemon> {
        val pokemonList = mutableListOf<Pokemon>()

        val jsonRoot = JSONObject(jsonAsString)
        val jsonPokemons = jsonRoot.getJSONArray("results")

        for (i in 0 until jsonPokemons.length()) {
            val jsonPokemon = jsonPokemons.getJSONObject(i)
            val name = jsonPokemon.getString("name")
            val pokemonId = i + 1
            val pictureUrl =
                "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$pokemonId.png"



            pokemonList.add(
                Pokemon(
                    pokemonId,
                    name.replaceFirst(name.first(), name.first().uppercaseChar()),
                    pictureUrl
                )
            )
        }

        return pokemonList
    }


}