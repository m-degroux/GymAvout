package fr.math.gymavout

import android.os.Bundle
import android.view.View
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.rvActivities)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = ActivityAdapter(activityList)
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
                activityList.reverse() // Pour voir les plus récentes en haut
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
