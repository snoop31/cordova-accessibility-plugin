package com.kbaylonh;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import java.util.ArrayList;

public class ContactHelper {

  public static boolean insertContact(ContentResolver contactAdder, String firstName, String mobileNumber) {

    ArrayList<ContentProviderOperation> ops = new ArrayList<>();
    int rawContactID = ops.size();
    ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
      .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
      .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
      .build());

    ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
      .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
      .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
      .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, firstName)
      .build());

    ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
      .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
      .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
      .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, mobileNumber)
      .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
      .build());

    try {
      contactAdder.applyBatch(ContactsContract.AUTHORITY, ops);
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  public static boolean contactExists(Context context, String number) {
    Uri lookupUri = Uri.withAppendedPath(
      ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
      Uri.encode(number));

    String[] mPhoneNumberProjection = { ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.NUMBER, ContactsContract.PhoneLookup.DISPLAY_NAME };

    Cursor cur = context.getContentResolver().query(lookupUri,mPhoneNumberProjection, null, null, null);
    try {
      if(cur != null && cur.getCount()>0){
        cur.close();
        return true;
      }
    } finally {
      if (cur != null)
        cur.close();
    }
    return false;
  }
}
