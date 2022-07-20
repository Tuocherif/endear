package com.imot.endear

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.imot.endear.viewHolder.FriendRequestViewHolder
import com.imot.endear.interfaces.IfirebaseLoadDone
import com.imot.endear.model.User
import com.imot.endear.utils.Common
import com.mancj.materialsearchbar.MaterialSearchBar

class FriendRequestActivity : AppCompatActivity(), IfirebaseLoadDone {

    var searchAdapter: FirebaseRecyclerAdapter<User, FriendRequestViewHolder>? = null
    var adapter : FirebaseRecyclerAdapter<User, FriendRequestViewHolder>? = null



    lateinit var recycler_friend_request : RecyclerView
    lateinit var firebaseLoadDone : IfirebaseLoadDone
    lateinit var expandable_search_bar : MaterialSearchBar


    var suggestList: List<String> = ArrayList<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_request)

        recycler_friend_request = findViewById(R.id.recycler_friend_request)
        expandable_search_bar = findViewById(R.id.expandable_search_bar)
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
                        recycler_friend_request.adapter = adapter
                    }
                }
            }

            override fun onSearchConfirmed(text: CharSequence?) {
                startSearch(text.toString())

            }

            override fun onButtonClicked(buttonCode: Int) {

            }

        })

        recycler_friend_request.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this)
        recycler_friend_request.layoutManager = layoutManager
        recycler_friend_request.addItemDecoration(DividerItemDecoration(this,layoutManager.orientation))

        firebaseLoadDone = this

        loadFriendRequestList()
        loadSearchData()

    } //end onCreate

    private fun loadSearchData(){

        val lstName = ArrayList<String>()
        val userList = FirebaseDatabase.getInstance().getReference(Common.USER_INFORMATION)
            .child(Common.loggedUser!!.uid!!)
            .child(Common.FRIEND_REQUEST)

        userList.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (userSnapShot in snapshot.children){
                    val user = userSnapShot.getValue(User::class.java)
                    lstName.add(user?.name!!)
                }
                onFirebaseLoadUserNameDone(lstName)
            }

            override fun onCancelled(error: DatabaseError) {
                onFirebaseLoadUserNameFailed(error.message)
            }

        })
    }


    private fun startSearch(search_string : String){
        val query = FirebaseDatabase.getInstance().getReference(Common.USER_INFORMATION)
            .child(Common.loggedUser!!.uid!!)
            .child(Common.FRIEND_REQUEST)
            .orderByChild("name")

        val options = FirebaseRecyclerOptions.Builder<User>()
            .setQuery(query, User::class.java)
            .build()


        searchAdapter = object : FirebaseRecyclerAdapter<User, FriendRequestViewHolder>(options){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendRequestViewHolder {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.layout_friend_request, parent, false)
                    return FriendRequestViewHolder(itemView)
            }

            override fun onBindViewHolder(holder: FriendRequestViewHolder, position: Int, model: User) {
                holder.tv_user_name.text = model.name

                holder.img_decline.setOnClickListener{
                    //delete Request

                    val builder = AlertDialog.Builder(this@FriendRequestActivity)
                    builder.setTitle("Suppression de demande d'ajout")
                    builder.setMessage("Êtes-vous sûr de vouloir supprimer "+model.name+" de votre liste de demande d'ajout de proches?" )
                    builder.setPositiveButton("Oui"){ _: DialogInterface, id: Int ->

                        deleteFriendRequest(model,true)


                    }
                    builder.setNegativeButton("Non"){ dialogInterface: DialogInterface, id: Int ->
                        dialogInterface.dismiss();
                    }
                }
                holder.img_accept.setOnClickListener{
                    //Accept request
                    val builder = AlertDialog.Builder(this@FriendRequestActivity)
                    builder.setTitle("Ajout d'un proche")
                    builder.setMessage("Attention!! Cette personne aura accès à votre localisation en permanence.\n\nÊtes-vous sûr de vouloir ajouter "+model.name+" à votre liste de proches?" )
                    builder.setPositiveButton("Oui"){ dialogInterface: DialogInterface, id: Int ->

                        deleteFriendRequest(model,false)
                        addToAcceptList(model)// Add sender to receiver FriendList
                        addUserToFriendContact(model)//Add receiver to sender FriendList

                    }
                    builder.setNegativeButton("Non"){ dialogInterface: DialogInterface, id: Int ->
                        dialogInterface.dismiss();
                    }

                }
            }



        }

        searchAdapter!!.startListening()
        recycler_friend_request.adapter = searchAdapter


    }


    private fun loadFriendRequestList() {

        val query = FirebaseDatabase.getInstance().getReference(Common.USER_INFORMATION)
            .child(Common.loggedUser!!.uid!!)
            .child(Common.FRIEND_REQUEST)

        val options = FirebaseRecyclerOptions.Builder<User>()
            .setQuery(query, User::class.java)
            .build()

        adapter = object:FirebaseRecyclerAdapter<User, FriendRequestViewHolder>(options){
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int,
            ): FriendRequestViewHolder {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.layout_friend_request, parent, false)
                return FriendRequestViewHolder(itemView)
            }

            override fun onBindViewHolder(
                holder: FriendRequestViewHolder,
                position: Int,
                model: User,
            ) {
                holder.tv_user_name.text = model.name

                holder.img_decline.setOnClickListener{
                    //delete Request

                    val builder = AlertDialog.Builder(this@FriendRequestActivity)
                    builder.setTitle("Suppression de demande d'ajout")
                    builder.setMessage("Êtes-vous sûr de vouloir supprimer "+model.name+" de votre liste de demande d'ajout de proches?" )
                    builder.setPositiveButton("Oui"){ dialogInterface: DialogInterface, id: Int ->

                        deleteFriendRequest(model,true)


                    }
                    builder.setNegativeButton("Non"){ dialogInterface: DialogInterface, id: Int ->
                        dialogInterface.dismiss();
                    }
                }
                holder.img_accept.setOnClickListener{
                    //Accept request
                    val builder = AlertDialog.Builder(this@FriendRequestActivity)
                    builder.setTitle("Ajout d'un proche")
                    builder.setMessage("Attention!! Cette personne aura accès à votre localisation en permanence.\n\nÊtes-vous sûr de vouloir ajouter "+model.name+" à votre liste de proches?" )
                    builder.setPositiveButton("Oui"){ _: DialogInterface, id: Int ->

                        deleteFriendRequest(model,false)
                        addToAcceptList(model)// Add sender to receiver FriendList
                        addUserToFriendContact(model)//Add receiver to sender FriendList

                    }
                    builder.setNegativeButton("Non"){ dialogInterface: DialogInterface, id: Int ->
                        dialogInterface.dismiss();
                    }

                }
            }

        }
        adapter!!.startListening()
        recycler_friend_request.adapter = adapter


    }

    private fun addUserToFriendContact(model: User) {
        val acceptList = FirebaseDatabase.getInstance()
            .getReference(Common.USER_INFORMATION)
            .child(model.uid!!)
            .child(Common.ACCEPT_LIST)

        acceptList.child(Common.loggedUser!!.uid!!).setValue(Common.loggedUser)

    }

    private fun addToAcceptList(model: User) {
        val acceptList = FirebaseDatabase.getInstance()
            .getReference(Common.USER_INFORMATION)
            .child(Common.loggedUser!!.uid!!)
            .child(Common.ACCEPT_LIST)

        acceptList.child(model.uid!!).setValue(model)
    }

    private fun deleteFriendRequest(model: User, isShowMessage: Boolean) {
        val friendRequest = FirebaseDatabase.getInstance()
            .getReference(Common.USER_INFORMATION)
            .child(Common.loggedUser!!.uid!!)
            .child(Common.FRIEND_REQUEST)

        friendRequest.child(model.uid!!).removeValue()
            .addOnSuccessListener {
                if(isShowMessage){
                    Toast.makeText(this@FriendRequestActivity,
                        "Demande d'ajout supprimée avec succès.",Toast.LENGTH_SHORT).show()
                }
            }
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


    override fun onFirebaseLoadUserNameDone(lstName: List<String>) {
        expandable_search_bar.lastSuggestions = lstName
    }

    override fun onFirebaseLoadUserNameFailed(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}