/**
 * Notifry for Android.
 * 
 * Copyright 2011 Daniel Foote
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.notifry.android;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.notifry.android.database.NotifryMessage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MessageDetail extends SherlockActivity
{
	private final static int BACK_TO_LIST = 1;
	private NotifryMessage message = null;

	/** Called when the activity is first created. */
	public void onCreate( Bundle savedInstanceState )
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.screen_message_detail);
	}
	
	public void onResume()
	{
		super.onResume();
		
		this.loadFromMessage(this.getMessage());
		
		// Clear the notification.
		Intent intentData = new Intent(getBaseContext(), NotificationService.class);
		intentData.putExtra("operation", "update");
		intentData.putExtra("sourceId", this.getMessage().getSource().getId());
		startService(intentData);		
	}
	
	/**
	 * Load this activity from the given message.
	 * @param message
	 */
	public void loadFromMessage( NotifryMessage message )
	{
		TextView title = (TextView) findViewById(R.id.message_detail_title);
		title.setText(message.getTitle());

		TextView source = (TextView) findViewById(R.id.message_detail_source);
		source.setText(message.getSource().getTitle());

		TextView timestamp = (TextView) findViewById(R.id.message_detail_timestamp);
		timestamp.setText(message.getDisplayTimestamp());

		TextView url = (TextView) findViewById(R.id.message_detail_url);
		if( message.getUrl() != null )
		{
			url.setVisibility(View.VISIBLE);
			url.setText(message.getUrl());
		}
		else
		{
			url.setVisibility(View.GONE);
			url.setText("No URL provided.");
		}

		TextView content = (TextView) findViewById(R.id.message_detail_content);
		content.setText(message.getMessage());
	}
	
	@Override
	public boolean onCreateOptionsMenu( Menu menu )
	{
		boolean result = super.onCreateOptionsMenu(menu);
		menu.add(0, BACK_TO_LIST, 0, R.string.message_list).setIcon(android.R.drawable.ic_menu_agenda);
		return result;
	}

	@Override
	public boolean onOptionsItemSelected( MenuItem item )
	{
		switch( item.getItemId() )
		{
			case android.R.id.home:			
			case BACK_TO_LIST:
				Intent intent = new Intent(this, MessageList.class);
				intent.putExtra("sourceId", getMessage().getSource().getId());
				startActivity(intent);
				return true;
		}

		return super.onOptionsItemSelected(item);
	}	

	/**
	 * Fetch the message.
	 * 
	 * @return
	 */
	public NotifryMessage getMessage()
	{
		if( this.message == null )
		{
			// Get the message from the intent.
			// We store it in a private variable to save us having to query the
			// DB each time.
			Intent sourceIntent = getIntent();
			this.message = NotifryMessage.FACTORY.get(this, sourceIntent.getLongExtra("messageId", 0));
			
			if( this.message == null )
			{
				// Ie, we tried to load it but it's been deleted.
				// This can happen if you're going backwards through the
				// activity stack and a later activity was used to delete all messages.
				// In this case, just exit this activity.
				this.finish();
			}

			// Change the seen flag if required.
			if( this.message.getSeen() == false )
			{
				this.message.setSeen(true);
				this.message.save(this);
			}
		}

		return this.message;
	}
}
