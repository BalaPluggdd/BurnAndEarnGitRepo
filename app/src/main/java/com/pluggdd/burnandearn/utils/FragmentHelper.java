package com.pluggdd.burnandearn.utils;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.pluggdd.burnandearn.R;


/**
 * Helper class to manage Fragment transaction
 */
public class FragmentHelper {

    private FragmentManager mFragmentManager;

    public FragmentHelper(FragmentManager fragmentManager) {
        mFragmentManager = fragmentManager;
    }

    // To add Fragment
    public void addFragment(int container, Fragment fragment) {
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        //fragmentTransaction.setCustomAnimations(R.anim.left_in,R.anim.right_out,R.anim.left_in,R.anim.right_out);
        fragmentTransaction.add(container, fragment);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
        fragmentTransaction.commit();
    }

    // To replace fragment
    public void replaceFragment(int container, Fragment fragment, boolean shouldAddToBackStack) {
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.enter_from_right,R.anim.exit_to_left,R.anim.enter_from_left,R.anim.exit_to_right);
        fragmentTransaction.replace(container, fragment);
        if (shouldAddToBackStack)
            fragmentTransaction.addToBackStack(fragment.getClass().getSimpleName());
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
        fragmentTransaction.commitAllowingStateLoss();
    }

    public void replaceFragmentWithAlterAnim(int container, Fragment fragment, boolean shouldAddToBackStack) {
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.enter_from_left,R.anim.exit_to_right,R.anim.enter_from_right,R.anim.exit_to_left);
        fragmentTransaction.replace(container, fragment);
        if (shouldAddToBackStack)
            fragmentTransaction.addToBackStack(fragment.getClass().getSimpleName());
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
        fragmentTransaction.commitAllowingStateLoss();
    }

    public void removeFromBackStack() {
        mFragmentManager.popBackStack();
    }

}
