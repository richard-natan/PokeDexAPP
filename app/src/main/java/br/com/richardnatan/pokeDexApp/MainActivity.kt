package br.com.richardnatan.pokeDexApp

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        searchEdit = findViewById(R.id.edit_search)
        rvMain = findViewById(R.id.rv_main)
        adapter = Adapter(pokeList)

        rvMain.adapter = adapter
        rvMain.layoutManager = GridLayoutManager(this, 2)

        val pokemonTask = PokemonTask(this)
        pokemonTask.execute()

        searchEdit.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH){
                if (cachedList.isEmpty()) {
                    cachedList.addAll(pokeList)
                }

                if (searchEdit.text.isNullOrEmpty()) {
                    pokeList.clear()
                    pokeList.addAll(cachedList)
                    adapter.notifyDataSetChanged()
                }


                val list = mutableListOf<Pokemon>()

                if (!searchEdit.text.isNullOrEmpty() && searchEdit.text.isDigitsOnly()) {
                    val filterList = cachedList.filter { pokemon ->
                        pokemon.id == searchEdit.text.toString().toInt()
                    }
                    list.addAll(filterList)
                } else {
                    val filterList = cachedList.filter { pokemon ->
                        pokemon.name.contains(searchEdit.text.toString().lowercase())
                    }
                    list.addAll(filterList)
                }


                pokeList.clear()
                pokeList.addAll(list)
                adapter.notifyDataSetChanged()
            }
            true
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
                txtName.text = item.name.replaceFirst(item.name.first(), item.name.first().uppercaseChar())
                Picasso.get().load(item.picture).into(imageView)

            }
        }
    }

    override fun onResult(pokemons: List<Pokemon>) {
        this.pokeList.clear()
        this.pokeList.addAll(pokemons)
        this.adapter.notifyDataSetChanged()
    }


}