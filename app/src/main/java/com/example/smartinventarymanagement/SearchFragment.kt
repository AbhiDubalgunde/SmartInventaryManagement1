package com.example.smartinventarymanagement

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.EditText
import androidx.fragment.app.Fragment

class SearchFragment : Fragment() {

    private var listener: OnSearchListener? = null

    interface OnSearchListener {
        fun onSearch(query: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        listener = parentFragment as? OnSearchListener

        if (listener == null) {
            throw RuntimeException("Parent fragment must implement OnSearchListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_search, container, false)

        val search = view.findViewById<EditText>(R.id.etSearch)

        search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                listener?.onSearch(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        return view
    }
}