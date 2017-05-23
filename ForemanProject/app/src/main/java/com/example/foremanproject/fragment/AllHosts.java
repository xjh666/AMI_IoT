package com.example.foremanproject.fragment;


import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.foremanproject.R;

public class AllHosts extends Fragment {
    public static AllHosts newInstance() {
        return new AllHosts();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.all_hosts, container, false);
        final Activity activity = getActivity();;
        return view;
    }
}
