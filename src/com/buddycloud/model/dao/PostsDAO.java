package com.buddycloud.model.dao;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.buddycloud.model.db.BuddycloudSQLiteOpenHelper;
import com.buddycloud.model.db.PostsTableHelper;

public class PostsDAO implements DAO<JSONObject, JSONArray> {
	
	private static final int DEF_LIMIT = 30;

	private static PostsDAO instance;
	
	private SQLiteDatabase db;
	private BuddycloudSQLiteOpenHelper helper;
	
	
	private PostsDAO(Context context) {
		this.helper = new BuddycloudSQLiteOpenHelper(context);
		this.db = helper.getWritableDatabase();
	}
	
	
	public static PostsDAO getInstance(Context context) {
		if (instance == null) {
			instance = new PostsDAO(context);
		}
		
		return instance;
	}

	
	private ContentValues buildValues(String id, String author, String published, String updated, 
			String content, String channel, String replyTo, String media) {
		
		ContentValues values = new ContentValues();
		values.put(PostsTableHelper.COLUMN_ID, id);
		values.put(PostsTableHelper.COLUMN_AUTHOR, author);
		values.put(PostsTableHelper.COLUMN_PUBLISHED, published);
		values.put(PostsTableHelper.COLUMN_UPDATED, updated);
		values.put(PostsTableHelper.COLUMN_CONTENT, content);
		values.put(PostsTableHelper.COLUMN_CHANNEL, channel);
		
		if (replyTo != null && !replyTo.equals("")) {
			values.put(PostsTableHelper.COLUMN_REPLY_TO, replyTo);
		}
		
		if (media != null && !media.equals("")) {
			values.put(PostsTableHelper.COLUMN_MEDIA, media);
		}
		
		return values;
	}
	
	private ContentValues buildValues(String channel, JSONObject json) {
		String id = json.optString("id");
		String author = json.optString("author");
		String published = json.optString("published");
		String updated = json.optString("updated");
		String content = json.isNull("content") ? null : json.optString("content");
		String replyTo = json.isNull("replyTo") ? null : json.optString("replyTo");
		String mediaId = json.isNull("media") ? null : json.optJSONArray("media").toString();
		
		return buildValues(id, author, published, updated, content, channel, replyTo, mediaId);
	}
	
	public boolean insert(String channel, JSONObject json) {
		ContentValues values = buildValues(channel, json);
		if (values != null) {
			long rowId = db.insert(PostsTableHelper.TABLE_NAME, null, values);
			return rowId != -1;
		}
		
		return false;
	}
	
	public boolean update(String channel, JSONObject json) {
		ContentValues values = buildValues(channel, json);
		
		if (values != null) {
			String filter = PostsTableHelper.COLUMN_CHANNEL + "=\"" + channel + "\"" + " AND " +
							PostsTableHelper.COLUMN_ID + "=\"" + values.getAsString(PostsTableHelper.COLUMN_ID) + "\"";
			int rowsAffected = db.update(PostsTableHelper.TABLE_NAME, 
					values, filter, null);
			return rowsAffected == 1;
		}
		
		return false;
	}
	
	public JSONArray get(String channel) {
		return get(channel, DEF_LIMIT);
	}

	public JSONObject get(String channel, String itemId) {
		String filter = PostsTableHelper.COLUMN_CHANNEL + "=\"" + channel + "\" AND " 
						+ PostsTableHelper.COLUMN_ID + "=\"" + itemId + "\"";
		return DAOHelper.queryUniqueOnSameThread(db, false, PostsTableHelper.TABLE_NAME, null, filter,
				null, null, null, null, null, cursorParser());
	}

	public int delete(String channel, String itemId) {
		String filter = PostsTableHelper.COLUMN_CHANNEL + "=\"" + channel + "\" AND " 
						+ PostsTableHelper.COLUMN_ID + "=\"" + itemId + "\"";
		return DAOHelper.deleteOnSameThread(db, PostsTableHelper.TABLE_NAME, filter, null);
	}
	
	public JSONArray getReplies(String channel, String itemId) {
		String filter = PostsTableHelper.COLUMN_CHANNEL + "=\"" + channel + "\" AND " 
						+ PostsTableHelper.COLUMN_REPLY_TO + "=\"" + itemId + "\"";
		String orderBy = "datetime(" + PostsTableHelper.COLUMN_UPDATED + ") ASC";
		return DAOHelper.queryOnSameThread(db, false, PostsTableHelper.TABLE_NAME, null, filter,
				null, null, null, orderBy, null, cursorParser());
	}

	public JSONArray get(String channel, String after, int limit) {
		String filter = PostsTableHelper.COLUMN_CHANNEL + "=\"" + channel + "\"";
		filter += " AND " + PostsTableHelper.COLUMN_REPLY_TO + " IS NULL";
		if (after != null) {
			filter += " AND " + PostsTableHelper.COLUMN_UPDATED + "<\"" + after + "\"";
		}
		String orderBy = "datetime(" + PostsTableHelper.COLUMN_UPDATED + ") DESC";

		return DAOHelper.queryOnSameThread(db, false, PostsTableHelper.TABLE_NAME, null, filter,
				null, null, null, orderBy, String.valueOf(limit), 
				cursorParser());
	}
	
	public JSONObject getLatest(String channel) {
		String filter = PostsTableHelper.COLUMN_CHANNEL + "=\"" + channel + "\" " +
				"AND " + PostsTableHelper.COLUMN_PUBLISHED + " <> ''";
		String orderBy = "datetime(" + PostsTableHelper.COLUMN_UPDATED + ") DESC";
		return DAOHelper.queryUniqueOnSameThread(db, false, PostsTableHelper.TABLE_NAME, null, filter,
				null, null, null, orderBy, String.valueOf(1), 
				cursorParser());
	}
	
	public Map<String, JSONArray> getPending() {
		String filter = PostsTableHelper.COLUMN_PUBLISHED + " = ''";
		return DAOHelper.queryCollectionMapOnSameThread(db, false, PostsTableHelper.TABLE_NAME, null, filter,
				null, null, null, null, null, cursorParser(), PostsTableHelper.COLUMN_CHANNEL);
	}
	
	public JSONArray get(String channel, int limit) {
		return get(channel, null, limit);
	}

	private DAOCursorParser cursorParser() {
		DAOCursorParser cursorParser = new DAOCursorParser() {
			@Override
			public JSONObject parse(Cursor c) {
				return cursorToJSON(c);
			}
		};
		return cursorParser;
	}
	
	private JSONObject cursorToJSON(Cursor cursor) {
		JSONObject json = new JSONObject();
		try {
			json.put("id", getString(cursor, PostsTableHelper.COLUMN_ID));
			json.put(PostsTableHelper.COLUMN_AUTHOR, getString(cursor, PostsTableHelper.COLUMN_AUTHOR));
			json.put(PostsTableHelper.COLUMN_PUBLISHED, getString(cursor, PostsTableHelper.COLUMN_PUBLISHED));
			json.put(PostsTableHelper.COLUMN_UPDATED, getString(cursor, PostsTableHelper.COLUMN_UPDATED));
			json.put(PostsTableHelper.COLUMN_CHANNEL, getString(cursor, PostsTableHelper.COLUMN_CHANNEL));
			json.put(PostsTableHelper.COLUMN_CONTENT, getString(cursor, PostsTableHelper.COLUMN_CONTENT));
			json.put(PostsTableHelper.COLUMN_REPLY_TO, getString(cursor, PostsTableHelper.COLUMN_REPLY_TO));
			json.put(PostsTableHelper.COLUMN_MEDIA, getString(cursor, PostsTableHelper.COLUMN_MEDIA));
		} catch (JSONException e) {
			return null;
		}
		
		return json;
	}
	
	private String getString(Cursor cursor, String columnName) {
		return cursor.getString(cursor.getColumnIndex(columnName));
	}


	@Override
	public Map<String, JSONArray> getAll() {
		return null;
	}

}
