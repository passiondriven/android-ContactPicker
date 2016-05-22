/*
 * Copyright (C) 2015-2016 Emanuel Moecklin
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

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * All contacts have been loaded (including details).
 *
 * Publisher: ContactFragment
 * Subscriber: GroupFragment
 */
public class ContactsLoaded {

	public static void post(List<? extends Contact> contacts) {
		ContactsLoaded event = new ContactsLoaded(contacts);
		EventBus.getDefault().postSticky( event );
	}

	final private List<? extends Contact> mContacts;

	private ContactsLoaded(List<? extends Contact> contacts) {
		mContacts = contacts;
	}

	public List<? extends Contact> getContacts() {
		return mContacts;
	}

}
