package br.com.richardnatan.pokeDexApp

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.isDigitsOnly
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.richardnatan.pokeDexApp.model.Pokemon
import br.com.richardnatan.pokeDexApp.util.PokemonTask
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity(), PokemonTask.Callback {

    private val pokeList = mutableListOf<Pokemon>()
    private val cachedList = mutableListOf<Pokemon>()
    private lateinit var adapter: Adapter
    private lateinit var rvMain: RecyclerView
    private lateinit var searchEdit: EditText
    private lateinit var returnButton: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        searchEdit = findViewById(R.id.edit_search)
        returnButton = findViewById(R.id.button_return)
        rvMain = findViewById(R.id.rv_main)
        adapter = Adapter(pokeList)
        rvMain.adapter = adapter
        rvMain.layoutManager = GridLayoutManager(this, 2)

        val pokemonTask = PokemonTask(this)
        pokemonTask.execute()

        // Search Button
        searchEdit.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (searchEdit.text.isNotEmpty()) {
                    returnButton.visibility = View.VISIBLE
                    hideKeyboard(this.searchEdit)
                }

                if (cachedList.isEmpty()) {
                    cachedList.addAll(pokeList)
                }

                pokeList.clear()
                pokeList.addAll(searchPokemon())
                adapter.notifyDataSetChanged()
            }
            true
        }

        // Return Button
        returnButton.setOnClickListener {
            pokeList.clear()
            pokeList.addAll(cachedList)
            adapter.notifyDataSetChanged()
            returnButton.visibility = View.GONE
            searchEdit.text = null
        }

    }

    private inner class Adapter(private val listPokemons: List<Pokemon>) :
        RecyclerView.Adapter<Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Adapter.ViewHolder {
            val view = layoutInflater.inflate(R.layout.rv_pokemon_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: Adapter.ViewHolder, position: Int) {
            val item = listPokemons[position]
            holder.bind(item)
        }

        override fun getItemCount(): Int {
            return listPokemons.size
        }

        private inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            fun bind(item: Pokemon) {
                val txtName: TextView = itemView.findViewById(R.id.txt_name_pokemon)
                val txtId: TextView = itemView.findViewById(R.id.txt_id_pokemon)
                val imageView: ImageView = itemView.findViewById(R.id.image_pokemon)


                txtId.text = getString(R.string.pokemon_id, item.id.toString())
                txtName.text =
                    item.name.replaceFirst(item.name.first(), item.name.first().uppercaseChar())
                Picasso.get().load(item.picture).into(imageView)

            }
        }
    }

    override fun onResult(pokemons: List<Pokemon>) {
        this.pokeList.clear()
        this.pokeList.addAll(pokemons)
        this.adapter.notifyDataSetChanged()
    }

    private fun searchPokemon(): List<Pokemon> {
        val filteredList = mutableListOf<Pokemon>()

        // Filter by ID
        if (!searchEdit.text.isNullOrEmpty() && searchEdit.text.isDigitsOnly()) {
            val tempList = cachedList.filter { pokemon ->
                pokemon.id == searchEdit.text.toString().toInt()
            }
            filteredList.addAll(tempList)
        } else { // Filter by Name
            val tempList = cachedList.filter { pokemon ->
                pokemon.name.contains(searchEdit.text.toString().lowercase())
            }
            filteredList.addAll(tempList)
        }

        if (filteredList.isEmpty()) {
            Toast.makeText(
                this,
                "No Pokémon found with the value: ${searchEdit.text}",
                Toast.LENGTH_LONG
            ).show()
        }

        return filteredList
    }

    private fun hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}