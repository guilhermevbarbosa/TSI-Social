package com.example.tsisocialapp.views.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.tsisocialapp.R
import com.example.tsisocialapp.model.Category
import com.example.tsisocialapp.services.CategoryService
import com.google.android.material.snackbar.Snackbar
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory

class HomePageFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home_page, container, false)

        return view
    }

    override fun onResume() {
        super.onResume()

        getCategories()
    }

    fun getCategories(){
        val retrofit = Retrofit.Builder().baseUrl("https://crudcrud.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(CategoryService::class.java)
        val call = service.list()

        val callback = object: Callback<List<Category>>{
            override fun onResponse(
                call: Call<List<Category>>,
                response: Response<List<Category>>
            ) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "Sucesso", Toast.LENGTH_LONG).show()
                    Log.e("Categorias", response.body().toString())
                }
                else {
                    Toast.makeText(context, "Não foi possível recuperar categorias", Toast.LENGTH_LONG).show()
                    Log.e("ERRO", "Erro da API")
                }
            }

            override fun onFailure(call: Call<List<Category>>, t: Throwable) {
                Toast.makeText(context, "Não foi possível recuperar categorias", Toast.LENGTH_LONG).show()
                Log.e("ERRO", "Falha ao chamar o serviço", t)
            }
        }

        call.enqueue(callback)
    }

    companion object {
        @JvmStatic
        fun newInstance() = HomePageFragment()
    }
}