package com.eecs3311.profilemicroservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.*;

import org.springframework.stereotype.Repository;
import org.neo4j.driver.v1.Transaction;

@Repository
public class ProfileDriverImpl implements ProfileDriver {

	Driver driver = ProfileMicroserviceApplication.driver;

	public static void InitProfileDb() {
		String queryStr;

		try (Session session = ProfileMicroserviceApplication.driver.session()) {
			try (Transaction trans = session.beginTransaction()) {
				queryStr = "CREATE CONSTRAINT ON (nProfile:profile) ASSERT exists(nProfile.userName)";
				trans.run(queryStr);

				queryStr = "CREATE CONSTRAINT ON (nProfile:profile) ASSERT exists(nProfile.password)";
				trans.run(queryStr);

				queryStr = "CREATE CONSTRAINT ON (nProfile:profile) ASSERT nProfile.userName IS UNIQUE";
				trans.run(queryStr);

				trans.success();
			} catch (Exception e) {
				if (e.getMessage().contains("An equivalent constraint already exists")) {
					System.out.println("INFO: Profile constraints already exist (DB likely already initialized), should be OK to continue");
				} else {
					// something else, yuck, bye
					throw e;
				}
			}
			session.close();
		}
	}
	
	@Override
	public DbQueryStatus createUserProfile(String userName, String fullName, String password) {

		try (Session session = driver.session()) {
			return session.writeTransaction(tx -> {
				// to check if the userName already exists
				String checkUserQuery = "MATCH (nProfile:profile {userName: $userName}) RETURN nProfile";
				StatementResult result = tx.run(checkUserQuery, Values.parameters("userName", userName));

				if (result.hasNext()) {
					// returns if the user with the same userName already exists
					return new DbQueryStatus("User profile with the given userName already exists", DbQueryExecResult.QUERY_ERROR_GENERIC);
				}

				// else creates a new user profile
				String createUserQuery = "CREATE (nProfile:profile {userName: $userName, fullName: $fullName, password: $password})";
				tx.run(createUserQuery, Values.parameters("userName", userName, "fullName", fullName, "password", password));

				return new DbQueryStatus("User profile created successfully", DbQueryExecResult.QUERY_OK);
			});
		} catch (Exception e) {
			return new DbQueryStatus("Error creating user profile", DbQueryExecResult.QUERY_ERROR_GENERIC);
		}

	}

	@Override
	public DbQueryStatus followFriend(String userName, String frndUserName) {

		try (Session session = driver.session()) {
			return session.writeTransaction(tx -> {
				// to check if the user exists
				String checkUserQuery = "MATCH (nProfile:profile {userName: $userName}) RETURN nProfile";
				StatementResult result = tx.run(checkUserQuery, Values.parameters("userName", userName, "frndUserName", frndUserName));

				// executes if the username does not exist
				if (!result.hasNext()) {
					return new DbQueryStatus("User profile with the given userName does not exist", DbQueryExecResult.QUERY_ERROR_GENERIC);
				}

				String followFriendQuery = "MATCH (u:profile {userName: $userName}), (f:profile {userName: $frndUserName}) MERGE (u)-[:FOLLOWS]->(f)";
				tx.run(followFriendQuery, Values.parameters("userName", userName, "frndUserName", frndUserName));

				// returns OK status if it was successful
				return new DbQueryStatus("Friendship created successfully", DbQueryExecResult.QUERY_OK);
			});
		} catch (Exception e) {
			return new DbQueryStatus("Error creating friendship", DbQueryExecResult.QUERY_ERROR_GENERIC);
		}

	}

	@Override
	public DbQueryStatus unfollowFriend(String userName, String frndUserName) {

		try (Session session = driver.session()) {
			return session.writeTransaction(tx -> {
				// checks if the username exists
				String checkUserQuery = "MATCH (nProfile:profile {userName: $userName}) RETURN nProfile";
				StatementResult result = tx.run(checkUserQuery, Values.parameters("userName", userName, "frndUserName", frndUserName));

				// returns if the userName does not exist
				if (!result.hasNext()) {
					return new DbQueryStatus("User profile with the given userName does not exist", DbQueryExecResult.QUERY_ERROR_GENERIC);
				}

				String unfollowFriendQuery = "MATCH (u:profile {userName: $userName})-[r:FOLLOWS]->(f:profile {userName: $frndUserName}) DELETE r";
				tx.run(unfollowFriendQuery, Values.parameters("userName", userName, "frndUserName", frndUserName));

				// returns Ok status if it works
				return new DbQueryStatus("Friendship deleted successfully", DbQueryExecResult.QUERY_OK);
			});
		} catch (Exception e) {
			return new DbQueryStatus("Error deleting friendship", DbQueryExecResult.QUERY_ERROR_GENERIC);
		}
	}

	@Override
	public DbQueryStatus getAllSongFriendsLike(String userName) {



		return null;

	}
}
