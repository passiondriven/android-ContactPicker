/*
 * Copyright (C) 2015-2023 Emanuel Moecklin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.onegravity.contactpicker.contact;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.onegravity.contactpicker.BaseFragment;
import com.onegravity.contactpicker.R;
import com.onegravity.contactpicker.picture.ContactPictureType;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import xyz.danoz.recyclerviewfastscroller.sectionindicator.title.SectionTitleIndicator;
import xyz.danoz.recyclerviewfastscroller.vertical.VerticalRecyclerViewFastScroller;

public class ContactFragment extends BaseFragment {

    private static final String REQUEST_SORT_ORDER = "sortOrder";
    private static final String REQUEST_PICTURE_TYPE = "pictureType";
    private static final String REQUEST_CONTACT_DESCRIPTION = "contactDescription";
    private static final String REQUEST_DESCRIPTION_TYPE = "descriptionType";

    private ContactSortOrder mSortOrder;
    private ContactPictureType mPictureType;
    private ContactDescription mDescription;
    private int mDescriptionType;

    /**
     * The list of all contacts.
     * This is only used as a reference to the original data set while we actually use
     * mFilteredContacts.
     */
    private List<? extends Contact> mContacts = new ArrayList<>();

    /**
     * The list of all visible and filtered contacts.
     */
    private List<? extends Contact> mFilteredContacts = new ArrayList<>();

    private ContactAdapter mAdapter;

    public static ContactFragment newInstance(ContactSortOrder sortOrder,
                                              ContactPictureType pictureType,
                                              ContactDescription contactDescription,
                                              int descriptionType) {
        Bundle args = new Bundle();
        args.putString(REQUEST_SORT_ORDER, sortOrder.name());
        args.putString(REQUEST_PICTURE_TYPE, pictureType.name());
        args.putString(REQUEST_CONTACT_DESCRIPTION, contactDescription.name());
        args.putInt(REQUEST_DESCRIPTION_TYPE, descriptionType);
        ContactFragment fragment = new ContactFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public ContactFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        mSortOrder = ContactSortOrder.lookup( args.getString(REQUEST_SORT_ORDER) );
        mPictureType = ContactPictureType.lookup( args.getString(REQUEST_PICTURE_TYPE) );
        mDescription = ContactDescription.lookup( args.getString(REQUEST_CONTACT_DESCRIPTION) );
        mDescriptionType = args.getInt(REQUEST_DESCRIPTION_TYPE);
    }

    @Override
    public final View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mAdapter = new ContactAdapter(getContext(), null, mSortOrder, mPictureType, mDescription, mDescriptionType);

        View rootLayout = super.createView(inflater, R.layout.cp_contact_list, mAdapter, mContacts);

        // configure fast scroll
        RecyclerView recyclerView = rootLayout.findViewById(android.R.id.list);
        VerticalRecyclerViewFastScroller fastScroller = rootLayout.findViewById(R.id.fast_scroller);
        fastScroller.setRecyclerView(recyclerView);
        recyclerView.addOnScrollListener(fastScroller.getOnScrollListener());

        // configure section indexer
        SectionTitleIndicator sectionTitleIndicator = rootLayout.findViewById(R.id.fast_scroller_section_title_indicator);
        fastScroller.setSectionIndicator(sectionTitleIndicator);

        return rootLayout;
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ContactsLoaded event) {
        EventBus.getDefault().removeStickyEvent(event);

        mContacts = event.getContacts();
        mFilteredContacts = mContacts;
        mAdapter.setData(mFilteredContacts);

        updateEmptyViewVisibility(mContacts);
    }

    @Override
    protected void checkAll() {
        if (mFilteredContacts == null) return;

        // determine if all contacts are checked
        boolean allChecked = true;
        for (Contact contact : mFilteredContacts) {
            if (! contact.isChecked()) {
                allChecked = false;
                break;
            }
        }

        // if all are checked then un-check the contacts, otherwise check them all
        boolean isChecked = ! allChecked;
        for (Contact contact : mFilteredContacts) {
            if (contact.isChecked() != isChecked) {
                contact.setChecked(isChecked, true);

            }
        }

        ContactSelectionChanged.post();

        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void performFiltering(String[] queryStrings) {
        if (mContacts == null) return;

        if (queryStrings == null || queryStrings.length == 0) {
            mFilteredContacts = mContacts;
        }
        else {
            List<Contact> filteredElements = new ArrayList<>();
            for (Contact contact : mContacts) {
                if (contact.matchesQuery(queryStrings)) {
                    filteredElements.add(contact);
                }
            }
            mFilteredContacts = filteredElements;
        }

        mAdapter.setData(mFilteredContacts);
    }

}
