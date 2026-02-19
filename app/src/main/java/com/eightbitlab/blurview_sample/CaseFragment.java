package com.eightbitlab.blurview_sample;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import eightbitlab.com.blurview.BlurTarget;
import eightbitlab.com.blurview.BlurView;

public class CaseFragment extends Fragment {
    public CaseFragment() {
        super(R.layout.fragment_list);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        BlurView topBlurView = view.findViewById(R.id.topBlurView);
        BlurTarget target = requireActivity().findViewById(R.id.target);

        final Drawable windowBackground = requireActivity().getWindow().getDecorView().getBackground();
        topBlurView.setupWith(target)
                .setFrameClearDrawable(windowBackground)
                .setBlurRadius(25f);

        ViewCompat.setOnApplyWindowInsetsListener(topBlurView, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            params.topMargin = insets.top + dpToPx(12);
            v.setLayoutParams(params);
            return windowInsets;
        });
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
