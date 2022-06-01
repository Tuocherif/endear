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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.imot.endear.R
import com.imot.endear.ViewHolder.UserViewHolder
import com.imot.endear.interfaces.IfirebaseLoadDone
import com.imot.endear.interfaces.InterfaceRecyclerItemClickListener
import com.imot.endear.model.MyResponse
import com.imot.endear.model.Request
import com.imot.endear.model.User
import com.imot.endear.utils.Common
import com.mancj.materialsearchbar.MaterialSearchBar
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class FindPeopleFragment : Fragment(), IfirebaseLoadDone {

    private var _binding: FragmentFindpeopleBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    var searchAdapter: FirebaseRecyclerAdapter<User,UserViewHolder>? = null
    var adapter : FirebaseRecyclerAdapter<User,UserViewHolder>? = null

    lateinit var recycler_all_user : RecyclerView
    lateinit var firebaseLoadDone : IfirebaseLoadDone
    lateinit var expandable_search_bar : MaterialSearchBar


    var suggestList: List<String> = ArrayList<String>()

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
            //textView.text = it

            //iFCMService = Common.fcmService

            recycler_all_user = binding.recyclerAllPeople
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
                            recycler_all_user.adapter = adapter
                        }
                    }
                }

                override fun onSearchConfirmed(text: CharSequence?) {
                    startSearch(text.toString())

                }

                override fun onButtonClicked(buttonCode: Int) {

                }

            })

            recycler_all_user.setHasFixedSize(true)
            val layoutManager = LinearLayoutManager(context)
            recycler_all_user.layoutManager = layoutManager
            recycler_all_user.addItemDecoration(DividerItemDecoration(context,layoutManager.orientation))

            firebaseLoadDone = this

            loadUserList()
            loadSearchData()
        }
        return root
    }//end onCreate

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun startSearch(search_string : String){
        val query = FirebaseDatabase.getInstance().getReference(Common.USER_INFORMATION)
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
                if (model.email.equals(Common.loggedUser!!.email)){
                    holder.tv_user_email.text = StringBuilder(model.email!!).append(" (me)")
                    holder.tv_user_email.setTypeface(holder.tv_user_email.typeface, Typeface.ITALIC)
                } else{
                    holder.tv_user_email.setText(StringBuilder(model.email!!))
                }

                //Event

                holder.setClick(object  : InterfaceRecyclerItemClickListener{
                    override fun onItemClickListener(view: View, position: Int) {

                    }

                })
            }

        }

        searchAdapter!!.startListening()
        recycler_all_user.adapter = searchAdapter


    }

    fun loadUserList(){
        val query = FirebaseDatabase.getInstance().getReference(Common.USER_INFORMATION)

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
                if (model.email == Common.loggedUser!!.email){
                    holder.tv_user_email.text = StringBuilder(model.email!!).append(" (me)")
                    holder.tv_user_email.setTypeface(holder.tv_user_email.typeface, Typeface.ITALIC)
                } else{
                    holder.tv_user_email.text = StringBuilder(model.email!!)
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
        recycler_all_user.adapter = adapter
    }

    private fun showDialogRequest(model: User) {
        val alertDialog = AlertDialog.Builder(requireContext(), R.style.MyRequestDialog)
        alertDialog.apply {
            setTitle("Ajouter un proche")
            setMessage("Souhaitez-vous envoyer une demande d'ajout à "+model.email+" ?")
            setIcon(R.drawable.ic_person_add)
            setNegativeButton("Annuler", {dialogInterface, _-> dialogInterface.dismiss()})

            alertDialog.setPositiveButton("Envoyer"){_, _->
                val acceptList = FirebaseDatabase.getInstance()
                    .getReference(Common.USER_INFORMATION)
                    .child(Common.loggedUser!!.uid!!)
                    .child(Common.ACCEPT_LIST)

                //Chek from actual user friend list to make sure is not friend before
                acceptList.orderByKey().equalTo(model.uid)
                    .addListenerForSingleValueEvent(object:ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.value == null)//not yet friend
                            {
                                sendFriendRequest(model)

                            }else{
                                Toast.makeText(context, model.email+"est déjà présent dans la liste de vos proches.",Toast.LENGTH_SHORT).show()
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
                        dataSend[Common.FROM_UID] = Common.loggedUser!!.uid!! //sender's uid
                        dataSend[Common.FROM_EMAIL] = Common.loggedUser!!.email!! //sender's email
                        dataSend[Common.TO_UID] = model.uid!! //receiver's uid
                        dataSend[Common.TO_EMAIL] = model.email!! //receiver's email

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

    fun loadSearchData(){

        val lstUserEmail = ArrayList<String>()
        val userList = FirebaseDatabase.getInstance().getReference(Common.USER_INFORMATION)

        userList.addListenerForSingleValueEvent(object : ValueEventListener{
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

        compositeDisposable.clear()
        super.onStop()
    }
    override fun onFirebaseLoadUserNameDone(lstEmail: List<String>) {
        expandable_search_bar.lastSuggestions = lstEmail
    }

    override fun onFirebaseLoadUserNameFailed(message: String) {
        Toast.makeText(context, message,Toast.LENGTH_LONG).show()
    }

}