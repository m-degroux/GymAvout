package fr.math.gymavout

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ActivityAdapter
    private val activityList = mutableListOf<Map<String, Any>>()
    private val filteredList = mutableListOf<Map<String, Any>>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.rvActivities)
        val etSearch = view.findViewById<EditText>(R.id.etSearch)

        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = ActivityAdapter(filteredList)
        recyclerView.adapter = adapter

        val dbRef = FirebaseDatabase.getInstance("https://gymavout-app-default-rtdb.europe-west1.firebasedatabase.app/")
            .reference.child("activities")

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                activityList.clear()
                for (postSnapshot in snapshot.children) {
                    val activity = postSnapshot.value as? Map<String, Any>
                    if (activity != null) {
                        activityList.add(activity)
                    }
                }
                activityList.reverse()
                filter("") // Initialise la liste filtrée
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filter(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filter(query: String) {
        filteredList.clear()
        if (query.isEmpty()) {
            filteredList.addAll(activityList)
        } else {
            val lowerCaseQuery = query.lowercase()
            for (activity in activityList) {
                val title = activity["titre"]?.toString()?.lowercase() ?: ""
                val sport = activity["sport"]?.toString()?.lowercase() ?: ""
                val location = activity["lieu"]?.toString()?.lowercase() ?: ""
                
                if (title.contains(lowerCaseQuery) || sport.contains(lowerCaseQuery) || location.contains(lowerCaseQuery)) {
                    filteredList.add(activity)
                }
            }
        }
        adapter.notifyDataSetChanged()
    }
}
