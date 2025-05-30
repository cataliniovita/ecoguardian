// Generated by view binder compiler. Do not edit!
package com.example.helloworld.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.example.helloworld.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class FragmentHomeBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final CardView featuresCard;

  @NonNull
  public final TextView userEmailTextView;

  @NonNull
  public final CardView welcomeCard;

  @NonNull
  public final TextView welcomeTextView;

  private FragmentHomeBinding(@NonNull ConstraintLayout rootView, @NonNull CardView featuresCard,
      @NonNull TextView userEmailTextView, @NonNull CardView welcomeCard,
      @NonNull TextView welcomeTextView) {
    this.rootView = rootView;
    this.featuresCard = featuresCard;
    this.userEmailTextView = userEmailTextView;
    this.welcomeCard = welcomeCard;
    this.welcomeTextView = welcomeTextView;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static FragmentHomeBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static FragmentHomeBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.fragment_home, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static FragmentHomeBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.featuresCard;
      CardView featuresCard = ViewBindings.findChildViewById(rootView, id);
      if (featuresCard == null) {
        break missingId;
      }

      id = R.id.userEmailTextView;
      TextView userEmailTextView = ViewBindings.findChildViewById(rootView, id);
      if (userEmailTextView == null) {
        break missingId;
      }

      id = R.id.welcomeCard;
      CardView welcomeCard = ViewBindings.findChildViewById(rootView, id);
      if (welcomeCard == null) {
        break missingId;
      }

      id = R.id.welcomeTextView;
      TextView welcomeTextView = ViewBindings.findChildViewById(rootView, id);
      if (welcomeTextView == null) {
        break missingId;
      }

      return new FragmentHomeBinding((ConstraintLayout) rootView, featuresCard, userEmailTextView,
          welcomeCard, welcomeTextView);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
