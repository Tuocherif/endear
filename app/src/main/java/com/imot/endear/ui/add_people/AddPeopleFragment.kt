package com.imot.endear.ui.add_people

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.imot.endear.databinding.FragmentFindpeopleBinding

class AddPeopleFragment : Fragment() {

    private var _binding: FragmentFindpeopleBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val addPeopleViewModel =
            ViewModelProvider(this)[AddPeopleViewModel::class.java]

        _binding = FragmentFindpeopleBinding.inflate(inflater, container, false)
        val root: View = binding.root

        addPeopleViewModel.text.observe(viewLifecycleOwner) {

        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}