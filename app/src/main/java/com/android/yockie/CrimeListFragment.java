package com.android.yockie;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class CrimeListFragment extends ListFragment {
    private ArrayList<Crime> mCrimes;
    private boolean mSubtitleVisible = false;
    private boolean mIsNewCrime = false;

    public static final String SUBTITLE_SHOWN = "subtitle";

    //Button mNewCrimeButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        getActivity().setTitle(R.string.crimes_title);
        //getActivity().setContentView(R.layout.empty_view);
        mCrimes = CrimeLab.get(getActivity()).getCrimes();
        CrimeAdapter adapter = new CrimeAdapter(mCrimes);
        setListAdapter(adapter);
        setRetainInstance(true);
        mSubtitleVisible = false;
    }
    
    @TargetApi(11)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, parent, savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (mSubtitleVisible) {
                getActivity().getActionBar().setSubtitle(R.string.subtitle);
            }
        }

        //Registering the listView for a contextual menu
        ListView listView = (ListView)v.findViewById(android.R.id.list);


        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            //Use floating context menu on froyo and on gingerbread
            registerForContextMenu(listView);
        }else{
            //Use contextual action bar on honeybomb and higher
            listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

            listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
                public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                    //Not used yet
                }

                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    MenuInflater inflater = mode.getMenuInflater();
                    inflater.inflate(R.menu.crime_list_item_context, menu);
                    return true;
                }

                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    //Not used yet
                    return false;
                }

                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    switch (item.getItemId()){
                        case R.id.menu_item_delete_crime:
                            CrimeAdapter adapter = (CrimeAdapter)getListAdapter();
                            CrimeLab crimeLab = CrimeLab.get(getActivity());
                            for (int i = adapter.getCount() - 1; i >= 0; i--){
                                if (getListView().isItemChecked(i)){
                                    //First, delete picture of the crime if there is any
                                    if (adapter.getItem(i).getPhoto() != null){
                                        getActivity().deleteFile(adapter.getItem(i).getPhoto().getFilename());
                                    }
                                    crimeLab.deleteCrime(adapter.getItem(i));
                                }
                            }
                            mode.finish();
                            adapter.notifyDataSetChanged();
                            return true;
                        default:
                            return false;
                    }
                }

                public void onDestroyActionMode(ActionMode mode) {
                    //Save crimes when coming back from multichoice menu
                    CrimeLab.get(getActivity()).saveCrimes();
                }
            });
        }
        return v;
    }

    public void onListItemClick(ListView l, View v, int position, long id) {
        // get the Crime from the adapter
        Crime c = ((CrimeAdapter)getListAdapter()).getItem(position);
        // start an instance of CrimePagerActivity
        Intent i = new Intent(getActivity(), CrimePagerActivity.class);
        i.putExtra(CrimeFragment.EXTRA_CRIME_ID, c.getId());
        startActivityForResult(i, 0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        ((CrimeAdapter)getListAdapter()).notifyDataSetChanged();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime_list, menu);
        MenuItem showSubtitle = menu.findItem(R.id.menu_item_show_subtitle);
        if (mSubtitleVisible && showSubtitle != null) {
            showSubtitle.setTitle(R.string.hide_subtitle);
        }
    }

    @TargetApi(11)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_new_crime:
                Crime crime = new Crime();
                CrimeLab.get(getActivity()).addCrime(crime);
                Intent i = new Intent(getActivity(), CrimeActivity.class);
                i.putExtra(CrimeFragment.EXTRA_CRIME_ID, crime.getId());
                startActivityForResult(i, 0);
                return true;
            case R.id.menu_item_show_subtitle:
            	if (getActivity().getActionBar().getSubtitle() == null) {
                    getActivity().getActionBar().setSubtitle(R.string.subtitle);
                    mSubtitleVisible = true;
                    item.setTitle(R.string.hide_subtitle);
            	}  else {
            		getActivity().getActionBar().setSubtitle(null);
            		 mSubtitleVisible = false;
            		item.setTitle(R.string.show_subtitle);
            	}
                return true;
            default:
                return super.onOptionsItemSelected(item);
        } 
    }

    /*
    Method to create a context menu.
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
        getActivity().getMenuInflater().inflate(R.menu.crime_list_item_context, menu);
    }

    /*
    Method that defines the context menu that prompts when an item from the list is pressed and held.
     */
    @Override
    public boolean onContextItemSelected(MenuItem item){
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int position = info.position;
        CrimeAdapter adapter = (CrimeAdapter)getListAdapter();
        Crime crime = adapter.getItem(position);

        switch (item.getItemId()){
            case R.id.menu_item_delete_crime:
                //Deleting crime's picture first if there is any
                if (crime.getPhoto() != null){
                    getActivity().deleteFile(crime.getPhoto().getFilename());
                }
                CrimeLab.get(getActivity()).deleteCrime(crime);
                adapter.notifyDataSetChanged();
                return true;
            /*case R.id.menu_item_modify_crime:
                //Crime c = ((CrimeAdapter)getListAdapter()).getItem(position);
                // start an instance of CrimePagerActivity
                Intent i = new Intent(getActivity(), CrimePagerActivity.class);
                i.putExtra(CrimeFragment.EXTRA_CRIME_ID, crime.getId());
                startActivityForResult(i, 0);
                adapter.notifyDataSetChanged();
                return true;*/
        }
        return super.onContextItemSelected(item);
    }

    private class CrimeAdapter extends ArrayAdapter<Crime> {
        public CrimeAdapter(ArrayList<Crime> crimes) {
            super(getActivity(), android.R.layout.simple_list_item_1, crimes);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // if we weren't given a view, inflate one
            if (null == convertView) {
                convertView = getActivity().getLayoutInflater()
                    .inflate(R.layout.list_item_crime, null);
            }

            // configure the view for this Crime
            Crime c = getItem(position);

            TextView titleTextView =
                (TextView)convertView.findViewById(R.id.crime_list_item_titleTextView);
            if (c.getTitle() == null || c.getTitle().equals("")){
                titleTextView.setText("Unnamed crime");
                c.setTitle("Unnamed crime");
            }else{
                titleTextView.setText(c.getTitle());
            }
            TextView dateTextView =
                (TextView)convertView.findViewById(R.id.crime_list_item_dateTextView);
            String dateFormat = "EEE, MMM dd, hh:mm aa";
            String dateString = android.text.format.DateFormat.format(dateFormat, c.getDate()).toString();
            dateTextView.setText(dateString);
            //dateTextView.setText(c.getDate().toString());
            CheckBox solvedCheckBox =
                (CheckBox)convertView.findViewById(R.id.crime_list_item_solvedCheckBox);
            solvedCheckBox.setChecked(c.isSolved());

            return convertView;
        }
    }

    /*public void onSaveInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean(SUBTITLE_SHOWN, mSubtitleVisible);
    }*/
}

