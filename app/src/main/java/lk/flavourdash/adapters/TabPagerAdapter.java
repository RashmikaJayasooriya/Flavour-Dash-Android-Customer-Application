package lk.flavourdash.adapters;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import lk.flavourdash.SignInFragment;
import lk.flavourdash.SignUpFragment;

public class TabPagerAdapter extends FragmentStateAdapter {

    private ViewPager2 viewPager;

    public TabPagerAdapter(FragmentActivity fragmentActivity, ViewPager2 viewPager) {
        super(fragmentActivity);
        this.viewPager = viewPager;
    }

    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new SignInFragment();
            case 1:
                return new SignUpFragment();
            default:
                return null;
        }
    }

    @Override
    public int getItemCount() {
        return 2; // Number of tabs
    }

    public void setCurrentItem(int position) {
        viewPager.setCurrentItem(position, true);
    }
}