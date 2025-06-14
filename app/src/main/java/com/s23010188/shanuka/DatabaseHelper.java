package com.s23010188.shanuka; // REPLACE WITH YOUR ACTUAL PACKAGE NAME

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * DatabaseHelper class to manage SQLite database operations for user authentication.
 * This class extends SQLiteOpenHelper to provide methods for creating, upgrading,
 * and interacting with the local SQLite database.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // Database constants
    private static final String DATABASE_NAME = "LoginDB";
    private static final int DATABASE_VERSION = 1;

    // Table and column names
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";

    // SQL query to create the users table
    private static final String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_USERNAME + " TEXT UNIQUE," // Username must be unique
            + COLUMN_PASSWORD + " TEXT" + ")"; // Password stored as hashed value

    /**
     * Constructor for DatabaseHelper.
     *
     * @param context The context of the application (e.g., MainActivity.this).
     */
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Called when the database is created for the first time.
     * This is where you create tables.
     *
     * @param db The database.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Execute the SQL statement to create the users table
        db.execSQL(CREATE_USERS_TABLE);
    }

    /**
     * Called when the database needs to be upgraded.
     * This method is called when the database version changes.
     *
     * @param db         The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop the old table if it exists
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        // Create a new table
        onCreate(db);
    }

    /**
     * Hashes a password using SHA-256.
     *
     * @param password The plain text password to hash.
     * @return The hashed password as a hexadecimal string, or null if hashing fails.
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Checks if a username already exists in the database.
     *
     * @param username The username to check.
     * @return true if the username exists, false otherwise.
     */
    public boolean doesUsernameExist(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        boolean exists = false;

        try {
            String[] columns = {COLUMN_ID};
            String selection = COLUMN_USERNAME + " = ?";
            String[] selectionArgs = {username};

            cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
            exists = cursor != null && cursor.moveToFirst();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return exists;
    }

    /**
     * Adds a new user (username and hashed password) to the database.
     *
     * @param username The username to add.
     * @param password The plain text password to hash and store.
     * @return true if the user was added successfully, false if the username already exists or insertion fails.
     */
    public boolean addUser(String username, String password) {
        // Check if username already exists
        if (doesUsernameExist(username)) {
            return false; // Username already taken
        }

        // Hash the password
        String hashedPassword = hashPassword(password);
        if (hashedPassword == null) {
            return false; // Hashing failed
        }

        // Get writable database instance
        SQLiteDatabase db = this.getWritableDatabase();
        // ContentValues object to store key-value pairs
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, hashedPassword);

        // Insert the row
        long result = db.insert(TABLE_USERS, null, values);
        // Close the database connection
        db.close();

        // If result is -1, insertion failed
        return result != -1;
    }

    /**
     * Checks if a user with the given username and password exists in the database.
     *
     * @param username The username to check.
     * @param password The plain text password to hash and check.
     * @return true if the user exists and credentials match, false otherwise.
     */
    public boolean checkUser(String username, String password) {
        // Hash the input password
        String hashedPassword = hashPassword(password);
        if (hashedPassword == null) {
            return false; // Hashing failed
        }

        // Get readable database instance
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        boolean userExists = false;

        try {
            // Define the columns to retrieve
            String[] columns = {COLUMN_ID};
            // Define the selection criteria (WHERE clause)
            String selection = COLUMN_USERNAME + " = ? AND " + COLUMN_PASSWORD + " = ?";
            // Define the selection arguments (values for the WHERE clause)
            String[] selectionArgs = {username, hashedPassword};

            // Query the database
            cursor = db.query(TABLE_USERS, // Table to query
                    columns,            // Columns to return
                    selection,          // Columns for the WHERE clause
                    selectionArgs,      // Values for the WHERE clause
                    null,               // Group by
                    null,               // Having
                    null);              // Order by

            // If a row is found, the user exists
            if (cursor != null && cursor.moveToFirst()) {
                userExists = true;
            }
        } finally {
            // Always close the cursor and database after use
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return userExists;
    }
}