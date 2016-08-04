package com.android.yockie.criminalintent;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import java.util.UUID;


//This is the class we started using, but now we will create CrimeListACtivity to use it instead
public class CrimeActivity extends SingleFragmentActivity {

    //NEW VERSION OF CRIME ACTIVITY. IT IS THE SAME AS CRIMELISTACTIVITY
    @Override
    protected Fragment createFragment() {
        //return new CrimeFragment();
        UUID crimeId = (UUID)getIntent().getSerializableExtra(CrimeFragment.EXTRA_CRIME_ID);
        //This is a new CrimeFragment filled with the data stored, if there is.
        CrimeFragment fragment = CrimeFragment.newInstance(crimeId);
        return fragment;
       //THIS IS THE OLD VERSION OF CRIMEACTIVITY }
  /*
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);

        //To manage Fragments, we need to use the fragment manager itself
        //In the book they use getSupportFragmentManager, but doesn't work
        //like that with newer version that I'm using.
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        android.support.v4.app.Fragment fragment = fm.findFragmentById(R.id.fragmentContainer);

        if (fragment == null){
            fragment = new CrimeFragment();
            fm.beginTransaction()
                    .add(R.id.fragmentContainer, fragment)
                    .commit();
        }
    */
  }
}
