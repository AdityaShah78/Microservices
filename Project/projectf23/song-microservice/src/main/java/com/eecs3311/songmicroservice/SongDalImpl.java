package com.eecs3311.songmicroservice;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class SongDalImpl implements SongDal {

	private final MongoTemplate db;

	@Autowired
	public SongDalImpl(MongoTemplate mongoTemplate) {
		this.db = mongoTemplate;
	}

	@Override
	public DbQueryStatus addSong(Song songToAdd) {

		try {
			// Checks if the song already exists
			Query query = new Query(Criteria.where(Song.KEY_SONG_NAME).is(songToAdd.getSongName())
					.and(Song.KEY_SONG_ARTIST_FULL_NAME).is(songToAdd.getSongArtistFullName()));
			Song existingSong = db.findOne(query, Song.class);

			// returns null if the song already exists
			if (existingSong != null) {
				return new DbQueryStatus("Song already exists", DbQueryExecResult.QUERY_ERROR_GENERIC);
			}

			// If the song doesn't exist then inserts it
			db.insert(songToAdd);

			// Returns an OK status
			return new DbQueryStatus(songToAdd.toString(), DbQueryExecResult.QUERY_OK);
		} catch (Exception e) {
			return new DbQueryStatus("Error adding song to the database", DbQueryExecResult.QUERY_ERROR_GENERIC);
		}

	}

	@Override
	public DbQueryStatus findSongById(String songId) {

		try {
			// Creates a query to find the song by its ID
			Query query = new Query(Criteria.where("_id").is(new ObjectId(songId)));

			// finds the song in the db
			Song foundSong = db.findOne(query, Song.class);

			if (foundSong != null) {
				// If the song is found then returns OK status with the song
				return new DbQueryStatus(foundSong.toString(), DbQueryExecResult.QUERY_OK);
			} else {
				// If the song doesnt exist then returns this
				return new DbQueryStatus("Song not found", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
			}
		} catch (Exception e) {
			return new DbQueryStatus("Error finding song in the database", DbQueryExecResult.QUERY_ERROR_GENERIC);
		}

	}

	@Override
	public DbQueryStatus getSongTitleById(String songId) {
		try {
			// Creates a query to find the song by its ID
			Query query = new Query(Criteria.where("_id").is(new ObjectId(songId)));

			// finds the song in the db
			Song foundSong = db.findOne(query, Song.class);

			if (foundSong != null) {
				// If the song is found then returns OK status
				return new DbQueryStatus(foundSong.getSongName(), DbQueryExecResult.QUERY_OK);
			} else {
				// If the song doesnt exist then return this
				return new DbQueryStatus("Song not found", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
			}
		} catch (Exception e) {
			return new DbQueryStatus("Error finding song in the database", DbQueryExecResult.QUERY_ERROR_GENERIC);
		}
	}

	@Override
	public DbQueryStatus deleteSongById(String songId) {
		try {
			// Creates a query to find the song by its ID
			Query query = new Query(Criteria.where("_id").is(new ObjectId(songId)));

			// finds and deletes song from db
			Song deletedSong = db.findAndRemove(query, Song.class);

			if (deletedSong != null) {
				// If the song is found and deleted then returns OK status
				return new DbQueryStatus("Song deleted successfully", DbQueryExecResult.QUERY_OK);
			} else {
				// If the song does not exist then return this
				return new DbQueryStatus("Song not found", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
			}
		} catch (Exception e) {
			return new DbQueryStatus("Error deleting song from the database", DbQueryExecResult.QUERY_ERROR_GENERIC);
		}
	}

	@Override
	public DbQueryStatus updateSongFavouritesCount(String songId, boolean shouldDecrement) {

		try {
			// Creates a query to find the song by its ID
			Query query = new Query(Criteria.where("_id").is(new ObjectId(songId)));

			// finds the song in db
			Song foundSong = db.findOne(query, Song.class);

			if (foundSong != null) {
				// runs if song is found
				long count = foundSong.getSongAmountFavourites();

				// executes if the count is 0 because it cannot be negative
				if(count == 0 && shouldDecrement){
					return new DbQueryStatus("Song favourites count cannot be negative", DbQueryExecResult.QUERY_ERROR_GENERIC);
				}

				if(shouldDecrement == true){
					count = count - 1;
				}

				else{
					count = count + 1;
				}

				// Updates the song amount favourites
				foundSong.setSongAmountFavourites(count);
				db.save(foundSong);

				// returns OK status if the favorites count has been updated successfully
				return new DbQueryStatus("Song favorites count updated successfully", DbQueryExecResult.QUERY_OK);
			} else {
				// If the song does not exist then return this
				return new DbQueryStatus("Song not found", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
			}
		} catch (Exception e) {
			return new DbQueryStatus("Error updating song favorites count", DbQueryExecResult.QUERY_ERROR_GENERIC);
		}

	}
}