package com.imot.endear.ui.sign_out_fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.imot.endear.HomeActivity
import com.imot.endear.MainActivity
import com.imot.endear.R
import com.imot.endear.databinding.FragmentSignOutBinding


class SignOutFragment: Fragment() {
    private var _binding: FragmentSignOutBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val SignOutViewModel =
            ViewModelProvider(this)[SignOutViewModel::class.java]

        _binding = FragmentSignOutBinding.inflate(inflater, container, false)
        val root: View = binding.root


        //val textView: TextView = binding.tvFindPeople
        SignOutViewModel.text.observe(viewLifecycleOwner) {
            //To sign out a user, call signOut:
            val alertDialog = AlertDialog.Builder(requireContext(), R.style.MyRequestDialog)
            alertDialog.apply {
                setTitle("Déconnexion")
                setMessage("Etes-vous sûr(e) de vouloir vous déconnecter ?")
                setIcon(R.drawable.ic_sign_out)
                setNegativeButton("Annuler") { dialogInterface, _ -> dialogInterface.dismiss()
                    Intent(context, HomeActivity::class.java).also {
                        startActivity(it)
                    }
                }

                alertDialog.setPositiveButton("Se décnnecter"){_, _->
                    Firebase.auth.signOut()
                    Intent(context, MainActivity::class.java).also {
                        startActivity(it)
                    }

                    view?.let { it1 ->
                        Snackbar.make(it1, "Vous avez été déconnecté.", Snackbar.LENGTH_LONG)
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