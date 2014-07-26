package com.frozendevs.periodictable.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.frozendevs.periodictable.R;
import com.frozendevs.periodictable.helper.Database;
import com.frozendevs.periodictable.model.ElementListItem;
import com.frozendevs.periodictable.model.adapter.ElementsAdapter;

import java.util.List;

public class ElementsFragment extends Fragment {

    private class LoadData extends AsyncTask<Void, Void, List<ElementListItem>> {

        private ListView mListView;

        private LoadData(ListView listView) {
            mListView = listView;
        }

        @Override
        protected List<ElementListItem> doInBackground(Void... params) {
            return Database.getInstance(getActivity()).getElementListItems();
        }

        @Override
        protected void onPostExecute(List<ElementListItem> result) {
            ((ElementsAdapter)mListView.getAdapter()).setData(result);

            mListView.setEmptyView(getActivity().findViewById(R.id.empty_elements_list));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.elements_list_fragment, container, false);

        ListView listView = (ListView)rootView.findViewById(R.id.elements_list);
        listView.setEmptyView(rootView.findViewById(R.id.progress_bar));
        listView.setAdapter(new ElementsAdapter(getActivity()));

        new LoadData(listView).execute();

        return rootView;
    }
}
