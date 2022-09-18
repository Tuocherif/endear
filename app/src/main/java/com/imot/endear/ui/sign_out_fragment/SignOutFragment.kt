package com.imot.endear.ui.sign_out_fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.imot.endear.HomeActivity
import com.imot.endear.MainActivity
import com.imot.endear.R
import com.imot.endear.databinding.FragmentSignOutBinding
import pl.droidsonroids.gif.GifImageView


class SignOutFragment: Fragment() {
    private var _binding: FragmentSignOutBinding? = null

    private lateinit var cry : GifImageView
    private lateinit var glad : GifImageView

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val SignOutViewModel =
            ViewModelProvider(this)[SignOutViewModel::class.java]
        _binding = FragmentSignOutBinding.inflate(inflater, container, false)
        val root: View = binding.root


        //val textView: TextView = binding.tvFindPeople
        SignOutViewModel.text.observe(viewLifecycleOwner) {


         glad = view?.findViewById(R.id.glad) as GifImageView
         cry = view?.findViewById(R.id.cry) as GifImageView
            //To sign out a user, call signOut:
            val alertDialog = AlertDialog.Builder(requireContext(), R.style.MyRequestDialog)
            alertDialog.apply {
                setTitle("Déconnexion")
                setMessage("Etes-vous sûr(e) de vouloir vous déconnecter ?")
                setIcon(R.drawable.ic_sign_out)
                setNegativeButton("Annuler") { dialogInterface, _ -> dialogInterface.dismiss()
                        cry.visibility = View.GONE
                        glad.visibility = View.VISIBLE
                        //Thread.sleep(5000)

                    Intent(context, HomeActivity::class.java).also {
                        startActivity(it)
                    }
                }

                alertDialog.setPositiveButton("Se décnnecter"){_, _->
                        glad.visibility = View.GONE
                        cry.visibility = View.VISIBLE
                    Firebase.auth.signOut()
                    Intent(context, MainActivity::class.java).also {
                        startActivity(it)
                    }

                    view?.let { it1 ->
                        Snackbar.make(it1, "Vous avez été déconnecté.\n A bientôt.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null)
                            //.setBackgroundTint()
                            .show()
                    }
                }

                alertDialog.show()
            }

        }

        return root
    }

}