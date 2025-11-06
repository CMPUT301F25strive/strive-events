//package com.example.eventlottery.entrant;
//
//import android.content.SharedPreferences;
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.EditText;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//import androidx.navigation.fragment.NavHostFragment;
//
//import com.example.eventlottery.R;
//
//public class WelcomeFragment extends Fragment {
//
//    private EditText usernameInput;
//    private EditText emailInput;
//
//    private static final String PREFS = "app_prefs";
//    private static final String KEY_USERNAME = "username";
//    private static final String KEY_EMAIL = "email";
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater,
//                             @Nullable ViewGroup container,
//                             @Nullable Bundle savedInstanceState) {
//        return inflater.inflate(R.layout.fragment_welcome, container, false);
//    }
//
//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        usernameInput = view.findViewById(R.id.usernameInput);
//        emailInput = view.findViewById(R.id.emailInput);
//
//        view.findViewById(R.id.buttonGetStarted).setOnClickListener(v -> {
//            String name = usernameInput.getText().toString().trim();
//            String email = emailInput.getText().toString().trim();
//
//            if (name.isEmpty() || email.isEmpty()) {
//                Toast.makeText(requireContext(), "Please fill out all fields", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            // Save to SharedPreferences (simple "signed in" flag + data)
//            SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS, 0);
//            prefs.edit()
//                    .putString(KEY_USERNAME, name)
//                    .putString(KEY_EMAIL, email)
//                    .apply();
//
//            // Navigate to home (EntrantEventListFragment)
//            NavHostFragment.findNavController(this)
//                    .navigate(R.id.action_welcomeFragment_to_entrantEventListFragment);
//        });
//    }
//}