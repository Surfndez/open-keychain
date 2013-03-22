/*
 * Copyright (C) 2012 Dominik Schürmann <dominik@dominikschuermann.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sufficientlysecure.keychain.provider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import org.spongycastle.bcpg.ArmoredOutputStream;
import org.spongycastle.openpgp.PGPKeyRing;
import org.spongycastle.openpgp.PGPPublicKey;
import org.spongycastle.openpgp.PGPPublicKeyRing;
import org.spongycastle.openpgp.PGPSecretKey;
import org.spongycastle.openpgp.PGPSecretKeyRing;
import org.sufficientlysecure.keychain.Constants;
import org.sufficientlysecure.keychain.helper.PgpConversionHelper;
import org.sufficientlysecure.keychain.helper.PgpHelper;
import org.sufficientlysecure.keychain.helper.PgpMain;
import org.sufficientlysecure.keychain.provider.KeychainContract.KeyRings;
import org.sufficientlysecure.keychain.provider.KeychainContract.Keys;
import org.sufficientlysecure.keychain.provider.KeychainContract.UserIds;
import org.sufficientlysecure.keychain.provider.KeychainDatabase.Tables;
import org.sufficientlysecure.keychain.util.IterableIterator;
import org.sufficientlysecure.keychain.util.Log;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.RemoteException;

public class ProviderHelper {

    /**
     * Private helper method to get PGPKeyRing from database
     * 
     * @param context
     * @param queryUri
     * @return
     */
    private static PGPKeyRing getPGPKeyRing(Context context, Uri queryUri) {
        Cursor cursor = context.getContentResolver().query(queryUri,
                new String[] { KeyRings._ID, KeyRings.KEY_RING_DATA }, null, null, null);

        PGPKeyRing keyRing = null;
        if (cursor != null && cursor.moveToFirst()) {
            int keyRingDataCol = cursor.getColumnIndex(KeyRings.KEY_RING_DATA);

            byte[] data = cursor.getBlob(keyRingDataCol);
            if (data != null) {
                keyRing = PgpConversionHelper.BytesToPGPKeyRing(data);
            }
        }

        if (cursor != null) {
            cursor.close();
        }

        return keyRing;
    }

    /**
     * Retrieves the actual PGPPublicKeyRing object from the database blob based on the rowId
     * 
     * @param context
     * @param rowId
     * @return
     */
    public static PGPPublicKeyRing getPGPPublicKeyRingByRowId(Context context, long rowId) {
        Uri queryUri = KeyRings.buildPublicKeyRingsUri(Long.toString(rowId));
        return (PGPPublicKeyRing) getPGPKeyRing(context, queryUri);
    }

    /**
     * Retrieves the actual PGPPublicKeyRing object from the database blob based on the maserKeyId
     * 
     * @param context
     * @param masterKeyId
     * @return
     */
    public static PGPPublicKeyRing getPGPPublicKeyRingByMasterKeyId(Context context,
            long masterKeyId) {
        Uri queryUri = KeyRings.buildPublicKeyRingsByMasterKeyIdUri(Long.toString(masterKeyId));
        return (PGPPublicKeyRing) getPGPKeyRing(context, queryUri);
    }

    /**
     * Retrieves the actual PGPPublicKeyRing object from the database blob associated with a key
     * with this keyId
     * 
     * @param context
     * @param keyId
     * @return
     */
    public static PGPPublicKeyRing getPGPPublicKeyRingByKeyId(Context context, long keyId) {
        Uri queryUri = KeyRings.buildPublicKeyRingsByKeyIdUri(Long.toString(keyId));
        return (PGPPublicKeyRing) getPGPKeyRing(context, queryUri);
    }

    /**
     * Retrieves the actual PGPPublicKey object from the database blob associated with a key with
     * this keyId
     * 
     * @param context
     * @param keyId
     * @return
     */
    public static PGPPublicKey getPGPPublicKeyByKeyId(Context context, long keyId) {
        PGPPublicKeyRing keyRing = getPGPPublicKeyRingByKeyId(context, keyId);
        if (keyRing == null) {
            return null;
        }

        return keyRing.getPublicKey(keyId);
    }

    /**
     * Retrieves the actual PGPSecretKeyRing object from the database blob based on the rowId
     * 
     * @param context
     * @param rowId
     * @return
     */
    public static PGPSecretKeyRing getPGPSecretKeyRingByRowId(Context context, long rowId) {
        Uri queryUri = KeyRings.buildSecretKeyRingsUri(Long.toString(rowId));
        return (PGPSecretKeyRing) getPGPKeyRing(context, queryUri);
    }

    /**
     * Retrieves the actual PGPSecretKeyRing object from the database blob based on the maserKeyId
     * 
     * @param context
     * @param masterKeyId
     * @return
     */
    public static PGPSecretKeyRing getPGPSecretKeyRingByMasterKeyId(Context context,
            long masterKeyId) {
        Uri queryUri = KeyRings.buildSecretKeyRingsByMasterKeyIdUri(Long.toString(masterKeyId));
        return (PGPSecretKeyRing) getPGPKeyRing(context, queryUri);
    }

    /**
     * Retrieves the actual PGPSecretKeyRing object from the database blob associated with a key
     * with this keyId
     * 
     * @param context
     * @param keyId
     * @return
     */
    public static PGPSecretKeyRing getPGPSecretKeyRingByKeyId(Context context, long keyId) {
        Uri queryUri = KeyRings.buildSecretKeyRingsByKeyIdUri(Long.toString(keyId));
        return (PGPSecretKeyRing) getPGPKeyRing(context, queryUri);
    }

    /**
     * Retrieves the actual PGPSecretKey object from the database blob associated with a key with
     * this keyId
     * 
     * @param context
     * @param keyId
     * @return
     */
    public static PGPSecretKey getPGPSecretKeyByKeyId(Context context, long keyId) {
        PGPSecretKeyRing keyRing = getPGPSecretKeyRingByKeyId(context, keyId);
        if (keyRing == null) {
            return null;
        }

        return keyRing.getSecretKey(keyId);
    }

    /**
     * Saves PGPPublicKeyRing with its keys and userIds in DB
     * 
     * @param context
     * @param keyRing
     * @return
     * @throws IOException
     * @throws GeneralException
     */
    @SuppressWarnings("unchecked")
    public static void saveKeyRing(Context context, PGPPublicKeyRing keyRing) throws IOException {
        PGPPublicKey masterKey = keyRing.getPublicKey();
        long masterKeyId = masterKey.getKeyID();

        // delete old version of this keyRing, which also deletes all keys and userIds on cascade
        Uri deleteUri = KeyRings.buildPublicKeyRingsByMasterKeyIdUri(Long.toString(masterKeyId));

        try {
            context.getContentResolver().delete(deleteUri, null, null);
        } catch (UnsupportedOperationException e) {
            Log.e(Constants.TAG, "Key could not be deleted! Maybe we are creating a new one!", e);
        }

        ContentValues values = new ContentValues();
        values.put(KeyRings.MASTER_KEY_ID, masterKeyId);
        values.put(KeyRings.KEY_RING_DATA, keyRing.getEncoded());

        // insert new version of this keyRing
        Uri uri = KeyRings.buildPublicKeyRingsUri();
        Uri insertedUri = context.getContentResolver().insert(uri, values);
        long keyRingRowId = Long.valueOf(insertedUri.getLastPathSegment());

        // save all keys and userIds included in keyRing object in database
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

        int rank = 0;
        for (PGPPublicKey key : new IterableIterator<PGPPublicKey>(keyRing.getPublicKeys())) {
            operations.add(buildPublicKeyOperations(context, keyRingRowId, key, rank));
            ++rank;
        }

        int userIdRank = 0;
        for (String userId : new IterableIterator<String>(masterKey.getUserIDs())) {
            operations.add(buildPublicUserIdOperations(context, keyRingRowId, userId, userIdRank));
            ++userIdRank;
        }

        try {
            context.getContentResolver().applyBatch(KeychainContract.CONTENT_AUTHORITY_INTERNAL,
                    operations);
        } catch (RemoteException e) {
            Log.e(Constants.TAG, "applyBatch failed!", e);
        } catch (OperationApplicationException e) {
            Log.e(Constants.TAG, "applyBatch failed!", e);
        }
    }

    /**
     * Saves PGPSecretKeyRing with its keys and userIds in DB
     * 
     * @param context
     * @param keyRing
     * @return
     * @throws IOException
     * @throws GeneralException
     */
    @SuppressWarnings("unchecked")
    public static void saveKeyRing(Context context, PGPSecretKeyRing keyRing) throws IOException {
        PGPSecretKey masterKey = keyRing.getSecretKey();
        long masterKeyId = masterKey.getKeyID();

        // delete old version of this keyRing, which also deletes all keys and userIds on cascade
        Uri deleteUri = KeyRings.buildSecretKeyRingsByMasterKeyIdUri(Long.toString(masterKeyId));

        try {
            context.getContentResolver().delete(deleteUri, null, null);
        } catch (UnsupportedOperationException e) {
            Log.e(Constants.TAG, "Key could not be deleted! Maybe we are creating a new one!", e);
        }

        ContentValues values = new ContentValues();
        values.put(KeyRings.MASTER_KEY_ID, masterKeyId);
        values.put(KeyRings.KEY_RING_DATA, keyRing.getEncoded());

        // insert new version of this keyRing
        Uri uri = KeyRings.buildSecretKeyRingsUri();
        Uri insertedUri = context.getContentResolver().insert(uri, values);
        long keyRingRowId = Long.valueOf(insertedUri.getLastPathSegment());

        // save all keys and userIds included in keyRing object in database
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

        int rank = 0;
        for (PGPSecretKey key : new IterableIterator<PGPSecretKey>(keyRing.getSecretKeys())) {
            operations.add(buildSecretKeyOperations(context, keyRingRowId, key, rank));
            ++rank;
        }

        int userIdRank = 0;
        for (String userId : new IterableIterator<String>(masterKey.getUserIDs())) {
            operations.add(buildSecretUserIdOperations(context, keyRingRowId, userId, userIdRank));
            ++userIdRank;
        }

        try {
            context.getContentResolver().applyBatch(KeychainContract.CONTENT_AUTHORITY_INTERNAL,
                    operations);
        } catch (RemoteException e) {
            Log.e(Constants.TAG, "applyBatch failed!", e);
        } catch (OperationApplicationException e) {
            Log.e(Constants.TAG, "applyBatch failed!", e);
        }
    }

    /**
     * Build ContentProviderOperation to add PGPPublicKey to database corresponding to a keyRing
     * 
     * @param context
     * @param keyRingRowId
     * @param key
     * @param rank
     * @return
     * @throws IOException
     */
    private static ContentProviderOperation buildPublicKeyOperations(Context context,
            long keyRingRowId, PGPPublicKey key, int rank) throws IOException {
        ContentValues values = new ContentValues();
        values.put(Keys.KEY_ID, key.getKeyID());
        values.put(Keys.IS_MASTER_KEY, key.isMasterKey());
        values.put(Keys.ALGORITHM, key.getAlgorithm());
        values.put(Keys.KEY_SIZE, key.getBitStrength());
        values.put(Keys.CAN_SIGN, PgpHelper.isSigningKey(key));
        values.put(Keys.CAN_ENCRYPT, PgpHelper.isEncryptionKey(key));
        values.put(Keys.IS_REVOKED, key.isRevoked());
        values.put(Keys.CREATION, PgpHelper.getCreationDate(key).getTime() / 1000);
        Date expiryDate = PgpHelper.getExpiryDate(key);
        if (expiryDate != null) {
            values.put(Keys.EXPIRY, expiryDate.getTime() / 1000);
        }
        values.put(Keys.KEY_RING_ROW_ID, keyRingRowId);
        values.put(Keys.KEY_DATA, key.getEncoded());
        values.put(Keys.RANK, rank);

        Uri uri = Keys.buildPublicKeysUri(Long.toString(keyRingRowId));

        return ContentProviderOperation.newInsert(uri).withValues(values).build();
    }

    /**
     * Build ContentProviderOperation to add PublicUserIds to database corresponding to a keyRing
     * 
     * @param context
     * @param keyRingRowId
     * @param key
     * @param rank
     * @return
     * @throws IOException
     */
    private static ContentProviderOperation buildPublicUserIdOperations(Context context,
            long keyRingRowId, String userId, int rank) {
        ContentValues values = new ContentValues();
        values.put(UserIds.KEY_RING_ROW_ID, keyRingRowId);
        values.put(UserIds.USER_ID, userId);
        values.put(UserIds.RANK, rank);

        Uri uri = UserIds.buildPublicUserIdsUri(Long.toString(keyRingRowId));

        return ContentProviderOperation.newInsert(uri).withValues(values).build();
    }

    /**
     * Build ContentProviderOperation to add PGPSecretKey to database corresponding to a keyRing
     * 
     * @param context
     * @param keyRingRowId
     * @param key
     * @param rank
     * @return
     * @throws IOException
     */
    private static ContentProviderOperation buildSecretKeyOperations(Context context,
            long keyRingRowId, PGPSecretKey key, int rank) throws IOException {
        ContentValues values = new ContentValues();

        boolean has_private = true;
        if (key.isMasterKey()) {
            if (PgpHelper.isSecretKeyPrivateEmpty(key)) {
                has_private = false;
            }
        }

        values.put(Keys.KEY_ID, key.getKeyID());
        values.put(Keys.IS_MASTER_KEY, key.isMasterKey());
        values.put(Keys.ALGORITHM, key.getPublicKey().getAlgorithm());
        values.put(Keys.KEY_SIZE, key.getPublicKey().getBitStrength());
        values.put(Keys.CAN_CERTIFY, (PgpHelper.isCertificationKey(key) && has_private));
        values.put(Keys.CAN_SIGN, (PgpHelper.isSigningKey(key) && has_private));
        values.put(Keys.CAN_ENCRYPT, PgpHelper.isEncryptionKey(key));
        values.put(Keys.IS_REVOKED, key.getPublicKey().isRevoked());
        values.put(Keys.CREATION, PgpHelper.getCreationDate(key).getTime() / 1000);
        Date expiryDate = PgpHelper.getExpiryDate(key);
        if (expiryDate != null) {
            values.put(Keys.EXPIRY, expiryDate.getTime() / 1000);
        }
        values.put(Keys.KEY_RING_ROW_ID, keyRingRowId);
        values.put(Keys.KEY_DATA, key.getEncoded());
        values.put(Keys.RANK, rank);

        Uri uri = Keys.buildSecretKeysUri(Long.toString(keyRingRowId));

        return ContentProviderOperation.newInsert(uri).withValues(values).build();
    }

    /**
     * Build ContentProviderOperation to add SecretUserIds to database corresponding to a keyRing
     * 
     * @param context
     * @param keyRingRowId
     * @param key
     * @param rank
     * @return
     * @throws IOException
     */
    private static ContentProviderOperation buildSecretUserIdOperations(Context context,
            long keyRingRowId, String userId, int rank) {
        ContentValues values = new ContentValues();
        values.put(UserIds.KEY_RING_ROW_ID, keyRingRowId);
        values.put(UserIds.USER_ID, userId);
        values.put(UserIds.RANK, rank);

        Uri uri = UserIds.buildSecretUserIdsUri(Long.toString(keyRingRowId));

        return ContentProviderOperation.newInsert(uri).withValues(values).build();
    }

    /**
     * Private helper method
     * 
     * @param context
     * @param queryUri
     * @return
     */
    private static ArrayList<Long> getKeyRingsMasterKeyIds(Context context, Uri queryUri) {
        Cursor cursor = context.getContentResolver().query(queryUri,
                new String[] { KeyRings.MASTER_KEY_ID }, null, null, null);

        ArrayList<Long> masterKeyIds = new ArrayList<Long>();
        if (cursor != null) {
            int masterKeyIdCol = cursor.getColumnIndex(KeyRings.MASTER_KEY_ID);
            if (cursor.moveToFirst()) {
                do {
                    masterKeyIds.add(cursor.getLong(masterKeyIdCol));
                } while (cursor.moveToNext());
            }
        }

        if (cursor != null) {
            cursor.close();
        }

        return masterKeyIds;
    }

    /**
     * Retrieves ids of all SecretKeyRings
     * 
     * @param context
     * @return
     */
    public static ArrayList<Long> getSecretKeyRingsMasterKeyIds(Context context) {
        Uri queryUri = KeyRings.buildSecretKeyRingsUri();
        return getKeyRingsMasterKeyIds(context, queryUri);
    }

    /**
     * Retrieves ids of all PublicKeyRings
     * 
     * @param context
     * @return
     */
    public static ArrayList<Long> getPublicKeyRingsMasterKeyIds(Context context) {
        Uri queryUri = KeyRings.buildPublicKeyRingsUri();
        return getKeyRingsMasterKeyIds(context, queryUri);
    }

    public static void deletePublicKeyRing(Context context, long rowId) {
        ContentResolver cr = context.getContentResolver();
        cr.delete(KeyRings.buildPublicKeyRingsUri(Long.toString(rowId)), null, null);
    }

    public static void deleteSecretKeyRing(Context context, long rowId) {
        ContentResolver cr = context.getContentResolver();
        cr.delete(KeyRings.buildSecretKeyRingsUri(Long.toString(rowId)), null, null);
    }

    /**
     * Get master key id of keyring by its row id
     * 
     * @param context
     * @param keyRingRowId
     * @return
     */
    public static long getPublicMasterKeyId(Context context, long keyRingRowId) {
        Uri queryUri = KeyRings.buildPublicKeyRingsUri(String.valueOf(keyRingRowId));
        return getMasterKeyId(context, queryUri, keyRingRowId);
    }

    /**
     * Get empty status of master key of keyring by its row id
     * 
     * @param context
     * @param keyRingRowId
     * @return
     */
    public static boolean getSecretMasterKeyCanSign(Context context, long keyRingRowId) {
        Uri queryUri = KeyRings.buildSecretKeyRingsUri(String.valueOf(keyRingRowId));
        return getMasterKeyCanSign(context, queryUri, keyRingRowId);
    }

    /**
     * Private helper method to get master key private empty status of keyring by its row id
     * 
     * @param context
     * @param queryUri
     * @param keyRingRowId
     * @return
     */
    private static boolean getMasterKeyCanSign(Context context, Uri queryUri, long keyRingRowId) {
        String[] projection = new String[] { KeyRings.MASTER_KEY_ID, "(SELECT COUNT(sign_keys." + 
            Keys._ID + ") FROM " + Tables.KEYS + " AS sign_keys WHERE sign_keys." + Keys.KEY_RING_ROW_ID + " = "
            + KeychainDatabase.Tables.KEY_RINGS + "." + KeyRings._ID + " AND sign_keys."
            + Keys.CAN_SIGN + " = '1' AND " + Keys.IS_MASTER_KEY + " = 1) AS sign",  };

        ContentResolver cr = context.getContentResolver();
        Cursor cursor = cr.query(queryUri, projection, null, null, null);

        long masterKeyId = -1;
        if (cursor != null && cursor.moveToFirst()) {
            int masterKeyIdCol = cursor.getColumnIndex("sign");

            masterKeyId = cursor.getLong(masterKeyIdCol);
        }

        if (cursor != null) {
            cursor.close();
        }

        return (masterKeyId > 0);
    }

    /**
     * Get master key id of keyring by its row id
     * 
     * @param context
     * @param keyRingRowId
     * @return
     */
    public static long getSecretMasterKeyId(Context context, long keyRingRowId) {
        Uri queryUri = KeyRings.buildSecretKeyRingsUri(String.valueOf(keyRingRowId));
        return getMasterKeyId(context, queryUri, keyRingRowId);
    }

    /**
     * Private helper method to get master key id of keyring by its row id
     * 
     * @param context
     * @param queryUri
     * @param keyRingRowId
     * @return
     */
    private static long getMasterKeyId(Context context, Uri queryUri, long keyRingRowId) {
        String[] projection = new String[] { KeyRings.MASTER_KEY_ID };

        ContentResolver cr = context.getContentResolver();
        Cursor cursor = cr.query(queryUri, projection, null, null, null);

        long masterKeyId = -1;
        if (cursor != null && cursor.moveToFirst()) {
            int masterKeyIdCol = cursor.getColumnIndex(KeyRings.MASTER_KEY_ID);

            masterKeyId = cursor.getLong(masterKeyIdCol);
        }

        if (cursor != null) {
            cursor.close();
        }

        return masterKeyId;
    }

    public static ArrayList<String> getPublicKeyRingsAsArmoredString(Context context,
            long[] masterKeyIds) {
        return getKeyRingsAsArmoredString(context, KeyRings.buildPublicKeyRingsUri(), masterKeyIds);
    }

    public static ArrayList<String> getSecretKeyRingsAsArmoredString(Context context,
            long[] masterKeyIds) {
        return getKeyRingsAsArmoredString(context, KeyRings.buildSecretKeyRingsUri(), masterKeyIds);
    }

    private static ArrayList<String> getKeyRingsAsArmoredString(Context context, Uri uri,
            long[] masterKeyIds) {
        ArrayList<String> output = new ArrayList<String>();

        if (masterKeyIds != null && masterKeyIds.length > 0) {

            Cursor cursor = getCursorWithSelectedKeyringMasterKeyIds(context, uri, masterKeyIds);

            if (cursor != null) {
                int masterIdCol = cursor.getColumnIndex(KeyRings.MASTER_KEY_ID);
                int dataCol = cursor.getColumnIndex(KeyRings.KEY_RING_DATA);
                if (cursor.moveToFirst()) {
                    do {
                        Log.d(Constants.TAG, "masterKeyId: " + cursor.getLong(masterIdCol));

                        // get actual keyring data blob and write it to ByteArrayOutputStream
                        try {
                            Object keyRing = null;
                            byte[] data = cursor.getBlob(dataCol);
                            if (data != null) {
                                keyRing = PgpConversionHelper.BytesToPGPKeyRing(data);
                            }

                            ByteArrayOutputStream bos = new ByteArrayOutputStream();
                            ArmoredOutputStream aos = new ArmoredOutputStream(bos);
                            aos.setHeader("Version", PgpMain.getFullVersion(context));

                            if (keyRing instanceof PGPSecretKeyRing) {
                                aos.write(((PGPSecretKeyRing) keyRing).getEncoded());
                            } else if (keyRing instanceof PGPPublicKeyRing) {
                                aos.write(((PGPPublicKeyRing) keyRing).getEncoded());
                            }
                            aos.close();

                            String armoredKey = bos.toString("UTF-8");

                            Log.d(Constants.TAG, "armouredKey:" + armoredKey);

                            output.add(armoredKey);
                        } catch (IOException e) {
                            Log.e(Constants.TAG, "IOException", e);
                        }
                    } while (cursor.moveToNext());
                }
            }

            if (cursor != null) {
                cursor.close();
            }

        } else {
            Log.e(Constants.TAG, "No master keys given!");
        }

        if (output.size() > 0) {
            return output;
        } else {
            return null;
        }
    }

    public static byte[] getPublicKeyRingsAsByteArray(Context context, long[] masterKeyIds) {
        return getKeyRingsAsByteArray(context, KeyRings.buildPublicKeyRingsUri(), masterKeyIds);
    }

    public static byte[] getSecretKeyRingsAsByteArray(Context context, long[] masterKeyIds) {
        return getKeyRingsAsByteArray(context, KeyRings.buildSecretKeyRingsUri(), masterKeyIds);
    }

    private static byte[] getKeyRingsAsByteArray(Context context, Uri uri, long[] masterKeyIds) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        if (masterKeyIds != null && masterKeyIds.length > 0) {

            Cursor cursor = getCursorWithSelectedKeyringMasterKeyIds(context, uri, masterKeyIds);

            if (cursor != null) {
                int masterIdCol = cursor.getColumnIndex(KeyRings.MASTER_KEY_ID);
                int dataCol = cursor.getColumnIndex(KeyRings.KEY_RING_DATA);
                if (cursor.moveToFirst()) {
                    do {
                        Log.d(Constants.TAG, "masterKeyId: " + cursor.getLong(masterIdCol));

                        // get actual keyring data blob and write it to ByteArrayOutputStream
                        try {
                            bos.write(cursor.getBlob(dataCol));
                        } catch (IOException e) {
                            Log.e(Constants.TAG, "IOException", e);
                        }
                    } while (cursor.moveToNext());
                }
            }

            if (cursor != null) {
                cursor.close();
            }

        } else {
            Log.e(Constants.TAG, "No master keys given!");
        }

        return bos.toByteArray();
    }

    private static Cursor getCursorWithSelectedKeyringMasterKeyIds(Context context, Uri baseUri,
            long[] masterKeyIds) {
        Cursor cursor = null;
        if (masterKeyIds != null && masterKeyIds.length > 0) {

            String inMasterKeyList = KeyRings.MASTER_KEY_ID + " IN (";
            for (int i = 0; i < masterKeyIds.length; ++i) {
                if (i != 0) {
                    inMasterKeyList += ", ";
                }
                inMasterKeyList += DatabaseUtils.sqlEscapeString("" + masterKeyIds[i]);
            }
            inMasterKeyList += ")";

            cursor = context.getContentResolver().query(baseUri,
                    new String[] { KeyRings._ID, KeyRings.MASTER_KEY_ID, KeyRings.KEY_RING_DATA },
                    inMasterKeyList, null, null);
        }

        return cursor;
    }
}
