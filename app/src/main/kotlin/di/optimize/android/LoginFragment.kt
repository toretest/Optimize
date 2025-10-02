package di.optimize.android

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import di.optimize.android.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonLogin.setOnClickListener {
            val user = binding.editTextUsername.text?.toString()?.trim() ?: ""
            val pass = binding.editTextPassword.text?.toString() ?: ""
            if (user == "a" && pass == "p") {
                // Navigate to FirstFragment on successful login
                findNavController().navigate(R.id.action_LoginFragment_to_FirstFragment)
            } else {
                Toast.makeText(requireContext(), "Feil brukernavn eller passord", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}