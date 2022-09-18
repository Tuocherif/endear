package com.imot.endear.ui.find_people


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.imot.endear.databinding.FragmentFindpeopleBinding
import android.graphics.Typeface
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.*
import com.imot.endear.R
import com.imot.endear.adapters.FindPeopleAdapter
import com.imot.endear.dataclasses.UserData
import com.imot.endear.viewHolder.UserViewHolder
import com.imot.endear.interfaces.IfirebaseLoadDone
import com.imot.endear.interfaces.InterfaceRecyclerItemClickListener
import com.imot.endear.model.MyResponse
import com.imot.endear.model.Request
import com.imot.endear.model.User
import com.imot.endear.remote.IFCMService
import com.imot.endear.utils.Common
import com.mancj.materialsearchbar.MaterialSearchBar
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.lang.reflect.Array

/*This activity presents all the people using the app and allows you
to send them a friend request to have the location of each other in real time.*/

class FindPeopleFragment : Fragment(), IfirebaseLoadDone {

    private var _binding: FragmentFindpeopleBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var searchAdapter: FirebaseRecyclerAdapter<User,UserViewHolder>? = null
    var adapter : FirebaseRecyclerAdapter<User,UserViewHolder>? = null

    lateinit var recycler_find_people : RecyclerView
    private lateinit var firebaseLoadDone : IfirebaseLoadDone
    private lateinit var iFCMService : IFCMService
    private lateinit var dbRef : DatabaseReference
    private lateinit var userArrayList : ArrayList<User>
    lateinit var expandable_search_bar : MaterialSearchBar


    var suggestList: List<String> = ArrayList()

    val compositeDisposable = CompositeDisposable()
    //lateinit var iFCMService:IFCMService

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val FindPeopleViewModel =
            ViewModelProvider(this)[FindPeopleViewModel::class.java]

        _binding = FragmentFindpeopleBinding.inflate(inflater, container, false)
        val root: View = binding.root

        //val textView: TextView = binding.tvFindPeople
        FindPeopleViewModel.text.observe(viewLifecycleOwner) {
            //Init View

            //iFCMService = Common.fcmService
            userArrayList = arrayListOf()
            getUserData()

            expandable_search_bar = binding.expandableSearchBar
            //recycler_find_people = view?.findViewById(R.id.recycler_find_people)  as RecyclerView
            //expandable_search_bar = view?.findViewById(R.id.expandable_search_bar)  as MaterialSearchBar
            expandable_search_bar.setCardViewElevation(10)
            expandable_search_bar.addTextChangeListener(object : TextWatcher {

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val suggest = ArrayList<String>()

                    for (search in suggestList){
                        if (search.lowercase().contentEquals(expandable_search_bar.text.lowercase())){
                            suggest.add(search)
                        }
                    }
                    expandable_search_bar.lastSuggestions = suggest
                }

                override fun afterTextChanged(s: Editable?) {

                }

            })
            expandable_search_bar.setOnSearchActionListener( object : MaterialSearchBar.OnSearchActionListener{
                override fun onSearchStateChanged(enabled: Boolean) {
                    if (!enabled){
                        //Close search == return default
                        if (adapter != null){
                            recycler_find_people.adapter = adapter
                        }
                    }
                }

                override fun onSearchConfirmed(text: CharSequence?) {
                    startSearch(text.toString())
                }

                override fun onButtonClicked(buttonCode: Int) {
                }

            })

            recycler_find_people = binding.recyclerFindPeople
            recycler_find_people.setHasFixedSize(true)
            val layoutManager = LinearLayoutManager(context)
            recycler_find_people.layoutManager = layoutManager
            recycler_find_people.addItemDecoration(DividerItemDecoration(context,layoutManager.orientation))

            firebaseLoadDone = this

            loadUserList()
            loadSearchData()
            //startSearch(readLine())
        }
        return root
    }//end onCreate

    private fun getUserData() {
        dbRef = FirebaseDatabase.getInstance().getReference("Users")

        dbRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    for (userSnapshot in snapshot.children){

                        val User = userSnapshot.getValue(User::class.java)
                        userArrayList.add(User!!)

                    }
                    recycler_find_people.adapter = FindPeopleAdapter(userArrayList)
                }
                           }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun startSearch(text_search : String?){
        val query = FirebaseDatabase.getInstance().getReference(Common.USER_INFORMATION)
            .orderByChild("name")
            .startAt(text_search)

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
                if (model.name == Common.loggedUser.name){
                    holder.tv_user_name_u.text = StringBuilder(model.name!!).append(" (moi)")
                    holder.tv_user_name_u.setTypeface(holder.tv_user_name_u.typeface, Typeface.ITALIC)
                } else{
                    holder.tv_user_name_u.text = StringBuilder(model.name!!)
                }

                //Event

                holder.setClick(object  : InterfaceRecyclerItemClickListener{
                    override fun onItemClickListener(view: View, position: Int) {

                    }

                })
            }

        }

        searchAdapter!!.startListening()
        recycler_find_people.adapter = searchAdapter


    }

    private fun loadUserList(){
        val query = FirebaseDatabase.getInstance().reference.child(Common.USER_INFORMATION)

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
                if (model.name == Common.loggedUser.name){
                    holder.tv_user_name_u.text = StringBuilder(model.name!!).append(" (moi)")
                    holder.tv_user_name_u.setTypeface(holder.tv_user_name_u.typeface, Typeface.ITALIC)

                    val bitmap = Array.getInt(model.image!!, 0)
                    holder.tv_user_image_u.setImageResource(bitmap)
                } else{
                    holder.tv_user_name_u.text = StringBuilder(model.name!!)
                    val bitmap = Array.getInt(model.image!!, 0)
                    holder.tv_user_image_u.setImageResource(bitmap)
                }

                //Event

                holder.setClick(object  : InterfaceRecyclerItemClickListener{
                    override fun onItemClickListener(view: View, position: Int) {
                        showDialogRequest(model)
                    }
                })
            }
        }

        adapter!!.startListening()
        recycler_find_people.adapter = adapter
    }

    private fun loadSearchData()  {

        val lstUserName = ArrayList<String>()
        val userList = FirebaseDatabase.getInstance().getReference(Common.USER_INFORMATION)

        userList.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (userSnapShot in snapshot.children){
                    val user = userSnapShot.getValue(User::class.java)
                    lstUserName.add(user!!.name!!)
                }
                firebaseLoadDone.onFirebaseLoadUserNameDone(lstUserName)
            }

            override fun onCancelled(error: DatabaseError) {
                firebaseLoadDone.onFirebaseLoadUserNameFailed(error.message)
            }
        })

        expandable_search_bar.lastSuggestions = lstUserName
    }

    private fun showDialogRequest(model: User) {
        val alertDialog = AlertDialog.Builder(requireContext(), R.style.MyRequestDialog)
        alertDialog.apply {
            setTitle("Ajouter un proche")
            setMessage("Souhaitez-vous envoyer une demande d'ajout à "+model.name+" ?")
            setIcon(R.drawable.ic_person_add)
            setNegativeButton("Annuler") { dialogInterface, _ -> dialogInterface.dismiss() }

            alertDialog.setPositiveButton("Envoyer"){_, _->
                val acceptList = FirebaseDatabase.getInstance()
                    .getReference(Common.USER_INFORMATION)
                    .child(Common.loggedUser.uid!!)
                    .child(Common.ACCEPT_LIST)

                //Chek from actual user friend list to make sure is not friend before
                acceptList.orderByKey().equalTo(model.uid)
                    .addListenerForSingleValueEvent(object:ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.value == null)//not yet friend
                            {
                                sendFriendRequest(model)
                            }else{
                                Toast.makeText(context, model.name+"est déjà présent dans la liste de vos proches.",Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }

                    })

            }

            alertDialog.show()
        }


    }

    private fun sendFriendRequest(model: User) {
        //Get token to send friend request
        val tokens = FirebaseDatabase.getInstance().getReference(Common.TOKENS)
        tokens.orderByKey().equalTo(model.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.value == null)//user not available
                    {
                        Toast.makeText(context,"La personne que vous " +
                                "essayez de joindre n'est pas disponible.", Toast.LENGTH_SHORT).show()
                    }else{
                        //Create request
                        val request = Request()
                        val dataSend = HashMap<String,String>()
                        dataSend[Common.FROM_UID] = Common.loggedUser.uid!! //sender's uid
                        dataSend[Common.FROM_EMAIL] = Common.loggedUser.email!! //sender's email
                        dataSend[Common.FROM_NAME] = Common.loggedUser.name!!  //sender's name
                        dataSend[Common.FROM_IMAGE] = Common.loggedUser.image.toString() //sender's image
                        dataSend[Common.TO_UID] = model.uid!! //receiver's uid
                        dataSend[Common.TO_EMAIL] = model.email!! //receiver's email
                        dataSend[Common.TO_NAME] = model.name!! //receiver's name
                        dataSend[Common.TO_IMAGE] = model.image.toString() //receiver's image

                        //set request
                        request.to = snapshot.child(model.uid!!).getValue(String::class.java)!!
                        request.data = dataSend

                        //send

                        compositeDisposable.add(Common.fcmService.sendFriendRequestToUser(request)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({t: MyResponse? ->
                                if (t!!.success == 1){
                                    Toast.makeText(context,
                                        "Votre demande a bien été envoyée.",Toast.LENGTH_LONG).show()
                                }
                            },{t: Throwable? ->
                                Toast.makeText(context,
                                    t!!.message,Toast.LENGTH_LONG).show()
                            }))

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })


    }


    override fun onStop() {
        adapter?.stopListening()
        searchAdapter?.stopListening()

        //compositeDisposable.clear()
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        adapter?.startListening()
        searchAdapter?.startListening()
    }
    override fun onFirebaseLoadUserNameDone(lstName: List<String>) {
        expandable_search_bar.lastSuggestions = lstName
    }

    override fun onFirebaseLoadUserNameFailed(message: String) {
        Toast.makeText(context, "erreur de chargement des noms",Toast.LENGTH_LONG).show()
    }

}