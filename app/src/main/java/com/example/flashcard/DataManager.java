package com.example.flashcard;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataManager {

    private static final String TAG = "DataManager";
    private static List<Category> allCategories = new ArrayList<>();
    private static final Map<String, String> CATEGORY_VN_NAMES = initializeCategoryNames();

    // --- SESSION ---
    private static final String PREF_NAME = "FlashCardPref";
    private static final String KEY_CURRENT_EMAIL = "current_email";

    public static void saveCurrentLogin(Context context, String email) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_CURRENT_EMAIL, email).apply();
    }

    public static String getCurrentEmail(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_CURRENT_EMAIL, null);
    }

    public static void logout(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().remove(KEY_CURRENT_EMAIL).apply();
    }

    // ==========================================
    // 1. DATABASE HELPER
    // ==========================================
    private static class LocalDbHelper extends SQLiteOpenHelper {
        private static final String DB_NAME = "flashcard_internal.db";
        private static final int DB_VERSION = 4;

        static final String TABLE_CAT = "categories";
        static final String COL_CAT_ID = "id";
        static final String COL_CAT_NAME = "name";

        static final String TABLE_WORD = "words";
        static final String COL_WORD_ID = "id";
        static final String COL_WORD_CAT_ID = "category_id";
        static final String COL_WORD_ENGLISH = "english_word";
        static final String COL_WORD_VN = "vietnamese_meaning";
        static final String COL_WORD_DEF = "definition";
        static final String COL_WORD_LEARNED = "is_learned";
        static final String COL_WORD_IMAGE = "image_url";

        static final String TABLE_USER = "users";
        static final String COL_USER_ID = "id";
        static final String COL_USER_EMAIL = "email";
        static final String COL_USER_PASS = "password";
        static final String COL_USER_NAME = "display_name";

        LocalDbHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLE_CAT + " (" + COL_CAT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COL_CAT_NAME + " TEXT)");
            db.execSQL("CREATE TABLE " + TABLE_WORD + " (" + COL_WORD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COL_WORD_CAT_ID + " INTEGER, " + COL_WORD_ENGLISH + " TEXT, " + COL_WORD_VN + " TEXT, " + COL_WORD_DEF + " TEXT, " + COL_WORD_LEARNED + " INTEGER DEFAULT 0, " + COL_WORD_IMAGE + " TEXT, FOREIGN KEY(" + COL_WORD_CAT_ID + ") REFERENCES " + TABLE_CAT + "(" + COL_CAT_ID + "))");
            db.execSQL("CREATE TABLE " + TABLE_USER + " (" + COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COL_USER_EMAIL + " TEXT UNIQUE, " + COL_USER_PASS + " TEXT, " + COL_USER_NAME + " TEXT)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion < 3) try { db.execSQL("ALTER TABLE " + TABLE_USER + " ADD COLUMN " + COL_USER_NAME + " TEXT"); } catch (Exception e) {}
            if (oldVersion < 4) try { db.execSQL("ALTER TABLE " + TABLE_WORD + " ADD COLUMN " + COL_WORD_IMAGE + " TEXT"); } catch (Exception e) {}
            if (oldVersion > newVersion) {
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_WORD);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_CAT);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
                onCreate(db);
            }
        }
    }

    // ==========================================
    // 2. USER STATISTICS
    // ==========================================
    public static String getUserDisplayName(Context context) {
        String email = getCurrentEmail(context);
        if (email == null) return "Người dùng";
        LocalDbHelper dbHelper = new LocalDbHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String name = "";
        try {
            Cursor cursor = db.query(LocalDbHelper.TABLE_USER, new String[]{LocalDbHelper.COL_USER_NAME}, LocalDbHelper.COL_USER_EMAIL + " = ?", new String[]{email}, null, null, null);
            if (cursor.moveToFirst()) name = cursor.getString(0);
            cursor.close();
        } catch (Exception e) { e.printStackTrace(); }
        db.close();
        return (name == null || name.isEmpty()) ? email : name;
    }

    public static void updateDisplayName(Context context, String newName) {
        String email = getCurrentEmail(context);
        if (email == null) return;
        LocalDbHelper dbHelper = new LocalDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(LocalDbHelper.COL_USER_NAME, newName);
        db.update(LocalDbHelper.TABLE_USER, values, LocalDbHelper.COL_USER_EMAIL + " = ?", new String[]{email});
        db.close();
    }

    public static int[] getLearningStats(Context context) {
        LocalDbHelper dbHelper = new LocalDbHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int total = 0, learned = 0;
        try {
            Cursor cTotal = db.rawQuery("SELECT COUNT(*) FROM " + LocalDbHelper.TABLE_WORD, null);
            if (cTotal.moveToFirst()) total = cTotal.getInt(0);
            cTotal.close();
            Cursor cLearned = db.rawQuery("SELECT COUNT(*) FROM " + LocalDbHelper.TABLE_WORD + " WHERE " + LocalDbHelper.COL_WORD_LEARNED + " = 1", null);
            if (cLearned.moveToFirst()) learned = cLearned.getInt(0);
            cLearned.close();
        } catch (Exception e) { e.printStackTrace(); }
        db.close();
        return new int[]{learned, total};
    }

    // ==========================================
    // 3. AUTHENTICATION
    // ==========================================
    public static boolean registerUser(Context context, String email, String password) {
        LocalDbHelper dbHelper = new LocalDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query(LocalDbHelper.TABLE_USER, null, LocalDbHelper.COL_USER_EMAIL + " = ?", new String[]{email}, null, null, null);
        if (cursor.getCount() > 0) { cursor.close(); db.close(); return false; }
        cursor.close();
        ContentValues values = new ContentValues();
        values.put(LocalDbHelper.COL_USER_EMAIL, email);
        values.put(LocalDbHelper.COL_USER_PASS, password);
        values.put(LocalDbHelper.COL_USER_NAME, "Người dùng mới");
        long result = db.insert(LocalDbHelper.TABLE_USER, null, values);
        db.close();
        if (result != -1) saveCurrentLogin(context, email);
        return result != -1;
    }

    public static boolean checkLogin(Context context, String email, String password) {
        LocalDbHelper dbHelper = new LocalDbHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(LocalDbHelper.TABLE_USER, null, LocalDbHelper.COL_USER_EMAIL + " = ? AND " + LocalDbHelper.COL_USER_PASS + " = ?", new String[]{email, password}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        if (exists) saveCurrentLogin(context, email);
        return exists;
    }

    public static boolean loginOrRegisterWithGoogle(Context context, String googleEmail) {
        saveCurrentLogin(context, googleEmail);
        LocalDbHelper dbHelper = new LocalDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query(LocalDbHelper.TABLE_USER, null, LocalDbHelper.COL_USER_EMAIL + " = ?", new String[]{googleEmail}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        if (exists) { db.close(); return true; }
        else {
            ContentValues values = new ContentValues();
            values.put(LocalDbHelper.COL_USER_EMAIL, googleEmail);
            values.put(LocalDbHelper.COL_USER_PASS, "GOOGLE_AUTH");
            values.put(LocalDbHelper.COL_USER_NAME, "Google User");
            long res = db.insert(LocalDbHelper.TABLE_USER, null, values);
            db.close();
            return res != -1;
        }
    }

    // ==========================================
    // 4. CHỨC NĂNG CHÍNH: CRUD TỪ VỰNG
    // ==========================================

    // --- A. THÊM TỪ MỚI (INSERT) ---
    public static boolean addNewWord(Context context, String categoryName, Word newWord) {
        newWord.setLearned(false); // Mặc định chưa thuộc

        LocalDbHelper dbHelper = new LocalDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int categoryId = -1;
        Cursor cursor = db.query(LocalDbHelper.TABLE_CAT, new String[]{LocalDbHelper.COL_CAT_ID}, LocalDbHelper.COL_CAT_NAME + " = ?", new String[]{categoryName}, null, null, null);
        if (cursor.moveToFirst()) categoryId = cursor.getInt(0);
        cursor.close();

        if (categoryId == -1) { db.close(); return false; }

        ContentValues values = new ContentValues();
        values.put(LocalDbHelper.COL_WORD_CAT_ID, categoryId);
        values.put(LocalDbHelper.COL_WORD_ENGLISH, newWord.getEnglishWord());
        values.put(LocalDbHelper.COL_WORD_VN, newWord.getVietnameseMeaning());
        values.put(LocalDbHelper.COL_WORD_DEF, newWord.getDefinition());
        values.put(LocalDbHelper.COL_WORD_IMAGE, newWord.getImageUrl());
        values.put(LocalDbHelper.COL_WORD_LEARNED, 0); // Lưu là 0 (Chưa thuộc)

        long result = db.insert(LocalDbHelper.TABLE_WORD, null, values);
        db.close();

        if (result != -1) {
            for (Category cat : allCategories) {
                if (cat.getName().equals(categoryName)) {
                    cat.getWords().add(newWord);
                    break;
                }
            }
            return true;
        }
        return false;
    }

    // --- B. XÓA TỪ (DELETE) ---
    public static boolean deleteWord(Context context, Word word) {
        LocalDbHelper dbHelper = new LocalDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Xóa khỏi Database
        long result = db.delete(LocalDbHelper.TABLE_WORD, LocalDbHelper.COL_WORD_ENGLISH + " = ?", new String[]{word.getEnglishWord()});
        db.close();

        // Xóa khỏi List RAM (để đồng bộ nếu Adapter chưa kịp xóa)
        if (result > 0) {
            for (Category cat : allCategories) {
                cat.getWords().removeIf(w -> w.getEnglishWord().equals(word.getEnglishWord()));
            }
            return true;
        }
        return false;
    }

    // --- C. RESET TIẾN ĐỘ ---
    public static boolean resetCategoryProgress(Context context, String categoryName) {
        LocalDbHelper dbHelper = new LocalDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int categoryId = -1;
        Cursor cursor = db.query(LocalDbHelper.TABLE_CAT, new String[]{LocalDbHelper.COL_CAT_ID}, LocalDbHelper.COL_CAT_NAME + " = ?", new String[]{categoryName}, null, null, null);
        if (cursor.moveToFirst()) categoryId = cursor.getInt(0);
        cursor.close();

        if (categoryId == -1) { db.close(); return false; }

        ContentValues values = new ContentValues();
        values.put(LocalDbHelper.COL_WORD_LEARNED, 0); // Reset về 0

        int rows = db.update(LocalDbHelper.TABLE_WORD, values, LocalDbHelper.COL_WORD_CAT_ID + " = ?", new String[]{String.valueOf(categoryId)});
        db.close();

        if (rows > 0) {
            for (Category cat : allCategories) {
                if (cat.getName().equals(categoryName)) {
                    for (Word w : cat.getWords()) w.setLearned(false);
                    break;
                }
            }
            return true;
        }
        return false;
    }

    // --- D. UPDATE TRẠNG THÁI (Lưu thay đổi checkbox) ---
    public static void saveData(Context context) {
        if (allCategories.isEmpty()) return;
        LocalDbHelper dbHelper = new LocalDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            db.beginTransaction();
            for (Category cat : allCategories) {
                for (Word w : cat.getWords()) {
                    ContentValues values = new ContentValues();
                    values.put(LocalDbHelper.COL_WORD_LEARNED, w.isLearned() ? 1 : 0);
                    values.put(LocalDbHelper.COL_WORD_IMAGE, w.getImageUrl());
                    db.update(LocalDbHelper.TABLE_WORD, values, LocalDbHelper.COL_WORD_ENGLISH + " = ?", new String[]{w.getEnglishWord()});
                }
            }
            db.setTransactionSuccessful();
        } catch (Exception e) { e.printStackTrace(); }
        finally { db.endTransaction(); db.close(); }
    }

    public static List<Category> loadData(Context context) {
        if (!allCategories.isEmpty()) return allCategories;
        LocalDbHelper dbHelper = new LocalDbHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT count(*) FROM " + LocalDbHelper.TABLE_CAT, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        if (count == 0) importFromAssetsAndSaveToDb(context, dbHelper);
        else loadFromDb(db);
        db.close();
        return allCategories;
    }

    public static void resetData(Context context) {
        LocalDbHelper dbHelper = new LocalDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("DELETE FROM " + LocalDbHelper.TABLE_WORD);
        db.execSQL("DELETE FROM " + LocalDbHelper.TABLE_CAT);
        db.close();
        allCategories.clear();
        loadData(context);
    }

    private static void loadFromDb(SQLiteDatabase db) {
        allCategories.clear();
        Cursor cursorCat = db.query(LocalDbHelper.TABLE_CAT, null, null, null, null, null, null);
        if (cursorCat.moveToFirst()) {
            do {
                int idIdx = cursorCat.getColumnIndex(LocalDbHelper.COL_CAT_ID);
                int nameIdx = cursorCat.getColumnIndex(LocalDbHelper.COL_CAT_NAME);
                if (idIdx == -1) continue;
                int catId = cursorCat.getInt(idIdx);
                String catName = cursorCat.getString(nameIdx);
                List<Word> words = new ArrayList<>();
                Cursor cw = db.query(LocalDbHelper.TABLE_WORD, null, LocalDbHelper.COL_WORD_CAT_ID + "=?", new String[]{String.valueOf(catId)}, null, null, null);
                if (cw.moveToFirst()) {
                    do {
                        Word w = new Word();
                        int idxEng = cw.getColumnIndex(LocalDbHelper.COL_WORD_ENGLISH);
                        int idxVn = cw.getColumnIndex(LocalDbHelper.COL_WORD_VN);
                        int idxDef = cw.getColumnIndex(LocalDbHelper.COL_WORD_DEF);
                        int idxLearn = cw.getColumnIndex(LocalDbHelper.COL_WORD_LEARNED);
                        int idxImg = cw.getColumnIndex(LocalDbHelper.COL_WORD_IMAGE);
                        if (idxEng != -1) w.setEnglishWord(cw.getString(idxEng));
                        if (idxVn != -1) w.setVietnameseMeaning(cw.getString(idxVn));
                        if (idxDef != -1) w.setDefinition(cw.getString(idxDef));
                        if (idxLearn != -1) w.setLearned(cw.getInt(idxLearn) == 1);
                        if (idxImg != -1) w.setImageUrl(cw.getString(idxImg));
                        words.add(w);
                    } while (cw.moveToNext());
                }
                cw.close();
                allCategories.add(new Category(catName, words));
            } while (cursorCat.moveToNext());
        }
        cursorCat.close();
    }

    private static void importFromAssetsAndSaveToDb(Context context, LocalDbHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            InputStream is = context.getAssets().open("initial_data.json");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);
            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, List<Word>>>(){}.getType();
            Map<String, List<Word>> dataMap = gson.fromJson(json, type);
            allCategories.clear();
            for (Map.Entry<String, List<Word>> entry : dataMap.entrySet()) {
                String key = entry.getKey();
                List<Word> rawWords = entry.getValue();
                String vnName = CATEGORY_VN_NAMES.getOrDefault(key, key);
                ContentValues cvCat = new ContentValues();
                cvCat.put(LocalDbHelper.COL_CAT_NAME, vnName);
                long catId = db.insert(LocalDbHelper.TABLE_CAT, null, cvCat);
                List<Word> processedWords = new ArrayList<>();
                for (Word word : rawWords) {
                    if (word.getVietnameseMeaning() == null || word.getVietnameseMeaning().isEmpty()) {
                        if (word.getDefinition() != null && !word.getDefinition().isEmpty()) word.setVietnameseMeaning(word.getDefinition());
                        else word.setVietnameseMeaning(englishWordToVn(word.getEnglishWord(), vnName));
                    }
                    ContentValues cvWord = new ContentValues();
                    cvWord.put(LocalDbHelper.COL_WORD_CAT_ID, catId);
                    cvWord.put(LocalDbHelper.COL_WORD_ENGLISH, word.getEnglishWord());
                    cvWord.put(LocalDbHelper.COL_WORD_VN, word.getVietnameseMeaning());
                    cvWord.put(LocalDbHelper.COL_WORD_DEF, word.getDefinition());
                    cvWord.put(LocalDbHelper.COL_WORD_LEARNED, word.isLearned() ? 1 : 0);
                    db.insert(LocalDbHelper.TABLE_WORD, null, cvWord);
                    processedWords.add(word);
                }
                allCategories.add(new Category(vnName, processedWords));
            }
            db.setTransactionSuccessful();
        } catch (Exception e) { e.printStackTrace(); }
        finally { db.endTransaction(); db.close(); }
    }

    private static String englishWordToVn(String eng, String cat) { return eng + " (TV)"; }
    private static Map<String, String> initializeCategoryNames() {
        Map<String, String> map = new HashMap<>();
        map.put("animals", "Động vật"); map.put("fruits", "Hoa quả"); map.put("vegetables", "Các loại rau");
        map.put("food_drinks", "Thức ăn và đồ uống"); map.put("school", "Trường học"); map.put("jobs", "Nghề nghiệp");
        map.put("clothing", "Quần áo"); map.put("home_furniture", "Nhà cửa & Nội thất"); map.put("body_parts", "Bộ phận cơ thể");
        map.put("colors", "Màu sắc"); map.put("sports", "Thể thao"); map.put("weather", "Thời tiết");
        map.put("transportation", "Phương tiện giao thông"); map.put("technology", "Công nghệ"); map.put("nature", "Thiên nhiên");
        map.put("emotions", "Cảm xúc"); map.put("hobbies", "Sở thích"); map.put("travel", "Du lịch");
        map.put("family", "Gia đình"); map.put("music", "Âm nhạc");
        return map;
    }
}