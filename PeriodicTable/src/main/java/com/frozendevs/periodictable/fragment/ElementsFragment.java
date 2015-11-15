package com.frozendevs.periodictable.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.frozendevs.periodictable.R;
import com.frozendevs.periodictable.content.AsyncTaskLoader;
import com.frozendevs.periodictable.content.Database;
import com.frozendevs.periodictable.model.ElementListItem;
import com.frozendevs.periodictable.model.adapter.ElementsAdapter;
import com.frozendevs.periodictable.view.RecyclerView;
import com.frozendevs.periodictable.widget.DividerDecoration;

import java.util.List;

public class ElementsFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<List<ElementListItem>> {
    private static final String STATE_LIST_ITEMS = "listItems";
    private static final String STATE_SEARCH_QUERY = "searchQuery";

    private ElementsAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private View mEmptyView;

    private String mSearchQuery;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        mAdapter = new ElementsAdapter();

        if (savedInstanceState != null) {
            mAdapter.setItems((ElementListItem[]) savedInstanceState.getParcelableArray(
                    STATE_LIST_ITEMS));

            mSearchQuery = savedInstanceState.getString(STATE_SEARCH_QUERY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.elements_list_fragment, container, false);

        mEmptyView = rootView.findViewById(R.id.empty_elements_list);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.elements_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setEmptyView(mAdapter.getItemCount() > 0 ? mEmptyView :
                rootView.findViewById(R.id.progress_bar));
        mRecyclerView.addItemDecoration(new DividerDecoration(getActivity()));

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (mAdapter.getItemCount() == 0) {
            getLoaderManager().initLoader(R.id.elements_list_loader, null, this);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.elements_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);

        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setQueryHint(getString(R.string.search_query_hint));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mSearchQuery = query;

                searchView.clearFocus();

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (ViewCompat.isLaidOut(searchView)) {
                    mSearchQuery = newText;
                }

                mAdapter.filter(getActivity(), newText);

                return true;
            }
        });

        MenuItemCompat.setOnActionExpandListener(searchItem,
                new MenuItemCompat.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        if (mSearchQuery == null) {
                            mSearchQuery = "";
                        }

                        return true;
                    }

                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        mSearchQuery = null;

                        mAdapter.clearFilter();

                        return true;
                    }
                });

        if (mSearchQuery != null) {
            MenuItemCompat.expandActionView(searchItem);

            searchView.setQuery(mSearchQuery, false);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }



    @Override
    public Loader<List<ElementListItem>> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<List<ElementListItem>>(getActivity()) {
            @Override
            public List<ElementListItem> loadInBackground() {
                return Database.getAllElements(getContext(), ElementListItem.class);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<List<ElementListItem>> loader, List<ElementListItem> data) {
        mAdapter.setItems(data);

        mAdapter.notifyDataSetChanged();

        mRecyclerView.setEmptyView(mEmptyView);
    }

    @Override
    public void onLoaderReset(Loader<List<ElementListItem>> loader) {
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArray(STATE_LIST_ITEMS, mAdapter.getItems());
        outState.putString(STATE_SEARCH_QUERY, mSearchQuery);
    }
}
