package com.imot.endear.ui.friends_list

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.imot.endear.R
import com.imot.endear.TrackingActivity
import com.imot.endear.ViewHolder.UserViewHolder
import com.imot.endear.databinding.FragmentFriendsListBinding
import com.imot.endear.interfaces.IfirebaseLoadDone
import com.imot.endear.interfaces.InterfaceRecyclerItemClickListener
import com.imot.endear.model.User
import com.imot.endear.services.MyLocationReceiver
import com.imot.endear.utils.Common
import com.mancj.materialsearchbar.MaterialSearchBar

class FriendsListFragment : Fragment(), IfirebaseLoadDone {

    private var _binding: FragmentFriendsListBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    var searchAdapter: FirebaseRecyclerAdapter<User, UserViewHolder>? = null
    var adapter : FirebaseRecyclerAdapter<User, UserViewHolder>? = null

    lateinit var recyclerFriendsList : RecyclerView
    lateinit var firebaseLoadDone : IfirebaseLoadDone
    lateinit var expandable_search_bar : MaterialSearchBar
    lateinit var locationRequest : LocationRequest
    lateinit var fusedLocationProviderClient : FusedLocationProviderClient


    var suggestList: List<String> = ArrayList<String>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val FriendsListViewModel =
            ViewModelProvider(this)[FriendsListViewModel::class.java]

        _binding = FragmentFriendsListBinding.inflate(inflater, container, false)
        val root: View = binding.root


        //val textView: TextView = binding.tvFindPeople
        FriendsListViewModel.text.observe(viewLifecycleOwner) {

            recyclerFriendsList = binding.recyclerFriendsList
            expandable_search_bar = binding.expandableSearchBar
            expandable_search_bar.setCardViewElevation(10)
            expandable_search_bar.addTextChangeListener(object : TextWatcher {

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val suggest: List<String> = ArrayList<String>()

                    for (search in suggestList){
                        if (search.lowercase().contentEquals(expandable_search_bar.text.lowercase())){
                            suggest.plus(search)
                        }
                        expandable_search_bar.lastSuggestions = suggest
                    }

                }

                override fun afterTextChanged(s: Editable?) {

                }

            })
            expandable_search_bar.setOnSearchActionListener( object : MaterialSearchBar.OnSearchActionListener{
                override fun onSearchStateChanged(enabled: Boolean) {
                    if (!enabled){
                        //Close search == return default
                        if (adapter != null){
                            recyclerFriendsList.adapter = adapter
                        }
                    }
                }

                override fun onSearchConfirmed(text: CharSequence?) {
                    startSearch(text.toString())

                }

                override fun onButtonClicked(buttonCode: Int) {

                }

            })

            recyclerFriendsList.setHasFixedSize(true)
            val layoutManager = LinearLayoutManager(context)
            recyclerFriendsList.layoutManager = layoutManager
            recyclerFriendsList.addItemDecoration(DividerItemDecoration(context,layoutManager.orientation))

            loadFriendList()
            loadSearchData()

            firebaseLoadDone = this

            //Listening Location for background
            updateLocation()

        }


        return root
    }//end onCreate

    private fun updateLocation() {
        buildLocationRequest()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        getPendingIntent()?.let {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest,
                it)
        }
    }

    private fun getPendingIntent(): PendingIntent? {
        val intent = Intent(context, MyLocationReceiver::class.java)
        intent.action = MyLocationReceiver.ACTION

        return PendingIntent.getBroadcast(context,0,intent,PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun buildLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            smallestDisplacement = 10f
            fastestInterval = 3000
            interval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

    }

    override fun onFirebaseLoadUserNameDone(lstEmail: List<String>) {
        expandable_search_bar.lastSuggestions = lstEmail
    }

    override fun onFirebaseLoadUserNameFailed(message: String) {
        Toast.makeText(context, message,Toast.LENGTH_LONG).show()
    }

    private fun startSearch(search_string : String){
        val query = FirebaseDatabase.getInstance().getReference(Common.USER_INFORMATION)
            .child(Common.loggedUser!!.uid!!)
            .child(Common.ACCEPT_LIST)
            .orderByChild("email")
            .startAt(search_string)


        val options = FirebaseRecyclerOptions.Builder<User>()
            .setQuery(query, User::class.java)
            .build()

        searchAdapter = object : FirebaseRecyclerAdapter<User,UserViewHolder>(options){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.layout_user, parent, false)
                return UserViewHolder(itemView)
            }

            override fun onBindViewHolder(holder: UserViewHolder, position: Int, model: User) {
                holder.tv_user_email.text = model.email

                //Event

                holder.setClick(object  : InterfaceRecyclerItemClickListener{
                    override fun onItemClickListener(view: View, position: Int) {
                        Common.trackingUser = model
                        Intent(context, TrackingActivity::class.java).also {
                            startActivity(it)
                        }


                    }

                })
            }
        }

        searchAdapter!!.startListening()
        recyclerFriendsList.adapter = searchAdapter


    }

    fun loadFriendList(){
        val query = FirebaseDatabase.getInstance().getReference(Common.USER_INFORMATION)
            .child(Common.loggedUser!!.uid!!)
            .child(Common.ACCEPT_LIST)

        val options = FirebaseRecyclerOptions.Builder<User>()
            .setQuery(query, User::class.java)
            .build()

        adapter = object : FirebaseRecyclerAdapter<User,UserViewHolder>(options){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.layout_user, parent, false)
                return UserViewHolder(itemView)
            }

            override fun onBindViewHolder(holder: UserViewHolder, position: Int, model: User) {
                holder.tv_user_email.text = model.email

                //Event

                holder.setClick(object  : InterfaceRecyclerItemClickListener{
                    override fun onItemClickListener(view: View, position: Int) {
                        Common.trackingUser = model

                        //Start tracking Activity
                        Intent(context, TrackingActivity::class.java).also {
                            startActivity(it)
                        }



                    }

                })
            }
        }

        adapter!!.startListening()
        recyclerFriendsList.adapter = adapter
    }

    fun loadSearchData(){

        val lstUserEmail = ArrayList<String>()
        val userList = FirebaseDatabase.getInstance().getReference(Common.USER_INFORMATION)
            .child(Common.loggedUser!!.uid!!)
            .child(Common.ACCEPT_LIST)


        userList.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (userSnapShot in snapshot.children){
                    val user = userSnapShot.getValue(User::class.java)
                    lstUserEmail.add(user?.email!!)
                }
                onFirebaseLoadUserNameDone(lstUserEmail)
            }

            override fun onCancelled(error: DatabaseError) {
                onFirebaseLoadUserNameFailed(error.message)
            }

        })
    }

    override fun onStop() {
        if (adapter != null){
            adapter!!.stopListening()
        }
        if (searchAdapter != null){
            searchAdapter!!.stopListening()
        }

        super.onStop()
    }

    override fun onResume() {
        super.onResume()

        if (adapter != null){
            adapter!!.startListening()
        }
        if (searchAdapter != null){
            searchAdapter!!.startListening()
        }
    }






}



