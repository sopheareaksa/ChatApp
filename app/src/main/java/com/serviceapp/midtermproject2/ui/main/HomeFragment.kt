package com.serviceapp.midtermproject2.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.serviceapp.midtermproject2.databinding.FragmentHomeBinding
import com.serviceapp.midtermproject2.viewmodel.ChatViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChatViewModel by viewModels()
    private lateinit var userAdapter: UserAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()

        viewModel.getUsers()
    }

    private fun setupRecyclerView() {
        userAdapter = UserAdapter { user ->
            val action = HomeFragmentDirections.actionHomeFragmentToChatFragment(
                otherUserId = user.uid,
                otherUserName = user.name
            )
            findNavController().navigate(action)
        }
        binding.rvRecentChats.apply {
            adapter = userAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupObservers() {
        viewModel.users.observe(viewLifecycleOwner) { users ->
            userAdapter.setUsers(users)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
